package com.github.onedirection;

import com.github.onedirection.geocoding.NamedCoordinates;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Event {

    final private int id;
    final private String name;
    final private NamedCoordinates location;
    final private ZonedDateTime startTime;
    final private ZonedDateTime endTime;

    public Event(int id, String name, NamedCoordinates location, ZonedDateTime startTime, ZonedDateTime endTime) {
        if (startTime.until(endTime, ChronoUnit.SECONDS) < 0) {
            throw new IllegalArgumentException("The end date should be later than the start date.");
        }

        this.name = Objects.requireNonNull(name);
        this.location = Objects.requireNonNull(location);
        this.startTime = Objects.requireNonNull(startTime);
        this.endTime = Objects.requireNonNull(endTime);
        this.id = id;
    }

    public Event setName(String new_value) {
        return Objects.requireNonNull(new_value).equals(this.name) ? this : new Event(id, new_value, location, startTime, endTime);
    }

    public Event setLocation(NamedCoordinates new_value) {
        return Objects.requireNonNull(new_value).equals(this.location) ? this : new Event(id, name, new_value, startTime, endTime);
    }

    public Event setStartTime(ZonedDateTime new_value) {
        return Objects.requireNonNull(new_value).equals(this.startTime) ? this : new Event(id, name, location, new_value, endTime);
    }

    public Event setEndTime(ZonedDateTime new_value) {
        return Objects.requireNonNull(new_value).equals(this.endTime) ? this : new Event(id, name, location, startTime, new_value);
    }

    public int getId() {
        return id;
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
}