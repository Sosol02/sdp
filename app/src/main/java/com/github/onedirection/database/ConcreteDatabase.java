package com.github.onedirection.database;

import com.github.onedirection.database.store.Storable;
import com.github.onedirection.database.store.Storer;
import com.github.onedirection.database.utils.FirebaseUtils;
import com.github.onedirection.utils.Id;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

/**
 * The auto synced database of the application
 */
public class ConcreteDatabase implements Database {
    private final FirebaseFirestore db;

    private static final ConcreteDatabase global = new ConcreteDatabase();

    public static ConcreteDatabase getDatabase() {
        return global;
    }

    private ConcreteDatabase() {
        db = FirebaseUtils.getFirestore();
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Id> store(T toStore) {
        CompletableFuture<Id> result = new CompletableFuture<Id>();

        Storer<T> storer = Objects.requireNonNull(toStore).storer();

        db.collection(storer.getCollection().getCollectionName()).document(toStore.getId().getUuid()).set(toStore.storer().storableToMap(toStore))
                .addOnCompleteListener(res -> {
                    if (res.isSuccessful()) {
                        result.complete(toStore.getId());
                    } else {
                        result.completeExceptionally(res.getException());
                    }
                }).addOnCanceledListener(() -> result.cancel(false));

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<T> retrieve(Id id, Storer<T> storer) {
        Objects.requireNonNull(storer, "Storer is null");
        CompletableFuture<T> result = new CompletableFuture<T>();

        db.collection(storer.getCollection().getCollectionName())
                .document(id.getUuid())
                .get()
                .addOnCompleteListener(res -> {
                    if (res.isSuccessful()) {
                        Map<String, Object> doc = res.getResult().getData();
                        if(doc == null || doc.isEmpty()) {
                            result.complete(null);
                        } else {
                            T obj = storer.mapToStorable(doc);
                            result.complete(obj);
                        }
                    } else {
                        result.completeExceptionally(res.getException());
                    }
                }).addOnCanceledListener(() -> result.cancel(false));

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Id> remove(Id id, Storer<T> storer) {
        Objects.requireNonNull(storer, "Storer is null");
        CompletableFuture<Id> result = new CompletableFuture<Id>();

        db.collection(storer.getCollection().getCollectionName())
                .document(id.getUuid()).delete().addOnCompleteListener(res -> {
            if (res.isSuccessful()) {
                result.complete(id);
            } else {
                result.completeExceptionally(res.getException());
            }
        }).addOnCanceledListener(() -> result.cancel(false));

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Boolean> contains(T storable) {
        CompletableFuture<Boolean> result = new CompletableFuture<Boolean>();

        Storer<T> storer = Objects.requireNonNull(storable).storer();
        Map<String, Object> doc = storer.storableToMap(storable);

        Query q = db.collection(storer.getCollection().getCollectionName());
        for(Map.Entry<String, Object> entry : doc.entrySet()) {
            q = q.whereEqualTo(entry.getKey(), entry.getValue());
        }

        q.get().addOnCompleteListener(res -> {
            if(res.isSuccessful()) {
                QuerySnapshot docs = res.getResult();
                result.complete(docs != null && !docs.isEmpty());
            } else {
                result.completeExceptionally(res.getException());
            }
        }).addOnCanceledListener(() -> result.cancel(false));

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Boolean> contains(Id id, Storer<T> storer) {
        return retrieve(id, storer).thenApply(r -> r != null);
    }

    private <T extends Storable<T>> CompletableFuture<List<T>> completeOnList(Task<QuerySnapshot> t, Storer<T> storer) {
        CompletableFuture<List<T>> result = new CompletableFuture<List<T>>();

        Objects.requireNonNull(t)
                .addOnCompleteListener(res -> {
                    if(res.isSuccessful()) {
                        QuerySnapshot docs = res.getResult();
                        List<T> storables = new ArrayList<T>();
                        if(docs != null) {
                            for (DocumentSnapshot ds : docs) {
                                storables.add(storer.mapToStorable(ds.getData()));
                            }
                        }
                        result.complete(storables);
                    } else {
                        result.completeExceptionally(res.getException());
                    }
                }).addOnCanceledListener(() -> result.cancel(false));

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Boolean> storeAll(List<T> listToStore) {
        Objects.requireNonNull(listToStore);
        for(T t : listToStore) {
            Objects.requireNonNull(t);
        }
        if(listToStore.isEmpty()) {
            CompletableFuture<Boolean> result = new CompletableFuture<Boolean>();
            result.complete(true);
            return result;
        }

        List<CompletableFuture<Id>> allStored = new ArrayList<CompletableFuture<Id>>();
        for(T toStore : listToStore) {
            allStored.add(store(toStore));
        }
        //Combine all results. We don't care about the Ids returned we just care about if exceptions are thrown.
        CompletableFuture<Void> stored = CompletableFuture.allOf(allStored.toArray(new CompletableFuture[listToStore.size()]));
        return stored.thenApply(t -> true);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> retrieveAll(Storer<T> storer) {
        Task<QuerySnapshot> t = db.collection(Objects.requireNonNull(storer).getCollection().getCollectionName()).get();
        return completeOnList(t, storer);
    }

    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereEquals(String key, Object value, Storer<T> storer) {
        Task<QuerySnapshot> t = db.collection(Objects.requireNonNull(storer).getCollection().getCollectionName()).whereEqualTo(Objects.requireNonNull(key), Objects.requireNonNull(value)).get();
        return completeOnList(t, storer);
    }

    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreater(String key, Object value, Storer<T> storer) {
        Task<QuerySnapshot> t = db.collection(Objects.requireNonNull(storer).getCollection().getCollectionName()).whereGreaterThan(Objects.requireNonNull(key), Objects.requireNonNull(value)).get();
        return completeOnList(t, storer);
    }

    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterEq(String key, Object value, Storer<T> storer) {
        Task<QuerySnapshot> t = db.collection(Objects.requireNonNull(storer).getCollection().getCollectionName()).whereGreaterThanOrEqualTo(Objects.requireNonNull(key), Objects.requireNonNull(value)).get();
        return completeOnList(t, storer);
    }

    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereLess(String key, Object value, Storer<T> storer) {
        Task<QuerySnapshot> t = db.collection(Objects.requireNonNull(storer).getCollection().getCollectionName()).whereLessThan(Objects.requireNonNull(key), Objects.requireNonNull(value)).get();
        return completeOnList(t, storer);
    }

    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereLessEq(String key, Object value, Storer<T> storer) {
        Task<QuerySnapshot> t = db.collection(Objects.requireNonNull(storer).getCollection().getCollectionName()).whereLessThanOrEqualTo(Objects.requireNonNull(key), Objects.requireNonNull(value)).get();
        return completeOnList(t, storer);
    }

    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterEqLess(String key, Object valueGreaterEq, Object valueLess, Storer<T> storer) {
        Task<QuerySnapshot> t = db.collection(Objects.requireNonNull(storer).getCollection().getCollectionName())
                .whereGreaterThanOrEqualTo(Objects.requireNonNull(key), Objects.requireNonNull(valueGreaterEq))
                .whereLessThan(key, Objects.requireNonNull(valueLess))
                .get();
        return completeOnList(t, storer);
    }

    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterLessEq(String key, Object valueGreater, Object valueLessEq, Storer<T> storer) {
        Task<QuerySnapshot> t = db.collection(Objects.requireNonNull(storer).getCollection().getCollectionName())
                .whereGreaterThan(Objects.requireNonNull(key), Objects.requireNonNull(valueGreater))
                .whereLessThanOrEqualTo(key, Objects.requireNonNull(valueLessEq))
                .get();
        return completeOnList(t, storer);
    }
}
