package com.github.onedirection.eventviewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.github.onedirection.R;
import com.github.onedirection.events.Event;
import com.github.onedirection.events.EventCreator;
import com.github.onedirection.events.LocationsAdapter;
import com.github.onedirection.geolocation.NamedCoordinates;
import com.github.onedirection.notifs.Notifications;
import com.github.onedirection.utils.Id;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EventView extends AppCompatActivity {

    RecyclerView eventList;
    EventViewerAdapter eventViewerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.event_viewer);

        Id ID = Id.generateRandom();
        String NAME = "Event name";
        String LOCATION_NAME = "Location name";
        NamedCoordinates LOCATION = new NamedCoordinates(0, 0, LOCATION_NAME);
        ZonedDateTime START_TIME = ZonedDateTime.now().plusDays(1);
        ZonedDateTime END_TIME = ZonedDateTime.now().plusDays(2);
        Event e = new Event(ID, NAME, LOCATION, START_TIME, END_TIME);

        List<Event> events = new ArrayList<Event>();
        events.add(e);

        eventViewerAdapter = new EventViewerAdapter(events);

        eventList = (RecyclerView) findViewById(R.id.recyclerEventView);
        eventList.setLayoutManager(new LinearLayoutManager(this));
        eventList.setAdapter(eventViewerAdapter);
    }

    private void updateResults(List<Event> events){

        eventList.setAdapter(new EventViewerAdapter(events));
    }

}