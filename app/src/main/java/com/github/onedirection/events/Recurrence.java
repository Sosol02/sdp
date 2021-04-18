package com.github.onedirection.events;

import com.github.onedirection.utils.Id;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Recurrence {

    private Id groupId;
    private Duration period;
    private Id prevEvent;
    private Id nextEvent;

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

    public Recurrence setGroupId(Id newId) {
        return Objects.requireNonNull(newId).equals(groupId) ? this : new Recurrence(newId, period, Optional.ofNullable(prevEvent), Optional.ofNullable(nextEvent));
    }

    public Recurrence setPrevEvent(Optional<Id> newId) {
        return Objects.requireNonNull(newId).equals(prevEvent) ? this : new Recurrence(groupId, period, newId, Optional.ofNullable(nextEvent));
    }

    public Recurrence setNextEvent(Optional<Id> newId) {
        return Objects.requireNonNull(newId).equals(prevEvent) ? this : new Recurrence(groupId, period, Optional.ofNullable(prevEvent), newId);
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
        return "[" + groupId.toString() + "] " +
                "Recurrence period: " + period.toString() +
                " [" + prevEvent.toString() + ", " + nextEvent.toString() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Recurrence recurrence = (Recurrence) o;
        return groupId.equals(recurrence.groupId) &&
                period.equals(recurrence.period) &&
                prevEvent.equals(recurrence.prevEvent) &&
                nextEvent.equals(recurrence.nextEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, period, prevEvent, nextEvent);
    }
}
