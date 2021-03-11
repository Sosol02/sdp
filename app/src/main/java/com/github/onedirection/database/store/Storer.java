package com.github.onedirection.database.store;

/**
 * Any object that wants to be stored in the database needs
 * to implement it.
 * @T The type being stored
 */
public abstract class Storer<T extends Storable<T>> {
    public abstract DatabaseCollection getCollection();
    public abstract Class<T> classTag();
}
