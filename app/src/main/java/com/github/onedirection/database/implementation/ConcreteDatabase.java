package com.github.onedirection.database.implementation;

import com.github.onedirection.authentication.service.FirebaseAuthentication;
import com.github.onedirection.authentication.service.IdentificationService;
import com.github.onedirection.authentication.service.User;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.database.store.Storable;
import com.github.onedirection.database.store.Storer;
import com.github.onedirection.database.utils.FirebaseUtils;
import com.github.onedirection.utils.Id;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The auto synced database of the application
 */
public class ConcreteDatabase implements Database {
    private final static String USERS_COLLECTIONS_NAME = "users";

    private final FirebaseFirestore db;

    ConcreteDatabase() {
        db = FirebaseUtils.getFirestore();
    }

    private String getUserId() {
        Optional<User> userOpt = FirebaseAuthentication.getInstance().getCurrentUser();
        String userPath;
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            userPath = user.getEmail();
        } else {
            userPath = IdentificationService.getDeviceId();
        }
        return userPath;
    }

    private <T extends Storable<T>> CollectionReference getCollection(Storer<T> storer) {
        return db.collection(USERS_COLLECTIONS_NAME).document(getUserId()).collection(storer.getCollection().getCollectionName());
    }

    private <T extends Storable<T>> DocumentReference getDoc(Storer<T> storer, String uuid) {
        return getCollection(storer).document(uuid);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Id> store(T toStore) {
        CompletableFuture<Id> result = new CompletableFuture<>();

        Storer<T> storer = Objects.requireNonNull(toStore).storer();

        getDoc(storer, toStore.getId().getUuid()).set(toStore.storer().storableToMap(toStore))
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
        CompletableFuture<T> result = new CompletableFuture<>();

        getDoc(storer, id.getUuid())
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
        CompletableFuture<Id> result = new CompletableFuture<>();

        getDoc(storer, id.getUuid()).delete().addOnCompleteListener(res -> {
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
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        Storer<T> storer = Objects.requireNonNull(storable).storer();
        Map<String, Object> doc = storer.storableToMap(storable);

        Query q = getCollection(storer);
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
        return retrieve(id, storer).thenApply(Objects::nonNull);
    }

    private <T extends Storable<T>> CompletableFuture<List<T>> completeOnList(Task<QuerySnapshot> t, Storer<T> storer) {
        CompletableFuture<List<T>> result = new CompletableFuture<>();

        Objects.requireNonNull(t)
                .addOnCompleteListener(res -> {
                    if(res.isSuccessful()) {
                        QuerySnapshot docs = res.getResult();
                        List<T> storables = new ArrayList<>();
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
            CompletableFuture<Boolean> result = new CompletableFuture<>();
            result.complete(true);
            return result;
        }

        List<CompletableFuture<Id>> allStored = new ArrayList<>();
        for(T toStore : listToStore) {
            allStored.add(store(toStore));
        }
        //Combine all results. We don't care about the Ids returned we just care about if exceptions are thrown.
        CompletableFuture<Void> stored = CompletableFuture.allOf(allStored.toArray(new CompletableFuture[listToStore.size()]));
        return stored.thenApply(t -> true);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> retrieveAll(Storer<T> storer) {
        Task<QuerySnapshot> t = getCollection(storer).get();
        return completeOnList(t, storer);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereEquals(String key, Object value, Storer<T> storer) {
        Task<QuerySnapshot> t = getCollection(storer).whereEqualTo(Objects.requireNonNull(key), Objects.requireNonNull(value)).get();
        return completeOnList(t, storer);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreater(String key, Object value, Storer<T> storer) {
        Task<QuerySnapshot> t = getCollection(storer).whereGreaterThan(Objects.requireNonNull(key), Objects.requireNonNull(value)).get();
        return completeOnList(t, storer);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterEq(String key, Object value, Storer<T> storer) {
        Task<QuerySnapshot> t = getCollection(storer).whereGreaterThanOrEqualTo(Objects.requireNonNull(key), Objects.requireNonNull(value)).get();
        return completeOnList(t, storer);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereLess(String key, Object value, Storer<T> storer) {
        Task<QuerySnapshot> t = getCollection(storer).whereLessThan(Objects.requireNonNull(key), Objects.requireNonNull(value)).get();
        return completeOnList(t, storer);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereLessEq(String key, Object value, Storer<T> storer) {
        Task<QuerySnapshot> t = getCollection(storer).whereLessThanOrEqualTo(Objects.requireNonNull(key), Objects.requireNonNull(value)).get();
        return completeOnList(t, storer);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterEqLess(String key, Object valueGreaterEq, Object valueLess, Storer<T> storer) {
        Task<QuerySnapshot> t = getCollection(storer)
                .whereGreaterThanOrEqualTo(Objects.requireNonNull(key), Objects.requireNonNull(valueGreaterEq))
                .whereLessThan(key, Objects.requireNonNull(valueLess))
                .get();
        return completeOnList(t, storer);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterLessEq(String key, Object valueGreater, Object valueLessEq, Storer<T> storer) {
        Task<QuerySnapshot> t = getCollection(storer)
                .whereGreaterThan(Objects.requireNonNull(key), Objects.requireNonNull(valueGreater))
                .whereLessThanOrEqualTo(key, Objects.requireNonNull(valueLessEq))
                .get();
        return completeOnList(t, storer);
    }
}
