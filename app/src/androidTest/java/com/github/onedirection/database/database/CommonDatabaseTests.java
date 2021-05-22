package com.github.onedirection.database.database;

import com.github.onedirection.database.queries.EventQueriesTest;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.event.model.Event;
import com.github.onedirection.utils.Id;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommonDatabaseTests {

    private static final int count = 10;

    public static void canStoreAndRetrieveAndRemoveEvents(Database db) throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        ArrayList<Id> ids = new ArrayList<Id>();
        for (Event event : events) {
            ids.add(db.store(event).get());
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

    public static void containsOnEventObject(Database db) throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        for (Event event : events) {
            Id id = db.store(event).get();
            assertEquals(event.getId(), id);
        }
        for (Event event : events) {
            Boolean contains = db.contains(event).get();
            assertTrue(contains);
        }
        for (Event event : events) {
            Id id = db.remove(event.getId(), EventStorer.getInstance()).get();
            assertEquals(event.getId(), id);
            Boolean contains = db.contains(event).get();
            assertFalse(contains);
        }
    }

    public static void containsOnEventId(Database db) throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        for (Event event : events) {
            Id id = db.store(event).get();
            assertEquals(event.getId(), id);
        }
        for (Event event : events) {
            Boolean contains = db.contains(event.getId(), EventStorer.getInstance()).get();
            assertTrue(contains);
        }
        for (Event event : events) {
            Id id = db.remove(event.getId(), EventStorer.getInstance()).get();
            assertEquals(event.getId(), id);
            Boolean contains = db.contains(event.getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    public static void storeAllWorksJustLikeManyStores(Database db) throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        Boolean stored = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        for (Event event : events) {
            Boolean contains = db.contains(event.getId(), EventStorer.getInstance()).get();
            assertTrue(contains);
        }
        for (Event event : events) {
            Id id = db.remove(event.getId(), EventStorer.getInstance()).get();
            assertEquals(event.getId(), id);
            Boolean contains = db.contains(event.getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    public static void storeAllWorksOnEmptyList(Database db) throws ExecutionException, InterruptedException {
        Boolean stored = db.storeAll(new ArrayList<Event>()).get();
        assertTrue(stored);
    }

    public static void retrieveAllWorksJustLikeManyRetrieves(Database db) throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        Boolean stored = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        List<Event> res = db.retrieveAll(EventStorer.getInstance()).get();
        assertEquals(events.length, res.size());
        for (Event event : events) {
            boolean contains = false;
            for (Event e : res) {
                if (e.equals(event)) {
                    contains = true;
                }
            }
            assertTrue(contains);
        }
        for (Event event : events) {
            Id id = db.remove(event.getId(), EventStorer.getInstance()).get();
            assertEquals(event.getId(), id);
            Boolean contains = db.contains(event.getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    public static void retrieveOnFilterKeyQueryActsLikeRDB(Database db) throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        Boolean stored = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        for(int i=0; i<events.length; ++i) {
            List<Event> e = db.filterWhereEquals(EventStorer.KEY_NAME, "MyCity"+i, EventStorer.getInstance()).get();
            assertEquals(1, e.size());
            if(e.size() > 0) {
                assertEquals(events[i], e.get(0));
            }
        }
        for (Event event : events) {
            Id id = db.remove(event.getId(), EventStorer.getInstance()).get();
            assertEquals(event.getId(), id);
            Boolean contains = db.contains(event.getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    public static void sameIdOverridesEntry(Database db) throws ExecutionException, InterruptedException  {
        Event[] events = EventQueriesTest.makeEvents(count);
        Boolean stored = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        Boolean stored2 = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        List<Event> eventsInDb= db.retrieveAll(EventStorer.getInstance()).get();
        assertEquals(events.length, eventsInDb.size());
        for (Event event : events) {
            Id id = db.remove(event.getId(), EventStorer.getInstance()).get();
            assertEquals(event.getId(), id);
            Boolean contains = db.contains(event.getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    public static void filterWhereGreaterFiltersLikeRDB(Database db) throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        Boolean stored = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        for(int i=0; i<events.length; ++i) {
            List<Event> e = db.filterWhereGreater(EventStorer.KEY_EPOCH_START_TIME, events[i].getStartTime().toEpochSecond(), EventStorer.getInstance()).get();
            assertEquals(events.length-i-1, e.size());
            for(int j=i+1; j<events.length; ++j) {
                assertEquals(events[j], e.get(j-i-1));
            }
        }
        for (Event event : events) {
            Id id = db.remove(event.getId(), EventStorer.getInstance()).get();
            assertEquals(event.getId(), id);
            Boolean contains = db.contains(event.getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    public static void filterWhereGreaterEqFiltersLikeRDB(Database db) throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        Boolean stored = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        for(int i=0; i<events.length; ++i) {
            List<Event> e = db.filterWhereGreaterEq(EventStorer.KEY_EPOCH_START_TIME, events[i].getStartTime().toEpochSecond(), EventStorer.getInstance()).get();
            assertEquals(events.length-i, e.size());
            for(int j=i; j<events.length; ++j) {
                assertEquals(events[j], e.get(j-i));
            }
        }
        for (Event event : events) {
            Id id = db.remove(event.getId(), EventStorer.getInstance()).get();
            assertEquals(event.getId(), id);
            Boolean contains = db.contains(event.getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    public static void filterWhereLessFiltersLikeRDB(Database db) throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        Boolean stored = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        for(int i=0; i<events.length; ++i) {
            List<Event> e = db.filterWhereLess(EventStorer.KEY_EPOCH_START_TIME, events[i].getStartTime().toEpochSecond(), EventStorer.getInstance()).get();
            assertEquals(i, e.size());
            for(int j=0; j<i; ++j) {
                assertEquals(events[j], e.get(j));
            }
        }
        for (Event event : events) {
            Id id = db.remove(event.getId(), EventStorer.getInstance()).get();
            assertEquals(event.getId(), id);
            Boolean contains = db.contains(event.getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    public static void filterWhereLessEqFiltersLikeRDB(Database db) throws ExecutionException, InterruptedException {
        Event[] events = EventQueriesTest.makeEvents(count);
        Boolean stored = db.storeAll(Arrays.asList(events)).get();
        assertTrue(stored);
        for(int i=0; i<events.length; ++i) {
            List<Event> e = db.filterWhereLessEq(EventStorer.KEY_EPOCH_START_TIME, events[i].getStartTime().toEpochSecond(), EventStorer.getInstance()).get();
            assertEquals(i+1, e.size());
            for(int j=0; j<i+1; ++j) {
                assertEquals(events[j], e.get(j));
            }
        }
        for (Event event : events) {
            Id id = db.remove(event.getId(), EventStorer.getInstance()).get();
            assertEquals(event.getId(), id);
            Boolean contains = db.contains(event.getId(), EventStorer.getInstance()).get();
            assertFalse(contains);
        }
    }

    public static void filterWhereGreaterEqLessFiltersLikeRDB(Database db) throws ExecutionException, InterruptedException {
        ZonedDateTime s = ZonedDateTime.now();
        Event event = new Event(Id.generateRandom(), "e", "loc", s, s.plusHours(6));
        Id stored = db.store(event).get();
        assertEquals(stored, event.getId());
        List<Event> e = db.filterWhereGreaterEqLess(EventStorer.KEY_EPOCH_START_TIME, event.getStartTime().minusHours(1).toEpochSecond(), event.getStartTime().plusHours(1).toEpochSecond(), EventStorer.getInstance()).get();
        assertEquals(1, e.size());
        Id id = db.remove(event.getId(), EventStorer.getInstance()).get();
        assertEquals(event.getId(), id);
        Boolean contains = db.contains(event.getId(), EventStorer.getInstance()).get();
        assertFalse(contains);
    }

    public static void filterWhereGreaterLessEqFiltersLikeRDB(Database db) throws ExecutionException, InterruptedException {
        ZonedDateTime s = ZonedDateTime.now();
        Event event = new Event(Id.generateRandom(), "e", "loc", s, s.plusHours(6));
        Id stored = db.store(event).get();
        assertEquals(stored, event.getId());
        List<Event> e = db.filterWhereGreaterLessEq(EventStorer.KEY_EPOCH_END_TIME, event.getEndTime().minusHours(1).toEpochSecond(), event.getEndTime().toEpochSecond(), EventStorer.getInstance()).get();
        assertEquals(1, e.size());
        Id id = db.remove(event.getId(), EventStorer.getInstance()).get();
        assertEquals(event.getId(), id);
        Boolean contains = db.contains(event.getId(), EventStorer.getInstance()).get();
        assertFalse(contains);
    }
}
