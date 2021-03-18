
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
        assertThrows(NullPointerException.class, () -> event.setName(null));
        Event eventChanged = event.setName("changed_parameter");
        assertEquals("changed_parameter",eventChanged.getName());
        eventChanged.setName("changed_parameter");
        assertEquals("changed_parameter",eventChanged.getName());
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
        assertThrows(NullPointerException.class, () -> event.setLocation(null));
        Event eventChanged = event.setLocation("changed_parameter");
        assertEquals("changed_parameter",eventChanged.getLocation());
        eventChanged.setLocation("changed_parameter");
        assertEquals("changed_parameter",eventChanged.getLocation());
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
        assertThrows(NullPointerException.class, () -> event.setDate(null));
        Event eventChanged = event.setDate("changed_parameter");
        assertEquals("changed_parameter",eventChanged.getDate());
        eventChanged.setDate("changed_parameter");
        assertEquals("changed_parameter",eventChanged.getDate());
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
        assertThrows(NullPointerException.class, () -> event.setStartTime(null));
        Event eventChanged = event.setStartTime("changed_parameter");
        assertEquals("changed_parameter",eventChanged.getStartTime());
        eventChanged.setStartTime("changed_parameter");
        assertEquals("changed_parameter",eventChanged.getStartTime());
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
        assertThrows(NullPointerException.class, () -> event.setEndTime(null));
        Event eventChanged = event.setEndTime("changed_parameter");
        assertEquals("changed_parameter",eventChanged.getEndTime());
    }
}