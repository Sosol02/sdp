package com.github.onedirection.navigation.fragment.calendar;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.skyhope.eventcalenderlibrary.CalenderEvent;
import com.skyhope.eventcalenderlibrary.listener.CalenderDayClickListener;
import com.skyhope.eventcalenderlibrary.model.DayContainerModel;
import com.skyhope.eventcalenderlibrary.model.Event;


import com.github.onedirection.R;

import java.util.Calendar;

public class CalendarFragment extends Fragment {

    private CalendarViewModel mViewModel;
    private CalenderEvent calendarView;
    private String startDate, endDate;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar, container, false);
        calendarView = (CalenderEvent) root.findViewById(R.id.calendarView);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CalendarViewModel.class);
        calendarView.initCalderItemClickCallback(dayContainerModel -> showEventScreen(dayContainerModel.getTimeInMillisecond()));

    }

    public void addEventToCalendar(1DirectionEvent oneDEvent, long timeInMillis){
        //Calendar calendar = Calendar.getInstance();
        //calendar.add(Calendar.DAY_OF_MONTH, 6);
        Event event = new Event(timeInMillis, oneDEvent.title);
        calendarView.addEvent(event);


    }

    public void showEventScreen(long timeInMillis){
        Intent intent = new Intent(this, 1DEvent.class);
        intent.putExtra();
        startActivity(intent);
    }




}