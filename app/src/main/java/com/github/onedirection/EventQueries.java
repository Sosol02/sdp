package com.github.onedirection;

import com.github.onedirection.database.Database;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.database.utils.TimeUtils;
import com.github.onedirection.events.Event;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        CompletableFuture<List<Event>> recurringEvents = db.filterWhereGreaterEq(EventStorer.KEY_RECURRING_PERIOD, 0L, EventStorer.getInstance());
        return CompletableFuture.allOf(res1, res2, res3, res4, recurringEvents).thenApply(ignoredVoid -> {
            List<Event> r1 = res1.join();
            List<Event> r2 = res2 .join();
            List<Event> r3 = res3.join();
            List<Event> r4 = res4.join();
            List<Event> recurring = recurringEvents.join();
            r1.removeIf(event -> r2.contains(event));
            r3.removeIf(event -> !r4.contains(event));
            r1.addAll(r2);
            r1.addAll(r3);
            recurring.removeIf(event -> r1.contains(event));

            for(Event e : recurring) {
                long tStart = e.getStartTime().toEpochSecond();
                long tEnd = e.getEndTime().toEpochSecond();
                long duration = tEnd-tStart;
                long period = e.getRecurrencePeriod().get().getEpochSecond();
                long x = Math.max(1, ti/tStart);
                while((tStart+(x-1)*period) < tf) {
                    long startTime = (tStart+(x-1)*period);
                    long endTime = startTime + duration;
                    Event newEvent = new Event(e.getId(), e.getName(), e.getLocationName(), e.getCoordinates(),
                            TimeUtils.epochToZonedDateTime(startTime), TimeUtils.epochToZonedDateTime(endTime), e.getRecurrencePeriod());
                    if(startTime >= ti && startTime < tf) {
                        r1.add(newEvent);
                    }
                    else if(endTime > ti && endTime <= tf) {
                        r1.add(newEvent);
                    }
                    else if(startTime < ti && endTime > tf) {
                        r1.add(newEvent);
                    }
                    ++x;
                }
            }
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
}
