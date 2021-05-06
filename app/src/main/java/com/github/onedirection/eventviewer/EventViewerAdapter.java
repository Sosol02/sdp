package com.github.onedirection.eventviewer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.onedirection.R;
import com.github.onedirection.events.Event;

import java.util.Arrays;
import java.util.List;

public class EventViewerAdapter extends RecyclerView.Adapter<EventViewerAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final View fullView;
        private final TextView name;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.fullView = itemView;
            this.name = itemView.findViewById(R.id.eventName);
        }

        public ViewHolder(@NonNull ViewGroup parent) {
            this(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_event_viewer_adapter, parent, false));
        }

        public void setPosition(int position){
            this.name.setText(events[position].getName());
        }

    }

    private final Event[] events;

    private EventViewerAdapter(Event[] events){
        this.events = Arrays.copyOf(events, events.length);
    }

    public EventViewerAdapter(List<Event> events){
        this(events.toArray(new Event[0]));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setPosition(position);
    }

    @Override
    public int getItemCount() {
        return events.length;
    }
}