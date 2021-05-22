package com.github.onedirection.database.database;

import com.github.onedirection.database.store.Storable;
import com.github.onedirection.database.store.Storer;
import com.github.onedirection.utils.Id;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class implements Database with default methods that throw.
 * Useful for tests.
 */
public class MockDatabase implements Database {
    @Override
    public <T extends Storable<T>> CompletableFuture<Id> store(T toStore) {
        throw new RuntimeException("MockDatabase: not implemented.");
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<T> retrieve(Id id, Storer<T> storer) {
        throw new RuntimeException("MockDatabase: not implemented.");
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Id> remove(Id id, Storer<T> storer) {
        throw new RuntimeException("MockDatabase: not implemented.");
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Boolean> contains(T storable) {
        throw new RuntimeException("MockDatabase: not implemented.");
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Boolean> contains(Id id, Storer<T> storer) {
        throw new RuntimeException("MockDatabase: not implemented.");
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Boolean> storeAll(List<T> listToStore) {
        throw new RuntimeException("MockDatabase: not implemented.");
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> retrieveAll(Storer<T> storer) {
        throw new RuntimeException("MockDatabase: not implemented.");
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereEquals(String key, Object value, Storer<T> storer) {
        throw new RuntimeException("MockDatabase: not implemented.");
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreater(String key, Object value, Storer<T> storer) {
        throw new RuntimeException("MockDatabase: not implemented.");
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterEq(String key, Object value, Storer<T> storer) {
        throw new RuntimeException("MockDatabase: not implemented.");
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereLess(String key, Object value, Storer<T> storer) {
        throw new RuntimeException("MockDatabase: not implemented.");
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereLessEq(String key, Object value, Storer<T> storer) {
        throw new RuntimeException("MockDatabase: not implemented.");
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterEqLess(String key, Object valueGreaterEq, Object valueLess, Storer<T> storer) {
        throw new RuntimeException("MockDatabase: not implemented.");
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterLessEq(String key, Object valueGreater, Object valueLessEq, Storer<T> storer) {
        throw new RuntimeException("MockDatabase: not implemented.");
    }
}
