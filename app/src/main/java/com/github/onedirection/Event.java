
package com.github.onedirection;

import java.util.Objects;

public class Event {

    final private int id;
    final private String name;
    final private String location;
    final private String date;
    final private String start_time;
    final private String end_time;

    public Event(int id,String name, String location, String date, String start_time, String end_time){
        this.name = Objects.requireNonNull(name);
        this.location = Objects.requireNonNull(location);
        this.date = Objects.requireNonNull(date);
        this.start_time = Objects.requireNonNull(start_time);
        this.end_time = Objects.requireNonNull(end_time);
        this.id = id;
    }

    public Event setName(String new_value){

        return Objects.requireNonNull(new_value) == this.name?this : new Event(id,new_value,location,date,start_time,end_time);
    }

    public Event setLocation(String new_value){

        return Objects.requireNonNull(new_value) == this.location? this: new Event(id,name,new_value,date,start_time,end_time);
    }

    public Event setDate(String new_value){

        return Objects.requireNonNull(new_value) == this.date? this:new Event(id,name,location,new_value,start_time,end_time);
    }

    public Event setStartTime(String new_value){

        return Objects.requireNonNull(new_value) == this.start_time? this: new Event(id,name,location,date,new_value,end_time);
    }

    public Event setEndTime(String new_value){

        return Objects.requireNonNull(new_value) == this.end_time? this:new Event(id,name,location,date,start_time,new_value);
    }

    public int getId(){
        return id;
    }

    public String getDate(){
        return date;
    }

    public String getLocation(){
        return location;
    }

    public String getName(){
        return name;
    }

    public String getStartTime(){
        return start_time;
    }

    public String getEndTime(){
        return end_time;
    }

}