package com.github.onedirection.database;

import androidx.annotation.VisibleForTesting;

public class DefaultDatabase {

    private static final ConcreteDatabase concreteDatabase = new ConcreteDatabase();
    private static final Database database = new ObservableDatabase(new CachedDatabase(new ConcreteDatabase()));

    static final Database getDefaultInstance() { return database; }

    @VisibleForTesting
    public static final ConcreteDatabase getDefaultConcreteInstance() { return concreteDatabase; }
}
