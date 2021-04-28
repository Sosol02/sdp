package com.github.onedirection.database;

import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.events.Event;
import com.github.onedirection.utils.Id;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class CachedDatabaseTest {
    private static int count = 10;

    @Before
    public void deleteAllEvents() throws ExecutionException, InterruptedException {
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        List<Event> events = db.retrieveAll(EventStorer.getInstance()).get();
        for(Event e : events) {
            Id id = db.remove(e.getId(), EventStorer.getInstance()).get();
            assertEquals(e.getId(), id);
        }
    }

    @Test
    public void canStoreAndRetrieveAndRemoveEvents() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.canStoreAndRetrieveAndRemoveEvents(new CachedDatabase(ConcreteDatabase.getDatabase()));
    }

    @Test
    public void containsOnEventObject() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.containsOnEventObject(new CachedDatabase(ConcreteDatabase.getDatabase()));
    }

    @Test
    public void containsOnEventId() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.containsOnEventId(new CachedDatabase(ConcreteDatabase.getDatabase()));
    }

    @Test
    public void storeAllWorksJustLikeManyStores() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.storeAllWorksJustLikeManyStores(new CachedDatabase(ConcreteDatabase.getDatabase()));
    }

    @Test
    public void storeAllWorksOnEmptyList() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.storeAllWorksOnEmptyList(new CachedDatabase(ConcreteDatabase.getDatabase()));
    }

    @Test
    public void retrieveAllWorksJustLikeManyRetrieves() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.retrieveAllWorksJustLikeManyRetrieves(new CachedDatabase(ConcreteDatabase.getDatabase()));
    }

    @Test
    public void retrieveOnFilterKeyQueryActsLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.retrieveOnFilterKeyQueryActsLikeRDB(new CachedDatabase(ConcreteDatabase.getDatabase()));
    }

    @Test
    public void sameIdOverridesEntry() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.sameIdOverridesEntry(new CachedDatabase(ConcreteDatabase.getDatabase()));
    }

    @Test
    public void filterWhereGreaterFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereGreaterFiltersLikeRDB(new CachedDatabase(ConcreteDatabase.getDatabase()));
    }

    @Test
    public void filterWhereGreaterEqFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereGreaterEqFiltersLikeRDB(new CachedDatabase(ConcreteDatabase.getDatabase()));
    }

    @Test
    public void filterWhereLessFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereLessFiltersLikeRDB(new CachedDatabase(ConcreteDatabase.getDatabase()));
    }

    @Test
    public void filterWhereLessEqFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereLessEqFiltersLikeRDB(new CachedDatabase(ConcreteDatabase.getDatabase()));
    }

    @Test
    public void filterWhereGreaterEqLessFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereGreaterEqLessFiltersLikeRDB(new CachedDatabase(ConcreteDatabase.getDatabase()));
    }

    @Test
    public void filterWhereGreaterLessEqFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereGreaterLessEqFiltersLikeRDB(ConcreteDatabase.getDatabase());
    }
}
