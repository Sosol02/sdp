package com.github.onedirection.testdatabase;

import com.github.onedirection.database.store.DatabaseCollection;
import com.github.onedirection.database.store.Storer;

import java.util.Map;

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

    @Override
    public Map<String, Object> storableToMap(Item storable) {
        return null;
    }

    @Override
    public Item mapToStorable(Map<String, Object> m) {
        return null;
    }
}
