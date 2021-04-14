package com.github.onedirection;

import android.util.Log;

import com.github.onedirection.database.ConcreteDatabase;
import com.github.onedirection.database.Database;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.database.utils.TimeUtils;
import com.github.onedirection.utils.Id;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
        if(end.toEpochSecond() - start.toEpochSecond() <= 0) {
            return CompletableFuture.completedFuture(new ArrayList<Event>());
        }
        if(db.getClass() == ConcreteDatabase.class) {
            Long ti = start.toEpochSecond();
            Long tf = end.toEpochSecond();
            ConcreteDatabase cdb = (ConcreteDatabase)db;
            CompletableFuture<List<Event>> res1 = cdb.filterWhereGreaterEqLess(EventStorer.KEY_EPOCH_START_TIME, ti, tf, EventStorer.getInstance()); // ti <= eventStartTime < tf
            CompletableFuture<List<Event>> res2 = cdb.filterWhereGreaterLessEq(EventStorer.KEY_EPOCH_END_TIME, ti, tf, EventStorer.getInstance()); // ti < eventEndTime <= tf
            CompletableFuture<List<Event>> res3 = cdb.filterWhereLess(EventStorer.KEY_EPOCH_START_TIME, ti, EventStorer.getInstance()); // eventStartTime < ti
            CompletableFuture<List<Event>> res4 = cdb.filterWhereGreater(EventStorer.KEY_EPOCH_END_TIME, tf, EventStorer.getInstance()); // eventEndTime > tf
            CompletableFuture<List<Event>> recurringEvents = cdb.filterWhereGreaterEq(EventStorer.KEY_RECURRING_PERIOD, new Long(0), EventStorer.getInstance());
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
                    long period = e.getRecurringPeriod().get().getEpochSecond();
                    long x = Math.max(1, ti/tStart);
                    while((tStart+(x-1)*period) < tf) {
                        long startTime = (tStart+(x-1)*period);
                        long endTime = startTime + duration;
                        Event newEvent = new Event(Id.generateRandom(), e.getName(), e.getLocationName(), e.getCoordinates(),
                                TimeUtils.epochToZonedDateTime(startTime), TimeUtils.epochToZonedDateTime(endTime), e.getRecurringPeriod());
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
        } else {
            throw new UnsupportedOperationException("This method is not supported for non firebase database.");
        }
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
