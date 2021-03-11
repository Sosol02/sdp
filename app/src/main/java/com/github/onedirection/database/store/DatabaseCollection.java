package com.github.onedirection.database.store;

/**
 * Represents the "collection" in which to store an item.
 * A "collection" is similar to a table in some sense.
 * See firebase docs for more info.
 */
public enum DatabaseCollection {
    Debug("debug"),
    User("user"),
    Event("event"),
    ; // TODO: add the actual collections

    private final String collectionName;

    DatabaseCollection(String documentName) {
        this.collectionName = documentName;
    }

    public String getCollectionName() {
        return collectionName;
    }
}
