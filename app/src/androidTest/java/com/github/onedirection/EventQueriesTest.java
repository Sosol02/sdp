package com.github.onedirection;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.database.ConcreteDatabase;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.events.Recurrence;
import com.github.onedirection.utils.TimeUtils;
import com.github.onedirection.events.Event;
import com.github.onedirection.geolocation.NamedCoordinates;
import com.github.onedirection.utils.Id;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class EventQueriesTest {

    private static int emptyCoordProportion = 2; //Proportion of empty coordinates events will be of 1/emptyCoordProportion

    public static Event[] makeEvents(int count) {
        Event[] l = new Event[count];
        Random r = new Random();
        for (int i = 0; i < count; ++i) {
            if(r.nextInt(emptyCoordProportion) % emptyCoordProportion == 0) {
                l[i] = new Event(Id.generateRandom(), "MyCity" + i, "city"+i, ZonedDateTime.now().plusHours(i), ZonedDateTime.now().plusHours(i));
            } else {
                l[i] = new Event(Id.generateRandom(), "MyCity" + i, new NamedCoordinates(i, i, "city" + i), ZonedDateTime.now().plusHours(i), ZonedDateTime.now().plusHours(i));
            }
        }
        return l;
    }

    public static ZonedDateTime getConventionStartTime() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String dateInString = "15-10-2015 10:20:56";
        //String dateInString = "15-10-2015 13:14:01";
        Date date = sdf.parse(dateInString);
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).truncatedTo(Event.TIME_PRECISION);
    }

    @Before
    public void deleteAllEvents() throws ExecutionException, InterruptedException, ParseException {
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        List<Event> events = db.retrieveAll(EventStorer.getInstance()).get();
        for(Event e : events) {
            Id id = db.remove(e.getId(), EventStorer.getInstance()).get();
            assertEquals(e.getId(), id);
        }
    }

    @Test
    public void returnNoEventsIfNoneInTimeFrame() throws ExecutionException, InterruptedException, ParseException {
        List<Event> events = new ArrayList<Event>();
        ZonedDateTime start  = getConventionStartTime();
        ZonedDateTime end = start.plusMinutes(10);
        for(int i = 0; i < 5; ++i) {
            events.add(new Event(Id.generateRandom(), "MyEvent"+2*i, "loc"+2*i, start.plusMinutes(20+2*i), end.plusMinutes(20+2*i+1)));
            events.add(new Event(Id.generateRandom(), "MyEvent"+2*i+1, "loc"+2*i+1, start.minusMinutes(20+2*i), start.minusMinutes(20+2*i).plusMinutes(1)));
        }
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        boolean b = db.storeAll(events).get();
        if(b) {
            EventQueries eq = new EventQueries(db);
            List<Event> eventsBetweenStartAndEnd = eq.getEventsInTimeframe(start, end).get();
            assertEquals(0, eventsBetweenStartAndEnd.size());
        }
        for(Event e : events) {
            db.remove(e.getId(), EventStorer.getInstance()).get();
        }
    }

    @Test
    public void returnNoEventsWhenTimeFrameIsInvalid() throws ExecutionException, InterruptedException, ParseException  {
        List<Event> events = new ArrayList<Event>();
        ZonedDateTime start = getConventionStartTime();
        ZonedDateTime end = start.plusMinutes(10);
        for(int i=0; i<5; ++i) {
            events.add(new Event(Id.generateRandom(), "MyEvent"+i, "loc"+i, start.plusMinutes(5).minusMinutes(i+1), start.plusMinutes(5).plusMinutes(i)));
        }
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        boolean b = db.storeAll(events).get();
        if(b) {
            EventQueries eq = new EventQueries(db);
            List<Event> e = eq.getEventsInTimeframe(start, start).get();
            assertEquals(0, e.size());
        }
        for(Event e : events) {
            db.remove(e.getId(), EventStorer.getInstance()).get();
        }
    }

    @Test
    public void nonDisjointTimeIntervalsMeansToBeReturned() throws ExecutionException, InterruptedException, ParseException {
        List<Event> eventsAccepted = new ArrayList<Event>();
        List<Event> eventsRejected = new ArrayList<Event>();
        ZonedDateTime start = getConventionStartTime();
        ZonedDateTime end = start.plusMinutes(10);
        for(int i = 0; i < 5; ++i) {
            eventsAccepted.add(new Event(Id.generateRandom(), "MyEvent"+2*i, "loc"+2*i, start.plusMinutes(5+i), start.plusMinutes(20+i)));
            eventsAccepted.add(new Event(Id.generateRandom(), "MyEvent"+2*i+1, "loc"+2*i+1, start.minusMinutes(20), start.plusMinutes(5).minusMinutes(i)));
            eventsAccepted.add(new Event(Id.generateRandom(), "MyEvent"+(-2)*i, "loc"+2*i, start.minusMinutes(i+1), end.plusMinutes(i+1)));
        }
        eventsAccepted.add(new Event(Id.generateRandom(), "MyEvent", "loc", start, end));
        eventsRejected.add(new Event(Id.generateRandom(), "nopeEvent", "nopeLoc", end, end.plusMinutes(1)));
        eventsRejected.add(new Event(Id.generateRandom(), "nopeEvent", "nopeLoc", start.minusMinutes(1), start));
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        boolean b = true;
        b = b && db.storeAll(eventsAccepted).get();
        b = b && db.storeAll(eventsRejected).get();
        if(b) {
            EventQueries eq = new EventQueries(db);
            List<Event> eventsBetweenStartAndEnd = eq.getEventsInTimeframe(start, end).get();
            assertEquals(eventsAccepted.size(), eventsBetweenStartAndEnd.size());
            for (Event e : eventsBetweenStartAndEnd) {
                assertTrue(eventsAccepted.contains(e));
                assertTrue(!eventsRejected.contains(e));
            }
        }
        for(Event e : eventsAccepted) {
            db.remove(e.getId(), EventStorer.getInstance()).get();
        }
        for(Event e : eventsRejected) {
            db.remove(e.getId(), EventStorer.getInstance()).get();
        }
    }

    @Test
    public void smallerEventTimeIntervalsThanGivenIntervalReturnedOnlyOnce() throws ExecutionException, InterruptedException, ParseException {
        List<Event> events = new ArrayList<Event>();
        ZonedDateTime start = getConventionStartTime();
        ZonedDateTime end = start.plusMinutes(10);
        for(int i=0; i<5; ++i) {
            events.add(new Event(Id.generateRandom(), "MyEvent"+i, "loc"+i, start.plusMinutes(5).minusMinutes(i+1), start.plusMinutes(5).plusMinutes(i)));
        }
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        boolean b = db.storeAll(events).get();
        if(b) {
            EventQueries eq = new EventQueries(db);
            List<Event> eventsBetweenStartAndEnd = eq.getEventsInTimeframe(start, end).get();
            assertEquals(events.size(), eventsBetweenStartAndEnd.size());
        }
        for(Event e : events) {
            db.remove(e.getId(), EventStorer.getInstance()).get();
        }
    }

    @Test
    public void allEventsOnGivenDayReturned() throws ExecutionException, InterruptedException, ParseException {
        List<Event> eventsAccepted = new ArrayList<Event>();
        List<Event> eventsRejected = new ArrayList<Event>();
        ZonedDateTime today = TimeUtils.truncateTimeToDays(getConventionStartTime());

        for(int i = 0; i < 5; ++i) {
            eventsAccepted.add(new Event(Id.generateRandom(), "validEvent"+i, "validLoc"+i, today.plusHours(2*i), today.plusHours(2*i+1)));
            eventsRejected.add(new Event(Id.generateRandom(), "nopeEvent"+i, "nopeLoc"+i, today.minusDays(i+2), today.minusDays(i+1)));
            eventsRejected.add(new Event(Id.generateRandom(), "nopeEvent"+i, "nopeLoc"+i, today.plusDays(i+1), today.plusDays(i+2)));
        }
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        boolean b = true;
        b = b && db.storeAll(eventsAccepted).get();
        b = b && db.storeAll(eventsRejected).get();
        if(b) {
            List<Event> eventsOnDay = EventQueries.getEventsByDay(db, today).get();
            assertEquals(eventsAccepted.size(), eventsOnDay.size());
            for (Event e : eventsOnDay) {
                assertTrue(eventsAccepted.contains(e));
                assertTrue(!eventsRejected.contains(e));
            }
        }
        for(Event e : eventsAccepted) {
            db.remove(e.getId(), EventStorer.getInstance()).get();
        }
        for(Event e : eventsRejected) {
            db.remove(e.getId(), EventStorer.getInstance()).get();
        }
    }

    @Test
    public void allEventsOnGivenWeekReturned() throws ExecutionException, InterruptedException, ParseException {
        List<Event> eventsAccepted = new ArrayList<Event>();
        List<Event> eventsRejected = new ArrayList<Event>();
        ZonedDateTime thisWeek = TimeUtils.truncateTimeToWeeks(getConventionStartTime());

        for(int i = 0; i < 5; ++i) {
            eventsAccepted.add(new Event(Id.generateRandom(), "validEvent"+i, "validLoc"+i, thisWeek.plusHours(2*i), thisWeek.plusHours(2*i+1)));
            eventsAccepted.add(new Event(Id.generateRandom(), "validEvent"+i, "validLoc"+i, thisWeek.plusDays(i), thisWeek.plusDays(i+1)));
            eventsRejected.add(new Event(Id.generateRandom(), "nopeEvent"+i, "nopeLoc"+i, thisWeek.minusDays(i+2), thisWeek.minusDays(i+1)));
            eventsRejected.add(new Event(Id.generateRandom(), "nopeEvent"+i, "nopeLoc"+i, thisWeek.minusWeeks(i+2), thisWeek.minusWeeks(i+1)));
            eventsRejected.add(new Event(Id.generateRandom(), "nopeEvent"+i, "nopeLoc"+i, thisWeek.plusWeeks(i+1), thisWeek.plusWeeks(i+2)));
        }
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        boolean b = true;
        b = b && db.storeAll(eventsAccepted).get();
        b = b && db.storeAll(eventsRejected).get();
        if(b) {
            List<Event> eventsOnWeek = EventQueries.getEventsByWeek(db, thisWeek).get();
            assertEquals(eventsAccepted.size(), eventsOnWeek.size());
            for (Event e : eventsOnWeek) {
                assertTrue(eventsAccepted.contains(e));
                assertTrue(!eventsRejected.contains(e));
            }
        }
        for(Event e : eventsAccepted) {
            db.remove(e.getId(), EventStorer.getInstance()).get();
        }
        for(Event e : eventsRejected) {
            db.remove(e.getId(), EventStorer.getInstance()).get();
        }
    }

    @Test
    public void allEventsOnGivenMonthReturned() throws ExecutionException, InterruptedException, ParseException {
        List<Event> eventsAccepted = new ArrayList<Event>();
        List<Event> eventsRejected = new ArrayList<Event>();
        ZonedDateTime thisMonth = TimeUtils.truncateTimeToMonths(getConventionStartTime());

        for(int i = 0; i < 5; ++i) {
            eventsAccepted.add(new Event(Id.generateRandom(), "validEvent"+i, "validLoc"+i, thisMonth.plusDays(2*i+3), thisMonth.plusDays(2*i+4)));
            eventsAccepted.add(new Event(Id.generateRandom(), "validEvent"+i, "validLoc"+i, thisMonth.minusWeeks(i), thisMonth.plusWeeks(i+2)));
            eventsRejected.add(new Event(Id.generateRandom(), "nopeEvent"+i, "nopeLoc"+i, thisMonth.minusDays(i+2), thisMonth.minusDays(i+1)));
            eventsRejected.add(new Event(Id.generateRandom(), "nopeEvent"+i, "nopeLoc"+i, thisMonth.minusWeeks(i+2), thisMonth.minusWeeks(i+1)));
            eventsRejected.add(new Event(Id.generateRandom(), "nopeEvent"+i, "nopeLoc"+i, thisMonth.plusMonths(i+1), thisMonth.plusMonths(i+2)));
        }
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        boolean b = true;
        b = b && db.storeAll(eventsAccepted).get();
        b = b && db.storeAll(eventsRejected).get();
        if(b) {
            List<Event> eventsOnMonth = EventQueries.getEventsByMonth(db, thisMonth).get();
            assertEquals(eventsAccepted.size(), eventsOnMonth.size());
            for (Event e : eventsOnMonth) {
                assertTrue(eventsAccepted.contains(e));
                assertTrue(!eventsRejected.contains(e));
            }
        }
        for(Event e : eventsAccepted) {
            db.remove(e.getId(), EventStorer.getInstance()).get();
        }
        for(Event e : eventsRejected) {
            db.remove(e.getId(), EventStorer.getInstance()).get();
        }
    }

    @Test
    public void storeRecurringEventShouldStoreCorrectNumberOfEvents() throws ExecutionException, InterruptedException {
        EventQueries queries = new EventQueries(ConcreteDatabase.getDatabase());
        ZonedDateTime start = ZonedDateTime.now().truncatedTo(Event.TIME_PRECISION);
        ZonedDateTime end = start.plusHours(1);
        Duration period = Duration.ofDays(1);
        Recurrence recurrence = new Recurrence(Id.generateRandom(), period, Optional.empty(), Optional.empty());
        Event recurrEvent = new Event(Id.generateRandom(), "recurrEvent", "noLocation", Optional.empty(), start, end, Optional.of(recurrence));
        int storeRecurrEvent = queries.storeRecurringEvent(recurrEvent, start.minusDays(1), start.plusDays(1).plusMinutes(2)).get();
        assertEquals(3, storeRecurrEvent);
        if(storeRecurrEvent != 0) {
            assertTrue(queries.removeRecurrEvents(recurrence.getGroupId()).get());
        }
        storeRecurrEvent = queries.storeRecurringEvent(recurrEvent, start.minusDays(1).minusMinutes(5), start.plusDays(2).minusMinutes(5)).get();
        assertEquals(3, storeRecurrEvent);
        if(storeRecurrEvent != 0) {
            assertTrue(queries.removeRecurrEvents(recurrence.getGroupId()).get());
        }
    }

    @Test
    public void eventsInRecurringSeriesHaveCorrectEventPointers() throws ExecutionException, InterruptedException {
        EventQueries queries = new EventQueries(ConcreteDatabase.getDatabase());
        ZonedDateTime start = ZonedDateTime.now().truncatedTo(Event.TIME_PRECISION);
        ZonedDateTime end = start.plusHours(1);
        Duration period = Duration.ofDays(1);
        Recurrence recurrence = new Recurrence(Id.generateRandom(), period, Optional.empty(), Optional.empty());
        Event recurrEvent = new Event(Id.generateRandom(), "recurrEvent", "noLocation", Optional.empty(), start, end, Optional.of(recurrence));
        int storeRecurrEvent = queries.storeRecurringEvent(recurrEvent, start.minusDays(3), start.plusDays(3)).get();
        assertEquals(7, storeRecurrEvent);
        List<Event> recurrEvents = queries.getRecurrEventSeriesOf(recurrence.getGroupId()).get();
        recurrEvents.sort((e1, e2) -> {
            return Long.compare(e1.getStartTime().toEpochSecond(), e2.getStartTime().toEpochSecond());
        });

        for(int i=1; i<recurrEvents.size()-1; ++i) {
            assertEquals(recurrEvents.get(i-1).getId(), recurrEvents.get(i).getRecurrence().get().getPrevEvent().orElse(null));
            assertEquals(recurrEvents.get(i+1).getId(), recurrEvents.get(i).getRecurrence().get().getNextEvent().orElse(null));
        }
        assertEquals(null, recurrEvents.get(0).getRecurrence().get().getPrevEvent().orElse(null));
        assertEquals(recurrEvents.get(1).getId(), recurrEvents.get(0).getRecurrence().get().getNextEvent().orElse(null));
        assertEquals(null, recurrEvents.get(recurrEvents.size()-1).getRecurrence().get().getNextEvent().orElse(null));
        assertEquals(recurrEvents.get(recurrEvents.size()-2).getId(), recurrEvents.get(recurrEvents.size()-1).getRecurrence().get().getPrevEvent().orElse(null));

        if(storeRecurrEvent != 0) {
            assertTrue(queries.removeRecurrEvents(recurrence.getGroupId()).get());
        }
    }

    @Test
    public void singleRecurringEventShouldBeStoredAsNonRecurring() throws ExecutionException, InterruptedException {
        EventQueries queries = new EventQueries(ConcreteDatabase.getDatabase());
        ZonedDateTime start = ZonedDateTime.now().truncatedTo(Event.TIME_PRECISION);
        ZonedDateTime end = start.plusHours(1);
        Duration period = Duration.ofDays(1);
        Recurrence recurrence = new Recurrence(Id.generateRandom(), period, Optional.empty(), Optional.empty());
        Event single = new Event(Id.generateRandom(), "single", "noLocation", Optional.empty(), start, end, Optional.of(recurrence));
        int storeSingleEvent = queries.storeRecurringEvent(single, start, start.plusMinutes(1)).get();
        assertEquals(1, storeSingleEvent);
        if(storeSingleEvent != 0) {
            Event e = ConcreteDatabase.getDatabase().retrieve(single.getId(), single.storer()).get();
            assertEquals(Optional.empty(), e.getRecurrence());
            queries.removeEvent(e.getId()).get();
        }
    }

    @Test
    public void modifyEventStoresCorrectModifiedVersion() throws ExecutionException, InterruptedException {
        ZonedDateTime time = ZonedDateTime.now();
        Event event = new Event(Id.generateRandom(), "any", "loc", Optional.empty(), time, time.plusHours(1), Optional.empty());
        assertEquals(event.getId(), ConcreteDatabase.getDatabase().store(event).get());
        Event modifiedEvent = event.setName("modified");
        assertEquals(modifiedEvent.getId(), EventQueries.modifyEvent(ConcreteDatabase.getDatabase(), modifiedEvent).get());
        Event e = ConcreteDatabase.getDatabase().retrieve(event.getId(), event.storer()).get();
        assertEquals(modifiedEvent, e);
        assertNotEquals(event, e);
        EventQueries.removeEvent(ConcreteDatabase.getDatabase(), e.getId()).get();
    }

    @Test
    public void removeNonRecurringEventRemovesItFromDatabase() throws ExecutionException, InterruptedException {
        ZonedDateTime time = ZonedDateTime.now();
        Event event = new Event(Id.generateRandom(), "any", "loc", Optional.empty(), time, time.plusHours(1), Optional.empty());
        assertEquals(event.getId(), ConcreteDatabase.getDatabase().store(event).get());
        assertTrue(ConcreteDatabase.getDatabase().contains(event.getId(), event.storer()).get());
        assertEquals(event.getId(), EventQueries.removeEvent(ConcreteDatabase.getDatabase(), event.getId()).get());
        assertFalse(ConcreteDatabase.getDatabase().contains(event.getId(), event.storer()).get());
    }

    @Test
    public void removeRecurringEventUpdatesEventPointersInRecurrenceSeries() throws ExecutionException, InterruptedException {
        EventQueries queries = new EventQueries(ConcreteDatabase.getDatabase());
        ZonedDateTime start = ZonedDateTime.now().truncatedTo(Event.TIME_PRECISION);
        ZonedDateTime end = start.plusHours(1);
        Duration period = Duration.ofDays(1);
        Recurrence recurrence = new Recurrence(Id.generateRandom(), period, Optional.empty(), Optional.empty());
        Event recurrEvent = new Event(Id.generateRandom(), "recurrEvent", "noLocation", Optional.empty(), start, end, Optional.of(recurrence));
        int storeRecurrEvent = queries.storeRecurringEvent(recurrEvent, start.minusDays(1), start.plusDays(1).plusMinutes(2)).get();
        assertEquals(3, storeRecurrEvent);
        List<Event> recurrEvents = queries.getRecurrEventSeriesOf(recurrence.getGroupId()).get();
        recurrEvents.sort((e1, e2) -> {
            return Long.compare(e1.getStartTime().toEpochSecond(), e2.getStartTime().toEpochSecond());
        });
        Id idRemoved = queries.removeEvent(recurrEvents.get(1).getId()).get();
        assertEquals(recurrEvent.getId(), idRemoved);
        recurrEvents = queries.getRecurrEventSeriesOf(recurrence.getGroupId()).get();
        recurrEvents.sort((e1, e2) -> {
            return Long.compare(e1.getStartTime().toEpochSecond(), e2.getStartTime().toEpochSecond());
        });
        assertFalse(recurrEvents.get(0).getRecurrence().get().getPrevEvent().isPresent());
        assertFalse(recurrEvents.get(1).getRecurrence().get().getNextEvent().isPresent());
        assertEquals(recurrEvents.get(1).getId(), recurrEvents.get(0).getRecurrence().get().getNextEvent().get());
        assertEquals(recurrEvents.get(0).getId(), recurrEvents.get(1).getRecurrence().get().getPrevEvent().get());
        if(storeRecurrEvent != 0) {
            assertTrue(queries.removeRecurrEvents(recurrence.getGroupId()).get());
        }
    }

    @Test
    public void removeEventInRecurrenceSeriesOfSize2RemovesTheSeries() throws ExecutionException, InterruptedException {
        EventQueries queries = new EventQueries(ConcreteDatabase.getDatabase());
        ZonedDateTime start = ZonedDateTime.now().truncatedTo(Event.TIME_PRECISION);
        ZonedDateTime end = start.plusHours(1);
        Duration period = Duration.ofDays(1);
        Recurrence recurrence = new Recurrence(Id.generateRandom(), period, Optional.empty(), Optional.empty());
        Event recurrEvent = new Event(Id.generateRandom(), "recurrEvent", "noLocation", Optional.empty(), start, end, Optional.of(recurrence));
        int storeRecurrEvent = queries.storeRecurringEvent(recurrEvent, start, start.plusDays(1).plusMinutes(2)).get();
        assertEquals(2, storeRecurrEvent);
        List<Event> recurrEvents = queries.getRecurrEventSeriesOf(recurrence.getGroupId()).get();
        recurrEvents.sort((e1, e2) -> {
            return Long.compare(e1.getStartTime().toEpochSecond(), e2.getStartTime().toEpochSecond());
        });
        Id id = queries.removeEvent(recurrEvents.get(1).getId()).get();
        assertEquals(recurrEvents.get(1).getId(), id);
        assertEquals(0, queries.getRecurrEventSeriesOf(recurrence.getGroupId()).get().size());
        assertEquals(Optional.empty(), ConcreteDatabase.getDatabase().retrieve(recurrEvent.getId(), recurrEvent.storer()).get().getRecurrence());

        assertEquals(recurrEvent.getId(), queries.removeEvent(recurrEvent.getId()).get());
    }

    @Test
    public void retrieveRecurrenceSeriesGivesBackCorrectNumberOfEvents() throws ExecutionException, InterruptedException {
        EventQueries queries = new EventQueries(ConcreteDatabase.getDatabase());
        ZonedDateTime start = ZonedDateTime.now().truncatedTo(Event.TIME_PRECISION);
        ZonedDateTime end = start.plusHours(1);
        Duration period = Duration.ofDays(1);
        Recurrence recurrence = new Recurrence(Id.generateRandom(), period, Optional.empty(), Optional.empty());
        Event recurrEvent = new Event(Id.generateRandom(), "recurrEvent", "noLocation", Optional.empty(), start, end, Optional.of(recurrence));
        int storeRecurrEvent = queries.storeRecurringEvent(recurrEvent, start.minusDays(3), start.plusDays(3)).get();
        assertEquals(7, storeRecurrEvent);
        List<Event> events = queries.getRecurrEventSeriesOf(recurrence.getGroupId()).get();
        assertEquals(7, events.size());
        if(storeRecurrEvent != 0) {
            assertTrue(queries.removeRecurrEvents(recurrence.getGroupId()).get());
        }
    }

    @Test
    public void removeRecurrEventsRemovesRecurrenceSeries() throws ExecutionException, InterruptedException {
        EventQueries queries = new EventQueries(ConcreteDatabase.getDatabase());
        ZonedDateTime start = ZonedDateTime.now().truncatedTo(Event.TIME_PRECISION);
        ZonedDateTime end = start.plusHours(1);
        Duration period = Duration.ofDays(1);
        Recurrence recurrence = new Recurrence(Id.generateRandom(), period, Optional.empty(), Optional.empty());
        Event recurrEvent = new Event(Id.generateRandom(), "recurrEvent", "noLocation", Optional.empty(), start, end, Optional.of(recurrence));
        int storeRecurrEvent = queries.storeRecurringEvent(recurrEvent, start.minusDays(3), start.plusDays(3)).get();
        assertEquals(7, storeRecurrEvent);
        if(storeRecurrEvent != 0) {
            assertTrue(queries.removeRecurrEvents(recurrence.getGroupId()).get());
        }
        assertEquals(0, queries.getRecurrEventSeriesOf(recurrence.getGroupId()).get().size());
    }

    @Test
    public void removeRecurrEventsRemovesRecurrenceSeriesFromEvent() throws ExecutionException, InterruptedException {
        EventQueries queries = new EventQueries(ConcreteDatabase.getDatabase());
        ZonedDateTime start = ZonedDateTime.now().truncatedTo(Event.TIME_PRECISION);
        ZonedDateTime end = start.plusHours(1);
        Duration period = Duration.ofDays(1);
        Recurrence recurrence = new Recurrence(Id.generateRandom(), period, Optional.empty(), Optional.empty());
        Event recurrEvent = new Event(Id.generateRandom(), "recurrEvent", "noLocation", Optional.empty(), start, end, Optional.of(recurrence));
        int storeRecurrEvent = queries.storeRecurringEvent(recurrEvent, start.minusDays(3), start.plusDays(3)).get();
        assertEquals(7, storeRecurrEvent);
        List<Event> events = queries.getRecurrEventSeriesOf(recurrence.getGroupId()).get();
        assertEquals(7, events.size());

        assertTrue(queries.removeRecurrEvents(events.get(new Random().nextInt(7))).get());

        assertEquals(0, queries.getRecurrEventSeriesOf(recurrence.getGroupId()).get().size());
    }

    @Test
    public void convertNonRecurringEventToRecurringWorksAsExpected() throws ExecutionException, InterruptedException {
        EventQueries queries = new EventQueries(ConcreteDatabase.getDatabase());
        ZonedDateTime start = ZonedDateTime.now().truncatedTo(Event.TIME_PRECISION);
        ZonedDateTime end = start.plusHours(1);
        Duration period = Duration.ofDays(1);
        Event nonRecurrEvent = new Event(Id.generateRandom(), "recurrEvent", "noLocation", Optional.empty(), start, end, Optional.empty());
        Id groupId = Id.generateRandom();
        int storeRecurrEvent = queries.convertToRecurring(nonRecurrEvent, groupId, period, start.minusDays(3), start.plusDays(3)).get();
        assertEquals(7, storeRecurrEvent);
        List<Event> events = queries.getRecurrEventSeriesOf(groupId).get();
        assertEquals(7, events.size());
        if(storeRecurrEvent != 0) {
            assertTrue(queries.removeRecurrEvents(groupId).get());
        }
        List<Event> noEvents = queries.getRecurrEventSeriesOf(groupId).get();
        assertEquals(0, noEvents.size());
    }
}
