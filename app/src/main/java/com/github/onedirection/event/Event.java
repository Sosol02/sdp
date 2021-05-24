package com.github.onedirection.event;

import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.database.store.Storable;
import com.github.onedirection.database.store.Storer;
import com.github.onedirection.geolocation.Coordinates;
import com.github.onedirection.geolocation.NamedCoordinates;
import com.github.onedirection.utils.Id;

import java.io.Serializable;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Event implements Serializable, Storable<Event> {

    final private Id id;
    final private String name;
    final private String locationName;
    final private Coordinates location;
    final private ZonedDateTime startTime;
    final private ZonedDateTime endTime;
    private boolean favorite;
    /**
     * A recurrence period of the event. This object contains references to the previous and the next event in the recurrence series, as well as the recurrence period.
     */
    final private Recurrence recurringPeriod;

    /**
     * Smallest time unit recorded inside the event.
     */
    final public static ChronoUnit TIME_PRECISION = ChronoUnit.MINUTES;

    public Event(Id id, String name, String locationName, Optional<Coordinates> location, ZonedDateTime startTime, ZonedDateTime endTime, Optional<Recurrence> recurringPeriod, boolean favorite) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.locationName = Objects.requireNonNull(locationName);
        this.location = Objects.requireNonNull(location).orElse(null);
        this.startTime = Objects.requireNonNull(startTime).truncatedTo(TIME_PRECISION);
        this.endTime = Objects.requireNonNull(endTime).truncatedTo(TIME_PRECISION);
        this.recurringPeriod = Objects.requireNonNull(recurringPeriod).orElse(null);
        if (this.startTime.until(this.endTime, TIME_PRECISION) < 0) {
            throw new IllegalArgumentException("The end date should be later than the start date.");
        }
        this.favorite = favorite;
    }

    public Event(Id id, String name, String locationName, Coordinates location, ZonedDateTime startTime, ZonedDateTime endTime, Optional<Recurrence> recurringPeriod, boolean favorite) {
        this(id, name, locationName, Optional.of(location), startTime, endTime, recurringPeriod, favorite);
    }

    public Event(Id id, String name, String locationName, ZonedDateTime startTime, ZonedDateTime endTime, Optional<Recurrence> recurringPeriod, boolean favorite) {
        this(id, name, locationName, Optional.empty(), startTime, endTime, recurringPeriod, favorite);
    }

    public Event(Id id, String name, String locationName, Coordinates location, ZonedDateTime startTime, ZonedDateTime endTime, boolean favorite) {
        this(id, name, locationName, Optional.of(location), startTime, endTime, Optional.empty(), favorite);
    }

    public Event(Id id, String name, String locationName, ZonedDateTime startTime, ZonedDateTime endTime, boolean favorite) {
        this(id, name, locationName, Optional.empty(), startTime, endTime, Optional.empty(), favorite);
    }

    public Event(Id id, String name, String locationName, Coordinates location, ZonedDateTime startTime, ZonedDateTime endTime, Recurrence recurringPeriod, boolean favorite) {
        this(id, name, locationName, Optional.of(location), startTime, endTime, Optional.of(recurringPeriod), favorite);
    }

    public Event(Id id, String name, String locationName, ZonedDateTime startTime, ZonedDateTime endTime, Recurrence recurringPeriod, boolean favorite) {
        this(id, name, locationName, Optional.empty(), startTime, endTime, Optional.of(recurringPeriod), favorite);
    }

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
    public Event(Id id, String name, NamedCoordinates location, ZonedDateTime startTime, ZonedDateTime endTime, boolean favorite) throws IllegalArgumentException {
        this(id, name, Objects.requireNonNull(location).name, Optional.of(location.dropName()), startTime, endTime, Optional.empty(), favorite);
    }

    public Event(Id id, String name, NamedCoordinates location, ZonedDateTime startTime, ZonedDateTime endTime, Recurrence recurringPeriod, boolean favorite) throws IllegalArgumentException {
        this(id, name, Objects.requireNonNull(location).name, Optional.of(location.dropName()), startTime, endTime, Optional.of(recurringPeriod), favorite);
    }

    public Event(Id id, String name, NamedCoordinates location, ZonedDateTime startTime, ZonedDateTime endTime, Optional<Recurrence> recurringPeriod, boolean favorite) throws IllegalArgumentException {
        this(id, name, Objects.requireNonNull(location).name, Optional.of(location.dropName()), startTime, endTime, recurringPeriod, favorite);
    }

    public Event setName(String new_value) {
        return Objects.requireNonNull(new_value).equals(this.name) ? this : new Event(id, new_value, locationName, Optional.ofNullable(location), startTime, endTime, Optional.ofNullable(recurringPeriod), favorite);
    }

    public Event setLocation(NamedCoordinates new_value) {
        return Optional.of(Objects.requireNonNull(new_value)).equals(getLocation()) ? this : new Event(id, name, new_value, startTime, endTime, Optional.ofNullable(recurringPeriod), favorite);
    }

    public Event setStartTime(ZonedDateTime new_value) {
        return Objects.requireNonNull(new_value).truncatedTo(TIME_PRECISION).equals(this.startTime)
                ? this
                : new Event(id, name, locationName, Optional.ofNullable(location), new_value, endTime, Optional.ofNullable(recurringPeriod), favorite);
    }

    public Event setEndTime(ZonedDateTime new_value) {
        return Objects.requireNonNull(new_value).truncatedTo(TIME_PRECISION).equals(this.endTime)
                ? this
                : new Event(id, name, locationName,  Optional.ofNullable(location), startTime, new_value, Optional.ofNullable(recurringPeriod), favorite);
    }

    public Event setRecurrence(Recurrence period) {
        return Optional.of(Objects.requireNonNull(period)).equals(getRecurrence())
                ? this
                : new Event(id, name, locationName, Optional.ofNullable(location),  startTime, endTime, Optional.of(period), favorite);
    }

    public Event setFavorite(boolean new_value){
        return Objects.requireNonNull(new_value).equals(this.favorite) ? this :  new Event(id, name, locationName, Optional.ofNullable(location), startTime, endTime, Optional.ofNullable(recurringPeriod), new_value);
    }



    @Override
    public Id getId() {
        return id;
    }

    @Override
    public Storer<Event> storer() {
        return EventStorer.getInstance();
    }

    public Optional<Coordinates> getCoordinates() {
        return Optional.ofNullable(location);
    }

    public String getLocationName(){
        return locationName;
    }

    public Optional<NamedCoordinates> getLocation() {
        return getCoordinates().map(coordinates -> coordinates.addName(locationName));
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

    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }

    public boolean isRecurrent() {
        return getRecurrence().isPresent();
    }

    public Optional<Recurrence> getRecurrence() {
        return Optional.ofNullable(recurringPeriod);
    }

    public boolean getFavorite(){ return favorite;}

    @Override
    public String toString() {
        return "Event" + id +
                " - " + name +
                "(@" + locationName +
                (location == null ? "" : "[" + location + "]") +
                ':' + startTime +
                "-" + endTime +
                ')' + (recurringPeriod == null ? "" : " recurrence: " + recurringPeriod)
                + (favorite? "favorite event":"");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return id.equals(event.id) &&
                name.equals(event.name) &&
                locationName.equals(event.locationName) &&
                getLocation().equals(event.getLocation()) &&
                startTime.equals(event.startTime) &&
                endTime.equals(event.endTime) &&
                getRecurrence().equals(event.getRecurrence())
                && (favorite  == event.getFavorite());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, locationName, location, startTime, endTime, recurringPeriod, favorite);
    }
}