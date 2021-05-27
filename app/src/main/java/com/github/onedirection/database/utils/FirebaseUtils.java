package com.github.onedirection.database.utils;

import com.github.onedirection.BuildConfig;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Utility class for using Firebase 'Firestore' database tools
 */
public class FirebaseUtils {
    private static final boolean useProdDb = BuildConfig.USE_PROD_DB;
    private static FirebaseFirestore firestore = null;

    public static FirebaseFirestore getFirestore() {
        if (firestore == null) {
            firestore = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings;
            if (useProdDb) {
                settings = new FirebaseFirestoreSettings.Builder()
                        .setSslEnabled(true)
                        .setPersistenceEnabled(true)
                        .build();
            } else {
                //firestore.useEmulator("10.0.2.2", 8080);
                settings = new FirebaseFirestoreSettings.Builder()
                        .setHost("10.0.2.2:8080")
                        .setSslEnabled(false)
                        .setPersistenceEnabled(true)
                        .build();
            }
            firestore.setFirestoreSettings(settings);
        }
        return firestore;
    }

}
