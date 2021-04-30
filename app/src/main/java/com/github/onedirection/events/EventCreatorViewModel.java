package com.github.onedirection.events;

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
import java.util.function.BiConsumer;

public class EventCreatorViewModel extends ViewModel {
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
    public boolean isEditing;
    public BiConsumer<Event, Boolean> callback;
    public CountingIdlingResource idling;


    public void init(Event event, BiConsumer<Event, Boolean> callback, boolean isEditing) {
        this.name = new MutableLiveData<>(event.getName());
        this.startTime = new MutableLiveData<>(event.getStartTime());
        this.endTime = new MutableLiveData<>(event.getEndTime());

        this.useGeolocation = new MutableLiveData<>(event.getCoordinates().isPresent());
        this.customLocation = new MutableLiveData<>(event.getLocationName());
        this.coordinates = new MutableLiveData<>(event.getLocation());

        this.isRecurrent = new MutableLiveData<>(event.isRecurrent());
        this.recId = event.getRecurrence().map(Recurrence::getGroupId).orElse(Id.generateRandom());
        this.recurrencePeriod = new MutableLiveData<>(
                event.getRecurrence().map(Recurrence::getPeriod).orElse(DEFAULT_EVENT_RECURRENCE_PERIOD)
        );
        this.recurrenceEnd = new MutableLiveData<>(
                event.getRecurrence().map(Recurrence::getEndTime).orElse(event.getStartTime().plus(DEFAULT_EVENT_RECURRENCE_DURATION))
        );

        this.eventId = event.getId();
        this.eventId = event.getRecurrence().map(Recurrence::getGroupId).orElse(event.getId());
        this.isEditing = isEditing;
        this.callback = callback;
        this.idling = new CountingIdlingResource("Event creator is loading.");
    }

    public void init(Event event, BiConsumer<Event, Boolean> callback) {
        init(event, callback, true);
    }

    private void init(ZonedDateTime start, BiConsumer<Event, Boolean> callback) {
        init(
                new Event(Id.generateRandom(), "", "", start, start.plus(DEFAULT_EVENT_DURATION)),
                callback,
                false
        );
    }

    public void init(BiConsumer<Event, Boolean> callback) {
        init(ZonedDateTime.now(), callback);
    }

    public void init(LocalDate date, BiConsumer<Event, Boolean> callback) {
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

    public void incrementLoad() {
        idling.increment();
    }

    public void decrementLoad() {
        idling.decrement();
    }
}