package com.github.onedirection.events;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.github.onedirection.R;
import com.github.onedirection.geolocation.DeviceLocationProvider;

import java.time.LocalDate;

/**
 * To use to create an event, just start the activity.
 * To edit an event, start using the following code
 * <pre>
 * {@code
 * Event eventToEdit = ...;
 * Intent intent = new Intent(..., EventCreator.class);
 * ... //prepare your intent
 * EventCreator.putEventExtra(eventToEdit);
 * startActivity(intent);
 * }
 * </pre>
 * A date can also be passed to specify the initial date
 * of the event. Ignored if an event is also given.
 */
public class EventCreator extends DeviceLocationProvider {
    public static final String EXTRA_EVENT = "EVENT_ID";
    public static final String EXTRA_DATE = "DATE";
    public static final Class<Event> EXTRA_EVENT_TYPE = Event.class;
    public static final Class<LocalDate> EXTRA_DATE_TYPE = LocalDate.class;

    public static boolean hasEventExtra(Intent intent){
        return intent.hasExtra(EXTRA_EVENT);
    }

    /**
     * Extract the event extra put by/for the Event creator.
     *
     * @param intent The intent.
     * @return The contained event.
     */
    public static Event getEventExtra(Intent intent) {
        return EXTRA_EVENT_TYPE.cast(intent.getSerializableExtra(EXTRA_EVENT));
    }

    /**
     * Put an event extra for the Event creator.
     *
     * @param intent The intent which will carry the event.
     * @param event  The event to put.
     * @return The passed intent.
     */
    public static Intent putEventExtra(Intent intent, Event event) {
        return intent.putExtra(EXTRA_EVENT, event);
    }

    public static boolean hasDateExtra(Intent intent){
        return intent.hasExtra(EXTRA_DATE);
    }

    /**
     * Extract the date extra put by/for the Event creator.
     *
     * @param intent The intent.
     * @return The contained date.
     */
    public static LocalDate getDateExtra(Intent intent) {
        return EXTRA_DATE_TYPE.cast(intent.getSerializableExtra(EXTRA_DATE));
    }

    /**
     * Put a date extra for the Event creator.
     *
     * @param intent The intent which will carry the event.
     * @param date   The date to put.
     * @return The passed intent.
     */
    public static Intent putDateExtra(Intent intent, LocalDate date) {
        return intent.putExtra(EXTRA_DATE, date);
    }

    EventCreatorViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_creator);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        this.model = new ViewModelProvider(this).get(EventCreatorViewModel.class);

        if(hasEventExtra(intent)){
            this.model.init(getEventExtra(intent));
        }
        else if(hasDateExtra(intent)){
            this.model.init(getDateExtra(intent));
        }
        else{
            this.model.init();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @VisibleForTesting
    public CountingIdlingResource getIdlingResource(){
        return model.idling;
    }
}