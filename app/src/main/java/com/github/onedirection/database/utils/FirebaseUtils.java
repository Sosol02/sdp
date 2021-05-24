package com.github.onedirection.database.utils;

import com.github.onedirection.BuildConfig;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Utility class for using Firebase 'Firestore' database tools
 */
public class FirebaseUtils {
    private static final boolean useEmulator = BuildConfig.DEBUG;
    private static FirebaseFirestore firestore = null;

    public static FirebaseFirestore getFirestore() {
        if (firestore == null) {
            firestore = FirebaseFirestore.getInstance();
            if (useEmulator) {
                //firestore.useEmulator("10.0.2.2", 8080);
                FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                        .setHost("10.0.2.2:8080")
                        .setSslEnabled(false)
                        .setPersistenceEnabled(false)
                        .build();
                firestore.setFirestoreSettings(settings);
            }
        }
        return firestore;
    }

}
