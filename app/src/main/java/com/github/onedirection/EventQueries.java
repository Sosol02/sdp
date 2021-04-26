package com.github.onedirection;

import android.util.Log;

import com.github.onedirection.database.Database;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.events.Event;
import com.github.onedirection.events.Recurrence;
import com.github.onedirection.utils.Id;
import com.github.onedirection.utils.TimeUtils;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class used to query the events in a database based on different operations
 */
public class EventQueries {

    private Database db;

    /**
     * Constructor of EventQueries
     * @param db (Database) : the database to which the queries are applied
     */
    public EventQueries(Database db) {
        this.db = db;
    }


    /**
     * Method to query events that take place in a time frame that is non-disjoint with the given time frame [start, end[
     * @param start (ZonedDateTime) : the start time of the target time frame
     * @param end (ZonedDateTime) : [exclusive[ the end time of the target time frame
     * @return (CompletableFuture<List<Event>>) : A list of events queried from the database that have a time frame non-disjoint with [start, end[, available once the query is done.
     */
    public CompletableFuture<List<Event>> getEventsInTimeframe(ZonedDateTime start, ZonedDateTime end) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        if(end.toEpochSecond() - start.toEpochSecond() == 0) {
            return CompletableFuture.completedFuture(new ArrayList<Event>());
        }
        if(end.toEpochSecond() - start.toEpochSecond() < 0) {
            throw new IllegalArgumentException("end time is less than start time");
        }
        Long ti = start.toEpochSecond();
        Long tf = end.toEpochSecond();
        CompletableFuture<List<Event>> res1 = db.filterWhereGreaterEqLess(EventStorer.KEY_EPOCH_START_TIME, ti, tf, EventStorer.getInstance()); // ti <= eventStartTime < tf
        CompletableFuture<List<Event>> res2 = db.filterWhereGreaterLessEq(EventStorer.KEY_EPOCH_END_TIME, ti, tf, EventStorer.getInstance()); // ti < eventEndTime <= tf
        CompletableFuture<List<Event>> res3 = db.filterWhereLess(EventStorer.KEY_EPOCH_START_TIME, ti, EventStorer.getInstance()); // eventStartTime < ti
        CompletableFuture<List<Event>> res4 = db.filterWhereGreater(EventStorer.KEY_EPOCH_END_TIME, tf, EventStorer.getInstance()); // eventEndTime > tf
        return CompletableFuture.allOf(res1, res2, res3, res4).thenApply(ignoredVoid -> {
            List<Event> r1 = res1.join();
            List<Event> r2 = res2 .join();
            List<Event> r3 = res3.join();
            List<Event> r4 = res4.join();
            r1.removeIf(event -> r2.contains(event));
            r3.removeIf(event -> !r4.contains(event));
            r1.addAll(r2);
            r1.addAll(r3);

            return r1;
        });
    }

    /**
     * Get events that take place during a given day
     * @param day (ZonedDateTime) : the given day
     * @return (CompletableFuture<List<Event>>) : A list of events occurring during the given day, once the query is done
     */
    public CompletableFuture<List<Event>> getEventsByDay(ZonedDateTime day) {
        ZonedDateTime dayStart = TimeUtils.truncateTimeToDays(day);
        ZonedDateTime dayEnd = dayStart.plusDays(1);
        return getEventsInTimeframe(dayStart, dayEnd);
    }

    /**
     * Get events that take place during a given week
     * @param week (ZonedDateTime) : the given week
     * @return (CompletableFuture<List<Event>>) : A list of events occurring during the given week, once the query is done
     */
    public CompletableFuture<List<Event>> getEventsByWeek(ZonedDateTime week) {
        ZonedDateTime weekStart = TimeUtils.truncateTimeToWeeks(week);
        ZonedDateTime weekEnd = weekStart.plusWeeks(1);
        return getEventsInTimeframe(weekStart, weekEnd);
    }

    /**
     * Get events that take place during a given month
     * @param month (ZonedDateTime) : the given month
     * @return (CompletableFuture<List<Event>>) : A list of events occurring during the given month, once the query is done
     */
    public CompletableFuture<List<Event>> getEventsByMonth(ZonedDateTime month) {
        ZonedDateTime monthStart = TimeUtils.truncateTimeToMonths(month);
        ZonedDateTime monthEnd = monthStart.plusMonths(1);
        return getEventsInTimeframe(monthStart, monthEnd);
    }

    /**
     * Static version of getEventsByDay
     * @param db (Database) : the database to which the queries are applied
     */
    public static CompletableFuture<List<Event>> getEventsByDay(Database db, ZonedDateTime day) {
        return new EventQueries(db).getEventsByDay(day);
    }

    /**
     * Static version of getEventsByWeek
     * @param db (Database) : the database to which the queries are applied
     */
    public static CompletableFuture<List<Event>> getEventsByWeek(Database db, ZonedDateTime week) {
        return new EventQueries(db).getEventsByWeek(week);
    }

    /**
     * Static version of getEventsByMonth
     * @param db (Database) : the database to which the queries are applied
     */
    public static CompletableFuture<List<Event>> getEventsByMonth(Database db, ZonedDateTime month) {
        return new EventQueries(db).getEventsByMonth(month);
    }

    /**
     * Method to create a full series of recurring events based on the attributes of a given event. The events are recurring from the startTime of the given event to time 'endTime' (included)
     * given in the Recurrence attribute of 'event', and the time between each recurrence of the event is based on the period given in the Recurrence attribute of 'event'.
     * Ex use : addRecurringEvent(event("tennis", startTime: "Jan 1 2021).Recurrence(period:"weekly", endTime: " Jan 1 2022")) means the event "tennis" will occur every week
     * starting at the begining of 2021 and will end at the begining of 2022
     * @param event (Event) : the given event representing the recurrence series
     * @return (CompletableFuture<Integer>) : If the recurrence series has been successfully created and stored into the database it returns the number of stored events, else it returns 0
     */
    public CompletableFuture<Integer> addRecurringEvent(Event event) {
        if(!Objects.requireNonNull(event).isRecurrent()) {
            throw new IllegalArgumentException("The given event is not recurrent");
        }
        if(event.getStartTime().toEpochSecond() > event.getRecurrence().get().getEndTime().toEpochSecond()) {
            throw new IllegalArgumentException("The end time is invalid");
        }
        List<Event> eventsToStore = new ArrayList<Event>();
        Recurrence newEventRecurrence = event.getRecurrence().get();

        Id groupId = newEventRecurrence.getGroupId();
        long period = newEventRecurrence.getPeriod().getSeconds();
        long recurrenceLimit = newEventRecurrence.getEndTime().toEpochSecond();
        long refStartTime = event.getStartTime().toEpochSecond();

        newEventRecurrence = newEventRecurrence.setPrevEvent(Optional.ofNullable(null));

        long x = (recurrenceLimit - refStartTime)/period;
        Id nextId = null;
        Id currId = Id.generateRandom();
        Id prevId = x <= 1 ? event.getId() : Id.generateRandom();
        long tmpStartTime = refStartTime + x * period;
        while(x > 0) {
            Recurrence r = new Recurrence(groupId, newEventRecurrence.getPeriod(), newEventRecurrence.getEndTime(), Optional.of(prevId), Optional.ofNullable(nextId));
            Event newEvent = new Event(currId, event.getName(), event.getLocationName(), event.getCoordinates(),
                    TimeUtils.epochToZonedDateTime(tmpStartTime), TimeUtils.epochToZonedDateTime(tmpStartTime+period), Optional.of(r));
            eventsToStore.add(newEvent);
            nextId = currId;
            currId = prevId;
            prevId = (--x) <= 1 ? event.getId() : Id.generateRandom();
            tmpStartTime = refStartTime + x * period;
        }
        newEventRecurrence = newEventRecurrence.setNextEvent(Optional.ofNullable(nextId));

        eventsToStore.add(event.setRecurrence(newEventRecurrence));

        if(eventsToStore.size() == 1) { //Preserve the invariant that an event series has more than 1 element.
            return db.store(new Event(event.getId(), event.getName(), event.getLocationName(), event.getCoordinates(), event.getStartTime(), event.getEndTime(), Optional.empty()))
                    .thenApply(id -> id == event.getId() ? 1 : 0);
        }
        return db.storeAll(eventsToStore).thenApply(b -> b ? eventsToStore.size() : 0);
    }

    /**
     * Stores a non-recurring event in the database
     * @param event (Event) : The recurring event to store to the database
     * @return (CompletableFuture<Id>) : The id of the event stored, once the query is done
     */
    public CompletableFuture<Id> addNonRecurringEvent(Event event) {
        if(Objects.requireNonNull(event).isRecurrent()) {
            throw new IllegalArgumentException("The given event is recurrent");
        }
        return db.store(event);
    }

    /**
     * Changes the end time of the recurring event series. This operation may delete the recurring event series, if the new end time is less than the start time of the first recurring event.
     * If the end time is such that only 1 event is left in the recurrence series, then the series is deleted and the lasting event is converted to a non-recurring event
     * @param event (Event) : A recurring event belonging to the target recurrence series
     * @param newEndTime (ZonedDateTime) : The new end time of the recurrence series
     * @return (CompletableFuture<Boolean>) : True if the operation completed successfully
     */
    public CompletableFuture<Boolean> changeRecurringSeriesEndTime(Event event, ZonedDateTime newEndTime) {
        if(!Objects.requireNonNull(event).isRecurrent()) {
            throw new IllegalArgumentException("The given event is not recurrent");
        }
        Objects.requireNonNull(newEndTime);
        Id groupId = event.getRecurrence().get().getGroupId();
        CompletableFuture<List<Event>> eventSeriesQuery = getRecurrEventSeriesOf(groupId);
        return eventSeriesQuery.thenCompose(eventSeries -> {
            if(eventSeries.isEmpty()) {
            throw new IllegalArgumentException("The recurring series doesn't exist");
            }
            ZonedDateTime prevEndTime = eventSeries.get(0).getRecurrence().get().getEndTime();

            if(newEndTime.toEpochSecond() < prevEndTime.toEpochSecond()) { //Remove some events
                List<CompletableFuture<Id>> changedEvents = new ArrayList<>();
                long smallestEpochSecond = prevEndTime.toEpochSecond();
                Id latestEventId = null;
                for(Event e : eventSeries) {
                    if(e.getStartTime().toEpochSecond() > newEndTime.toEpochSecond()) {
                        changedEvents.add(db.remove(e.getId(), e.storer()));
                        if(e.getStartTime().toEpochSecond() < smallestEpochSecond) {
                            smallestEpochSecond = e.getStartTime().toEpochSecond();
                            latestEventId = e.getRecurrence().get().getPrevEvent().orElse(null);
                        }
                    }
                }
                if(latestEventId != null) { //Update next event pointer of last event in series
                    Event target = null;
                    for(Event e : eventSeries) {
                        if(e.getId().equals(latestEventId)) {
                            target = e;
                        }
                    }
                    if(!target.getRecurrence().get().getPrevEvent().isPresent()) {
                        changedEvents.add(db.store(new Event(target.getId(), target.getName(), target.getLocationName(), target.getCoordinates(), target.getStartTime(), target.getEndTime(), Optional.empty())));
                    } else {
                        changedEvents.add(db.store(target.setRecurrence(target.getRecurrence().get().setNextEvent(Optional.empty()))));
                    }
                }
                return CompletableFuture.allOf(changedEvents.toArray(new CompletableFuture[changedEvents.size()])).thenApply(t -> true);

            } else { //Add some events
                List<Event> eventsToStore = new ArrayList<Event>();
                Event lastEvent = null;
                for(Event e : eventSeries) {
                    if(!e.getRecurrence().get().getNextEvent().isPresent()) {
                        lastEvent = e;
                    }
                }
                long period = event.getRecurrence().get().getPeriod().getSeconds();
                long refStartTime = lastEvent.getStartTime().toEpochSecond();
                long x = (newEndTime.toEpochSecond() - refStartTime)/period;
                Id nextId = null;
                Id currId = Id.generateRandom();
                Id prevId = x <= 1 ? lastEvent.getId() : Id.generateRandom();
                long tmpStartTime = refStartTime + x * period;
                while(x > 0) {
                    Recurrence r = new Recurrence(groupId, event.getRecurrence().get().getPeriod(), newEndTime, Optional.of(prevId), Optional.ofNullable(nextId));
                    Event newEvent = new Event(currId, event.getName(), event.getLocationName(), event.getCoordinates(),
                            TimeUtils.epochToZonedDateTime(tmpStartTime), TimeUtils.epochToZonedDateTime(tmpStartTime+period), Optional.of(r));
                    eventsToStore.add(newEvent);
                    nextId = currId;
                    currId = prevId;
                    prevId = (--x) <= 1 ? lastEvent.getId() : Id.generateRandom();
                    tmpStartTime = refStartTime + x * period;
                }
                eventsToStore.add(lastEvent.setRecurrence(lastEvent.getRecurrence().get().setNextEvent(Optional.ofNullable(nextId))));

                return db.storeAll(eventsToStore);
            }
        });
    }

    /**
     * Modifies the existing event having the same Id as parameter 'event' to have the same fields as 'event'
     * @throws IllegalArgumentException if the recurrence attributes of the event are changed
     * @param event (Event) : the modified event
     * @return (CompletableFuture<Id>) : the id of the modified event, once the query is done
     */
    public CompletableFuture<Id> modifyEvent(Event event) {
        return db.retrieve(Objects.requireNonNull(event).getId(), event.storer()).thenCompose(e -> {
            Objects.requireNonNull(e, "Event doesn't exist in the database");

            if(!e.getRecurrence().equals(event.getRecurrence())) {
                throw new IllegalArgumentException("Cannot change the recurrence attributes of the event");
            }

            return e.equals(event) ? CompletableFuture.completedFuture(e.getId())
                    : db.store(new Event(e.getId(), event.getName(), event.getLocationName(), event.getCoordinates(), event.getStartTime(), event.getEndTime(), e.getRecurrence()));
        });
    }

    /**
     * Static version of modifyEvent
     * @param db (Database) : the database to which the queries are applied
     */
    public static CompletableFuture<Id> modifyEvent(Database db, Event event) {
        return new EventQueries(db).modifyEvent(event);
    }

    /**
     * Removes an event from the database according to its Id.
     * If the event is recurring (part of a recurrence series) then the previous and next event from the recurrence series need to have their pointers updated,
     * as the recurrence series is a linked list of recurring events. Else we simple remove the event from the database.
     * INVARIANT : A recurrence series has always more than 1 element (event). That is, if an event belongs to a recurrence series (i.e. is recurrent) then its pointers to the previous and next events
     * of the recurrence series are not both null.
     * This implies that if there is only 1 event left in the recurrence series, then the series is deleted and the single event is transformed into an ordinary non-recurring event.
     * @param eventId (Id) : The id of the event that has to be removed from the database
     * @return (CompletableFuture<Id>) : the id of the modified event, once the query is done
     */
    public CompletableFuture<Id> removeEvent(Id eventId) {
        return db.retrieve(eventId, EventStorer.getInstance()).thenCompose(event -> {
            if(event == null) { //The event doesn't exist in the database so the task is already done
                return CompletableFuture.completedFuture(eventId);
            }
            if(event.isRecurrent()) {
                Optional<Id> prev = event.getRecurrence().get().getPrevEvent();
                Optional<Id> next = event.getRecurrence().get().getNextEvent();

                if(prev.isPresent() && next.isPresent()) {
                    CompletableFuture<Event> prevEvent = db.retrieve(prev.get(), event.storer());
                    CompletableFuture<Event> nextEvent = db.retrieve(next.get(), event.storer());
                    return CompletableFuture.allOf(prevEvent, nextEvent).thenCompose(aVoid -> {
                        Event e = prevEvent.join();
                        return db.store(e.setRecurrence(e.getRecurrence().get().setNextEvent(next)));
                    }).thenCompose(id1 -> {
                        Event e = nextEvent.join();
                        return db.store(e.setRecurrence(e.getRecurrence().get().setPrevEvent(prev)));
                    }).thenCompose(id2 -> db.remove(event.getId(), event.storer()));

                } else if(prev.isPresent()) {
                    CompletableFuture<Event> prevEvent = db.retrieve(prev.get(), event.storer());
                    return prevEvent.thenCompose(e -> {
                        if(!e.getRecurrence().get().getPrevEvent().isPresent()) { //Convert to non-recurring event
                            return db.store(new Event(e.getId(), e.getName(), e.getLocationName(), e.getCoordinates(), e.getStartTime(), e.getEndTime(), Optional.empty()));
                        } else {
                            return db.store(e.setRecurrence(e.getRecurrence().get().setNextEvent(Optional.empty())));
                        }
                    }).thenCompose(n -> db.remove(event.getId(), event.storer()));

                } else { //Impossible to have both prev AND next not present
                    CompletableFuture<Event> nextEvent = db.retrieve(next.get(), event.storer());
                    return nextEvent.thenCompose(e -> {
                        if(!e.getRecurrence().get().getNextEvent().isPresent()) { //Convert to non-recurring event
                            return db.store(new Event(e.getId(), e.getName(), e.getLocationName(), e.getCoordinates(), e.getStartTime(), e.getEndTime(), Optional.empty()));
                        } else {
                            return db.store(e.setRecurrence(e.getRecurrence().get().setPrevEvent(Optional.empty())));
                        }
                    }).thenCompose(n -> db.remove(event.getId(), event.storer()));
                }

            } else {
                return db.remove(event.getId(), event.storer());
            }
        });
    }

    /**
     * Static version of removeEvent
     * @param db (Database) : the database to which the queries are applied
     */
    public static CompletableFuture<Id> removeEvent(Database db, Id eventId) {
        return new EventQueries(db).removeEvent(eventId);
    }

    /**
     * Returns all the events of the recurrence series defined by the Id 'groupId'
     * @param groupId (Id) : The id of the recurrence series that has to be returned
     * @return (CompletableFuture<List<Event>>) : A list of all the events belonging to the recurrence series, once the query is done
     */
    public CompletableFuture<List<Event>> getRecurrEventSeriesOf(Id groupId) {
        return db.filterWhereEquals(EventStorer.KEY_RECURR_ID, Objects.requireNonNull(groupId).getUuid(), EventStorer.getInstance());
    }

    /**
     * Remove a full recurrence series, i.e. all the events belonging to the recurrence series to which 'event' belongs
     * @param event (Event) : an event belonging to the recurrence series that has to be removed
     * @return (CompletableFuture<Boolean>) : True if the operation of removal succeeded
     */
    public CompletableFuture<Boolean> removeRecurrEvents(Event event) {
        if(!Objects.requireNonNull(event).isRecurrent()) {
            throw new IllegalArgumentException("The given event is not recurrent");
        }
        return removeRecurrEvents(event.getRecurrence().get().getGroupId());
    }

    /**
     * Remove a full recurrence series based on the id of the series
     * @param groupId (Id) : the id of the recurrence series that has to be removed
     * @return (CompletableFuture<Boolean>) : True if the operation of removal succeeded
     */
    public CompletableFuture<Boolean> removeRecurrEvents(Id groupId) {
        CompletableFuture<List<Event>> eventsToRemove = getRecurrEventSeriesOf(Objects.requireNonNull(groupId));
        return eventsToRemove.thenCompose(events -> {
            List<CompletableFuture<Id>> removedEvents = new ArrayList<>();
            for(Event e : events) {
                removedEvents.add(db.remove(e.getId(), e.storer()));
            }
            //Combine all results. We don't care about the Ids returned we just care about if exceptions are thrown.
            return CompletableFuture.allOf(removedEvents.toArray(new CompletableFuture[removedEvents.size()])).thenApply(t -> true);
        });
    }

    /**
     * Converts an existing non-recurring event to a recurring-event, by creating its recurrence series in the time interval ['startRecurrence', 'endRecurrence']
     * @param event (Event) : The event to convert
     * @param groupId (Id) : the group id of the recurrence series
     * @param period (Duration) : the time between each recurrence of the event
     * @param endRecurrence (ZonedDateTime) : the end time limit of the recurrence of the event
     * @return (CompletableFuture<Integer>) : The number of events in the recurrence series
     */
    public CompletableFuture<Integer> convertToRecurring(Event event, Id groupId, Duration period, ZonedDateTime endRecurrence) {
        if(Objects.requireNonNull(event).isRecurrent()) {
            throw new IllegalArgumentException("The given event is already recurrent");
        }
        Recurrence newRecurrenceSeries = new Recurrence(Objects.requireNonNull(groupId), Objects.requireNonNull(period), Objects.requireNonNull(endRecurrence), Optional.empty(), Optional.empty());
        return addRecurringEvent(event.setRecurrence(newRecurrenceSeries));
    }

}