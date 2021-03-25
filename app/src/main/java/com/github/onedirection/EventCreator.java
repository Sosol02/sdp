package com.github.onedirection;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.onedirection.geocoding.Coordinates;
import com.github.onedirection.geocoding.DeviceLocation;
import com.github.onedirection.geocoding.GeocodingService;
import com.github.onedirection.geocoding.NamedCoordinates;
import com.github.onedirection.geocoding.NominatimGeocoding;
import com.github.onedirection.utils.Id;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

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
public class EventCreator extends AppCompatActivity {
    public static final String EXTRA_EVENT = "EVENT_ID";
    public static final String EXTRA_DATE = "DATE";
    public static final Class<Event> EXTRA_EVENT_TYPE = Event.class;
    public static final Class<LocalDate> EXTRA_DATE_TYPE = LocalDate.class;

    private final static Duration DEFAULT_EVENT_DURATION = Duration.of(1, ChronoUnit.HOURS);

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

    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private Optional<Coordinates> coordinates;
    private Id eventId;
    private boolean isEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_creator);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        isEditing = false;
        coordinates = Optional.empty();

        Intent intent = getIntent();

        if (intent.hasExtra(EXTRA_EVENT)) {
            loadEvent(getEventExtra(intent));
        } else {
            eventId = Id.generateRandom();
            startTime = intent.hasExtra(EXTRA_DATE) ?
                    ZonedDateTime.of(getDateExtra(intent), LocalTime.now(), ZoneId.systemDefault()) :
                    ZonedDateTime.now();
            endTime = startTime.plus(DEFAULT_EVENT_DURATION);
            updateTimeDates();
        }

        findViewById(R.id.buttonEventAdd).setOnClickListener(v -> {
            Event event = generateEvent();

            if (isEditing) {
                // TODO: update event in db
            } else {
                // TODO: put event in db
            }

            viewEvent(event);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void viewEvent(Event event) {
        Intent intent = new Intent(this, EventsView.class);
        putEventExtra(intent, event);
        startActivity(intent);
    }

    private Event generateEvent() {
        EditText name = findViewById(R.id.editEventName);
        EditText loc = findViewById(R.id.editEventLocationName);

        return new Event(
                eventId,
                name.getText().toString(),
                loc.getText().toString(),
                coordinates,
                startTime,
                endTime
        );
    }

    private void loadEvent(Event event) {
        eventId = event.getId();

        EditText name = findViewById(R.id.editEventName);
        EditText loc = findViewById(R.id.editEventLocationName);

        name.setText(event.getName());
        loc.setText(event.getLocationName());
        startTime = event.getStartTime();
        endTime = event.getEndTime();
        coordinates = event.getCoordinates();
        updateTimeDates();

        isEditing = true;
    }

    private void updateTimeDates() {
        Button startTimeBtn = findViewById(R.id.buttonStartTime);
        Button endTimeBtn = findViewById(R.id.buttonEndTime);
        Button startDateBtn = findViewById(R.id.buttonStartDate);
        Button endDateBtn = findViewById(R.id.buttonEndDate);

        startTimeBtn.setText(LocalTime.of(startTime.getHour(), startTime.getMinute()).toString());
        endTimeBtn.setText(LocalTime.of(endTime.getHour(), endTime.getMinute()).toString());
        startDateBtn.setText(startTime.toLocalDate().toString());
        endDateBtn.setText(endTime.toLocalDate().toString());
    }

    public void showStartTimePicker(View v) {
        TimePickerDialog timePicker = new TimePickerDialog(
                v.getContext(),
                (view, hourOfDay, minute) -> {
                    startTime = ZonedDateTime.of(startTime.toLocalDate(), LocalTime.of(hourOfDay, minute), startTime.getZone());
                    updateTimeDates();
                },
                startTime.getHour(),
                startTime.getMinute(),
                true);
        timePicker.show();
    }

    public void showEndTimePicker(View v) {
        TimePickerDialog timePicker = new TimePickerDialog(
                v.getContext(),
                (view, hourOfDay, minute) -> {
                    endTime = ZonedDateTime.of(endTime.toLocalDate(), LocalTime.of(hourOfDay, minute), endTime.getZone());
                    updateTimeDates();
                },
                endTime.getHour(),
                endTime.getMinute(),
                true);
        timePicker.show();
    }

    public void showStartDatePicker(View v) {
        DatePickerDialog datePicker = new DatePickerDialog(
                v.getContext(),
                (view, year, month, dayOfMonth) ->
                        startTime = ZonedDateTime.of(
                                LocalDate.of(year, month + 1, dayOfMonth),
                                startTime.toLocalTime(),
                                startTime.getZone()
                        ),
                startTime.getYear(),
                startTime.getMonthValue(),
                startTime.getDayOfMonth()
        );
        datePicker.show();
    }

    public void showEndDatePicker(View v) {
        DatePickerDialog datePicker = new DatePickerDialog(
                v.getContext(),
                (view, year, month, dayOfMonth) ->
                        endTime = ZonedDateTime.of(
                                LocalDate.of(year, month + 1, dayOfMonth),
                                endTime.toLocalTime(),
                                endTime.getZone()
                        ),
                endTime.getYear(),
                endTime.getMonthValue(),
                endTime.getDayOfMonth()
        );
        datePicker.show();
    }

    public void usePhoneLocation(View v) {
        DeviceLocation.getCurrentLocation(this).thenAccept(coords -> {
            coordinates = Optional.of(coords);
            EditText locName = findViewById(R.id.editEventLocationName);
            // TODO: display the location somehow (better)
            DecimalFormat format = new DecimalFormat("#.##");
            locName.setText("Current location (" + format.format(coords.latitude) + " ; " + format.format(coords.longitude)  + ")");
        });
    }
}