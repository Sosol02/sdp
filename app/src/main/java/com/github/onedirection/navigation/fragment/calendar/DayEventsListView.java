package com.github.onedirection.navigation.fragment.calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.github.onedirection.EventQueries;
import com.github.onedirection.R;
import com.github.onedirection.database.Database;
import com.github.onedirection.events.Event;
import com.github.onedirection.events.EventsListAdapter;
import com.github.onedirection.utils.LoadingDialog;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressLint("ViewConstructor")
public class DayEventsListView extends LinearLayout {
    private final Context context;
    private final ZonedDateTime day;
    private final LayoutInflater inflater;
    private View view;
    private CountingIdlingResource idling;


    public DayEventsListView(Context context, ZonedDateTime day){
        super(context);
        this.context = context;
        this.day = day;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        refreshView();
    }

    public DayEventsListView(Context context, ZonedDateTime day, CountingIdlingResource idling){
        super(context);
        this.context = context;
        this.day = day;
        this.idling = idling;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        refreshView();
    }

    public void refreshView(){
        if(idling != null){
            idling.increment();
        }
        LoadingDialog loadingDialog = new LoadingDialog(context);
        loadingDialog.startLoadingAnimation();
        CompletableFuture<List<Event>> dayEvents = getDayEvents(day);
        dayEvents.whenComplete((events, throwable) -> {
            loadingDialog.dismissDialog();
            if(((List<Event>)events).size() == 0){
                view = inflater.inflate(R.layout.empty_event_list_screen, this);
            } else {
                view = inflater.inflate(R.layout.day_events_list, this);
                setupEventsListView((List<Event>) events, view);
            }
            if(idling != null){
                idling.decrement();
            }
        });
    }


    private CompletableFuture<List<Event>> getDayEvents(ZonedDateTime day){
        Database database = Database.getDefaultInstance();
        EventQueries queryManager = new EventQueries(database);
        return queryManager.getEventsByDay(day);
    }


    private void setupEventsListView(List<Event> dayEvents, View view){
        ListView eventsListView = view.findViewById(R.id.dayEventsList);
        EventsListAdapter eventsListAdapter = new EventsListAdapter(getContext(), dayEvents, this::onEditEvent, this::onDeleteEvent);
        eventsListView.setAdapter(eventsListAdapter);
    }

    private void onEditEvent(){
        refreshView();
    }

    private void onDeleteEvent(){
        refreshView();
    }

}
