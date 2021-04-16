package com.github.onedirection.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.onedirection.database.ConcreteDatabase;
import com.github.onedirection.events.Event;

import java.util.List;

public class EventViewAdapter extends RecyclerView.Adapter {
    private Context context;
    ConcreteDatabase db;
    private List<Event> events;

    public EventViewAdapter(Context context, List<Event> events){
        this.context = context;
        db = ConcreteDatabase.getDatabase();
        this.events = events;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
        return new EventHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private class EventHolder extends RecyclerView.ViewHolder{
        EventHolder(View view){
            super(view);
        }
    }

}
