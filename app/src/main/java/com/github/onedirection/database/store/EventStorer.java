package com.github.onedirection.database;


import android.os.Build;

import androidx.annotation.RequiresApi;

import com.github.onedirection.geocoding.NamedCoordinates;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventStorer extends Storer<Event> {

    private static final EventStorer GLOBAL = new EventStorer();
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_COORD_LATITUDE = "coordLatitude";
    public static final String KEY_COORD_LONGITUDE = "coordLongitude";
    public static final String KEY_COORD_NAME = "coordName";
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Map<String, Object> storableToMap(Event storable) {
        if(storable == null) {
            throw new IllegalArgumentException("argument is null");
        }

        String id = storable.getId().getUuid();
        String name = storable.getName();
        double coordLatitude = storable.getLocation().latitude;
        double coordLongitude = storable.getLocation().longitude;
        String coordName = storable.getLocation().name;
        long epochStartTime = storable.getStartTime().toEpochSecond();
        long epochEndTime = storable.getEndTime().toEpochSecond();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(KEY_ID, id);
        map.put(KEY_NAME, name);
        map.put(KEY_COORD_LATITUDE, coordLatitude);
        map.put(KEY_COORD_LONGITUDE, coordLongitude);
        map.put(KEY_COORD_NAME, coordName);
        map.put(KEY_EPOCH_START_TIME, epochStartTime);
        map.put(KEY_EPOCH_END_TIME, epochEndTime);

        return map;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Event mapToStorable(Map m) {
        if(m == null) {
            throw new IllegalArgumentException("argument is null");
        }
        String id = (String) m.get(KEY_ID);
        String name = (String) m.get(KEY_NAME);
        double coordLatitude = (double) m.get(KEY_COORD_LATITUDE);
        double coordLongitude = (double) m.get(KEY_COORD_LONGITUDE);
        String coordName = (String) m.get(KEY_COORD_NAME);
        long epochStartTime = (long) m.get(KEY_EPOCH_START_TIME);
        long epochEndTime = (long) m.get(KEY_EPOCH_END_TIME);

        return new Event(new Id(UUID.fromString(id)), name, new NamedCoordinates(coordLatitude, coordLongitude, coordName),
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochStartTime), ZoneId.systemDefault()),
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochEndTime), ZoneId.systemDefault()));
    }

}
