package com.github.onedirection.navigation.fragment.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.github.onedirection.event.Event;
import com.github.onedirection.utils.Id;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import androidx.appcompat.app.ActionBar;


/*
 **Home of the application
 **Display the events of the month
 */
public class HomeFragment extends Fragment implements  EventViewerAdapter.OnNoteListener{

    RecyclerView eventList;
    EventViewerAdapter eventViewerAdapter;
    List<Event> events = new ArrayList<Event>();
    public static HomeFragment homeFragment;
    Map<Id,Boolean> favorites = new HashMap<>();

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

    public void deleteEvent(Id id){
        for(int i =0; i<events.size();i++){
            if(events.get(i).getId() == id){
                events.remove(i);
            }
        }
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
            Database database = Database.getDefaultInstance();
            EventQueries queryManager = new EventQueries(database);
            queryManager.removeEvent(events.get(viewHolder.getPosition()).getId());
            eventList.getAdapter().notifyItemRemoved(viewHolder.getPosition());
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