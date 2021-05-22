package com.github.onedirection.database.database;

import com.github.onedirection.database.store.Storable;
import com.github.onedirection.database.store.Storer;
import com.github.onedirection.utils.Id;

import static com.github.onedirection.utils.ObserverPattern.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * A wrapper around a database which implements Observable in order to add callbacks to
 * database actions.
 */
public class ObservableDatabase implements Database, Observable<ObservableDatabase.Action> {

    ObservableDatabase(Database innerDb) {
        this.innerDb = Objects.requireNonNull(innerDb);
    }

    private final ArrayList<Observer<Action>> observers = new ArrayList<>();
    private final Database innerDb;

    @Override
    public boolean addObserver(Observer<Action> observer) {
        return observers.add(Objects.requireNonNull(observer));
    }

    @Override
    public boolean removeObserver(Observer<Action> observer) {
        return observers.add(Objects.requireNonNull(observer));
    }

    public void removeAllObservers() {
        observers.clear();
    }

    /*
store
retrieve
remove
contains
contains
storeAll
retrieveAll
filterWhereEquals
filterWhereGreater
filterWhereGreaterEq
filterWhereLess
filterWhereLessEq
filterWhereGreaterEqLess
filterWhereGreaterLessEq
     */

    @Override
    public <T extends Storable<T>> CompletableFuture<Id> store(T toStore) {
        CompletableFuture<Id> result = innerDb.store(toStore)
                .thenApply(res -> {
                    observers.forEach(obs -> obs.onObservableUpdate(this, new Action(ActionKind.Store, res, toStore)));
                    return res;
                });

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<T> retrieve(Id id, Storer<T> storer) {
        CompletableFuture<T> result = innerDb.retrieve(id, storer)
                .thenApply(res -> {
                    observers.forEach(obs -> obs.onObservableUpdate(this, new Action(ActionKind.Retrieve, id, res)));
                    return res;
                });

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Id> remove(Id id, Storer<T> storer) {
        CompletableFuture<Id> result = innerDb.remove(id, storer)
                .thenApply(res -> {
                    observers.forEach(obs -> obs.onObservableUpdate(this, new Action(ActionKind.Remove, id, null)));
                    return res;
                });

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Boolean> contains(T storable) {
        CompletableFuture<Boolean> result = innerDb.contains(storable)
                .thenApply(res -> {
                    observers.forEach(obs -> obs.onObservableUpdate(this, new Action(ActionKind.Retrieve, storable.getId(), storable)));
                    return res;
                });

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Boolean> contains(Id id, Storer<T> storer) {
        CompletableFuture<Boolean> result = innerDb.contains(id, storer)
                .thenApply(res -> {
                    observers.forEach(obs -> obs.onObservableUpdate(this, new Action(ActionKind.Retrieve, id, null)));
                    return res;
                });

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<Boolean> storeAll(List<T> listToStore) {
        CompletableFuture<Boolean> result = innerDb.storeAll(listToStore)
                .thenApply(res -> {
                    observers.forEach(obs -> obs.onObservableUpdate(this,
                            new Action(ActionKind.Store, listToStore.stream().map(Storable::getId).collect(Collectors.toList()), res)));
                    return res;
                });

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> retrieveAll(Storer<T> storer) {
        CompletableFuture<List<T>> result = innerDb.retrieveAll(storer)
                .thenApply(res -> {
                    observers.forEach(obs -> obs.onObservableUpdate(this,
                            new Action(ActionKind.Retrieve, res.stream().map(Storable::getId).collect(Collectors.toList()), res)));
                    return res;
                });

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereEquals(String key, Object value, Storer<T> storer) {
        CompletableFuture<List<T>> result = innerDb.filterWhereEquals(key, value, storer)
                .thenApply(res -> {
                    observers.forEach(obs -> obs.onObservableUpdate(this,
                            new Action(ActionKind.Retrieve, res.stream().map(Storable::getId).collect(Collectors.toList()), res)));
                    return res;
                });

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreater(String key, Object value, Storer<T> storer) {
        CompletableFuture<List<T>> result = innerDb.filterWhereGreater(key, value, storer)
                .thenApply(res -> {
                    observers.forEach(obs -> obs.onObservableUpdate(this,
                            new Action(ActionKind.Retrieve, res.stream().map(Storable::getId).collect(Collectors.toList()), res)));
                    return res;
                });

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterEq(String key, Object value, Storer<T> storer) {
        CompletableFuture<List<T>> result = innerDb.filterWhereGreaterEq(key, value, storer)
                .thenApply(res -> {
                    observers.forEach(obs -> obs.onObservableUpdate(this,
                            new Action(ActionKind.Retrieve, res.stream().map(Storable::getId).collect(Collectors.toList()), res)));
                    return res;
                });

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereLess(String key, Object value, Storer<T> storer) {
        CompletableFuture<List<T>> result = innerDb.filterWhereLess(key, value, storer)
                .thenApply(res -> {
                    observers.forEach(obs -> obs.onObservableUpdate(this,
                            new Action(ActionKind.Retrieve, res.stream().map(Storable::getId).collect(Collectors.toList()), res)));
                    return res;
                });

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereLessEq(String key, Object value, Storer<T> storer) {
        CompletableFuture<List<T>> result = innerDb.filterWhereLessEq(key, value, storer)
                .thenApply(res -> {
                    observers.forEach(obs -> obs.onObservableUpdate(this,
                            new Action(ActionKind.Retrieve, res.stream().map(Storable::getId).collect(Collectors.toList()), res)));
                    return res;
                });

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterEqLess(String key, Object valueGreaterEq, Object valueLess, Storer<T> storer) {
        CompletableFuture<List<T>> result = innerDb.filterWhereGreaterEqLess(key, valueGreaterEq, valueLess, storer)
                .thenApply(res -> {
                    observers.forEach(obs -> obs.onObservableUpdate(this,
                            new Action(ActionKind.Retrieve, res.stream().map(Storable::getId).collect(Collectors.toList()), res)));
                    return res;
                });

        return result;
    }

    @Override
    public <T extends Storable<T>> CompletableFuture<List<T>> filterWhereGreaterLessEq(String key, Object valueGreater, Object valueLessEq, Storer<T> storer) {
        CompletableFuture<List<T>> result = innerDb.filterWhereGreaterLessEq(key, valueGreater, valueLessEq, storer)
                .thenApply(res -> {
                    observers.forEach(obs -> obs.onObservableUpdate(this,
                            new Action(ActionKind.Retrieve, res.stream().map(Storable::getId).collect(Collectors.toList()), res)));
                    return res;
                });

        return result;
    }

    public enum ActionKind {
        Store, Remove, Retrieve
    }

    /**
     * An event that happened in the database:
     * - Either QueryKind.Store, which is a store operation.
     * - Or QueryKind.Remove which is a remove operation.
     * - Or a QurtyKind.Retrieve, which corresponds to all other operations
     * which don't modifiy the database.
     */
    public static class Action {
        public final ActionKind kind;
        public final List<Id> ids;
        public final Object element;

        private Action(ActionKind kind, List<Id> id, Object element) {
            this.kind = Objects.requireNonNull(kind);
            this.ids = Objects.requireNonNull(id);
            this.element = element;
        }

        private Action(ActionKind kind, Id id, Object element) {
            ArrayList<Id> ids = new ArrayList<>();
            ids.add(id);
            this.kind = Objects.requireNonNull(kind);
            this.ids = ids;
            this.element = element;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Action event = (Action) o;
            return kind == event.kind &&
                    ids.equals(event.ids) &&
                    Objects.equals(element, event.element);
        }

        @Override
        public int hashCode() {
            return Objects.hash(kind, ids, element);
        }
    }
}
