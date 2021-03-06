package com.github.onedirection.event.ui;

import androidx.lifecycle.MutableLiveData;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.github.onedirection.event.model.Event;
import com.github.onedirection.event.model.Recurrence;
import com.github.onedirection.geolocation.model.NamedCoordinates;
import com.github.onedirection.utils.Id;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;


/**
 * View model for the Event Creator.
 * Basically the list of all attributes the event creator would have
 * if the main class + all fragments were merged.
 * <p>
 * Note: I made the mistake of using some outdated documentation
 * as inspiration for this class, so fields are public, where they shouldn't.
 */
public class ViewModel extends androidx.lifecycle.ViewModel {
    private final static Duration DEFAULT_EVENT_DURATION = Duration.of(1, ChronoUnit.HOURS);
    private final static Duration DEFAULT_EVENT_RECURRENCE_PERIOD = Duration.of(1, ChronoUnit.HOURS);
    private final static Duration DEFAULT_EVENT_RECURRENCE_DURATION = Duration.of(1, ChronoUnit.DAYS);

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
    public Id recId;
    public boolean isFavorite;
    public boolean isEditing;
    public BiFunction<Event, Boolean, CompletableFuture<?>> callback;
    public CountingIdlingResource idling;


    public void init(Event event, BiFunction<Event, Boolean, CompletableFuture<?>> callback, boolean isEditing) {
        this.name = new MutableLiveData<>(event.getName());
        this.startTime = new MutableLiveData<>(event.getStartTime());
        this.endTime = new MutableLiveData<>(event.getEndTime());

        this.useGeolocation = new MutableLiveData<>(event.getCoordinates().isPresent());
        this.customLocation = new MutableLiveData<>(event.getLocationName());
        this.coordinates = new MutableLiveData<>(event.getLocation());

        this.isRecurrent = new MutableLiveData<>(event.isRecurrent());
        this.recurrencePeriod = new MutableLiveData<>(
                event.getRecurrence().map(Recurrence::getPeriod).orElse(DEFAULT_EVENT_RECURRENCE_PERIOD)
        );
        this.recurrenceEnd = new MutableLiveData<>(
                event.getRecurrence().map(Recurrence::getEndTime).orElse(event.getStartTime().plus(DEFAULT_EVENT_RECURRENCE_DURATION))
        );

        this.eventId = event.getId();
        this.recId = event.getRecurrence().map(Recurrence::getGroupId).orElse(event.getId());
        this.isFavorite = event.getIsFavorite();
        this.isEditing = isEditing;
        this.callback = callback;
        this.idling = new CountingIdlingResource("Event creator is loading.");
    }

    public void init(Event event, BiFunction<Event, Boolean, CompletableFuture<?>> callback) {
        init(event, callback, true);
    }

    private void init(ZonedDateTime start, BiFunction<Event, Boolean, CompletableFuture<?>> callback) {
        init(
                new Event(Id.generateRandom(), "", "", start, start.plus(DEFAULT_EVENT_DURATION),false),
                callback,
                false
        );
    }

    public void init(BiFunction<Event, Boolean, CompletableFuture<?>> callback) {
        init(ZonedDateTime.now(), callback);
    }

    public void init(LocalDate date, BiFunction<Event, Boolean, CompletableFuture<?>> callback) {
        init(ZonedDateTime.of(date, LocalTime.now(), ZoneId.systemDefault()), callback);
    }

    public Optional<Recurrence> generateRecurrence() {
        return isRecurrent.getValue() ?
                Optional.of(new Recurrence(recId, recurrencePeriod.getValue(), recurrenceEnd.getValue())) :
                Optional.empty();
    }

    public Event generateEvent() {
        return useGeolocation.getValue() ?
                new Event(
                        eventId,
                        name.getValue(),
                        coordinates.getValue().get(),
                        startTime.getValue(),
                        endTime.getValue(),
                        generateRecurrence(),
                        isFavorite
                ) :
                new Event(
                        eventId,
                        name.getValue(),
                        customLocation.getValue(),
                        startTime.getValue(),
                        endTime.getValue(),
                        generateRecurrence(),
                        isFavorite
                );
    }

    public void incrementLoad() {
        idling.increment();
    }

    public void decrementLoad() {
        idling.decrement();
    }
}