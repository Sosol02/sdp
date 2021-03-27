package com.github.onedirection;

import com.github.onedirection.database.ConcreteDatabase;
import com.github.onedirection.database.Database;
import com.github.onedirection.database.store.EventStorer;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EventQueries {

    private Database db;

    public EventQueries(Database db) {
        this.db = db;
    }

    public CompletableFuture<List<Event>> getEventsInTimeframe(ZonedDateTime start, ZonedDateTime end) {
        Long ti = start.toEpochSecond();
        Long tf = end.toEpochSecond();
        if(db.getClass() == ConcreteDatabase.class) {
            CompletableFuture<List<Event>> res1 = ((ConcreteDatabase) db).filterWhereGreaterEqLess(EventStorer.KEY_EPOCH_START_TIME, ti, tf, EventStorer.getInstance());
            CompletableFuture<List<Event>> res2 = ((ConcreteDatabase) db).filterWhereGreaterEqLess(EventStorer.KEY_EPOCH_END_TIME, ti, tf, EventStorer.getInstance());
            CompletableFuture<List<Event>> res3 = null; //starTime < start AND endTime > end
        }
        return null;
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
