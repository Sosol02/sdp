package com.github.onedirection;

import com.github.onedirection.geocoding.NamedCoordinates;

import java.util.Date;
import java.util.Objects;

public class Event {

    final private int id;
    final private String name;
    final private NamedCoordinates location;
    final private Date startTime;
    final private Date endTime;

    public Event(int id, String name, NamedCoordinates location, Date startTime, Date endTime) {
        if(endTime.before(startTime)){
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

    public Event setStartTime(Date new_value) {
        return Objects.requireNonNull(new_value).equals(this.startTime) ? this : new Event(id, name, location, new_value, endTime);
    }

    public Event setEndTime(Date new_value) {
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

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }
}