package com.github.onedirection.database.store;

public interface Storable<T extends Storable<T>> {
    Id getId();
    Storer<T> storer();
    //Map<String, Object> toMap();
}
