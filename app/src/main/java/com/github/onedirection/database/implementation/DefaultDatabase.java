package com.github.onedirection.database.implementation;

import androidx.annotation.VisibleForTesting;

/**
 * A class to organise and hold all Database global instances.
 */
public class DefaultDatabase {

    private static final ConcreteDatabase concreteDatabase = new ConcreteDatabase();
    private static final CachedDatabase cachedDatabase = new CachedDatabase(concreteDatabase);
    private static final ObservableDatabase database = new ObservableDatabase(cachedDatabase);

    public static ObservableDatabase getDefaultInstance() { return database; }

    public static void clearCaches() {
        cachedDatabase.clearCaches();
    }

    public static ConcreteDatabase getDefaultConcreteInstance() { return concreteDatabase; }
}
