package com.github.onedirection;

import org.junit.Test;

import static org.junit.Assert.*;


public class EventTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testEventWithNullArgument(){
        int id = 001;
        String name = "name";
        String location = "location";
        String date = "date";
        String start_time = "start_time";
        String end_time = "end_time";

        assertThrows(NullPointerException.class,() -> new Event(id,null,location,date,start_time,end_time));
        assertThrows(NullPointerException.class,() -> new Event(id,name,null,date,start_time,end_time));
        assertThrows(NullPointerException.class,() -> new Event(id,name,location,null,start_time,end_time));
        assertThrows(NullPointerException.class,() -> new Event(id,name,location,date,null,end_time));
        assertThrows(NullPointerException.class,() -> new Event(id,name,location,date,start_time,null));
    }

    @Test
    public void testEventSetNameAndGet(){
        int id = 001;
        String name = "name";
        String location = "location";
        String date = "date";
        String start_time = "start_time";
        String end_time = "end_time";
        Event event = new Event(id,name,location,date,start_time,end_time);
        assertThrows(NullPointerException.class, () -> event.set_name(null));
        Event eventChanged = event.set_name("changed_parameter");
        assertEquals("changed_parameter",eventChanged.get_name());
        eventChanged.set_name("changed_parameter");
        assertEquals("changed_parameter",eventChanged.get_name());
    }

    @Test
    public void testEventSetLocationAndGet(){
        int id = 001;
        String name = "name";
        String location = "location";
        String date = "date";
        String start_time = "start_time";
        String end_time = "end_time";
        Event event = new Event(id,name,location,date,start_time,end_time);
        assertThrows(NullPointerException.class, () -> event.set_location(null));
        Event eventChanged = event.set_location("changed_parameter");
        assertEquals("changed_parameter",eventChanged.get_location());
        eventChanged.set_location("changed_parameter");
        assertEquals("changed_parameter",eventChanged.get_location());
    }

    @Test
    public void testEventSetDateAndGet(){
        int id = 001;
        String name = "name";
        String location = "location";
        String date = "date";
        String start_time = "start_time";
        String end_time = "end_time";
        Event event = new Event(id,name,location,date,start_time,end_time);
        assertThrows(NullPointerException.class, () -> event.set_date(null));
        Event eventChanged = event.set_date("changed_parameter");
        assertEquals("changed_parameter",eventChanged.get_date());
        eventChanged.set_date("changed_parameter");
        assertEquals("changed_parameter",eventChanged.get_date());
    }

    @Test
    public void testEventSetStartTimeAndGet(){
        int id = 001;
        String name = "name";
        String location = "location";
        String date = "date";
        String start_time = "start_time";
        String end_time = "end_time";
        Event event = new Event(id,name,location,date,start_time,end_time);
        assertThrows(NullPointerException.class, () -> event.set_start_time(null));
        Event eventChanged = event.set_start_time("changed_parameter");
        assertEquals("changed_parameter",eventChanged.get_start_time());
        eventChanged.set_start_time("changed_parameter");
        assertEquals("changed_parameter",eventChanged.get_start_time());
    }

    @Test
    public void testEventSetEndTimeAndGet(){
        int id = 001;
        String name = "name";
        String location = "location";
        String date = "date";
        String start_time = "start_time";
        String end_time = "end_time";
        Event event = new Event(id,name,location,date,start_time,end_time);
        assertThrows(NullPointerException.class, () -> event.set_end_time(null));
        Event eventChanged = event.set_end_time("changed_parameter");
        assertEquals("changed_parameter",eventChanged.get_end_time());
    }
}