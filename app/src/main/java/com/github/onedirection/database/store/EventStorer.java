package com.github.onedirection.database.store;


import android.os.Build;

import androidx.annotation.RequiresApi;

import com.github.onedirection.Event;
import com.github.onedirection.geocoding.Coordinates;
import com.github.onedirection.utils.Id;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EventStorer extends Storer<Event> {

    private static final EventStorer GLOBAL = new EventStorer();
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_COORD_LATITUDE = "coordLatitude";
    public static final String KEY_COORD_LONGITUDE = "coordLongitude";
    public static final String KEY_COORD_NAME = "locationName";
    public static final String KEY_EPOCH_START_TIME = "epochStartTime";
    public static final String KEY_EPOCH_END_TIME = "epochEndTime";

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

        return map;
    }

    @Override
    public Event mapToStorable(Map<String, Object> m) {
        Objects.requireNonNull(m, "Argument is null");

        String id = (String) m.get(KEY_ID);
        String name = (String) m.get(KEY_NAME);
        Double coordLatitude = (Double) m.getOrDefault(KEY_COORD_LATITUDE, null);
        Double coordLongitude = (Double) m.getOrDefault(KEY_COORD_LONGITUDE, null);
        String locationName = (String) m.get(KEY_COORD_NAME);
        long epochStartTime = (long) Objects.requireNonNull(m.get(KEY_EPOCH_START_TIME));
        long epochEndTime = (long) Objects.requireNonNull(m.get(KEY_EPOCH_END_TIME));

        if(coordLatitude == null || coordLongitude == null) {
            return new Event(new Id(UUID.fromString(id)), name, locationName,
                    ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochStartTime), ZoneId.systemDefault()),
                    ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochEndTime), ZoneId.systemDefault()));
        } else {
            return new Event(new Id(UUID.fromString(id)), name, locationName, new Coordinates(coordLatitude, coordLongitude),
                    ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochStartTime), ZoneId.systemDefault()),
                    ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochEndTime), ZoneId.systemDefault()));
        }
    }

}
