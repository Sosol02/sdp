package com.github.onedirection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class EventsView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_view);

        Intent intent = getIntent();
        String name = intent.getStringExtra(EventCreator.EXTRA_NAME);
        String location = intent.getStringExtra(EventCreator.EXTRA_LOCATION);
        String date = intent.getStringExtra(EventCreator.EXTRA_DATE);
        String start_time = intent.getStringExtra(EventCreator.EXTRA_START_TIME);
        String end_time = intent.getStringExtra(EventCreator.EXTRA_END_TIME);

        TextView textViewName = findViewById(R.id.textViewNameView);
        textViewName.setText(name);

        TextView textViewLocation = findViewById(R.id.textViewLocationView);
        textViewLocation.setText(location);

        TextView textViewDate = findViewById(R.id.textViewDateView);
        textViewDate.setText(date);

        TextView textViewStartTime = findViewById(R.id.textViewStartTimeView);
        textViewStartTime.setText(start_time);

        TextView textViewEndTime = findViewById(R.id.textViewEndTimeView);
        textViewEndTime.setText(end_time);

    }
}