package com.github.onedirection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.github.onedirection.utils.EventViewAdapter;

public class event_viewer_event extends AppCompatActivity {

    private RecyclerView eventRecycler;
    private EventViewAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer_event);

        eventRecycler = (RecyclerView) findViewById(R.id.recycler_gchat);
        eventAdapter = new EventViewAdapter(this, eventList);
        eventRecycler.setLayoutManager(new LinearLayoutManager(this));
    }
}