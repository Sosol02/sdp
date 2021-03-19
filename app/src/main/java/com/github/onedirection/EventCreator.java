package com.github.onedirection;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Locale;

public class EventCreator extends AppCompatActivity {
    public static final String EXTRA_NAME = "NAME";
    public static final String EXTRA_LOCATION = "LOCATION";
    public static final String EXTRA_DATE = "DATE";
    public static final String EXTRA_START_TIME = "START_TIME";
    public static final String EXTRA_END_TIME = "END_TIME";

    public static final String EXTRA_EVENT_NAME = "EVENT";
    public static final Class<Event> EXTRA_EVENT_TYPE = Event.class;

    private ZonedDateTime startTime;
    private ZonedDateTime endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_creator);

        startTime = ZonedDateTime.now();
        endTime = startTime.plusHours(1);

        findViewById(R.id.buttonEventAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateEvent(v);
            }
        });

        updateTimeDates();
    }

    public void validateEvent(View view) {
        Intent intent = new Intent(this, EventsView.class);
        EditText name = findViewById(R.id.editEventName);
        EditText location = findViewById(R.id.editEventLocation);
        //EditText start_time = findViewById(R.id.editStartDate);
        //DatePicker end_time = (DatePicker) findViewById(R.id.editEndDate);
        //generate id?
        //Event event = new Event(id,name.getText().toString(),location.getText().toString(),date.getText().toString(),start_time.getText().toString(),end_time.getText().toString());
        //send event to the db
        intent.putExtra(EXTRA_NAME, name.getText().toString());
        intent.putExtra(EXTRA_LOCATION, location.getText().toString());
        //intent.putExtra(EXTRA_START_TIME, start_time.getDay());
        //intent.putExtra(EXTRA_END_TIME, end_time.getText().toString());

        startActivity(intent);
    }

    private void updateTimeDates(){
        Button startTimeBtn = findViewById(R.id.buttonStartTime);
        Button endTimeBtn = findViewById(R.id.buttonEndTime);
        Button startDateBtn = findViewById(R.id.buttonStartDate);
        Button endDateBtn = findViewById(R.id.buttonEndDate);

        startTimeBtn.setText(LocalTime.of(startTime.getHour(), startTime.getMinute()).toString());
        endTimeBtn.setText(LocalTime.of(endTime.getHour(), endTime.getMinute()).toString());
        startDateBtn.setText(startTime.toLocalDate().toString());
        endDateBtn.setText(endTime.toLocalDate().toString());
    }

    public void showStartTimePicker(View v){
        TimePickerDialog timePicker = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                startTime = ZonedDateTime.of(startTime.toLocalDate(), LocalTime.of(hourOfDay, minute), startTime.getZone());
                updateTimeDates();
            }
        }, startTime.getHour(), startTime.getMinute(), true);
        timePicker.show();
    }

    public void showEndTimePicker(View v){
        TimePickerDialog timePicker = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                endTime = ZonedDateTime.of(endTime.toLocalDate(), LocalTime.of(hourOfDay, minute), endTime.getZone());
                updateTimeDates();
            }
        }, endTime.getHour(), endTime.getMinute(), true);
        timePicker.show();
    }

    public void showStartDatePicker(View v){
        DatePickerDialog datePicker = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                startTime = ZonedDateTime.of(LocalDate.of(year, month, dayOfMonth), startTime.toLocalTime(), startTime.getZone());
            }
        }, startTime.getYear(), startTime.getMonthValue(), startTime.getDayOfMonth());
        datePicker.show();
    }

    public void showEndDatePicker(View v){
        DatePickerDialog datePicker = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                startTime = ZonedDateTime.of(LocalDate.of(year, month, dayOfMonth), startTime.toLocalTime(), startTime.getZone());
            }
        }, startTime.getYear(), startTime.getMonthValue(), startTime.getDayOfMonth());
        datePicker.show();
    }
}