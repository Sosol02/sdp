package com.github.onedirection;

import com.github.onedirection.geocoding.Coordinates;
import com.github.onedirection.geocoding.NamedCoordinates;
import com.github.onedirection.utils.Id;

import java.io.Serializable;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Event implements Serializable {

    final private Id id;
    final private String name;
    final private NamedCoordinates location;
    final private ZonedDateTime startTime;
    final private ZonedDateTime endTime;

    /**
     * Smallest time unit recorded inside the event.
     */
    final public static ChronoUnit TIME_PRECISION = ChronoUnit.SECONDS;

    /**
     * Create a new event.
     *
     * @param id        The unique identifier.
     * @param name      The display name.
     * @param location  Where the event takes place.
     * @param startTime When the event starts.
     * @param endTime   When the event ends.
     * @throws IllegalArgumentException If startTime happens before endTime.
     */
    public Event(Id id, String name, NamedCoordinates location, ZonedDateTime startTime, ZonedDateTime endTime) throws IllegalArgumentException {
        this.name = Objects.requireNonNull(name);
        this.location = Objects.requireNonNull(location);
        this.startTime = Objects.requireNonNull(startTime).truncatedTo(TIME_PRECISION);
        this.endTime = Objects.requireNonNull(endTime).truncatedTo(TIME_PRECISION);
        this.id = Objects.requireNonNull(id);

        if (startTime.until(endTime, TIME_PRECISION) < 0) {
            throw new IllegalArgumentException("The end date should be later than the start date.");
        }
    }

    public Event setName(String new_value) {
        return Objects.requireNonNull(new_value).equals(this.name) ? this : new Event(id, new_value, location, startTime, endTime);
    }

    public Event setLocation(NamedCoordinates new_value) {
        return Objects.requireNonNull(new_value).equals(this.location) ? this : new Event(id, name, new_value, startTime, endTime);
    }

    public Event setStartTime(ZonedDateTime new_value) {
        return Objects.requireNonNull(new_value).truncatedTo(TIME_PRECISION).equals(this.startTime)
                ? this
                : new Event(id, name, location, new_value, endTime);
    }

    public Event setEndTime(ZonedDateTime new_value) {
        return Objects.requireNonNull(new_value).truncatedTo(TIME_PRECISION).equals(this.endTime)
                ? this
                : new Event(id, name, location, startTime, new_value);
    }

    public Id getId() {
        return id;
    }

    public Coordinates getCoordinates(){
        return location.dropName();
    }

    public NamedCoordinates getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public Duration getDuration(){
        return Duration.between(startTime, endTime);
    }

    @Override
    public String toString() {
        return "Event" +  id +
                " - " + name +
                "(@" + location +
                ':' + startTime +
                "-" + endTime +
                ')';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return id.equals(event.id) &&
                name.equals(event.name) &&
                location.equals(event.location) &&
                startTime.equals(event.startTime) &&
                endTime.equals(event.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, location, startTime, endTime);
    }
}