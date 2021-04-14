package com.github.onedirection.navigation.fragment.calendar;

import com.github.onedirection.utils.Id;

import java.time.LocalDate;

public class CalendarEvents {
    private String name;
    private LocalDate date;
    private Id id;

    public CalendarEvents(String name, LocalDate date, Id id) {
        this.name = name;
        this.date = date;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }
}
