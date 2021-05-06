package com.github.onedirection.database.store;

import com.github.onedirection.events.Recurrence;
import com.github.onedirection.utils.TimeUtils;
import com.github.onedirection.events.Event;
import com.github.onedirection.geolocation.Coordinates;
import com.github.onedirection.utils.Id;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * A utility class that provides operations to store Events in a conventional way
 * This class uses the Singleton pattern, and the single instance of this class is used and passed to generic queries to
 * specify that these queries are done on Events.
 */
public class EventStorer extends Storer<Event> {

    private static final EventStorer GLOBAL = new EventStorer();
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_COORD_LATITUDE = "coordLat";
    public static final String KEY_COORD_LONGITUDE = "coordLong";
    public static final String KEY_COORD_NAME = "locName";
    public static final String KEY_EPOCH_START_TIME = "epochStartTime";
    public static final String KEY_EPOCH_END_TIME = "epochEndTime";
    public static final String KEY_RECURR_ID = "recurrId";
    public static final String KEY_RECURR_END_TIME = "recurrEndTime";
    public static final String KEY_RECURR_PERIOD = "recurrPeriod";

    private EventStorer() {
    }

    public static EventStorer getInstance() {
        return GLOBAL;
    }

    @Override
    public DatabaseCollection getCollection() {
        return DatabaseCollection.Event;
    }

    @Override
    public Class<Event> classTag() {
        return Event.class;
    }

    @Override
    public Map<String, Object> storableToMap(Event storable) {
        Objects.requireNonNull(storable, "Argument is null");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(KEY_ID, storable.getId().getUuid());
        map.put(KEY_NAME, storable.getName());
        storable.getLocation().ifPresent(loc -> {
            map.put(KEY_COORD_LATITUDE, loc.latitude);
            map.put(KEY_COORD_LONGITUDE, loc.longitude);
        });
        map.put(KEY_COORD_NAME, storable.getLocationName());
        map.put(KEY_EPOCH_START_TIME, storable.getStartTime().toEpochSecond());
        map.put(KEY_EPOCH_END_TIME, storable.getEndTime().toEpochSecond());
        storable.getRecurrence().ifPresent(recurrence -> {
            map.put(KEY_RECURR_ID, recurrence.getGroupId().getUuid());
            map.put(KEY_RECURR_PERIOD, recurrence.getPeriod().getSeconds());
            map.put(KEY_RECURR_END_TIME, recurrence.getEndTime().toEpochSecond());
        });
        return map;
    }

    @Override
    public Event mapToStorable(Map<String, Object> m) {
        Objects.requireNonNull(m, "Argument is null");

        String id = (String) m.get(KEY_ID);
        String name = (String) m.get(KEY_NAME);
        String locationName = (String) m.get(KEY_COORD_NAME);
        long epochStartTime = (long) Objects.requireNonNull(m.get(KEY_EPOCH_START_TIME));
        long epochEndTime = (long) Objects.requireNonNull(m.get(KEY_EPOCH_END_TIME));

        Double coordLatitude = (Double) m.getOrDefault(KEY_COORD_LATITUDE, null);
        Double coordLongitude = (Double) m.getOrDefault(KEY_COORD_LONGITUDE, null);
        Coordinates coords = coordLatitude == null || coordLongitude == null ? null : new Coordinates(coordLatitude, coordLongitude);
        String recurrId = (String) m.getOrDefault(KEY_RECURR_ID, null);
        Long recurrPeriod = (Long) m.getOrDefault(KEY_RECURR_PERIOD, null);
        Long recurrEpochEndTime = (Long) m.getOrDefault(KEY_RECURR_END_TIME, null);
        Recurrence recurrence = recurrId == null ? null : new Recurrence(new Id(UUID.fromString(recurrId)), Duration.ofSeconds(recurrPeriod),
                TimeUtils.epochToZonedDateTime(recurrEpochEndTime));

        return new Event(new Id(UUID.fromString(id)), name, locationName, Optional.ofNullable(coords),
                TimeUtils.epochToZonedDateTime(epochStartTime),
                TimeUtils.epochToZonedDateTime(epochEndTime), Optional.ofNullable(recurrence));
    }

}
