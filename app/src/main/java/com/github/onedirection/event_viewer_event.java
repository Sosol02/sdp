package com.github.onedirection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.github.onedirection.utils.EventViewAdapter;

import java.util.concurrent.ExecutionException;

public class event_viewer_event extends AppCompatActivity {

    private RecyclerView eventRecycler;
    private EventViewAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer_event);

        eventRecycler = (RecyclerView) findViewById(R.id.recycler_gchat);
        try {
            eventAdapter = new EventViewAdapter(this);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        eventRecycler.setLayoutManager(new LinearLayoutManager(this));
    }
}