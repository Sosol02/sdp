package com.github.onedirection.database;

import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.event.Event;
import com.github.onedirection.utils.Id;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.hamcrest.Matchers.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class ObservableDatabaseTest {

    private static Event TEST_EVENT_1 = new Event(Id.generateRandom(), "Test event 1", "nowhere", Optional.empty(),
            ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(5), Optional.empty());

    private static Event TEST_EVENT_2 = new Event(Id.generateRandom(), "Test event 2", "nowhreerfwfeewe", Optional.empty(),
            ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(10), Optional.empty());

    @Before
    public void deleteAllEvents() throws ExecutionException, InterruptedException {
        ObservableDatabase db = DefaultDatabase.getDefaultInstance();
        db.removeAllObservers();
        List<Event> events = db.retrieveAll(EventStorer.getInstance()).get();
        for(Event e : events) {
            db.remove(e.getId(), EventStorer.getInstance()).get();
        }
    }

    @Test
    public void testStoreObservesWell() {
        ObservableDatabase odb = DefaultDatabase.getDefaultInstance();
        odb.addObserver((observable, obj) -> {
            assertThat(obj.element, is(TEST_EVENT_1));
            assertThat(obj.ids.get(0), is(TEST_EVENT_1.getId()));
            assertThat(obj.kind, is(ObservableDatabase.ActionKind.Store));
        });
        odb.store(TEST_EVENT_1).join();
        odb.removeAllObservers();

        odb.addObserver((observable, obj) -> {
            assertThat(obj.element, is(TEST_EVENT_1));
            assertThat(obj.ids.get(0), is(TEST_EVENT_1.getId()));
            assertThat(obj.kind, is(ObservableDatabase.ActionKind.Retrieve));
        });
        odb.contains(TEST_EVENT_1).join();
        odb.removeAllObservers();

        odb.addObserver((observable, obj) -> {
            assertThat(obj.element, is(nullValue()));
            assertThat(obj.ids.get(0), is(TEST_EVENT_1.getId()));
            assertThat(obj.kind, is(ObservableDatabase.ActionKind.Retrieve));
        });
        odb.contains(TEST_EVENT_1.getId(), TEST_EVENT_1.storer()).join();
        odb.removeAllObservers();

        odb.addObserver((observable, obj) -> {
            assertThat(obj.element, nullValue());
            assertThat(obj.ids.get(0), is(TEST_EVENT_1.getId()));
            assertThat(obj.kind, is(ObservableDatabase.ActionKind.Remove));
        });
        odb.remove(TEST_EVENT_1.getId(), TEST_EVENT_1.storer()).join();
    }

    @Test
    public void testRetrieveObservesWell() {
        ObservableDatabase odb = DefaultDatabase.getDefaultInstance();
        odb.store(TEST_EVENT_1);
        odb.addObserver((observable, obj) -> {
            assertThat(obj.element, is(TEST_EVENT_1));
            assertThat(obj.ids.get(0), is(TEST_EVENT_1.getId()));
            assertThat(obj.kind, is(ObservableDatabase.ActionKind.Retrieve));
        });
        odb.retrieve(TEST_EVENT_1.getId(), TEST_EVENT_1.storer()).join();
    }


    @Test
    public void testStoreRetrieveAllObservesWell() {
        ObservableDatabase odb = DefaultDatabase.getDefaultInstance();
        odb.addObserver((observable, obj) -> {
            assertThat((Boolean) obj.element, is(true));

            assertThat(obj.ids.get(0), is(TEST_EVENT_1.getId()));
            assertThat(obj.ids.get(1), is(TEST_EVENT_2.getId()));

            assertThat(obj.kind, is(ObservableDatabase.ActionKind.Store));
        });
        List<Event> els = new ArrayList<>();
        els.add(TEST_EVENT_1);
        els.add(TEST_EVENT_2);
        odb.storeAll(els).join();

        odb.removeAllObservers();
        odb.addObserver((observable, obj) -> {
            List<Event> ls = (List<Event>) obj.element;
            assertThat(ls.get(0), is(TEST_EVENT_1));
            assertThat(ls.get(1), is(TEST_EVENT_2));

            assertThat(obj.ids.get(0), is(TEST_EVENT_1.getId()));
            assertThat(obj.ids.get(1), is(TEST_EVENT_2.getId()));

            assertThat(obj.kind, is(ObservableDatabase.ActionKind.Store));
        });
        odb.retrieveAll(EventStorer.getInstance());
    }
}
