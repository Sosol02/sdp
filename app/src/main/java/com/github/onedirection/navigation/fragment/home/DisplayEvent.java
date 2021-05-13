package com.github.onedirection.navigation.fragment.home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.onedirection.R;
import com.github.onedirection.events.Event;
import com.github.onedirection.events.ui.EventCreator;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * To use to view a unique events, just start the activity an provide the event
 */

public class DisplayEvent extends AppCompatActivity {

    public static final String EXTRA_EVENT = "EVENT_ID";
    Event event;


    public static boolean hasEventExtra(Intent intent) {
        return intent.hasExtra(EXTRA_EVENT);
    }

    /**
     * Extract the event list extra.
     *
     * @param intent The intent.
     * @return The contained event list.
     */
    public static Event getEventExtra(Intent intent) {
        return (Event) (intent.getSerializableExtra(EXTRA_EVENT));
    }

    /**
     * Put an event extra for the Event creator.
     *
     * @param intent The intent which will carry the event.
     * @param event  The event list to put.
     * @return The passed intent.
     */
    public static Intent putEventExtra(Intent intent, Event event) {
        return intent.putExtra(EXTRA_EVENT, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_event);

        Intent intent = getIntent();

        if(hasEventExtra(intent)){
            event = getEventExtra(intent);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");

        TextView name = this.findViewById(R.id.eventNameDisplay);
        name.setText(event.getName());
        TextView location = this.findViewById(R.id.eventNameLocation);
        location.setText(event.getLocationName());
        TextView startTime = this.findViewById(R.id.eventStartTimeDisplay);
        startTime.setText(event.getStartTime().format(formatter));
        TextView endTime = this.findViewById(R.id.eventEndTimeDisplay);
        endTime.setText(event.getEndTime().format(formatter));
    }

    /** Called when the user taps the Send button */
    public void buttonStartEditEvent(View view){
        Intent intent = new Intent(this, EventCreator.class);
        intent = EventCreator.putEventExtra(intent,event);
        startActivity(intent);
    }
}