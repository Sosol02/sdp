package com.github.onedirection.database.implementation;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.github.onedirection.database.store.Storable;
import com.github.onedirection.database.store.Storer;
import com.github.onedirection.cache.Cache;
import com.github.onedirection.utils.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a database that contains a cache to optimize the number of queries into the database.
 */
public class CachedDatabase implements Database {

    private final Database innerDatabase;
    // We can't have a concrete type for the values. Refactoring this type should not be attempted.
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    final Cache<Id, CompletableFuture<? extends Storable<?>>> storeCache;
    private final Cache<Query, CompletableFuture<? extends List<?>>> queryCache;

    public CachedDatabase(Database innerDatabase, int cacheMaxHistory) {
        this.innerDatabase = Objects.requireNonNull(innerDatabase);
        this.storeCache = new Cache<>(cacheMaxHistory);
        this.queryCache = new Cache<>(cacheMaxHistory);
    }

    public CachedDatabase(Database innerDatabase) {
        this(innerDatabase, Cache.MAX_HISTORY_DEFAULT);
    }

    public void clearCaches() {
        storeCache.invalidate();
        queryCache.invalidate();
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Id> store(T toStore) {
        Objects.requireNonNull(toStore);
        queryCache.invalidate();
        return innerDatabase.store(toStore)
                .thenApply(id -> {
                    CompletableFuture<Storable<?>> fut = new CompletableFuture<>();
                    fut.complete(toStore);
                    storeCache.set(id, fut, (k, v) -> true, true);
                    return id;
                });
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<T> retrieve(Id id, Storer<T> storer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(storer);
        return storeCache.get(id, sameId -> innerDatabase.retrieve(id, storer))
                .whenComplete((res, err) -> {
                    Log.d("CachedDatabase", "retrieve: whenComplete: res: " + res + ", err: " + err);
                    if (err != null) {
                        Log.d("CachedDatabase", "retrieve: future failed at id: " + id + ", err: " + err);
                        storeCache.invalidate(id);
                    }
                })
                .thenApply(res -> (T) res);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Id> remove(Id id, Storer<T> storer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(storer);
        queryCache.invalidate();
        storeCache.invalidate(id);
        // Need to invalidate before returning from this function (example: one should not put the invalidate() in a thenApply call)
        // Otherwise, someone may call store(x), remove(x), retrieve(x) and still get back x because of the cache.
        return innerDatabase.remove(id, storer);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Boolean> contains(T storable) {
        Objects.requireNonNull(storable);
        return retrieve(storable.getId(), storable.storer())
                .thenApply(res -> res != null);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Boolean> contains(Id id, Storer<T> storer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(storer);
        return retrieve(id, storer).thenApply(res -> res != null);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Boolean> storeAll(List<T> listToStore) {
        Objects.requireNonNull(listToStore);
        List<T> listToStoreSame = new ArrayList<>(listToStore);
        queryCache.invalidate();
        return innerDatabase.storeAll(listToStoreSame)
                .thenApply(res -> {
                    for (T t : listToStoreSame) {
                        CompletableFuture<Storable<?>> fut = new CompletableFuture<>();
                        fut.complete(t);
                        storeCache.set(t.getId(), fut, (k, v) -> true, true);
                    }
                    return res;
                });
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> retrieveAll(Storer<T> storer) {
        Objects.requireNonNull(storer);
        return innerDatabase.retrieveAll(storer)
                .thenApply(res -> {
                    for (T t : res) {
                        CompletableFuture<Storable<?>> fut = new CompletableFuture<>();
                        fut.complete(t);
                        storeCache.set(t.getId(), fut, (k, v) -> true, true);
                    }
                    return res;
                });
    }

    private static class Query {

        public enum QueryType {
            Eq, Gr, GrEq, Le, LeEq, GrEqLe, GrLeEq
        }

        private final Class<?> classTag;
        private final QueryType queryType;
        private final String key;
        private final Object valueUp;
        private final Object valueDown;

        Query(Class<?> classTag, QueryType queryType, String key, Object valueUp, Object valueDown) {
            this.classTag = Objects.requireNonNull(classTag);
            this.queryType = Objects.requireNonNull(queryType);
            this.key = Objects.requireNonNull(key);
            this.valueUp = Objects.requireNonNull(valueUp);
            this.valueDown = valueDown; // Allow null, this field is unused except for GrEqLe and GrLeEq

            if (valueDown != null && queryType != QueryType.GrEqLe && queryType != QueryType.GrLeEq)
                throw new IllegalArgumentException("valueDown may only be null iff queryType is GrEqLe or GrLeEq.");
        }

        Query(Class<?> classTag, QueryType queryType, String key, Object valueUp) {
            this(classTag, queryType, key, valueUp, null);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Query query = (Query) o;
            return classTag.equals(query.classTag) &&
                    queryType == query.queryType &&
                    key.equals(query.key) &&
                    valueUp.equals(query.valueUp) &&
                    Objects.equals(valueDown, query.valueDown);
        }

        @Override
        public int hashCode() {
            return Objects.hash(classTag, queryType, key, valueUp, valueDown);
        }
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereEquals(String key, Object value, Storer<T> storer) {
        final Query query = new Query(storer.classTag(), Query.QueryType.Eq, key, value);
        return queryCache.get(query, sameQuery -> innerDatabase.filterWhereEquals(key, value, storer))
                .whenComplete((res, err) -> { if (err != null) queryCache.invalidate(query); })
                .thenApply(res -> (List<T>) res);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreater(String key, Object value, Storer<T> storer) {
        final Query query = new Query(storer.classTag(), Query.QueryType.Gr, key, value);
        return queryCache.get(query, sameQuery -> innerDatabase.filterWhereGreater(key, value, storer))
                .whenComplete((res, err) -> { if (err != null) queryCache.invalidate(query); })
                .thenApply(res -> (List<T>) res);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterEq(String key, Object value, Storer<T> storer) {
        final Query query = new Query(storer.classTag(), Query.QueryType.GrEq, key, value);
        return queryCache.get(query, sameQuery -> innerDatabase.filterWhereGreaterEq(key, value, storer))
                .whenComplete((res, err) -> { if (err != null) queryCache.invalidate(query); })
                .thenApply(res -> (List<T>) res);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereLess(String key, Object value, Storer<T> storer) {
        final Query query = new Query(storer.classTag(), Query.QueryType.Le, key, value);
        return queryCache.get(query, sameQuery -> innerDatabase.filterWhereLess(key, value, storer))
                .whenComplete((res, err) -> { if (err != null) queryCache.invalidate(query); })
                .thenApply(res -> (List<T>) res);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereLessEq(String key, Object value, Storer<T> storer) {
        final Query query = new Query(storer.classTag(), Query.QueryType.LeEq, key, value);
        return queryCache.get(query, sameQuery -> innerDatabase.filterWhereLessEq(key, value, storer))
                .whenComplete((res, err) -> { if (err != null) queryCache.invalidate(query); })
                .thenApply(res -> (List<T>) res);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterEqLess(String key, Object valueGreaterEq, Object valueLess, Storer<T> storer) {
        final Query query = new Query(storer.classTag(), Query.QueryType.GrEqLe, key, valueGreaterEq, valueLess);
        return queryCache.get(query, sameQuery -> innerDatabase.filterWhereGreaterEqLess(key, valueGreaterEq, valueLess, storer))
                .whenComplete((res, err) -> {
                    if (err != null) queryCache.invalidate(query);
                })
                .thenApply(res -> (List<T>) res);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterLessEq(String key, Object valueGreater, Object valueLessEq, Storer<T> storer) {
        final Query query = new Query(storer.classTag(), Query.QueryType.GrLeEq, key, valueGreater, valueLessEq);
        return queryCache.get(query, sameQuery -> innerDatabase.filterWhereGreaterLessEq(key, valueGreater, valueLessEq, storer))
                .whenComplete((res, err) -> {
                    if (err != null) queryCache.invalidate(query);
                })
                .thenApply(res -> (List<T>) res);
    }
}
