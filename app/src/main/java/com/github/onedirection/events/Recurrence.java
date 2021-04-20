package com.github.onedirection.events;

import com.github.onedirection.utils.Id;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.concurrent.Immutable;

@Immutable
/**
 * Class representing the recurrence of an event.
 * A recurring event is stored as a recurrence series where all its recurring times are stored as individual events.
 * The recurrence series is abstracted as a linked-list of events, where each event in the linked-list has pointers to its neighboring events. The Recurrence class is thus here to append these informations
 * to an event whenever this event belongs to a recurrence series.
 * INVARIANT : A recurrence series has always more than 1 element (event). That is, if an event belongs to a recurrence series (i.e. is recurrent) then its pointers to the previous and next events
 * of the recurrence series are not both null.
 */
public class Recurrence implements Serializable {

    private Id groupId;
    private Duration period;
    private Id prevEvent;
    private Id nextEvent;

    /**
     * Constructor of Recurrence
     * @param groupId (Id) : The id of a specific recurrence series. The event that has this recurrence appended to it belongs to this specific recurrence series
     * @param period (Duration) : The time interval between each recurrence of the event
     * @param prev (Id) : The id of the previous event neighbor in the recurrence series
     * @param next (Id) : The id of the next event neighbor in the recurrence series
     */
    public Recurrence(Id groupId, Duration period, Optional<Id> prev, Optional<Id> next) {
        this.groupId = Objects.requireNonNull(groupId);
        this.period = Objects.requireNonNull(period);
        this.prevEvent = Objects.requireNonNull(prev).orElse(null);
        this.nextEvent = Objects.requireNonNull(next).orElse(null);
    }

    public Recurrence(Id groupId, long periodSeconds, Optional<Id> prev, Optional<Id> next) {
        this(groupId, Duration.ofSeconds(periodSeconds), prev, next);
    }

    public Id getGroupId() { return groupId; }

    public Optional<Id> getPrevEvent() {
        return Optional.ofNullable(prevEvent);
    }

    public Optional<Id> getNextEvent() {
        return Optional.ofNullable(nextEvent);
    }

    public Duration getPeriod() {
        return period;
    }

    public Recurrence setPrevEvent(Optional<Id> newId) {
        return Objects.requireNonNull(newId).equals(Optional.ofNullable(prevEvent)) ? this : new Recurrence(groupId, period, newId, Optional.ofNullable(nextEvent));
    }

    public Recurrence setNextEvent(Optional<Id> newId) {
        return Objects.requireNonNull(newId).equals(Optional.ofNullable(nextEvent)) ? this : new Recurrence(groupId, period, Optional.ofNullable(prevEvent), newId);
    }

    public Recurrence setPeriod(Duration newPeriod) {
        return Objects.requireNonNull(newPeriod).equals(period) ? this : new Recurrence(groupId, newPeriod, Optional.ofNullable(prevEvent), Optional.ofNullable(nextEvent));
    }

    public Recurrence setPeriodFromSeconds(long periodSeconds) {
        Duration d = Duration.ofSeconds(periodSeconds);
        return setPeriod(d);
    }

    @Override
    public String toString() {
        return "[" + groupId + "] " +
                "Recurrence period: " + period +
                " [" + (prevEvent == null ? "" : prevEvent) + ", " +
                (nextEvent == null ? "" : nextEvent) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Recurrence recurrence = (Recurrence) o;
        return groupId.equals(recurrence.groupId) &&
                period.equals(recurrence.period) &&
                getPrevEvent().equals(recurrence.getPrevEvent()) &&
                getNextEvent().equals(recurrence.getNextEvent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, period, prevEvent, nextEvent);
    }
}
