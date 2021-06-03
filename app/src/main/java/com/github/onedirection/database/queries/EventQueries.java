package com.github.onedirection.database.queries;

import com.github.onedirection.database.implementation.Database;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.event.model.Event;
import com.github.onedirection.event.model.Recurrence;
import com.github.onedirection.utils.Id;
import com.github.onedirection.utils.TimeUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A class used to query the events in a database based on different operations
 */
public class EventQueries {

    private final Database db;

    /**
     * Constructor of EventQueries
     * @param db (Database) : the database to which the queries are applied
     */
    public EventQueries(Database db) {
        this.db = db;
    }

    public static CompletableFuture<List<Event>> getAllEvents(Database db) {
        return new EventQueries(db).getAllEvents();
    }

    public CompletableFuture<List<Event>> getAllEvents() {
        return db.retrieveAll(EventStorer.getInstance());
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

    public static CompletableFuture<List<Event>> getEventsInTimeframe(Database db, ZonedDateTime start, ZonedDateTime end) {
        return new EventQueries(db).getEventsInTimeframe(start,end);
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
     * Note : the new recurrence series should have as id the given 'event' id (this event is called the root of the recurrence series).
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
        if(!event.getRecurrence().get().getGroupId().equals(event.getId())) {
            throw new IllegalArgumentException("The given recurrence series id does not match the root event id");
        }
        List<Event> eventsToStore = new ArrayList<Event>();
        Recurrence newEventRecurrence = event.getRecurrence().get();

        final long period = newEventRecurrence.getPeriod().getSeconds();
        final long recurrenceLimit = newEventRecurrence.getEndTime().toEpochSecond();
        final long refStartTime = event.getStartTime().toEpochSecond();
        final long duration = event.getEndTime().toEpochSecond() - event.getStartTime().toEpochSecond();

        long x = (recurrenceLimit - refStartTime)/period;
        long tmpStartTime = refStartTime + x * period;
        while(x > 0) {
            Event newEvent = new Event(Id.generateRandom(), event.getName(), event.getLocationName(), event.getCoordinates(),
                    TimeUtils.epochToZonedDateTime(tmpStartTime), TimeUtils.epochToZonedDateTime(tmpStartTime+duration), Optional.of(newEventRecurrence), event.getIsFavorite());
            eventsToStore.add(newEvent);
            tmpStartTime = refStartTime + (--x) * period;
        }

        eventsToStore.add(event);

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
            Long period = eventSeries.get(0).getRecurrence().get().getPeriod().getSeconds();
            Recurrence newRecurrence = new Recurrence(groupId, period, newEndTime);

            if(newEndTime.toEpochSecond() < prevEndTime.toEpochSecond()) { //Remove some events
                List<CompletableFuture<Id>> changedEvents = new ArrayList<>();
                for(Event e : eventSeries) {
                    if(e.getStartTime().toEpochSecond() > newEndTime.toEpochSecond()) {
                        changedEvents.add(db.remove(e.getId(), e.storer()));
                    } else {
                        changedEvents.add(db.store(e.setRecurrence(newRecurrence)));
                    }
                }

                return CompletableFuture.allOf(changedEvents.toArray(new CompletableFuture[changedEvents.size()])).thenApply(t -> true);

            } else { //Add some events
                List<Event> changedEvents = new ArrayList<Event>();
                Event lastEvent = null;
                long largestStartTime = Long.MIN_VALUE;
                for(Event e : eventSeries) {
                    if(e.getStartTime().toEpochSecond() > largestStartTime) {
                        lastEvent = e;
                        largestStartTime = e.getStartTime().toEpochSecond();
                    }
                    changedEvents.add(e.setRecurrence(newRecurrence));
                }
                long refStartTime = lastEvent.getStartTime().toEpochSecond();
                long x = (newEndTime.toEpochSecond() - refStartTime)/period;
                long tmpStartTime = refStartTime + x * period;
                while(x > 0) {
                    Event newEvent = new Event(Id.generateRandom(), lastEvent.getName(), lastEvent.getLocationName(), lastEvent.getCoordinates(),
                            TimeUtils.epochToZonedDateTime(tmpStartTime), TimeUtils.epochToZonedDateTime(tmpStartTime+period), Optional.of(newRecurrence), event.getIsFavorite());
                    changedEvents.add(newEvent);
                    tmpStartTime = refStartTime + (--x) * period;
                }

                return db.storeAll(changedEvents);
            }
        });
    }

    /**
     * Modifies the existing event having the same Id as parameter 'event' to have the same fields as 'event'
     * @throws IllegalArgumentException if the recurrence period of the event is changed or if the recurrence series id is changed
     * @param event (Event) : the modified event
     * @return (CompletableFuture<Id>) : the id of the modified event, once the query is done
     */
    public CompletableFuture<Id> modifyEvent(Event event) {
        return db.retrieve(Objects.requireNonNull(event).getId(), event.storer()).thenCompose(e -> {
            Objects.requireNonNull(e, "Event doesn't exist in the database");

            CompletableFuture<Boolean> changedRecurringEndTime = CompletableFuture.completedFuture(true);
            if(e.getRecurrence().isPresent() && event.getRecurrence().isPresent()) {
                if(!e.getRecurrence().get().getPeriod().equals(event.getRecurrence().get().getPeriod())) {
                    throw new IllegalArgumentException("Cannot change the period of the recurrence series");
                }
                if(!e.getRecurrence().get().getGroupId().equals(event.getRecurrence().get().getGroupId())) {
                    throw new IllegalArgumentException("Cannot change the recurrence id of the event");
                }
                if(!e.getRecurrence().get().getEndTime().equals(event.getRecurrence().get().getEndTime())) {
                    changedRecurringEndTime = changedRecurringEndTime.thenCompose(t -> changeRecurringSeriesEndTime(e, event.getRecurrence().get().getEndTime()));
                }
            }

            return changedRecurringEndTime.thenCompose(t -> {
                if(t) {
                    if(e.getRecurrence().isPresent() && !event.getRecurrence().isPresent()) { //Remove from recurrence series
                        return db.store(new Event(e.getId(), event.getName(), event.getLocationName(), event.getCoordinates(), event.getStartTime(), event.getEndTime(), Optional.empty(), event.getIsFavorite()));
                    } else if(!e.getRecurrence().isPresent() && event.getRecurrence().isPresent()) { //Convert to recurring
                        Event newEvent = new Event(e.getId(), event.getName(), event.getLocationName(), event.getCoordinates(), event.getStartTime(), event.getEndTime(), Optional.empty(), event.getIsFavorite());
                        return convertToRecurring(newEvent, event.getRecurrence().get()).thenApply(n -> n != 0 ? e.getId() : null);
                    } else { //No changes on the recurrence option
                        if(e.getRecurrence().isPresent() && event.getRecurrence().isPresent()) {
                            if(e.getRecurrence().get().getEndTime().toEpochSecond() > event.getRecurrence().get().getEndTime().toEpochSecond()) {
                                return CompletableFuture.completedFuture(e.getId()); //The event has been removed when changing the recurrence end time, so it is not modified in the database
                            }
                        }
                         return e.equals(new Event(e.getId(), event.getName(), event.getLocationName(), event.getCoordinates(), event.getStartTime(), event.getEndTime(), e.getRecurrence(), event.getIsFavorite())) ? CompletableFuture.completedFuture(event.getId())

                                : db.store(event);
                    }
                } else {
                    throw new IllegalArgumentException("An error occurred while changing the recurrence end time");
                }
            });
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
     * @param eventId (Id) : The id of the event that has to be removed from the database
     * @return (CompletableFuture<Id>) : the id of the removed event, once the query is done
     */
    public CompletableFuture<Id> removeEvent(Id eventId) {
        return db.remove(Objects.requireNonNull(eventId), EventStorer.getInstance());
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
     * Converts an existing non-recurring event to a recurring-event, by creating its recurrence series in the time interval ['eventStart', 'endRecurrence']
     * @param event (Event) : The event to convert
     * @param recurrenceSeries (Recurrence) : the new recurrence series of the event
     * @return (CompletableFuture<Integer>) : The number of events in the recurrence series
     */
    public CompletableFuture<Integer> convertToRecurring(Event event, Recurrence recurrenceSeries) {
        if(Objects.requireNonNull(event).isRecurrent()) {
            throw new IllegalArgumentException("The given event is already recurrent");
        }
        Recurrence newRecurrenceSeries = recurrenceSeries.getGroupId().equals(event.getId()) ? recurrenceSeries
                : new Recurrence(event.getId(), recurrenceSeries.getPeriod(), recurrenceSeries.getEndTime());
        return addRecurringEvent(event.setRecurrence(Objects.requireNonNull(newRecurrenceSeries)));
    }

}