package com.github.onedirection.navigation.fragment.calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.github.onedirection.R;
import com.github.onedirection.database.Database;
import com.github.onedirection.database.queries.EventQueries;
import com.github.onedirection.event.Event;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A view displaying the list of events for a given day
 */
@SuppressLint("ViewConstructor")


public class DayEventsListView extends LinearLayout {

    private final static int WINDOW_LAYOUT_WIDTH = 1000;
    private final static int WINDOW_LAYOUT_HEIGHT = 1200;
    private final Context context;
    private final ZonedDateTime day;
    private List<Event> dayEvents;
    private final LayoutInflater inflater;
    private final View view;
    private final CountingIdlingResource idling;
    private final Runnable onDialogDismiss;
    private AlertDialog alertDialog;
    private EventsListAdapter eventsListAdapter;

    public DayEventsListView(Context context, ZonedDateTime day, List<Event> dayEvents, Runnable onDialogDismiss, CountingIdlingResource idling) {
        super(context);
        this.context = context;
        this.day = day;
        this.dayEvents = dayEvents;
        this.onDialogDismiss = onDialogDismiss;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.view = inflater.inflate(R.layout.day_events_list, this);
        this.idling = idling;

        setupEventsListView(view);
        setupDialog();
    }

    public void updateEventsList(){
        if(idling != null){
            idling.increment();
        }
        CompletableFuture<List<Event>> newEventsList = getDayEvents(day);
        newEventsList.whenComplete((events, throwable) -> {
            dayEvents.clear();
            dayEvents.addAll(events);
            eventsListAdapter.notifyDataSetChanged();

            if(idling != null){
                idling.decrement();
            }
        });
    }

    private void setupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setView(view);
        alertDialog = builder.create();
        if (onDialogDismiss != null) {
            alertDialog.setOnDismissListener(dialog -> onDialogDismiss.run());
        }
        alertDialog.show();
        alertDialog.getWindow().setLayout(WINDOW_LAYOUT_WIDTH, WINDOW_LAYOUT_HEIGHT);
    }

    private CompletableFuture<List<Event>> getDayEvents(ZonedDateTime day) {
        Database database = Database.getDefaultInstance();
        EventQueries queryManager = new EventQueries(database);
        return queryManager.getEventsByDay(day);
    }

    private void setupEventsListView(View view) {
        ListView eventsListView = view.findViewById(R.id.dayEventsList);
        eventsListAdapter = new EventsListAdapter(getContext(), dayEvents, this::onDeleteEvent);
        eventsListView.setAdapter(eventsListAdapter);
    }


    private void onDeleteEvent() {
        if(eventsListAdapter.getCount() == 0){
            alertDialog.dismiss();
        }
    }
}
