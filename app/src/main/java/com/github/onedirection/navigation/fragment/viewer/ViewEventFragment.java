package com.github.onedirection.navigation.fragment.viewer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.onedirection.EventQueries;
import com.github.onedirection.R;
import com.github.onedirection.database.ConcreteDatabase;
import com.github.onedirection.events.Event;
import com.github.onedirection.geolocation.NamedCoordinates;
import com.github.onedirection.utils.Id;

import org.apache.commons.math3.exception.NullArgumentException;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ViewEventFragment extends Fragment {

    ConcreteDatabase db;
    private RecyclerView eventRecycler;
    private EventViewAdapter eventAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.event_viewer, container, false);

        eventRecycler = root.findViewById(R.id.recycler_events);

        /*EventQueries db = new EventQueries(ConcreteDatabase.getDatabase());
        db.getEventsByMonth(ZonedDateTime.now()).whenComplete((events1, throwable) ->
        {
            if (events1 == null) {
                throw new NullArgumentException();
            } else {
                eventAdapter = new EventViewAdapter(this.getContext(), events1);
            }
        });*/
        Event e = new Event(Id.generateRandom(),"Event name",new NamedCoordinates(0, 0, "Location name"),ZonedDateTime.now().plusDays(1),ZonedDateTime.now().plusDays(2));
        List<Event> events1 = new ArrayList<Event>();
        events1.add(e);

        eventAdapter = new EventViewAdapter(this.getContext(), events1);

        eventRecycler.setLayoutManager(new LinearLayoutManager(this.getContext()));

        return root;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
