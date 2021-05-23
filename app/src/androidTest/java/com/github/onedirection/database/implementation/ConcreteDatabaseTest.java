package com.github.onedirection.database.implementation;

import androidx.test.ext.junit.runners.AndroidJUnit4;


import com.github.onedirection.event.model.Event;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.utils.Id;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ConcreteDatabaseTest {

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
        CommonDatabaseTests.canStoreAndRetrieveAndRemoveEvents(DefaultDatabase.getDefaultConcreteInstance());
    }

    @Test
    public void containsOnEventObject() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.containsOnEventObject(DefaultDatabase.getDefaultConcreteInstance());
    }

    @Test
    public void containsOnEventId() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.containsOnEventId(DefaultDatabase.getDefaultConcreteInstance());
    }

    @Test
    public void storeAllWorksJustLikeManyStores() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.storeAllWorksJustLikeManyStores(DefaultDatabase.getDefaultConcreteInstance());
    }

    @Test
    public void storeAllWorksOnEmptyList() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.storeAllWorksOnEmptyList(DefaultDatabase.getDefaultConcreteInstance());
    }

    @Test
    public void retrieveAllWorksJustLikeManyRetrieves() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.retrieveAllWorksJustLikeManyRetrieves(DefaultDatabase.getDefaultConcreteInstance());
    }

    @Test
    public void retrieveOnFilterKeyQueryActsLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.retrieveOnFilterKeyQueryActsLikeRDB(DefaultDatabase.getDefaultConcreteInstance());
    }

    @Test
    public void sameIdOverridesEntry() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.sameIdOverridesEntry(DefaultDatabase.getDefaultConcreteInstance());
    }

    @Test
    public void filterWhereGreaterFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereGreaterFiltersLikeRDB(DefaultDatabase.getDefaultConcreteInstance());
    }

    @Test
    public void filterWhereGreaterEqFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereGreaterEqFiltersLikeRDB(DefaultDatabase.getDefaultConcreteInstance());
    }

    @Test
    public void filterWhereLessFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereLessFiltersLikeRDB(DefaultDatabase.getDefaultConcreteInstance());
    }

    @Test
    public void filterWhereLessEqFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereLessEqFiltersLikeRDB(DefaultDatabase.getDefaultConcreteInstance());
    }

    @Test
    public void filterWhereGreaterEqLessFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereGreaterEqLessFiltersLikeRDB(DefaultDatabase.getDefaultConcreteInstance());
    }

    @Test
    public void filterWhereGreaterLessEqFiltersLikeRDB() throws ExecutionException, InterruptedException {
        CommonDatabaseTests.filterWhereGreaterLessEqFiltersLikeRDB(DefaultDatabase.getDefaultConcreteInstance());
    }
}
