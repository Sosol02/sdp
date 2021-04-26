package com.github.onedirection.database;

import com.github.onedirection.database.store.Storable;
import com.github.onedirection.database.store.Storer;
import com.github.onedirection.utils.Cache;
import com.github.onedirection.utils.Id;
import com.github.onedirection.utils.Pair;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CachedDatabase implements Database {

    private final Database innerDatabase;
    // sadly, we can't have a concrete type for the values.
    private final Cache<Id, CompletableFuture<?>> storeCache;
    private final Cache<Query, CompletableFuture<? extends List<?>>> queryCache;

    public CachedDatabase(Database innerDatabase, int cacheMaxHistory) {
        this.innerDatabase = Objects.requireNonNull(innerDatabase);
        this.storeCache = new Cache<>(cacheMaxHistory);
        this.queryCache = new Cache<>(cacheMaxHistory);
    }

    public CachedDatabase(Database innerDatabase) {
        this(innerDatabase, Cache.MAX_HISTORY_DEFAULT);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Id> store(T toStore) {
        Objects.requireNonNull(toStore);
        queryCache.invalidate();
        return storeCache.get(toStore.getId(), id -> innerDatabase.store(toStore))
                .thenApply(res -> (Id) res);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<T> retrieve(Id id, Storer<T> storer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(storer);
        return storeCache.get(id, sameId -> innerDatabase.retrieve(id, storer))
                .thenApply(res -> (T) res);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Id> remove(Id id, Storer<T> storer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(storer);
        queryCache.invalidate();
        storeCache.invalidate(id);
        // need to invalidate before returning from this function (like don't put the invalidate() in a thenApply)
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
        queryCache.invalidate();
        return innerDatabase.storeAll(listToStore)
                // This is kinda dumb because the inner db will always return true, it should really
                // be Void: either it is exceptionally completed or it succeeded, there is no Success(false).
                .thenApply(res -> {
                    for (T t : listToStore) {
                        CompletableFuture<Object> fut = new CompletableFuture<>();
                        fut.complete(t);
                        storeCache.set(t.getId(), fut, (k, v) -> true, true);
                    }
                    return res;
                });
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> retrieveAll(Storer<T> storer) {
        Objects.requireNonNull(storer);
        //return completeOnList(innerDatabase.retrieveAll(storer));
        return innerDatabase.retrieveAll(storer)
                .thenApply(res -> {
                    for (T t : res) {
                        CompletableFuture<Object> fut = new CompletableFuture<>();
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
            this.valueDown = valueDown; // allow null, this field is unused except for GrEqLe and GrLeEq

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

    // in these functions most null checks are done in the Query constructor.

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereEquals(String key, Object value, Storer<T> storer) {
        return queryCache.get(new Query(storer.classTag(), Query.QueryType.Eq, key, value), query ->
            innerDatabase.filterWhereEquals(key, value, storer)
        ).thenApply(res -> (List<T>) res);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreater(String key, Object value, Storer<T> storer) {
        return queryCache.get(new Query(storer.classTag(), Query.QueryType.Gr, key, value), query ->
                innerDatabase.filterWhereGreater(key, value, storer)
        ).thenApply(res -> (List<T>) res);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterEq(String key, Object value, Storer<T> storer) {
        return queryCache.get(new Query(storer.classTag(), Query.QueryType.GrEq, key, value), query ->
                innerDatabase.filterWhereGreaterEq(key, value, storer)
        ).thenApply(res -> (List<T>) res);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereLess(String key, Object value, Storer<T> storer) {
        return queryCache.get(new Query(storer.classTag(), Query.QueryType.Le, key, value), query ->
                innerDatabase.filterWhereLess(key, value, storer)
        ).thenApply(res -> (List<T>) res);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereLessEq(String key, Object value, Storer<T> storer) {
        return queryCache.get(new Query(storer.classTag(), Query.QueryType.LeEq, key, value), query ->
                innerDatabase.filterWhereLessEq(key, value, storer)
        ).thenApply(res -> (List<T>) res);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterEqLess(String key, Object valueGreaterEq, Object valueLess, Storer<T> storer) {
        return queryCache.get(new Query(storer.classTag(), Query.QueryType.GrEqLe, key, valueGreaterEq, valueLess), query ->
                innerDatabase.filterWhereGreaterEqLess(key, valueGreaterEq, valueLess, storer)
        ).thenApply(res -> (List<T>) res);
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterLessEq(String key, Object valueGreater, Object valueLessEq, Storer<T> storer) {
        return queryCache.get(new Query(storer.classTag(), Query.QueryType.GrLeEq, key, valueGreater, valueLessEq), query ->
                innerDatabase.filterWhereGreaterLessEq(key, valueGreater, valueLessEq, storer)
        ).thenApply(res -> (List<T>) res);
    }
}
