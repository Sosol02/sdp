package com.github.onedirection;

import com.github.onedirection.database.Database;

import java.time.ZonedDateTime;
import java.util.List;

public class EventQueries {

    private Database db;

    public EventQueries(Database db) {
        this.db = db;
    }

    public List<Event> getEventsByDay(ZonedDateTime day) {
        //TODO
        return null;
    }

    public List<Event> getEventsByWeek() {
        //TODO
        return null;
    }

    public List<Event> getEventsByMonth() {
        //TODO
        return null;
    }


}
