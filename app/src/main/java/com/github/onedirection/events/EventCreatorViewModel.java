package com.github.onedirection.events;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.github.onedirection.geolocation.NamedCoordinates;
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

    public MutableLiveData<String> name;
    public MutableLiveData<ZonedDateTime> startTime;
    public MutableLiveData<ZonedDateTime> endTime;

    public MutableLiveData<Boolean> useGeolocation;
    public MutableLiveData<String> customLocation;
    public MutableLiveData<Optional<NamedCoordinates>> coordinates;

    public MutableLiveData<Boolean> isRecurrent;
    public MutableLiveData<Duration> recurrencePeriod;
    public MutableLiveData<ZonedDateTime> recurrenceEnd;

    public Id eventId;
    public boolean isEditing;
    public CountingIdlingResource idling;

    private void init(ZonedDateTime start){
        this.name = new MutableLiveData<>("");
        this.customLocation = new MutableLiveData<>("");
        this.coordinates = new MutableLiveData<>(Optional.empty());
        this.startTime = new MutableLiveData<>(start);
        this.endTime = new MutableLiveData<>(start.plus(DEFAULT_EVENT_DURATION));
        this.useGeolocation = new MutableLiveData<>(false);

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
        this.name = new MutableLiveData<>(event.getName());
        this.customLocation = new MutableLiveData<>(event.getLocationName());
        this.coordinates = new MutableLiveData<>(event.getLocation());
        this.startTime = new MutableLiveData<>(event.getStartTime());
        this.endTime = new MutableLiveData<>(event.getEndTime());
        this.useGeolocation = new MutableLiveData<>(event.getCoordinates().isPresent());

        this.eventId = event.getId();
        this.isEditing = true;
        this.idling = new CountingIdlingResource("Event creator is loading.");
    }

    public Optional<Recurrence> generateRecurrence(){
        // TODO: ask remi what id to use...
        Id recId = new Id();
        return isRecurrent.getValue() ?
                Optional.of(new Recurrence(recId, recurrencePeriod.getValue(), recurrenceEnd.getValue())):
                Optional.empty();
    }

    public Event generateEvent(){
        return useGeolocation.getValue() ?
            new Event(
                eventId,
                name.getValue(),
                coordinates.getValue().get(),
                startTime.getValue(),
                endTime.getValue(),
                generateRecurrence()
            ) :
            new Event(
                eventId,
                name.getValue(),
                customLocation.getValue(),
                startTime.getValue(),
                endTime.getValue(),
                generateRecurrence()
            );
    }

    public void incrementLoad(){
        idling.increment();
    }

    public void decrementLoad(){
        idling.decrement();
    }
}