package com.github.onedirection.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.onedirection.R;
import com.github.onedirection.database.ConcreteDatabase;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.events.Event;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class EventViewAdapter extends RecyclerView.Adapter {
    private Context context;
    ConcreteDatabase db;
    private List<Event> events;

    public EventViewAdapter(Context context) throws ExecutionException, InterruptedException {
        this.context = context;
        db = ConcreteDatabase.getDatabase();
        events = db.retrieveAll(EventStorer.getInstance()).get();
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_event_viewer_event, parent, false);
        return new EventHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Event event = events.get(position);
        ((EventHolder) holder).bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private class EventHolder extends RecyclerView.ViewHolder{

        EventHolder(View view){
            super(view);
        }
        void bind(Event event){

        }
    }

}
