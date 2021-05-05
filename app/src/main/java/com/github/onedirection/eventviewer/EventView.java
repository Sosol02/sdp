package com.github.onedirection.eventviewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.idling.CountingIdlingResource;

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

/**
 * To use to view a list of events, just start the activity
 */

public class EventView extends AppCompatActivity {

    RecyclerView eventList;
    EventViewerAdapter eventViewerAdapter;
    List<Event> events = new ArrayList<Event>();
    public static final String EXTRA_LIST_EVENT = "EVENT_LIST_ID";

    public static boolean hasEventListExtra(Intent intent) {
        return intent.hasExtra(EXTRA_LIST_EVENT);
    }

    /**
     * Extract the event list extra.
     *
     * @param intent The intent.
     * @return The contained event list.
     */
    public static ArrayList<Event> getEventListExtra(Intent intent) {
        return (ArrayList<Event>) (intent.getSerializableExtra(EXTRA_LIST_EVENT));
    }

    /**
     * Put an event extra for the Event creator.
     *
     * @param intent The intent which will carry the event.
     * @param eventList  The event list to put.
     * @return The passed intent.
     */
    public static Intent putEventExtra(Intent intent, List<Event> eventList) {
        return intent.putExtra(EXTRA_LIST_EVENT, new ArrayList<Event>(eventList));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if(hasEventListExtra(intent)){
            events = getEventListExtra(intent);
        }
        setContentView(R.layout.event_viewer);

        eventViewerAdapter = new EventViewerAdapter(events);
        eventList = (RecyclerView) findViewById(R.id.recyclerEventView);
        eventList.setAdapter(eventViewerAdapter);
        eventList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateResults(List<Event> events){
        eventList.setAdapter(new EventViewerAdapter(events));
    }

}