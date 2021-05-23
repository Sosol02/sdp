package com.github.onedirection.navigation.fragment.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.onedirection.R;
import com.github.onedirection.database.implementation.Database;
import com.github.onedirection.database.queries.EventQueries;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.event.model.Event;
import com.github.onedirection.event.ui.EventCreator;
import com.github.onedirection.utils.Id;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;


/**
 *Home of the application
 *Display the events of the month
 */

public class HomeFragment extends Fragment implements  EventViewerAdapter.OnNoteListener{

    RecyclerView eventList;
    EventViewerAdapter eventViewerAdapter;
    List<Event> events = new ArrayList<Event>();
    Map<Id,Boolean> favorites = new HashMap<>();
    public static HomeFragment homeFragment;

    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        ZonedDateTime date = ZonedDateTime.now();

        CompletableFuture<List<Event>> monthEventsFuture = EventQueries.getEventsInTimeframe(Database.getDefaultInstance(),date,date.plusMonths(1));

        View root = inflater.inflate(R.layout.event_viewer, container, false);

        eventViewerAdapter = new EventViewerAdapter(events, this);
        eventList = root.findViewById(R.id.recyclerEventView);
        eventList.setAdapter(eventViewerAdapter);
        eventList.setLayoutManager(new LinearLayoutManager(this.getContext()));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(eventList);

        monthEventsFuture.whenComplete((monthEvents, throwable) -> {
            events = monthEvents;
            eventList.setAdapter(new EventViewerAdapter(events, this));
        });

        homeFragment = this;


        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(root.getContext(), EventCreator.class);
                startActivity(intent);
            }
        });

        Toolbar toolbar = root.findViewById(R.id.toolbar);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void updateResults(List<Event> events){
        this.events = events;
        eventList.setAdapter(new EventViewerAdapter(this.events, this));

    }

    public void updateResults(){
        eventList.setAdapter(new EventViewerAdapter(events, this));
    }

    public void updateResultsWithCallToDb(){
        ZonedDateTime date = ZonedDateTime.now();

        CompletableFuture<List<Event>> monthEventsFuture = EventQueries.getEventsInTimeframe(Database.getDefaultInstance(),date,date.plusMonths(1));
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

        CompletableFuture<List<Event>> monthEventsFuture = EventQueries.getEventsInTimeframe(Database.getDefaultInstance(),date,date.plusMonths(1));
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


}