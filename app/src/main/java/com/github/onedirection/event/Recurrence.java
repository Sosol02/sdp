package com.github.onedirection.events;

import com.github.onedirection.utils.Id;

import java.io.Serializable;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

import javax.annotation.concurrent.Immutable;

@Immutable
/**
 * Class representing the recurrence of an event.
 * A recurring event is stored as a recurrence series where all its recurring times are stored as individual events.
 * The Recurrence class thus represents a recurrence series.
 */
public class Recurrence implements Serializable {

    private final Id groupId;
    private final Duration period;
    private final ZonedDateTime endTime;

    /**
     * Constructor of Recurrence
     * @param groupId (Id) : The id of the recurrence series. The event that has this recurrence appended to it belongs to this specific recurrence series
     * @param period (Duration) : The time interval between each recurrence of the event
     * @param endTime (ZonedDateTime) : the end time of the recurrence series (end of the recurrence).
     */
    public Recurrence(Id groupId, Duration period, ZonedDateTime endTime) {
        this.groupId = Objects.requireNonNull(groupId);
        this.period = Objects.requireNonNull(period);
        this.endTime = Objects.requireNonNull(endTime);
    }

    public Recurrence(Id groupId, long periodSeconds, ZonedDateTime endTime) {
        this(groupId, Duration.ofSeconds(periodSeconds), endTime);
    }

    public Id getGroupId() { return groupId; }

    public Duration getPeriod() {
        return period;
    }

    public ZonedDateTime getEndTime() { return endTime; }

    public Recurrence setPeriod(Duration newPeriod) {
        return Objects.requireNonNull(newPeriod).equals(period) ? this : new Recurrence(groupId, newPeriod, endTime);
    }

    public Recurrence setEndTime(ZonedDateTime newEndTime) {
        return Objects.requireNonNull(newEndTime).equals(endTime) ? this : new Recurrence(groupId, period, newEndTime);
    }

    public Recurrence setPeriodFromSeconds(long periodSeconds) {
        Duration d = Duration.ofSeconds(periodSeconds);
        return setPeriod(d);
    }

    @Override
    public String toString() {
        return "[" + groupId + ", " +
                "Recurrence period: " + period + ", " +
                "Ending: " + endTime + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Recurrence recurrence = (Recurrence) o;
        return groupId.equals(recurrence.groupId) &&
                period.equals(recurrence.period) &&
                endTime.equals(recurrence.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, period, endTime);
    }
}
