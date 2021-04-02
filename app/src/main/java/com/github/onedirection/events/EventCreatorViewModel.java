package com.github.onedirection.events;

import android.view.View;
import android.widget.EditText;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.github.onedirection.R;
import com.github.onedirection.geocoding.Coordinates;
import com.github.onedirection.utils.Id;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class EventCreatorViewModel extends ViewModel {
    private final static Duration DEFAULT_EVENT_DURATION = Duration.of(1, ChronoUnit.HOURS);

    public MutableLiveData<Optional<Coordinates>> coordinates;
    public MutableLiveData<ZonedDateTime> startTime;
    public MutableLiveData<ZonedDateTime> endTime;
    public Id eventId;
    public boolean isEditing;
    public CountingIdlingResource idling;

    private void init(ZonedDateTime start){
        this.coordinates = new MutableLiveData<>(Optional.empty());
        this.startTime = new MutableLiveData<>(start);
        this.endTime = new MutableLiveData<>(start.plus(DEFAULT_EVENT_DURATION));

        this.eventId = Id.generateRandom();
        this.isEditing = false;
        this.idling = new CountingIdlingResource("Event creator is loading.");
    }

    public void init(){
        init(ZonedDateTime.now());
    }

    public void init(LocalDate date){
        init(ZonedDateTime.of(date, LocalTime.now(), ZoneId.systemDefault()));
    }

    public void init(Event event){
        this.coordinates = new MutableLiveData<>(event.getCoordinates());
        this.startTime = new MutableLiveData<>(event.getStartTime());
        this.endTime = new MutableLiveData<>(event.getEndTime());

        this.eventId = event.getId();
        this.isEditing = true;
        this.idling = new CountingIdlingResource("Event creator is loading.");
    }

    public Event generateEvent(String name, String locationName){
        return new Event(
                eventId,
                name,
                locationName,
                coordinates.getValue(),
                startTime.getValue(),
                endTime.getValue()
        );
    }

    public void incrementLoad(){
        idling.increment();
    }

    public void decrementLoad(){
        idling.decrement();
    }
}