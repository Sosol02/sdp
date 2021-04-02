package com.github.onedirection.database.store;

import java.util.Map;

/**
 * Any object that wants to be stored in the database needs
 * to implement it.
 * @T The type being stored
 */
public abstract class Storer<T extends Storable<T>> {
    public abstract DatabaseCollection getCollection();
    public abstract Class<T> classTag();
    public abstract Map<String, Object> storableToMap(T storable);
    public abstract T mapToStorable(Map<String, Object> m);
}
