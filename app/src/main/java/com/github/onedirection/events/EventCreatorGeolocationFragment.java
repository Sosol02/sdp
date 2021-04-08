package com.github.onedirection.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.onedirection.R;
import com.github.onedirection.geolocation.DeviceLocationProvider;
import com.github.onedirection.geolocation.GeocodingService;
import com.github.onedirection.geolocation.LocationProvider;
import com.github.onedirection.geolocation.NominatimGeocoding;

import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

public class EventCreatorGeolocationFragment extends Fragment {

    private static final String NO_LOCATION = "None";

    private DeviceLocationProvider locationProvider;

    private EventCreatorViewModel model;
    private GeocodingService geocoding;

    private ProgressBar requestLoading;
    private CompletableFuture<?> lastRequest;

    private EditText locationQuery;
    private TextView locationSelected;
    private TextView locationSelectedFull;
    private Button cancel;
    private Button validate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.event_creator_geolocation_fragment, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.locationProvider = (DeviceLocationProvider) requireActivity();

        this.model = new ViewModelProvider(requireActivity()).get(EventCreatorViewModel.class);
        this.geocoding = new NominatimGeocoding(getContext());

        this.requestLoading = getView().findViewById(R.id.progressBarEventCreatorLoading);
        this.lastRequest = CompletableFuture.completedFuture(null);

        this.locationQuery = getView().findViewById(R.id.editLocationQuery);
        this.locationSelected = getView().findViewById(R.id.textLocationResult);
        this.locationSelectedFull = getView().findViewById(R.id.textSelectedLocationFull);
        this.cancel = getView().findViewById(R.id.buttonCancelGeolocation);
        this.validate = getView().findViewById(R.id.buttonSetGeolocation);

        // Model listeners
        model.coordinates.observe(getViewLifecycleOwner(), coordinates -> {
            boolean valid = coordinates.isPresent();

            locationSelected.setText(valid ? coordinates.get().toString().split(",")[0] : NO_LOCATION);

            if (valid) {
                locationSelectedFull.setVisibility(View.VISIBLE);
                locationSelectedFull.setText(coordinates.get().toString());
            } else {
                locationSelectedFull.setVisibility(View.GONE);
            }

            validate.setEnabled(valid);
        });

        // Click listeners
        getView().findViewById(R.id.buttonSearchLocation).setOnClickListener(v -> {
            lastRequest.cancel(true);
            requestLoading.setVisibility(View.VISIBLE);

            model.incrementLoad();
            lastRequest = geocoding.getBestNamedCoordinates(locationQuery.getText().toString()).whenComplete(
                    (coordinates, throwable) -> {
                        if (coordinates != null) {
                            model.coordinates.postValue(Optional.of(coordinates));
                            requestLoading.setVisibility(View.INVISIBLE);
                        }
                        if (!(throwable instanceof CancellationException)) {
                            requestLoading.setVisibility(View.INVISIBLE);
                        }

                        model.decrementLoad();
                    }
            );
        });

        getView().findViewById(R.id.buttonUseCurrentLocation).setOnClickListener(v -> {
            lastRequest.cancel(true);
            requestLoading.setVisibility(View.VISIBLE);

            model.incrementLoad();
            lastRequest = locationProvider.getNextLocation().whenComplete((coordinates, throwable) -> {
                if (coordinates != null) {
                    lastRequest = geocoding.getBestNamedCoordinates(coordinates).whenComplete(
                            (namedCoordinates, throwable1) -> {
                                if (namedCoordinates != null) {
                                    model.coordinates.postValue(Optional.of(namedCoordinates));
                                    requestLoading.setVisibility(View.INVISIBLE);
                                }
                                model.decrementLoad();
                            }
                    );
                } else {
                    if (!(throwable instanceof CancellationException)) {
                        requestLoading.setVisibility(View.INVISIBLE);
                    }

                    model.decrementLoad();
                }
            });
        });

        cancel.setOnClickListener(v -> gotoMain());

        validate.setOnClickListener(v -> {
            model.useGeolocation.postValue(true);
            gotoMain();
        });
    }

    private void gotoMain() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.eventCreatorFragmentContainer, EventCreatorMainFragment.class, null)
                .setReorderingAllowed(true)
                .commit();
    }
}