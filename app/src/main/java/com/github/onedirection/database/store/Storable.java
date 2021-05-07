package com.github.onedirection.database.store;

import com.github.onedirection.utils.Id;

/**
 * Any object that wants to be stored in the database needs
 * to implement it.
 * It specifies that this object can be stored into the database, and that a Storer has been defined for this object.
 * @param <T> The type being stored
 */
public interface Storable<T extends Storable<T>> {
    Id getId();
    Storer<T> storer();
}
