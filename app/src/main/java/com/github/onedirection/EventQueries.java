package com.github.onedirection;

import com.github.onedirection.database.Database;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.events.Event;
import com.github.onedirection.events.Recurrence;
import com.github.onedirection.utils.Id;
import com.github.onedirection.utils.TimeUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EventQueries {

    private Database db;

    public EventQueries(Database db) {
        this.db = db;
    }

    /**
     * Method to query events that take place in a time frame that is non-disjoint with the given time frame [start, end[
     * The mechanism to handle recurring events is the following : A single event is stored in the database to represent a series of recurring events. It is then retrieved, and evaluated to see if
     * any of the events from the series of recurring events fall in the given time frame [start, end[. If there are, they are concretely created and added to the returning list. They are however not added to
     * the database.
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

    public CompletableFuture<List<Event>> getEventsByDay(ZonedDateTime day) {
        ZonedDateTime dayStart = TimeUtils.truncateTimeToDays(day);
        ZonedDateTime dayEnd = dayStart.plusDays(1);
        return getEventsInTimeframe(dayStart, dayEnd);
    }

    public CompletableFuture<List<Event>> getEventsByWeek(ZonedDateTime week) {
        ZonedDateTime weekStart = TimeUtils.truncateTimeToWeeks(week);
        ZonedDateTime weekEnd = weekStart.plusWeeks(1);
        return getEventsInTimeframe(weekStart, weekEnd);
    }

    public CompletableFuture<List<Event>> getEventsByMonth(ZonedDateTime month) {
        ZonedDateTime monthStart = TimeUtils.truncateTimeToMonths(month);
        ZonedDateTime monthEnd = monthStart.plusMonths(1);
        return getEventsInTimeframe(monthStart, monthEnd);
    }

    public static CompletableFuture<List<Event>> getEventsByDay(Database db, ZonedDateTime day) {
        return new EventQueries(db).getEventsByDay(day);
    }

    public static CompletableFuture<List<Event>> getEventsByWeek(Database db, ZonedDateTime week) {
        return new EventQueries(db).getEventsByWeek(week);
    }

    public static CompletableFuture<List<Event>> getEventsByMonth(Database db, ZonedDateTime month) {
        return new EventQueries(db).getEventsByMonth(month);
    }

    public CompletableFuture<Boolean> storeRecurringEvent(Event event, ZonedDateTime startRecurrence, ZonedDateTime endRecurrence) {
        if(!Objects.requireNonNull(event).isRecurrent()) {
            throw new IllegalArgumentException("The given event is not recurrent");
        }
        Objects.requireNonNull(startRecurrence);
        Objects.requireNonNull(endRecurrence);
        //TODO : implement
        return null;
    }

    public CompletableFuture<Boolean> extendRecurringEvent(Event event, ZonedDateTime newEndRecurrence) {
        if(!Objects.requireNonNull(event).isRecurrent()) {
            throw new IllegalArgumentException("The given event is not recurrent");
        }
        Objects.requireNonNull(newEndRecurrence);
        //TODO : implement
        return null;
    }

    public CompletableFuture<Boolean> extendBackRecurringEvent(Event event, ZonedDateTime newStartRecurrence) {
        if(!Objects.requireNonNull(event).isRecurrent()) {
            throw new IllegalArgumentException("The given event is not recurrent");
        }
        Objects.requireNonNull(newStartRecurrence);
        //TODO : implement
        return null;
    }

    public CompletableFuture<Boolean> removePrevRecurrFrom(Event event) {
        if(!Objects.requireNonNull(event).isRecurrent()) {
            throw new IllegalArgumentException("The given event is not recurrent");
        }
        //TODO : implement
        return null;
    }

    public CompletableFuture<Boolean> removeNextRecurrFrom(Event event) {
        if(!Objects.requireNonNull(event).isRecurrent()) {
            throw new IllegalArgumentException("The given event is not recurrent");
        }
        //TODO : implement
        return null;
    }

    public CompletableFuture<Id> modifyEvent(Event event) {
        return db.retrieve(Objects.requireNonNull(event).getId(), event.storer()).thenCompose(e -> {
            Objects.requireNonNull(e, "Event doesn't exist in the database");

            return db.store(new Event(e.getId(), event.getName(), event.getLocationName(), event.getCoordinates(), event.getStartTime(), event.getEndTime(), e.getRecurrence()));
        });
    }

    public static CompletableFuture<Id> modifyEvent(Database db, Event event) {
        return new EventQueries(db).modifyEvent(event);
    }

    public CompletableFuture<Id> removeEvent(Event event) {
        if(Objects.requireNonNull(event).isRecurrent()) {
            Optional<Id> prev = event.getRecurrence().get().getPrevEvent();
            Optional<Id> next = event.getRecurrence().get().getNextEvent();

            if(prev.isPresent() && next.isPresent()) {
                CompletableFuture<Event> prevEvent = db.retrieve(prev.get(), event.storer());
                CompletableFuture<Event> nextEvent = db.retrieve(next.get(), event.storer());
                return CompletableFuture.allOf(prevEvent, nextEvent).thenCompose(aVoid -> {
                    Event e = prevEvent.join();
                    return db.store(e.setRecurrence(e.getRecurrence().get().setNextEvent(next)));
                }).thenCompose(m -> {
                    Event e = nextEvent.join();
                    return db.store(e.setRecurrence(e.getRecurrence().get().setPrevEvent(prev)));
                }).thenCompose(n -> db.remove(event.getId(), event.storer()));

            } else if(prev.isPresent()) {
                CompletableFuture<Event> prevEvent = db.retrieve(prev.get(), event.storer());
                return CompletableFuture.allOf(prevEvent).thenCompose(aVoid -> {
                    Event e = prevEvent.join();
                    if(!e.getRecurrence().get().getPrevEvent().isPresent()) { //Convert to non-recurring event
                        return db.store(new Event(e.getId(), e.getName(), e.getLocationName(), e.getCoordinates(), e.getStartTime(), e.getEndTime(), Optional.empty()));
                    } else {
                        return db.store(e.setRecurrence(e.getRecurrence().get().setNextEvent(Optional.empty())));
                    }
                }).thenCompose(n -> db.remove(event.getId(), event.storer()));

            } else { //Impossible to have both prev AND next not present
                CompletableFuture<Event> nextEvent = db.retrieve(next.get(), event.storer());
                return CompletableFuture.allOf(nextEvent).thenCompose(aVoid -> {
                    Event e = nextEvent.join();
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
    }

    public static CompletableFuture<Id> removeEvent(Database db, Event event) {
        return new EventQueries(db).removeEvent(event);
    }

    public CompletableFuture<Boolean> convertToRecurring(Event event, ZonedDateTime startRecurrence, ZonedDateTime endRecurrence) {
        if(Objects.requireNonNull(event).isRecurrent()) {
            throw new IllegalArgumentException("The given event is already recurrent");
        }
        return storeRecurringEvent(event, startRecurrence, endRecurrence);
    }

}
