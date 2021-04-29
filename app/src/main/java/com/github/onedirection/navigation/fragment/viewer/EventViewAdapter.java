package com.github.onedirection.navigation.fragment.viewer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    private List<Event> events;

    public EventViewAdapter(Context context, List<Event> events)  {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_viewer, parent, false);
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
        TextView eventName;
        EventHolder(View view){
            super(view);
            eventName = view.findViewById(R.id.name);
        }
        void bind(Event event){
            eventName.setText(event.getName());
        }
    }

}
