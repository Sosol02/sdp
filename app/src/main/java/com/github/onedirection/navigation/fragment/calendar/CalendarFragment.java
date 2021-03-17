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

import com.github.onedirection.R;
import com.skyhope.eventcalenderlibrary.CalenderEvent;
import com.skyhope.eventcalenderlibrary.model.Event;

import java.util.Objects;

public class CalendarFragment extends Fragment {

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
            if (dayContainerModel.isHaveEvent()){

            }
                showEventCreationScreen(dayContainerModel.getTimeInMillisecond()))};
    }

    public void addEventToCalendar(com.github.onedirection.Event event, long timeInMillis) {
        Objects.requireNonNull(event, "tried to add null Event");
        //Calendar calendar = Calendar.getInstance();
        //calendar.add(Calendar.DAY_OF_MONTH, 6);
        Event calendarEvent = new Event(timeInMillis, event.getName());
        calendarView.addEvent(calendarEvent);
    }

    public void removeEventFromCalendar(Event calendarEvent) {
        Objects.requireNonNull(calendarEvent, "tried to remove null calendarEvent");
        calendarView.removeEvent(calendarEvent);
    }

    public void showEventCreationScreen(long timeInMillis) {
        Intent intent = new Intent(this, oneDEventUI.class);
        intent.putExtra();
        startActivity(intent);
    }
}