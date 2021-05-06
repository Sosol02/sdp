package com.github.onedirection.eventviewer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.idling.CountingIdlingResource;

import android.content.Intent;
import android.graphics.Color;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * To use to view a list of events, just start the activity
 */

public class EventView extends AppCompatActivity implements EventViewerAdapter.OnNoteListener{

    /*private final static Id ID = Id.generateRandom();
    private final static String NAME = "Event name";
    private final static String LOCATION_NAME = "Location name";
    private final static NamedCoordinates LOCATION = new NamedCoordinates(0, 0, LOCATION_NAME);
    private final static ZonedDateTime START_TIME = ZonedDateTime.now().plusDays(1);
    private final static ZonedDateTime END_TIME = ZonedDateTime.now().plusDays(2);

    private final static String EPFL_QUERY = "EPFL";
    private final static String EPFL_CANTON = "Vaud";

    private final static Event e = new Event(ID, NAME, LOCATION, START_TIME, END_TIME);*/

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
    public static Intent putEventListExtra(Intent intent, List<Event> eventList) {
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

        /* For testing
        events.add(e);
        events.add(e);
        events.add(e);
        events.add(e);*/


        eventViewerAdapter = new EventViewerAdapter(events, this);
        eventList = (RecyclerView) findViewById(R.id.recyclerEventView);
        eventList.setAdapter(eventViewerAdapter);
        eventList.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(eventList);

    }

    private void updateResults(List<Event> events){
        this.events = events;
        eventList.setAdapter(new EventViewerAdapter(events, this));
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN |
            ItemTouchHelper.START | ItemTouchHelper.END, ItemTouchHelper.END) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            Collections.swap(events,fromPosition,toPosition);

            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        }
    };



    @Override
    public void onNoteClick(int position) {
        Event event = events.get(position);
        Intent intent = new Intent(this, DisplayEvent.class);
        intent = DisplayEvent.putEventExtra(intent,event);
        startActivity(intent);
    }
}