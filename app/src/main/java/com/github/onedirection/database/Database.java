package com.github.onedirection.database;

import com.github.onedirection.database.store.Storable;
import com.github.onedirection.database.store.Storer;
import com.github.onedirection.utils.Id;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Database {

    static Database getDefaultInstance() { return CachedDatabase.getInstance(); }

    <T extends Storable<T>> CompletableFuture<Id> store(T toStore);

    <T extends Storable<T>> CompletableFuture<T> retrieve(Id id, Storer<T> storer);

    <T extends Storable<T>> CompletableFuture<Id> remove(Id id, Storer<T> storer);

    <T extends Storable<T>> CompletableFuture<Boolean> contains(T storable);

    <T extends Storable<T>> CompletableFuture<Boolean> contains(Id id, Storer<T> storer);

    <T extends Storable<T>> CompletableFuture<Boolean> storeAll(List<T> listToStore);

    <T extends Storable<T>> CompletableFuture<List<T>> retrieveAll(Storer<T> storer);

    <T extends Storable<T>> CompletableFuture<List<T>> filterWhereEquals(String key, Object value, Storer<T> storer);

    <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreater(String key, Object value, Storer<T> storer);

    <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterEq(String key, Object value, Storer<T> storer);

    <T extends Storable<T>> CompletableFuture<List<T>> filterWhereLess(String key, Object value, Storer<T> storer);

    <T extends Storable<T>> CompletableFuture<List<T>> filterWhereLessEq(String key, Object value, Storer<T> storer);

    <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterEqLess(String key, Object valueGreaterEq, Object valueLess, Storer<T> storer);

    <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterLessEq(String key, Object valueGreater, Object valueLessEq, Storer<T> storer);
}
