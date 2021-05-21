package com.github.onedirection.navigation.fragment.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.onedirection.R;
import com.github.onedirection.database.Database;
import com.github.onedirection.database.queries.EventQueries;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.event.Event;
import com.github.onedirection.navigation.fragment.calendar.DayEventsListView;
import com.github.onedirection.event.ui.EventCreator;
import com.github.onedirection.utils.Id;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import androidx.appcompat.app.ActionBar;


/**
 *Home of the application
 *Display the events of the month
 */

public class HomeFragment extends Fragment implements  EventViewerAdapter.OnNoteListener{

    private RecyclerView eventList;
    private EventViewerAdapter eventViewerAdapter;
    List<Event> events = new ArrayList<Event>();
    Map<Id,Boolean> favorites = new HashMap<>();
    public static HomeFragment homeFragment;
    private EventViewerAdapter.OnNoteListener onNoteListener;
    private boolean isOnFavoriteView = false;
    private boolean isOnOrderedView = false;
    private List<Event> favoritesEvents;
    private List<Event> orderedEvents;

    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        ZonedDateTime date = ZonedDateTime.now();

        CompletableFuture<List<Event>> monthEventsFuture = EventQueries.getEventsInTimeframe(Database.getDefaultInstance(),date,date.plusMonths(1));;

        View root = inflater.inflate(R.layout.event_viewer, container, false);

        eventViewerAdapter = new EventViewerAdapter(events, this);
        eventList = (RecyclerView) root.findViewById(R.id.recyclerEventView);
        eventList.setAdapter(eventViewerAdapter);
        eventList.setLayoutManager(new LinearLayoutManager(this.getContext()));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(eventList);

        monthEventsFuture.whenComplete((monthEvents, throwable) -> {
            events = monthEvents;
            eventList.setAdapter(new EventViewerAdapter(events, this));
        });

        homeFragment = this;
        onNoteListener = this;

        FloatingActionButton fabAdd = (FloatingActionButton) root.findViewById(R.id.fab);
        fabAdd.setOnClickListener(fabAdd());

        FloatingActionButton fabFavorite = (FloatingActionButton) root.findViewById(R.id.fabFavorite);
        fabFavorite.setOnClickListener(fabFavorite());

        FloatingActionButton fabOrder = (FloatingActionButton) root.findViewById(R.id.fabOrder);
        fabOrder.setOnClickListener(fabSortTime());

        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    };

    public void updateResults(List<Event> events){
        this.events = events;
        eventList.setAdapter(new EventViewerAdapter(this.events, this));

    }

    public void updateResults(){
        eventList.setAdapter(new EventViewerAdapter(events, this));
    }

    public void updateResultsWithCallToDb(){
        ZonedDateTime date = ZonedDateTime.now();

        CompletableFuture<List<Event>> monthEventsFuture = EventQueries.getEventsInTimeframe(Database.getDefaultInstance(),date,date.plusMonths(1));;
        monthEventsFuture.whenComplete((monthEvents, throwable) -> {
            events = monthEvents;
            eventList.setAdapter(new EventViewerAdapter(events, this));
        });
    }

    public void updateModifiedEvent(Id id){
        int position = 0;
        for(int i =0; i<events.size();i++){
            if(events.get(i).getId().equals(id)){
                position = i;
            }
        }
        Database database = Database.getDefaultInstance();
        CompletableFuture<Event> e = database.retrieve(Objects.requireNonNull(id), EventStorer.getInstance());
        eventList.setAdapter(new EventViewerAdapter(events, this));
        eventViewerAdapter.notifyItemChanged(position);
    }

    @Override
    public void onResume() {
        super.onResume();
        ZonedDateTime date = ZonedDateTime.now();

        CompletableFuture<List<Event>> monthEventsFuture = EventQueries.getEventsInTimeframe(Database.getDefaultInstance(),date,date.plusMonths(1));;
        monthEventsFuture.whenComplete((monthEvents, throwable) -> {
            events = monthEvents;
            eventList.setAdapter(new EventViewerAdapter(events, this));
        });
    }

    public void deleteEvent(Id id){
        int position = 0;
        for(int i =0; i<events.size();i++){
            if(events.get(i).getId().equals(id)){
                events.remove(i);
                position = i;
            }
        }
        eventList.setAdapter(new EventViewerAdapter(events, this));
        eventViewerAdapter.notifyItemRemoved(position);
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
            Database database = Database.getDefaultInstance();
            EventQueries queryManager = new EventQueries(database);
            Id id = events.get(viewHolder.getPosition()).getId();
            queryManager.removeEvent(id);
            deleteEvent(id);
            //eventList.getAdapter().notifyItemRemoved(viewHolder.getPosition());
        }
    };

    @Override
    public void onNoteClick(int position) {
        Event event = events.get(position);
        Intent intent = new Intent(this.getContext(), DisplayEvent.class);
        intent = DisplayEvent.putEventExtra(intent,event);
        startActivity(intent);
    }

    public View.OnClickListener fabAdd(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), EventCreator.class);
                startActivity(intent);
            }
        };
    }

    public View.OnClickListener fabFavorite(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isOnFavoriteView) {
                    List<Event> listFavorites = new ArrayList<>();
                    if(isOnOrderedView){
                        for (Event e : orderedEvents) {
                            if (favorites.get(e.getId())) listFavorites.add(e);
                        }
                    }else{
                        for (Event e : events) {
                            if (favorites.get(e.getId())) listFavorites.add(e);
                        }
                    }
                    eventList.setAdapter(new EventViewerAdapter(listFavorites, onNoteListener));
                    isOnFavoriteView = true;
                    favoritesEvents = listFavorites;
                }else{
                    if(isOnOrderedView){
                        eventList.setAdapter(new EventViewerAdapter(orderedEvents, onNoteListener));
                    }else {
                        eventList.setAdapter(new EventViewerAdapter(events, onNoteListener));
                    }
                    isOnFavoriteView = false;
                }
            }
        };
    }

    public View.OnClickListener fabSortTime(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isOnOrderedView) {
                    List<Event> listOrdered = new ArrayList<>();
                    if(isOnFavoriteView){
                        for (Event event : favoritesEvents) {
                            listOrdered.add(new Event(event.getId(), event.getName(), event.getLocationName(), event.getCoordinates(), event.getStartTime(), event.getEndTime(), event.getRecurrence()));
                        }
                    }else{
                        for (Event event : events) {
                            listOrdered.add(new Event(event.getId(), event.getName(), event.getLocationName(), event.getCoordinates(), event.getStartTime(), event.getEndTime(), event.getRecurrence()));
                        }
                    }
                    listOrdered.sort((l, r) -> {
                        if (l.equals(r)) return 0;
                        return l.getStartTime().isBefore(r.getStartTime()) ? -1 : 1;
                    });
                    eventList.setAdapter(new EventViewerAdapter(listOrdered, onNoteListener));
                    isOnOrderedView = true;
                    orderedEvents = listOrdered;
                }else{
                    if(isOnFavoriteView){
                        eventList.setAdapter(new EventViewerAdapter(favoritesEvents, onNoteListener));
                    }else{
                        eventList.setAdapter(new EventViewerAdapter(events, onNoteListener));
                    }
                    isOnOrderedView = false;
                }
            };
        };
    }
}