package com.github.onedirection.eventviewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.github.onedirection.EventQueries;
import com.github.onedirection.R;
import com.github.onedirection.database.ConcreteDatabase;
import com.github.onedirection.events.Event;
import com.github.onedirection.events.EventCreator;
import com.github.onedirection.events.LocationsAdapter;
import com.github.onedirection.geolocation.NamedCoordinates;
import com.github.onedirection.notifs.Notifications;
import com.github.onedirection.utils.Id;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class EventView extends AppCompatActivity {

    RecyclerView eventList;
    EventViewerAdapter eventViewerAdapter;
    List<Event> events = new ArrayList<Event>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.event_viewer);


        ConcreteDatabase database = ConcreteDatabase.getDatabase();
        EventQueries queryManager = new EventQueries(database);
        ZonedDateTime firstInstantOfMonth = ZonedDateTime.of(2021, 4, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        CompletableFuture<List<Event>> monthEventsFuture = queryManager.getEventsByMonth(firstInstantOfMonth);
        monthEventsFuture.whenComplete((monthEvents, throwable) -> {
            events = monthEvents;
            updateResults(monthEvents);
        });
        eventViewerAdapter = new EventViewerAdapter(events);
        eventList = (RecyclerView) findViewById(R.id.recyclerEventView);
        eventList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateResults(List<Event> events){
        eventList.setAdapter(new EventViewerAdapter(events));
    }

}