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
 * Home of the application
 * Display the events of the month
 */

public class HomeFragment extends Fragment implements EventViewerAdapter.OnNoteListener {

    public static HomeFragment homeFragment;
    List<Event> events = new ArrayList<Event>();
    Map<Id, Boolean> favorites = new HashMap<>();
    private RecyclerView eventList;
    private EventViewerAdapter eventViewerAdapter;
    private EventViewerAdapter.OnNoteListener onNoteListener;
    private boolean isOnFavoriteView = false;
    private boolean isOnOrderedView = false;
    private List<Event> favoritesEvents;
    private List<Event> orderedEvents;
    private View root;
    private TextView displayEmpty;

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN |
            ItemTouchHelper.START | ItemTouchHelper.END, ItemTouchHelper.END) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            Collections.swap(events, fromPosition, toPosition);

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
            checkEventListIsEmpty();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ZonedDateTime date = ZonedDateTime.now();

        CompletableFuture<List<Event>> monthEventsFuture = EventQueries.getEventsInTimeframe(Database.getDefaultInstance(), date, date.plusMonths(1));

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
        onNoteListener = this;

        FloatingActionButton fabAdd = root.findViewById(R.id.fab);
        fabAdd.setOnClickListener(fabAdd());

        FloatingActionButton fabFavorite = root.findViewById(R.id.fabFavorite);
        fabFavorite.setOnClickListener(fabFavorite());

        FloatingActionButton fabOrder = root.findViewById(R.id.fabOrder);
        fabOrder.setOnClickListener(fabSortTime());

        Toolbar toolbar = root.findViewById(R.id.toolbar);

        this.root = root;

        displayEmpty = root.findViewById(R.id.displayNoEvents);

        checkEventListIsEmpty();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void updateResults(List<Event> events) {
        this.events = events;
        checkEventListIsEmpty();
        eventList.setAdapter(new EventViewerAdapter(this.events, this));

    }

    public void updateResults() {
        checkEventListIsEmpty();
        eventList.setAdapter(new EventViewerAdapter(events, this));
    }

    public void updateResultsWithCallToDb() {
        ZonedDateTime date = ZonedDateTime.now();

        CompletableFuture<List<Event>> monthEventsFuture = EventQueries.getEventsInTimeframe(Database.getDefaultInstance(), date, date.plusMonths(1));
        monthEventsFuture.whenComplete((monthEvents, throwable) -> {
            events = monthEvents;
            checkEventListIsEmpty();
            eventList.setAdapter(new EventViewerAdapter(events, this));
        });
    }

    public void updateModifiedEvent(Id id) {
        int position = 0;
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(id)) {
                position = i;
            }
        }
        Database database = Database.getDefaultInstance();
        CompletableFuture<Event> e = database.retrieve(Objects.requireNonNull(id), EventStorer.getInstance());
        checkEventListIsEmpty();
        eventList.setAdapter(new EventViewerAdapter(events, this));
        eventViewerAdapter.notifyItemChanged(position);
    }

    @Override
    public void onResume() {
        super.onResume();
        ZonedDateTime date = ZonedDateTime.now();

        CompletableFuture<List<Event>> monthEventsFuture = EventQueries.getEventsInTimeframe(Database.getDefaultInstance(), date, date.plusMonths(1));
        monthEventsFuture.whenComplete((monthEvents, throwable) -> {
            events = monthEvents;
            checkEventListIsEmpty();
            eventList.setAdapter(new EventViewerAdapter(events, this));
        });
    }

    public void deleteEvent(Id id) {
        int position = 0;
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(id)) {
                events.remove(i);
                position = i;
            }
        }
        checkEventListIsEmpty();
        eventList.setAdapter(new EventViewerAdapter(events, this));
        eventViewerAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onNoteClick(int position) {
        Event event = events.get(position);
        Intent intent = new Intent(this.getContext(), DisplayEvent.class);
        intent = DisplayEvent.putEventExtra(intent, event);
        startActivity(intent);
    }

    public View.OnClickListener fabAdd() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), EventCreator.class);
                startActivity(intent);
            }
        };
    }

    public View.OnClickListener fabFavorite() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOnFavoriteView) {
                    List<Event> listFavorites = new ArrayList<>();
                    for (Event e : isOnOrderedView ? orderedEvents : events) {
                        if (favorites.get(e.getId())) listFavorites.add(e);
                    }
                    eventList.setAdapter(new EventViewerAdapter(listFavorites, onNoteListener));
                    isOnFavoriteView = true;
                    favoritesEvents = listFavorites;
                } else {
                    eventList.setAdapter(new EventViewerAdapter(isOnOrderedView ? orderedEvents : events, onNoteListener));
                    isOnFavoriteView = false;
                }
            }
        };
    }

    public View.OnClickListener fabSortTime() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isOnOrderedView) {
                    List<Event> listOrdered = new ArrayList<>();
                    for (Event e : isOnFavoriteView ? favoritesEvents : events) {
                        listOrdered.add(e);
                    }
                    listOrdered.sort((l, r) -> {
                        return Long.compare(l.getStartTime().toEpochSecond(), r.getStartTime().toEpochSecond());
                    });
                    eventList.setAdapter(new EventViewerAdapter(listOrdered, onNoteListener));
                    isOnOrderedView = true;
                    orderedEvents = listOrdered;
                } else {
                    eventList.setAdapter(new EventViewerAdapter(isOnFavoriteView ? favoritesEvents : events, onNoteListener));
                    isOnOrderedView = false;
                }
            }

        };
    }

    public void checkEventListIsEmpty() {
        if (events.isEmpty()) {
            displayEmpty.setVisibility(View.VISIBLE);
        } else {
            displayEmpty.setVisibility(View.INVISIBLE);
        }
    }
}