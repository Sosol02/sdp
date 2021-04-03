package com.github.onedirection;

import com.github.onedirection.database.ConcreteDatabase;
import com.github.onedirection.database.Database;
import com.github.onedirection.database.store.EventStorer;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class EventQueries {

    private Database db;

    public EventQueries(Database db) {
        this.db = db;
    }

    public CompletableFuture<List<Event>> getEventsInTimeframe(ZonedDateTime start, ZonedDateTime end) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        if(db.getClass() == ConcreteDatabase.class) {
            Long ti = start.toEpochSecond();
            Long tf = end.toEpochSecond();
            ConcreteDatabase cdb = (ConcreteDatabase)db;
            CompletableFuture<List<Event>> res1 = cdb.filterWhereGreaterEqLess(EventStorer.KEY_EPOCH_START_TIME, ti, tf, EventStorer.getInstance()); // ti <= eventStartTime < tf
            CompletableFuture<List<Event>> res2 = cdb.filterWhereGreaterLessEq(EventStorer.KEY_EPOCH_END_TIME, ti, tf, EventStorer.getInstance()); // ti < eventEndTime <= tf
            CompletableFuture<List<Event>> res3 = cdb.filterWhereLess(EventStorer.KEY_EPOCH_START_TIME, ti, EventStorer.getInstance()); // eventStartTime < ti
            CompletableFuture<List<Event>> res4 = cdb.filterWhereGreater(EventStorer.KEY_EPOCH_END_TIME, tf, EventStorer.getInstance()); // eventEndTime > tf
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
        } else {
            throw new UnsupportedOperationException("This method is not supported for non firebase database.");
        }
    }

    public CompletableFuture<List<Event>> getEventsByDay(ZonedDateTime day) {
        ZonedDateTime dayStart = day.truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime dayEnd = dayStart.plusDays(1);
        return getEventsInTimeframe(dayStart, dayEnd);
    }

    public CompletableFuture<List<Event>> getEventsByWeek(ZonedDateTime week) {
        ZonedDateTime weekStart = week.truncatedTo(ChronoUnit.WEEKS);
        ZonedDateTime weekEnd = weekStart.plusWeeks(1);
        return getEventsInTimeframe(weekStart, weekEnd);
    }

    public CompletableFuture<List<Event>> getEventsByMonth(ZonedDateTime month) {
        ZonedDateTime monthStart = month.truncatedTo(ChronoUnit.MONTHS);
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
