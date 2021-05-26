package com.github.onedirection.event.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.onedirection.R;
import com.github.onedirection.geolocation.model.NamedCoordinates;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Adapter used to display NamedCoordinates in a recycler view.
 */
public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final View fullView;
        private final TextView name;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.fullView = itemView;
            this.name = itemView.findViewById(R.id.locationName);
        }

        public ViewHolder(@NonNull ViewGroup parent) {
            this(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_location_adapter, parent, false));
        }

        public void setPosition(int position) {
            this.name.setText(locations[position].name);
            this.fullView.setOnClickListener(v -> callback.accept(locations[position]));
        }
    }

    private final NamedCoordinates[] locations;
    private final Consumer<NamedCoordinates> callback;

    private LocationsAdapter(NamedCoordinates[] locations, Consumer<NamedCoordinates> callback) {
        this.locations = Arrays.copyOf(locations, locations.length);
        this.callback = callback;
    }

    public LocationsAdapter(List<NamedCoordinates> locations, Consumer<NamedCoordinates> callback) {
        this(locations.toArray(new NamedCoordinates[0]), callback);
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
        return locations.length;
    }
}