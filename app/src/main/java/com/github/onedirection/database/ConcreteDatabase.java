package com.github.onedirection.database;

import android.util.Log;

import com.github.onedirection.database.store.Id;
import com.github.onedirection.database.store.Storable;
import com.github.onedirection.database.store.Storer;
import com.github.onedirection.database.utils.FirebaseUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The auto synced database of the application
 */
public class ConcreteDatabase /* implements Database */ {
    private final FirebaseFirestore db;

    private static final ConcreteDatabase global = new ConcreteDatabase();

    public static ConcreteDatabase getDatabase() {
        return global;
    }

    private ConcreteDatabase() {
        db = FirebaseUtils.getFirestore();
    }

    public <T extends Storable<T>> CompletableFuture<Id> store(T toStore) {
        CompletableFuture<Id> result = new CompletableFuture<Id>();

        Storer<T> storer = toStore.storer();

        db.collection(storer.getCollection().getCollectionName())
                .add(toStore)
                .addOnCompleteListener(res -> {
                    if (res.isSuccessful()) {
                        result.complete(toStore.getId());
                    } else {
                        result.completeExceptionally(res.getException());
                    }
                }).addOnCanceledListener(() -> result.cancel(false));

        return result;
    }

    public <T extends Storable<T>> CompletableFuture<T> retrieve(Id id, Storer<T> storer) {
        CompletableFuture<T> result = new CompletableFuture<T>();

        db.collection(storer.getCollection().getCollectionName())
                .whereEqualTo("id", id)
                .get()
                .addOnCompleteListener(res -> {
                    if (res.isSuccessful()) {
                        List<DocumentSnapshot> docs = res.getResult()
                                .getDocuments();

                        if (docs.size() > 1) {
                            // TODO: make real exception type
                            Log.d("DB", String.format("More than 1 element with given id: %s", id.toString()));
                            result.completeExceptionally(new RuntimeException("Id not unique"));
                        } else {
                            // TODO: docs don't specify what happens if class cast fails
                            T obj = docs.get(0).toObject(storer.classTag());
                            result.complete(obj);
                        }
                    } else {
                        result.completeExceptionally(res.getException());
                    }
                }).addOnCanceledListener(() -> result.cancel(false));

        return result;
    }

}
