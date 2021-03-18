package com.github.onedirection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.EditText;

public class EventCreator extends AppCompatActivity {
    public static final String EXTRA_NAME = "NAME";
    public static final String EXTRA_LOCATION = "LOCATION";
    public static final String EXTRA_DATE = "DATE";
    public static final String EXTRA_START_TIME = "START_TIME";
    public static final String EXTRA_END_TIME = "END_TIME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_creator);

        findViewById(R.id.buttonEventAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateEvent(v);
            }
        });
    }

    public void validateEvent(View view) {
        Intent intent = new Intent(this,EventsView.class);
        EditText name = (EditText) findViewById(R.id.editTextName);
        EditText location = (EditText) findViewById(R.id.editTextLocation);
        EditText date = (EditText) findViewById(R.id.editTextDate);
        EditText start_time = (EditText) findViewById(R.id.editTextEndTime);
        EditText end_time = (EditText) findViewById(R.id.editTextStartTime);
        //generate id?
        int id = 001;
        Event event = new Event(id,name.getText().toString(),location.getText().toString(),date.getText().toString(),start_time.getText().toString(),end_time.getText().toString());
        //send event to the db
        intent.putExtra(EXTRA_NAME,name.getText().toString());
        intent.putExtra(EXTRA_LOCATION,location.getText().toString());
        intent.putExtra(EXTRA_DATE,date.getText().toString());
        intent.putExtra(EXTRA_START_TIME,start_time.getText().toString());
        intent.putExtra(EXTRA_END_TIME,end_time.getText().toString());

        startActivity(intent);
    }


}