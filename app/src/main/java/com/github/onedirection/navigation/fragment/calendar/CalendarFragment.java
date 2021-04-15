package com.github.onedirection.navigation.fragment.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.onedirection.EventQueries;
import com.github.onedirection.database.Database;
import com.github.onedirection.events.EventCreator;
import com.github.onedirection.R;
import com.skyhope.eventcalenderlibrary.CalenderEvent;
import com.skyhope.eventcalenderlibrary.model.Event;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;

public class CalendarFragment extends Fragment {

    private final String EXTRA_MESSAGE_DATE = "DATE";
    private CalendarViewModel mViewModel;
    private CalenderEvent calendarView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar, container, false);
        calendarView = root.findViewById(R.id.calendarView);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CalendarViewModel.class);
        calendarView.initCalderItemClickCallback(dayContainerModel -> {
            DaySelectPopupFragment popup = new DaySelectPopupFragment(this, dayContainerModel.getDay(), dayContainerModel.getMonthNumber(), dayContainerModel.getYear());
            popup.show(getChildFragmentManager(), getResources().getString(R.string.day_select_dialog_tag));
        });

        EventQueries.getEventsByMonth(Database.getDefaultInstance(), ZonedDateTime.now()).thenAccept(events -> {
           for(com.github.onedirection.events.Event event : events){
               addEventToCalendar(event);
               // TODO: Check with Paul why nothing is display
           }
        });
    }

    public void addEventToCalendar(com.github.onedirection.events.Event event) {
        Objects.requireNonNull(event, "Tried to add null Event");
        Event calendarEvent = new Event(event.getStartTime().toInstant().toEpochMilli(), event.getName());
        calendarView.addEvent(calendarEvent);
    }

    public void removeEventFromCalendar(Event calendarEvent) {
        Objects.requireNonNull(calendarEvent, "Tried to remove null calendarEvent");
        calendarView.removeEvent(calendarEvent);
    }

    public void showEventCreationScreen(int day, int month, int year) {
        Intent intent = new Intent(getActivity(), EventCreator.class);
        EventCreator.putDateExtra(intent, LocalDate.of(year, month, day));
        startActivity(intent);
    }
}