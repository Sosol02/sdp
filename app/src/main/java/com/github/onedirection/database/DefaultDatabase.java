package com.github.onedirection.database;

import androidx.annotation.VisibleForTesting;

/**
 * A class to organise and hold all Database global instances.
 */
public class DefaultDatabase {

    private static final ConcreteDatabase concreteDatabase = new ConcreteDatabase();
    private static final ObservableDatabase database = new ObservableDatabase(new CachedDatabase(concreteDatabase));

    public static ObservableDatabase getDefaultInstance() { return database; }

    @VisibleForTesting
    public static final ConcreteDatabase getDefaultConcreteInstance() { return concreteDatabase; }
}
