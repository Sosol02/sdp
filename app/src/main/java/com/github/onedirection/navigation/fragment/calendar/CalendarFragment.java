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

import com.github.onedirection.EventCreator;
import com.github.onedirection.R;
import com.skyhope.eventcalenderlibrary.CalenderEvent;
import com.skyhope.eventcalenderlibrary.model.Event;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
            DaySelectPopupFragment popup = new DaySelectPopupFragment(this, dayContainerModel.getTimeInMillisecond());
            popup.show(getChildFragmentManager(), getResources().getString(R.string.day_select_dialog_tag));
        });
    }

    public void addEventToCalendar(com.github.onedirection.Event event, long timeInMillis) {
        Objects.requireNonNull(event, "tried to add null Event");
        Event calendarEvent = new Event(timeInMillis, event.getName());
        calendarView.addEvent(calendarEvent);
    }

    public void removeEventFromCalendar(Event calendarEvent) {
        Objects.requireNonNull(calendarEvent, "tried to remove null calendarEvent");
        calendarView.removeEvent(calendarEvent);
    }

    public void showEventCreationScreen(long timeInMillis) {
        Intent intent = new Intent(getActivity(), EventCreator.class);
        Instant instant = Instant.ofEpochMilli(timeInMillis);
        intent.putExtra(EXTRA_MESSAGE_DATE, instant.truncatedTo(ChronoUnit.DAYS));
        startActivity(intent);
    }
}