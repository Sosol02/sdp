package com.github.onedirection.database;

import com.github.onedirection.database.store.Storable;
import com.github.onedirection.database.store.Storer;
import com.github.onedirection.utils.Id;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Database {

    static Database getDefaultInstance(){ return ConcreteDatabase.getDatabase(); }

    public <T extends Storable<T>> CompletableFuture<Id> store(T toStore);

    public <T extends Storable<T>> CompletableFuture<T> retrieve(Id id, Storer<T> storer);

    public <T extends Storable<T>> CompletableFuture<Id> remove(Id id, Storer<T> storer);

    public <T extends Storable<T>> CompletableFuture<Boolean> contains(T storable);

    public <T extends Storable<T>> CompletableFuture<Boolean> contains(Id id, Storer<T> storer);

    public <T extends Storable<T>> CompletableFuture<Boolean> storeAll(List<T> listToStore);

    public <T extends Storable<T>> CompletableFuture<List<T>> retrieveAll(Storer<T> storer);
}
