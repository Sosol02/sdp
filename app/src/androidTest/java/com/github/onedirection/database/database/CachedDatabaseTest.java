package com.github.onedirection.database.database;

import com.github.onedirection.database.store.Item;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.database.store.Storable;
import com.github.onedirection.database.store.Storer;
import com.github.onedirection.event.model.Event;
import com.github.onedirection.utils.Id;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CachedDatabaseTest {
    private static final int count = 10;

    @Before
    public void deleteAllEvents() throws ExecutionException, InterruptedException {
        ConcreteDatabase db = DefaultDatabase.getDefaultConcreteInstance();
        List<Event> events = db.retrieveAll(EventStorer.getInstance()).get();
        for(Event e : events) {
            Id id = db.remove(e.getId(), EventStorer.getInstance()).get();
            assertEquals(e.getId(), id);
        }
    }

    @Test
    public void canStoreAndRetrieveAndRemoveEvents() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.canStoreAndRetrieveAndRemoveEvents(new CachedDatabase(DefaultDatabase.getDefaultConcreteInstance()));
    }

    @Test
    public void containsOnEventObject() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.containsOnEventObject(new CachedDatabase(DefaultDatabase.getDefaultConcreteInstance()));
    }

    @Test
    public void containsOnEventId() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.containsOnEventId(new CachedDatabase(DefaultDatabase.getDefaultConcreteInstance()));
    }

    @Test
    public void storeAllWorksJustLikeManyStores() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.storeAllWorksJustLikeManyStores(new CachedDatabase(DefaultDatabase.getDefaultConcreteInstance()));
    }

    @Test
    public void storeAllWorksOnEmptyList() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.storeAllWorksOnEmptyList(new CachedDatabase(DefaultDatabase.getDefaultConcreteInstance()));
    }

    @Test
    public void retrieveAllWorksJustLikeManyRetrieves() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.retrieveAllWorksJustLikeManyRetrieves(new CachedDatabase(DefaultDatabase.getDefaultConcreteInstance()));
    }

    @Test
    public void retrieveOnFilterKeyQueryActsLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.retrieveOnFilterKeyQueryActsLikeRDB(new CachedDatabase(DefaultDatabase.getDefaultConcreteInstance()));
    }

    @Test
    public void sameIdOverridesEntry() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.sameIdOverridesEntry(new CachedDatabase(DefaultDatabase.getDefaultConcreteInstance()));
    }

    @Test
    public void filterWhereGreaterFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereGreaterFiltersLikeRDB(new CachedDatabase(DefaultDatabase.getDefaultConcreteInstance()));
    }

    @Test
    public void filterWhereGreaterEqFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereGreaterEqFiltersLikeRDB(new CachedDatabase(DefaultDatabase.getDefaultConcreteInstance()));
    }

    @Test
    public void filterWhereLessFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereLessFiltersLikeRDB(new CachedDatabase(DefaultDatabase.getDefaultConcreteInstance()));
    }

    @Test
    public void filterWhereLessEqFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereLessEqFiltersLikeRDB(new CachedDatabase(DefaultDatabase.getDefaultConcreteInstance()));
    }

    @Test
    public void filterWhereGreaterEqLessFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereGreaterEqLessFiltersLikeRDB(new CachedDatabase(DefaultDatabase.getDefaultConcreteInstance()));
    }

    @Test
    public void filterWhereGreaterLessEqFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereGreaterLessEqFiltersLikeRDB(DefaultDatabase.getDefaultConcreteInstance());
    }

    @Test
    public void failedFutureInvalidatesCache() throws ExecutionException, InterruptedException {
        int[] counter = new int[] {0};
        CompletableFuture<? extends Storable<?>> fut = new CompletableFuture<>();
        final ConcreteDatabase realdb = DefaultDatabase.getDefaultConcreteInstance();
        final CachedDatabase cdb = new CachedDatabase(new MockDatabase() {
            @Override
            public <T extends Storable<T>> CompletableFuture<Id> store(T toStore) {
                return realdb.store(toStore);
            }

            @Override
            public <T extends Storable<T>> CompletableFuture<T> retrieve(Id id, Storer<T> storer) {
                if (counter[0] == 1) {
                    return (CompletableFuture<T>) fut;
                }
                return realdb.retrieve(id, storer);
            }
        });

        Item[] items = getItems(10);

        CompletableFuture<Id> storeF = cdb.store(items[0]);
        storeF.get();
        assertThat(cdb.storeCache.getMap().containsKey(items[0].getId()), is(true));

        CompletableFuture<Item> retrieveF = cdb.retrieve(items[0].getId(), items[0].storer());
        retrieveF.completeExceptionally(new RuntimeException("ZA WARUDO!!")); // ignored since future is already completed by db on the storeF.get()
        retrieveF.get();
        assertThat(cdb.storeCache.getMap().containsKey(items[0].getId()), is(true)); // future was cached

        // ---

        cdb.storeCache.invalidate(items[0].getId());
        counter[0] = 1;

        Id brokenId = Id.generateRandom();

        assertThat(cdb.storeCache.getMap().containsKey(brokenId), is(false)); // future was not cached and removed from cache

        CompletableFuture<Item> retrieveF2 = cdb.retrieve(brokenId, items[0].storer());
        fut.completeExceptionally(new RuntimeException("ZA WARUDO!!"));
        boolean ok = false;
        try {
            retrieveF2.get();
        } catch (ExecutionException ignored) {
            ok = true;
        }
        assertTrue(ok);
        assertThat(cdb.storeCache.getMap().containsKey(brokenId), is(false)); // future was not cached and removed from cache
    }

    private Item[] getItems(int n) {
        Item[] items = new Item[n];
        for (int i = 0; i < n; ++i) {
            items[i] = new Item(i, Integer.toString(i));
        }
        return items;
    }
    
}
