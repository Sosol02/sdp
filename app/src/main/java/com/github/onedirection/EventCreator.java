package com.github.onedirection;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.github.onedirection.geocoding.NamedCoordinates;
import com.github.onedirection.utils.Id;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public class EventCreator extends AppCompatActivity {
    public static final String EXTRA_EVENT = "EVENT_ID";
    public static final Class<Event> EXTRA_EVENT_TYPE = Event.class;

    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private Id eventId;

    private boolean isEditing() {
        return eventId != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_creator);

        startTime = ZonedDateTime.now();
        endTime = startTime.plusHours(1);
        updateTimeDates();


        if (getIntent().hasExtra(EXTRA_EVENT)) {
            Event event = EXTRA_EVENT_TYPE.cast(getIntent().getSerializableExtra(EXTRA_EVENT));
            loadEvent(event);
        } else {
            eventId = Id.generateRandom();
        }

        findViewById(R.id.buttonEventAdd).setOnClickListener(v -> {
            Event event = generateEvent();
            // TODO: add logic to put/update (into) db
            viewEvent(event);
        });
    }

    private void viewEvent(Event event) {
        Intent intent = new Intent(this, EventsView.class);
        intent.putExtra(EXTRA_EVENT, event);
        startActivity(intent);
    }

    private Event generateEvent() {
        EditText name = findViewById(R.id.editEventName);
        EditText loc = findViewById(R.id.editEventLocation);

        return new Event(
                eventId,
                name.getText().toString(),
                new NamedCoordinates(0, 0, loc.getText().toString()),
                startTime,
                endTime
        );
    }

    private void loadEvent(Event event) {
        eventId = event.getId();

        EditText name = findViewById(R.id.editEventName);
        EditText loc = findViewById(R.id.editEventLocation);

        name.setText(event.getName());
        loc.setText(event.getLocation().name);
        startTime = event.getStartTime();
        endTime = event.getEndTime();
        updateTimeDates();
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
                                LocalDate.of(year, month, dayOfMonth),
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
                        startTime = ZonedDateTime.of(
                                LocalDate.of(year, month, dayOfMonth),
                                startTime.toLocalTime(),
                                startTime.getZone()
                        ),
                startTime.getYear(),
                startTime.getMonthValue(),
                startTime.getDayOfMonth()
        );
        datePicker.show();
    }
}