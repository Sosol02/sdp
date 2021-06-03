package com.github.onedirection.navigation.fragment.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.onedirection.R;
import com.github.onedirection.database.implementation.Database;
import com.github.onedirection.database.queries.EventQueries;
import com.github.onedirection.event.model.Event;
import com.github.onedirection.event.ui.EventCreator;
import com.github.onedirection.utils.Id;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * Home of the application
 * Display the events of the month
 */

public class HomeFragment extends Fragment implements EventViewerAdapter.OnNoteListener {

    List<Event> events = new ArrayList<Event>();
    private RecyclerView eventList;
    private EventViewerAdapter eventViewerAdapter;
    private EventViewerAdapter.OnNoteListener onNoteListener;
    private boolean isOnFavoriteView = false;
    private boolean isOnOrderedView = false;
    private List<Event> favoritesEvents;
    private List<Event> orderedEvents;
    private View root;
    private TextView displayEmpty;
    private ActivityResultLauncher<Intent> newEventsList;

    /** Callback for swiping */
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN |
            ItemTouchHelper.START | ItemTouchHelper.END, ItemTouchHelper.END) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            Database database = Database.getDefaultInstance();
            EventQueries queryManager = new EventQueries(database);
            Id id = events.get(viewHolder.getPosition()).getId();
            queryManager.removeEvent(id);
            int position = 0;
            for (int i = 0; i < events.size(); i++) {
                if (events.get(i).getId().equals(id)) {
                    events.remove(i);
                    position = i;
                }
            }
            checkEventListIsEmpty();
            eventList.setAdapter(new EventViewerAdapter(events, onNoteListener));
            eventViewerAdapter.notifyItemRemoved(position);
            checkEventListIsEmpty();
        }

        @Override
        public void onChildDraw (Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){
            Bitmap icon;
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                View itemView = viewHolder.itemView;

                float height = (float) itemView.getBottom() - (float) itemView.getTop();
                float width = height / 3;

                Paint p = new Paint();
                if (dX > 0) {
                    p.setColor(Color.RED);
                    c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                            (float) itemView.getBottom(), p);
                } else {
                    c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                            (float) itemView.getRight(), (float) itemView.getBottom(), p);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }
    };


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ZonedDateTime date = ZonedDateTime.now();

        //Get the events of the month as a future
        CompletableFuture<List<Event>> monthEventsFuture = EventQueries.getEventsInTimeframe(Database.getDefaultInstance(), date, date.plusMonths(1));

        View root = inflater.inflate(R.layout.event_viewer, container, false);

        eventViewerAdapter = new EventViewerAdapter(events, this);
        eventList = root.findViewById(R.id.recyclerEventView);
        eventList.setAdapter(eventViewerAdapter);
        eventList.setLayoutManager(new LinearLayoutManager(this.getContext()));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(eventList);

        //Retrieve the events of the month once they are fetch from the database
        monthEventsFuture.whenComplete((monthEvents, throwable) -> {
            events = monthEvents;
            eventList.setAdapter(new EventViewerAdapter(events, this));
        });

        onNoteListener = this;

        //Set actions for the different fab buttons
        FloatingActionButton fabAdd = root.findViewById(R.id.fab);
        fabAdd.setOnClickListener(fabAdd());

        FloatingActionButton fabFavorite = root.findViewById(R.id.fabFavorite);
        fabFavorite.setOnClickListener(fabFavorite());

        FloatingActionButton fabOrder = root.findViewById(R.id.fabOrder);
        fabOrder.setOnClickListener(fabSortTime());

        this.root = root;

        displayEmpty = root.findViewById(R.id.displayNoEvents);

        this.newEventsList = requireActivity().registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            if(data.hasExtra(DisplayEvent.EXTRA_MODIFIED)){
                Event newEvent = (Event) data.getSerializableExtra(DisplayEvent.EXTRA_MODIFIED);
                Id id = newEvent.getId();
                int position = 0;
                for (int i = 0; i < events.size(); i++) {
                    if (events.get(i).getId().equals(id)) {
                        events.set(position, newEvent);
                        position = i;
                    }
                }
                checkEventListIsEmpty();
                eventViewerAdapter.notifyItemChanged(position);
                eventList.setAdapter(new EventViewerAdapter(events, this));
            }else if(data.hasExtra(DisplayEvent.EXTRA_DELETED)){
                Id id = (Id) data.getSerializableExtra(DisplayEvent.EXTRA_DELETED);
                int position = 0;
                for (int i = 0; i < events.size(); i++) {
                    if (events.get(i).getId().equals(id)) {
                        events.remove(i);
                        position = i;
                    }
                }
                checkEventListIsEmpty();
                eventViewerAdapter.notifyItemRemoved(position);
                eventList.setAdapter(new EventViewerAdapter(events, this));
            }else if(data.hasExtra(DisplayEvent.EXTRA_FAVORITE)){
                Event newEvent = (Event) data.getSerializableExtra(DisplayEvent.EXTRA_FAVORITE);
                Id id = newEvent.getId();
                int position = 0;
                for (int i = 0; i < events.size(); i++) {
                    if (events.get(i).getId().equals(id)) {
                        events.get(position).setFavorite(newEvent.getIsFavorite());
                        position = i;
                        eventViewerAdapter.notifyItemChanged(position);
                    }
                }
                checkEventListIsEmpty();
                eventList.setAdapter(new EventViewerAdapter(events, this));
            }
        });

        checkEventListIsEmpty();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /** Method executed each time we come back to the fragment*/
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

    /**
     * Listener to the event in the recycler view to launch DisplayEvent with that specific event
     *
     * @param position The position of the events in the recyclerView to be launched
     * */
    @Override
    public void onNoteClick(int position) {
        Event event = events.get(position);
        Intent intent = new Intent(this.getContext(), DisplayEvent.class);
        intent = DisplayEvent.putEventExtra(intent, event);
        newEventsList.launch(intent);
    }

    /** Assign functionality to the fabAdd button */
    public View.OnClickListener fabAdd() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), EventCreator.class);
                startActivity(intent);
            }
        };
    }

    /** Assign functionality to the fabFavorite button */
    public View.OnClickListener fabFavorite() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOnFavoriteView) {
                    List<Event> listFavorites = new ArrayList<>();
                    for (Event e : isOnOrderedView ? orderedEvents : events) {
                        if (e.getIsFavorite()) listFavorites.add(e);
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

    /** Assign functionality to the fabOrder button */
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

    /** Check the list of events to display is not empty an update the view accordingly */
    public void checkEventListIsEmpty() {
        if (events.isEmpty()) {
            displayEmpty.setVisibility(View.VISIBLE);
        } else {
            displayEmpty.setVisibility(View.INVISIBLE);
        }
    }
}
