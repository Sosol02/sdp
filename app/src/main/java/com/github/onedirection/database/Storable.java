package com.github.onedirection.database;

/**
 * Any object that wants to be stored in the database needs
 * to implement it.
 */
public interface Storable {
    // currently empty because firebase stores any Plain Old Java Object

    DatabaseCollection getCollection();
    Id getId();
}
