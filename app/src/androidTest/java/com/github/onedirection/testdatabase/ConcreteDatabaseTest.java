package com.github.onedirection.testdatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.Event;
import com.github.onedirection.EventQueriesTest;
import com.github.onedirection.database.ConcreteDatabase;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.geocoding.NamedCoordinates;
import com.github.onedirection.utils.Id;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ConcreteDatabaseTest {

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
        Event[] events = EventQueriesTest.makeEvents(count);
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        ArrayList<Id> ids = new ArrayList<Id>();
        for (int i = 0; i < events.length; ++i) {
            ids.add(db.store(events[i]).get());
        }
        for (int i = 0; i < events.length; ++i) {
            Event e = db.retrieve(ids.get(i), EventStorer.getInstance()).get();
            assertEquals(events[i], e);
        }
        for (int i = 0; i < events.length; ++i) {
            Id id = db.remove(ids.get(i), EventStorer.getInstance()).get();
            assertEquals(ids.get(i), id);
            Boolean contains = db.contains(ids.get(i), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    @Test
    public void containsOnEventObject() throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        for (int i = 0; i < events.length; ++i) {
            Id id = db.store(events[i]).get();
            assertEquals(events[i].getId(), id);
        }
        for(int i = 0; i < events.length; ++i) {
            Boolean contains = db.contains(events[i]).get();
            assertTrue(contains);
        }
        for(int i = 0; i < events.length; ++i) {
            Id id = db.remove(events[i].getId(), EventStorer.getInstance()).get();
            assertEquals(events[i].getId(), id);
            Boolean contains = db.contains(events[i]).get();
            assertFalse(contains);
        }
    }

    @Test
    public void containsOnEventId() throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        for (int i = 0; i < events.length; ++i) {
            Id id = db.store(events[i]).get();
            assertEquals(events[i].getId(), id);
        }
        for(int i = 0; i < events.length; ++i) {
            Boolean contains = db.contains(events[i].getId(), EventStorer.getInstance()).get();
            assertTrue(contains);
        }
        for(int i = 0; i < events.length; ++i) {
            Id id = db.remove(events[i].getId(), EventStorer.getInstance()).get();
            assertEquals(events[i].getId(), id);
            Boolean contains = db.contains(events[i].getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    @Test
    public void storeAllWorksJustLikeManyStores() throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        Boolean stored = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        for(int i = 0; i < events.length; ++i) {
            Boolean contains = db.contains(events[i].getId(), EventStorer.getInstance()).get();
            assertTrue(contains);
        }
        for(int i = 0; i < events.length; ++i) {
            Id id = db.remove(events[i].getId(), EventStorer.getInstance()).get();
            assertEquals(events[i].getId(), id);
            Boolean contains = db.contains(events[i].getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    @Test
    public void storeAllWorksOnEmptyList() throws ExecutionException, InterruptedException {
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        Boolean stored = db.storeAll(new ArrayList<Event>()).get();
        assertTrue(stored);
    }

    @Test
    public void retrieveAllWorksJustLikeManyRetrieves() throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        Boolean stored = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        List<Event> res = db.retrieveAll(EventStorer.getInstance()).get();
        assertEquals(events.length, res.size());
        for(int i = 0; i < events.length; ++i) {
            Boolean contains = false;
            for(Event e : res) {
                if(e.equals(events[i])) {
                    contains = true;
                }
            }
            assertTrue(contains);
        }
        for(int i = 0; i < events.length; ++i) {
            Id id = db.remove(events[i].getId(), EventStorer.getInstance()).get();
            assertEquals(events[i].getId(), id);
            Boolean contains = db.contains(events[i].getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    @Test
    public void retrieveOnFilterKeyQueryActsLikeRDB() throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        Boolean stored = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        for(int i=0; i<events.length; ++i) {
            List<Event> e = db.filterWhereEquals(EventStorer.KEY_NAME, "MyCity"+i, EventStorer.getInstance()).get();
            assertEquals(1, e.size());
            if(e.size() > 0) {
                assertEquals(events[i], e.get(0));
            }
        }
        for(int i = 0; i < events.length; ++i) {
            Id id = db.remove(events[i].getId(), EventStorer.getInstance()).get();
            assertEquals(events[i].getId(), id);
            Boolean contains = db.contains(events[i].getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    @Test
    public void sameIdOverridesEntry() throws ExecutionException, InterruptedException  {
        Event[] events = EventQueriesTest.makeEvents(count);
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        Boolean stored = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        Boolean stored2 = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        List<Event> eventsInDb= db.retrieveAll(EventStorer.getInstance()).get();
        assertEquals(events.length, eventsInDb.size());
        for(int i = 0; i < events.length; ++i) {
            Id id = db.remove(events[i].getId(), EventStorer.getInstance()).get();
            assertEquals(events[i].getId(), id);
            Boolean contains = db.contains(events[i].getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    @Test
    public void filterWhereGreaterFiltersLikeRDB() throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        Boolean stored = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        for(int i=0; i<events.length; ++i) {
            List<Event> e = db.filterWhereGreater(EventStorer.KEY_EPOCH_START_TIME, events[i].getStartTime().toEpochSecond(), EventStorer.getInstance()).get();
            assertEquals(events.length-i-1, e.size());
            for(int j=i+1; j<events.length; ++j) {
                assertEquals(events[j], e.get(j-i-1));
            }
        }
        for(int i = 0; i < events.length; ++i) {
            Id id = db.remove(events[i].getId(), EventStorer.getInstance()).get();
            assertEquals(events[i].getId(), id);
            Boolean contains = db.contains(events[i].getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    @Test
    public void filterWhereGreaterEqFiltersLikeRDB() throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        Boolean stored = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        for(int i=0; i<events.length; ++i) {
            List<Event> e = db.filterWhereGreaterEq(EventStorer.KEY_EPOCH_START_TIME, events[i].getStartTime().toEpochSecond(), EventStorer.getInstance()).get();
            assertEquals(events.length-i, e.size());
            for(int j=i; j<events.length; ++j) {
                assertEquals(events[j], e.get(j-i));
            }
        }
        for(int i = 0; i < events.length; ++i) {
            Id id = db.remove(events[i].getId(), EventStorer.getInstance()).get();
            assertEquals(events[i].getId(), id);
            Boolean contains = db.contains(events[i].getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    @Test
    public void filterWhereLessFiltersLikeRDB() throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        Boolean stored = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        for(int i=0; i<events.length; ++i) {
            List<Event> e = db.filterWhereLess(EventStorer.KEY_EPOCH_START_TIME, events[i].getStartTime().toEpochSecond(), EventStorer.getInstance()).get();
            assertEquals(i, e.size());
            for(int j=0; j<i; ++j) {
                assertEquals(events[j], e.get(j));
            }
        }
        for(int i = 0; i < events.length; ++i) {
            Id id = db.remove(events[i].getId(), EventStorer.getInstance()).get();
            assertEquals(events[i].getId(), id);
            Boolean contains = db.contains(events[i].getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    @Test
    public void filterWhereLessEqFiltersLikeRDB() throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        Boolean stored = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        for(int i=0; i<events.length; ++i) {
            List<Event> e = db.filterWhereLessEq(EventStorer.KEY_EPOCH_START_TIME, events[i].getStartTime().toEpochSecond(), EventStorer.getInstance()).get();
            assertEquals(i+1, e.size());
            for(int j=0; j<i+1; ++j) {
                assertEquals(events[j], e.get(j));
            }
        }
        for(int i = 0; i < events.length; ++i) {
            Id id = db.remove(events[i].getId(), EventStorer.getInstance()).get();
            assertEquals(events[i].getId(), id);
            Boolean contains = db.contains(events[i].getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    @Test
    public void filterWhereGreaterEqLessFiltersLikeRDB() throws ExecutionException, InterruptedException {
        ZonedDateTime s = ZonedDateTime.now();
        Event event = new Event(Id.generateRandom(), "e", "loc", s, s.plusHours(6));
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        Id stored = db.store(event).get();
        assertTrue(stored.equals(event.getId()));
        List<Event> e = db.filterWhereGreaterEqLess(EventStorer.KEY_EPOCH_START_TIME, event.getStartTime().minusHours(1).toEpochSecond(), event.getStartTime().plusHours(1).toEpochSecond(), EventStorer.getInstance()).get();
        assertEquals(1, e.size());
        Id id = db.remove(event.getId(), EventStorer.getInstance()).get();
        assertEquals(event.getId(), id);
        Boolean contains = db.contains(event.getId(), EventStorer.getInstance()).get();
        assertFalse(contains);
    }

    @Test
    public void filterWhereGreaterLessEqFiltersLikeRDB() throws ExecutionException, InterruptedException {
        ZonedDateTime s = ZonedDateTime.now();
        Event event = new Event(Id.generateRandom(), "e", "loc", s, s.plusHours(6));
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        Id stored = db.store(event).get();
        assertTrue(stored.equals(event.getId()));
        List<Event> e = db.filterWhereGreaterLessEq(EventStorer.KEY_EPOCH_END_TIME, event.getEndTime().minusHours(1).toEpochSecond(), event.getEndTime().toEpochSecond(), EventStorer.getInstance()).get();
        assertEquals(1, e.size());
        Id id = db.remove(event.getId(), EventStorer.getInstance()).get();
        assertEquals(event.getId(), id);
        Boolean contains = db.contains(event.getId(), EventStorer.getInstance()).get();
        assertFalse(contains);
    }
}
