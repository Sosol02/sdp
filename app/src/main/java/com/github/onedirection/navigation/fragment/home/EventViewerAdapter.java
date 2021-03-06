package com.github.onedirection.navigation.fragment.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.onedirection.R;
import com.github.onedirection.event.model.Event;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Adapter for the EventView Recycler
 */

public class EventViewerAdapter extends RecyclerView.Adapter<EventViewerAdapter.ViewHolder> {

    private final OnNoteListener mOnNoteListener;
    private final Event[] events;

    public class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener {

        private final TextView name;
        private final TextView location;
        private final TextView startTime;
        private final TextView endTime;
        private final ImageButton favorite;

        final OnNoteListener onNoteListener;

        private ViewHolder(@NonNull View itemView, OnNoteListener onNoteListener) {
            super(itemView);
            this.name = itemView.findViewById(R.id.eventName);
            this.location = itemView.findViewById(R.id.eventLocation);
            this.startTime = itemView.findViewById(R.id.eventStartTime);
            this.endTime = itemView.findViewById(R.id.eventEndTime);
            this.favorite = itemView.findViewById(R.id.favoriteButton);

            this.onNoteListener = onNoteListener;
            itemView.setOnClickListener(this);

        }

        public ViewHolder(@NonNull ViewGroup parent, OnNoteListener onNoteListener) {
            this(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_view_event_viewer_adapter, parent, false), onNoteListener);
        }

        /**
         * Method used to bind the viewHolder to its components
         *
         * @param position The position in the recyclerView
         */
        public void setPosition(int position){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm");
            this.name.setText(events[position].getName());
            if(events[position].getLocationName() != ""){
                this.location.setText(events[position].getLocationName());
            }else{
                this.location.setText(R.string.no_location_event);
            }
            this.startTime.setText(events[position].getStartTime().format(formatter));
            this.endTime.setText(events[position].getEndTime().format(formatter));

            if(events[position].getIsFavorite()){
                favorite.setVisibility(View.VISIBLE);
            }else{
                favorite.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View view) {
            onNoteListener.onNoteClick(getBindingAdapterPosition());
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
