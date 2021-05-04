package com.github.onedirection.navigation.fragment.calendar;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.github.onedirection.EventQueries;
import com.github.onedirection.R;
import com.github.onedirection.database.Database;
import com.github.onedirection.events.DayEventsListView;
import com.github.onedirection.events.Event;
import com.github.onedirection.events.EventCreator;
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

public class CustomCalendarView extends LinearLayout {

    private static final int MAX_CALENDAR_DAYS = 42;
    private final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.ENGLISH);
    private final Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH);
    private final SimpleDateFormat monthFormat = new SimpleDateFormat(("MMM"), Locale.ENGLISH);
    private final List<Date> dates = new ArrayList<>();
    private List<Event> eventsList = new ArrayList<>();
    private DayEventsListView dayEventsView = null;

    private Context context;
    private ImageButton nextButton, previousButton;
    private TextView CurrentDate;
    private GridView gridView;
    private AlertDialog alertDialog;


    public CustomCalendarView(Context context) {
        super(context);
    }

    public CustomCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initializeLayout();
        setUpCalendar();

        previousButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            setUpCalendar();
        });

        nextButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            setUpCalendar();
        });

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(true);
            View onDaySelectedPopup = LayoutInflater.from(parent.getContext()).inflate(R.layout.day_select_popup, null);
            Button addEvent = onDaySelectedPopup.findViewById(R.id.addEvent);
            Button viewEvents = onDaySelectedPopup.findViewById(R.id.viewEvents);
            int dateOfMonth = dateOfMonthAtPosition(position);
            LocalDate localDate = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, dateOfMonth);

            addEvent.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    callEventCreator(localDate);
                    alertDialog.cancel();
                }
            });
            viewEvents.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.cancel();
                    callDayEventsList(parent, localDate.atStartOfDay(ZoneId.systemDefault()));
                }
            });
            builder.setView(onDaySelectedPopup);
            alertDialog = builder.create();
            alertDialog.show();
        });
    }

    public CustomCalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void refreshCalendarView() {
        setUpCalendar();
    }

    public DayEventsListView getDayEventView(){
        return dayEventsView;
    }


    private void initializeLayout() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_calendar_view, this);
        nextButton = view.findViewById(R.id.nextBtn);
        previousButton = view.findViewById(R.id.previousBtn);
        CurrentDate = view.findViewById(R.id.currentDate);
        gridView = view.findViewById(R.id.gridView);
    }

    private void setUpCalendar() {
        String currentDate = dateFormat.format(calendar.getTime());
        CurrentDate.setText(currentDate);

        dates.clear();
        Calendar monthCalendar = (Calendar) calendar.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int FirstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        monthCalendar.add(Calendar.DAY_OF_MONTH, -FirstDayOfMonth);
        while (dates.size() < MAX_CALENDAR_DAYS) {
            dates.add(monthCalendar.getTime());
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        gridView.setAdapter(null);

        CompletableFuture<List<Event>> monthEventsFuture = collectEventsPerMonth(getMonthNumber(calendar), monthCalendar.get(Calendar.YEAR));
        LoadingDialog loadingDialog = startLoadingAnimation();
        monthEventsFuture.whenComplete((monthEvents, throwable) -> {
            eventsList = monthEvents;
            setUpGridView(getMonthNumber(calendar), monthCalendar.get(Calendar.YEAR));
            stopLoadingAnimation(loadingDialog);
        });
    }

    private CompletableFuture<List<Event>> collectEventsPerMonth(int monthNumber, int year) {
        Database database = Database.getDefaultInstance();
        EventQueries queryManager = new EventQueries(database);
        LocalDate localDate = LocalDate.of(year, monthNumber, 1);
        ZonedDateTime firstInstantOfMonth = localDate.atStartOfDay(ZoneId.systemDefault());
        CompletableFuture<List<Event>> monthEventsFuture = queryManager.getEventsByMonth(firstInstantOfMonth);
        return monthEventsFuture;
    }

    private void setUpGridView(int monthNumber, int year) {
        CalendarGridAdapter calendarGridAdapter = new CalendarGridAdapter(context, dates, calendar, eventsList);
        gridView.setAdapter(calendarGridAdapter);
    }

    private int dateOfMonthAtPosition(int positionInGrid) {
        Calendar monthCalendar = (Calendar) calendar.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int FirstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        monthCalendar.add(Calendar.DAY_OF_MONTH, -FirstDayOfMonth);
        monthCalendar.add(Calendar.DAY_OF_MONTH, positionInGrid);
        return monthCalendar.get(Calendar.DAY_OF_MONTH);
    }

    private void callEventCreator(LocalDate date) {
        Intent intent = new Intent(this.getContext(), EventCreator.class);
        EventCreator.putDateExtra(intent, date);
        this.getContext().startActivity(intent);
    }

    private void callDayEventsList(AdapterView parent, ZonedDateTime day) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        dayEventsView = new DayEventsListView(getContext(), day);
        builder.setView(dayEventsView);
        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(1000, 1200);
        alertDialog.setOnDismissListener(dialog -> refreshCalendarView());
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
}
