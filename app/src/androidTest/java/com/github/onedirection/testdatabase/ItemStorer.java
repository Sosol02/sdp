package com.github.onedirection.testdatabase;

import com.github.onedirection.database.store.DatabaseCollection;
import com.github.onedirection.database.store.Storer;

public class ItemStorer extends Storer<Item> {
    private static final ItemStorer global = new ItemStorer();

    public static ItemStorer getInstance() {
        return global;
    }

    @Override
    public DatabaseCollection getCollection() {
        return DatabaseCollection.Debug;
    }

    @Override
    public Class<Item> classTag() {
        return Item.class;
    }
}
