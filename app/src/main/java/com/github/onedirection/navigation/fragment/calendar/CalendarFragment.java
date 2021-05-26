package com.github.onedirection.navigation.fragment.calendar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.github.onedirection.R;
import com.github.onedirection.database.Database;
import com.github.onedirection.database.queries.EventQueries;
import com.github.onedirection.event.Event;
import com.github.onedirection.event.ui.EventCreator;
import com.github.onedirection.utils.LoadingDialog;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * The fragment displaying the elements related to the calendar of the app
 */
public class CalendarFragment extends Fragment {



    private static final int MAX_CALENDAR_DAYS = 42;

    private final Calendar calendar = Calendar.getInstance(Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH);
    private final List<Date> dates = new ArrayList<>();
    private List<Event> eventsList = new ArrayList<>();

    private View calendarView;
    private CalendarGridAdapter calendarGridAdapter;
    private DayEventsListView eventsListView;

    private final CountingIdlingResource idling = new CountingIdlingResource("Calendar events are loading.");


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar, container, false);
        calendarView = root.findViewById(R.id.calendarView);

        setupGeneral();
        setupGrid();



        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        calendarGridAdapter = null;
        setupGrid();

        if(eventsListView != null){
            eventsListView.updateEventsList();
        }
    }


    private void setupGeneral() {
        ImageButton nextButton = calendarView.findViewById(R.id.nextBtn);
        ImageButton previousButton = calendarView.findViewById(R.id.previousBtn);

        previousButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            setupGrid();
        });

        nextButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            setupGrid();
        });
    }



    private void setupGrid() {
        TextView currentDateView = calendarView.findViewById(R.id.currentDate);
        GridView gridView = calendarView.findViewById(R.id.gridView);

        String currentDate = dateFormat.format(calendar.getTime());
        currentDateView.setText(currentDate);

        dates.clear();
        Calendar monthCalendar = (Calendar) calendar.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int FirstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK) -1;
        monthCalendar.add(Calendar.DAY_OF_MONTH, -FirstDayOfMonth);
        while (dates.size() < MAX_CALENDAR_DAYS) {
            dates.add(monthCalendar.getTime());
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        idling.increment();
        CompletableFuture<List<Event>> monthEventsFuture = collectEventsPerMonth(getMonthNumber(calendar), calendar.get(Calendar.YEAR));
        LoadingDialog loadingDialog = startLoadingAnimation();
        monthEventsFuture.whenComplete((monthEvents, throwable) -> {
            eventsList = monthEvents;
            calendarGridAdapter = new CalendarGridAdapter(getContext(), dates, calendar, eventsList);
            gridView.setAdapter(calendarGridAdapter);
            stopLoadingAnimation(loadingDialog);
            idling.decrement();
        });

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            LocalDate date = dateAtPosition(position);
            List<Event> dayEvents = dayEventsList(date);
            if(dayEvents.size() == 0){
                callEventCreator(date);
            } else {
                setupAlertDialog(date, dayEvents);
            }
        });
    }

    private void setupAlertDialog(LocalDate localDate, List<Event> dayEvents){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        View onDaySelectedPopup = LayoutInflater.from(getContext()).inflate(R.layout.day_select_popup, null);
        Button addEvent = onDaySelectedPopup.findViewById(R.id.addEvent);
        Button viewEvents = onDaySelectedPopup.findViewById(R.id.viewEvents);
        builder.setView(onDaySelectedPopup);

        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        addEvent.setOnClickListener(v -> {
            callEventCreator(localDate);
            alertDialog.cancel();
        });

        viewEvents.setOnClickListener(v -> {
            alertDialog.cancel();
            ZonedDateTime zonedDate = localDate.atStartOfDay(ZoneId.systemDefault());
            eventsListView = new DayEventsListView(getContext(), zonedDate, dayEvents, () -> refreshGrid(), idling);
        });

        alertDialog.show();
    }

    private CompletableFuture<List<Event>> collectEventsPerMonth(int monthNumber, int year) {
        Database database = Database.getDefaultInstance();
        EventQueries queryManager = new EventQueries(database);
        LocalDate localDate = LocalDate.of(year, monthNumber, 1);
        ZonedDateTime firstInstantOfMonth = localDate.atStartOfDay(ZoneId.systemDefault());
        return queryManager.getEventsByMonth(firstInstantOfMonth);
    }



    private LocalDate dateAtPosition(int dayPositionInGrid) {
        Calendar monthCalendar = (Calendar) calendar.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int FirstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        monthCalendar.add(Calendar.DAY_OF_MONTH, -FirstDayOfMonth);
        monthCalendar.add(Calendar.DAY_OF_MONTH, dayPositionInGrid);
        int dateOfMonth = monthCalendar.get(Calendar.DAY_OF_MONTH);
        LocalDate localDate = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, dateOfMonth);
        return localDate;
    }

    private void callEventCreator(LocalDate date) {
        Intent intent = new Intent(this.getContext(), EventCreator.class);
        EventCreator.putDateExtra(intent, date);
        this.getContext().startActivity(intent);
    }

    private List<Event> dayEventsList(LocalDate localDate) {
        List<Event> dayEvents = new ArrayList<>();
        for(Event event : eventsList){
            if(event.getStartTime().getDayOfYear() == localDate.getDayOfYear() &&
                    event.getStartTime().getYear() == localDate.getYear()) {
                dayEvents.add(event);
            }
        }
        return dayEvents;
    }

    private void refreshGrid() {
        setupGrid();
    }


    private int getMonthNumber(Calendar cal) {
        return cal.get(Calendar.MONTH) + 1;
    }


    private LoadingDialog startLoadingAnimation() {
        LoadingDialog loadingDialog = new LoadingDialog(this.getContext());
        loadingDialog.startLoadingAnimation();
        return loadingDialog;
    }

    private void stopLoadingAnimation(LoadingDialog loadingDialog) {
        loadingDialog.dismissDialog();
    }

    @VisibleForTesting
    public CountingIdlingResource getIdlingResource() {
        return idling;
    }
}

