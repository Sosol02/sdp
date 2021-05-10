package com.github.onedirection.database;

import androidx.annotation.VisibleForTesting;

public class DefaultDatabase {

    private static final ConcreteDatabase concreteDatabase = new ConcreteDatabase();
    private static final ObservableDatabase database = new ObservableDatabase(new CachedDatabase(new ConcreteDatabase()));

    public static ObservableDatabase getDefaultInstance() { return database; }

    @VisibleForTesting
    public static final ConcreteDatabase getDefaultConcreteInstance() { return concreteDatabase; }
}
