package com.github.onedirection.database.store;

import com.github.onedirection.utils.Id;

public interface Storable<T extends Storable<T>> {
    Id getId();
    Storer<T> storer();
}
