package com.github.onedirection.testdatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.Event;
import com.github.onedirection.database.ConcreteDatabase;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.geocoding.NamedCoordinates;
import com.github.onedirection.utils.Id;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ConcreteDatabaseTest {

    private int count = 100;

    private Event[] makeEvents(int count) {
        Event[] l = new Event[count];
        for (int i = 0; i < count; ++i) {
            l[i] = new Event(Id.createId(), "MyCity"+i, new NamedCoordinates(i, i, "city"+i), ZonedDateTime.now(), ZonedDateTime.now());
        }
        return l;
    }

    @Test
    public void canStoreAndRetrieveAndRemoveEvents() throws ExecutionException, InterruptedException {
        Event[] events = makeEvents(count);
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        ArrayList<Id> ids = new ArrayList<Id>();
        for (int i = 0; i < events.length; ++i) {
            ids.add(db.store(events[i]).get());
        }
        for (int i = 0; i < events.length; ++i) {
            Event e = db.retrieve(ids.get(i), EventStorer.getInstance()).get();
            assertEquals(e, events[i]);
        }
        for(int i = 0; i < events.length; ++i) {
            Id id = db.remove(ids.get(i), EventStorer.getInstance()).get();
            assertEquals(id, ids.get(i));
            Boolean contains = db.contains(ids.get(i), EventStorer.getInstance()).get();
            assertEquals(contains, false);
        }
    }

    @Test
    public void containsOnEventObject() throws ExecutionException, InterruptedException {
        Event[] events = makeEvents(count);
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        for (int i = 0; i < events.length; ++i) {
            Id id = db.store(events[i]).get();
            assertEquals(id, events[i].getId());
        }
        for(int i = 0; i < events.length; ++i) {
            Boolean contains = db.contains(events[i]).get();
            assertEquals(contains, true);
        }
        for(int i = 0; i < events.length; ++i) {
            Id id = db.remove(events[i].getId(), EventStorer.getInstance()).get();
            assertEquals(id, events[i].getId());
            Boolean contains = db.contains(events[i]).get();
            assertEquals(contains, false);
        }
    }

    @Test
    public void containsOnEventId() throws ExecutionException, InterruptedException {
        Event[] events = makeEvents(count);
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        for (int i = 0; i < events.length; ++i) {
            Id id = db.store(events[i]).get();
            assertEquals(id, events[i].getId());
        }
        for(int i = 0; i < events.length; ++i) {
            Boolean contains = db.contains(events[i].getId(), EventStorer.getInstance()).get();
            assertEquals(contains, true);
        }
        for(int i = 0; i < events.length; ++i) {
            Id id = db.remove(events[i].getId(), EventStorer.getInstance()).get();
            assertEquals(id, events[i].getId());
            Boolean contains = db.contains(events[i].getId(), EventStorer.getInstance()).get();
            assertEquals(contains, false);
        }
    }

    @Test
    public void storeAllWorksJustLikeManyStores() {

    }

    @Test
    public void retrieveAllWorksJustLikeManyRetrieves() {

    }

    @Test
    public void retrieveOnFilterKeyQueryActsLikeRDB() {

    }
}
