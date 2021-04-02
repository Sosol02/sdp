package com.github.onedirection;


import com.github.onedirection.navigation.fragment.calendar.CalendarFragment;

import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class CalendarFragmentTest {

    CalendarFragment calendar = new CalendarFragment();

    @Test
    public void addEventToCalendarRefusesNullArgTest(){
        assertThrows(NullPointerException.class, () -> calendar.addEventToCalendar(null, 100));
    }

    public void removeEventFromCalendarRefusesNullArgTest(){
        assertThrows(NullPointerException.class, () -> calendar.removeEventFromCalendar(null));
    }
}

