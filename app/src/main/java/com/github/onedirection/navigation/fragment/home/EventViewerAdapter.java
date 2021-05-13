package com.github.onedirection.navigation.fragment.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.onedirection.R;
import com.github.onedirection.event.Event;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Adapter for the EventView Recycler
 */

public class EventViewerAdapter extends RecyclerView.Adapter<EventViewerAdapter.ViewHolder> {

    private OnNoteListener mOnNoteListener;
    private final Event[] events;

    public class ViewHolder extends RecyclerView.ViewHolder implements  View.OnLongClickListener {

        private final View fullView;
        private final TextView name;
        private final TextView location;
        private final TextView startTime;
        private final TextView endTime;

        OnNoteListener onNoteListener;

        private ViewHolder(@NonNull View itemView, OnNoteListener onNoteListener) {
            super(itemView);
            this.fullView = itemView;
            this.name = itemView.findViewById(R.id.eventName);
            this.location = itemView.findViewById(R.id.eventLocation);
            this.startTime = itemView.findViewById(R.id.eventStartTime);
            this.endTime = itemView.findViewById(R.id.eventEndTime);

            this.onNoteListener = onNoteListener;
            itemView.setOnLongClickListener(this);

        }

        public ViewHolder(@NonNull ViewGroup parent, OnNoteListener onNoteListener) {
            this(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_view_event_viewer_adapter, parent, false), onNoteListener);
        }

        public void setPosition(int position){

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");
            this.name.setText(events[position].getName());
            this.location.setText(events[position].getLocationName());
            this.startTime.setText(events[position].getStartTime().format(formatter));
            this.endTime.setText(events[position].getEndTime().format(formatter));

        }


        @Override
        public boolean onLongClick(View view) {
            onNoteListener.onNoteClick(getAdapterPosition());
            return false;
        }
    }

    private EventViewerAdapter(Event[] events, OnNoteListener onNoteListener){
        this.events = Arrays.copyOf(events, events.length);
        this.mOnNoteListener = onNoteListener;
    }

    public EventViewerAdapter(List<Event> events, OnNoteListener onNoteListener){
        this(events.toArray(new Event[0]), onNoteListener);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(parent, mOnNoteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setPosition(position);
    }

    @Override
    public int getItemCount() {
        return events.length;
    }

    public interface OnNoteListener{
        void onNoteClick(int position);
    }
}