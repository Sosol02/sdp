package com.github.onedirection.database;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The auto synced database of the application
 */
public class ConcreteDatabase /* implements Database */ {
    private final FirebaseFirestore db;

    public ConcreteDatabase() {
        db = FirebaseFirestore.getInstance();
    }

    public CompletableFuture<Id> store(Storable toStore) {

    }

    public Storable retrieve(Id id) {

    }

}
