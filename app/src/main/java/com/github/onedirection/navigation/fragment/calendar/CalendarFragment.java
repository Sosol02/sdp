package com.github.onedirection.navigation.fragment.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.github.onedirection.R;

/**
 * The fragment displaying the elements related to the calendar of the app
 */
public class CalendarFragment extends Fragment {

    private final String EXTRA_MESSAGE_DATE = "DATE";
    private CalendarViewModel mViewModel;
    private CustomCalendarView calendarView;

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
    }

    @Override
    public void onResume() {
        super.onResume();
        calendarView.refreshCalendarView();
        DayEventsListView eventsListView = calendarView.getDayEventView();
        if(eventsListView != null){
            eventsListView.refreshView();
        }
    }

    @VisibleForTesting
    public CountingIdlingResource getIdlingResource(){
        return calendarView.getIdlingResource();
    }
}