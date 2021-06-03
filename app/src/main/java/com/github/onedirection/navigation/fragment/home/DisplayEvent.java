package com.github.onedirection.navigation.fragment.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.onedirection.R;
import com.github.onedirection.database.implementation.Database;
import com.github.onedirection.database.queries.EventQueries;
import com.github.onedirection.event.model.Event;
import com.github.onedirection.event.ui.EventCreator;
import com.github.onedirection.utils.Id;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * To use to view a unique events, just start the activity an provide the event as an intent
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

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();

        if(hasEventExtra(intent)){
            event = getEventExtra(intent);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm");

        TextView name = this.findViewById(R.id.eventNameDisplay);
        name.setText(event.getName());

        TextView location = this.findViewById(R.id.eventNameLocation);
        if(event.getLocationName().equals("")){
            location.setText(R.string.no_loc);
        }else {
            location.setText(event.getLocationName());
        }

        TextView startTime = this.findViewById(R.id.eventStartTimeDisplay);
        startTime.setText(event.getStartTime().format(formatter));

        TextView endTime = this.findViewById(R.id.eventEndTimeDisplay);
        endTime.setText(event.getEndTime().format(formatter));

        if(event.getIsFavorite()){
            ImageButton btn = (ImageButton)findViewById(R.id.favorite_button);

            btn.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(),android.R.drawable.btn_star_big_on,this.getTheme()));
        }
    }

    /**
     * Called when the user taps the Edit button, gets the new list with the modified events and pass it to the homeFragment
     */
    public void buttonStartEditEvent(View view){
        Intent intent = new Intent(this, EventCreator.class);
        intent = EventCreator.putEventExtra(intent,event);
        startActivity(intent);
        super.onBackPressed();
        ZonedDateTime date = ZonedDateTime.now();

        CompletableFuture<List<Event>> monthEventsFuture = EventQueries.getEventsInTimeframe(Database.getDefaultInstance(),date,date.plusMonths(1));
        monthEventsFuture.whenComplete((monthEvents, throwable) -> {
            HomeFragment.homeFragment.updateResults(monthEvents);
            super.onBackPressed();
        });
    }

    /**
     * Called when the user taps the Delete button, deletes the event in the database and update the list of events in homeFragment
     */
    public void buttonStartDeleteEvent(View view){
        Database database = Database.getDefaultInstance();
        EventQueries queryManager = new EventQueries(database);

        CompletableFuture<Id> eventDeleted = queryManager.removeEvent(event.getId());

        eventDeleted.whenComplete(((id, throwable) -> {
                    for (int i = 0; i < HomeFragment.homeFragment.events.size(); i++) {
                        if (HomeFragment.homeFragment.events.get(i).getId().equals(id)){
                          HomeFragment.homeFragment.events.remove(i);
                       }
                   }
            HomeFragment.homeFragment.updateResults();
            super.onBackPressed();
              }));
    }

    /** Called when the user taps the star button to assign an event as favorite */
    public void buttonStarEvent(View view){
        Id id = event.getId();

        boolean isFavorite = event.getIsFavorite();
        ImageButton btn = (ImageButton) findViewById(R.id.favorite_button);

        if(isFavorite){
            HomeFragment.homeFragment.updateModifiedEvent(id,false);
            event = event.setFavorite(false);
            btn.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(),android.R.drawable.btn_star_big_off,this.getTheme()));
        }else{
            HomeFragment.homeFragment.updateModifiedEvent(id,true);
            event = event.setFavorite(true);
            btn.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(),android.R.drawable.btn_star_big_on,this.getTheme()));
        }
    }

    /** Arrow to go back to the main menu */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}