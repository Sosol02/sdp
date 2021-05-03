package com.github.onedirection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.github.onedirection.events.Event;
import com.github.onedirection.events.EventCreator;

import java.util.HashSet;

public class EventsView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_viewer);

        Intent intent = getIntent();
        Event event = EventCreator.getEventExtra(intent);

        TextView textViewName = findViewById(R.id.textViewNameView);
        textViewName.setText(event.getName());

        TextView textViewLocation = findViewById(R.id.textViewLocationView);
        textViewLocation.setText(event.getLocationName());

        TextView textViewStartTime = findViewById(R.id.textViewStartTimeView);
        textViewStartTime.setText(event.getStartTime().toString());

        TextView textViewEndTime = findViewById(R.id.textViewEndTimeView);
        textViewEndTime.setText(event.getEndTime().toString());

        HashSet<Event> eventHashSet = new HashSet<>();
        eventHashSet.add(event);
        //Notifications.getInstance(getApplicationContext()).scheduleEventNotifs(getApplicationContext(), eventHashSet);
    }
}