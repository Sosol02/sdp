package com.github.onedirection;

class Event {

    final private int id;
    final private String name;
    final private String location;
    final private String date;
    final private String start_time;
    final private String end_time;


    public Event(int id,String name, String location, String date, String start_time, String end_time){
        if(name == null || location == null || date == null || start_time == null || end_time == null){
            throw new IllegalArgumentException();
        }
        this.id = id;
        this.name = name;
        this.location = location;
        this.date = date;
        this.start_time = start_time;
        this.end_time = end_time;
        //send information to the db?
    }

    public Event set_name(String new_value){
        if(new_value ==  null){
            throw new IllegalArgumentException();
        }
        if(new_value == this.name){
            return this;
        }
        return new Event(id,new_value,location,date,start_time,end_time);
    }

    public Event set_location(String new_value){
        if(new_value ==  null){
            throw new IllegalArgumentException();
        }
        if(new_value == this.location){
            return this;
        }
        return new Event(id,name,new_value,date,start_time,end_time);
    }

    public Event set_date(String new_value){
        if(new_value ==  null){
            throw new IllegalArgumentException();
        }
        if(new_value == this.date){
            return this;
        }
        return new Event(id,name,location,new_value,start_time,end_time);
    }

    public Event set_start_time(String new_value){
        if(new_value ==  null){
            throw new IllegalArgumentException();
        }
        if(new_value == this.start_time){
            return this;
        }
        return new Event(id,name,location,date,new_value,end_time);
    }

    public Event set_end_time(String new_value){
        if(new_value ==  null){
            throw new IllegalArgumentException();
        }
        if(new_value == this.end_time){
            return this;
        }
        return new Event(id,name,location,date,start_time,new_value);
    }

    public int get_id(){
        return id;
    }

    public String get_date(){
        return date;
    }

    public String get_location(){
        return location;
    }

    public String get_name(){
        return name;
    }

    public String get_start_time(){
        return start_time;
    }

    public String get_end_time(){
        return end_time;
    }

    public String serialize(){

        return "";
    }
}