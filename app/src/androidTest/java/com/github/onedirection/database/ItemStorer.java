package com.github.onedirection.database;

import com.github.onedirection.database.store.DatabaseCollection;
import com.github.onedirection.database.store.Storer;
import com.github.onedirection.utils.Id;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        Map<String, Object> map = new HashMap<>();
        map.put("id", storable.getId().getUuid());
        map.put("number", storable.getNumber());
        map.put("s", storable.getS());
        return map;
    }

    @Override
    public Item mapToStorable(Map<String, Object> m) {
        final Id id = new Id(UUID.fromString((String) m.get("id")));
        final int number = ((Long) m.get("number")).intValue();
        final String s = (String) m.get("s");
        return new Item(id, number, s);
    }
}
