package com.github.onedirection.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.onedirection.R;
import com.github.onedirection.geolocation.DeviceLocationProviderActivity;
import com.github.onedirection.geolocation.GeocodingService;
import com.github.onedirection.geolocation.NamedCoordinates;
import com.github.onedirection.geolocation.NominatimGeocoding;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.github.onedirection.utils.OnTextChanged.onTextChanged;

public class EventCreatorGeolocationFragment extends Fragment {

    private static final String NO_LOCATION = "None";
    private static final int SEARCH_COUNT = 5;
    // TODO: research this threshold a bit more
    private static final double COORDINATES_TOLERANCE = 5e-3;

    private DeviceLocationProviderActivity locationProvider;

    private EventCreatorViewModel model;
    private GeocodingService geocoding;

    private CheckBox usePhoneLocation;

    private MutableLiveData<Optional<NamedCoordinates>> phoneLocation;
    private MutableLiveData<List<NamedCoordinates>> geocodingMatches;

    private ProgressBar requestLoading;
    private CompletableFuture<List<NamedCoordinates>> lastRequest;

    private EditText locationQuery;
    private TextView locationSelected;
    private TextView locationSelectedFull;
    private Button cancel;
    private Button validate;

    private RecyclerView locationList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.event_creator_geolocation_fragment, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ////////////////////////////////
        //  Attributs initialization  //
        ////////////////////////////////
        this.locationProvider = (DeviceLocationProviderActivity) requireActivity();

        this.model = new ViewModelProvider(requireActivity()).get(EventCreatorViewModel.class);
        this.geocoding = new NominatimGeocoding(getContext());

        this.usePhoneLocation = getView().findViewById(R.id.buttonUseCurrentLocation);

        this.phoneLocation = new MutableLiveData<>(Optional.empty());
        this.geocodingMatches = new MutableLiveData<>(new ArrayList<>());

        this.requestLoading = getView().findViewById(R.id.progressBarEventCreatorLoading);
        this.lastRequest = CompletableFuture.completedFuture(null);

        this.locationQuery = getView().findViewById(R.id.editLocationQuery);
        this.locationSelected = getView().findViewById(R.id.textLocationResult);
        this.locationSelectedFull = getView().findViewById(R.id.textSelectedLocationFull);
        this.cancel = getView().findViewById(R.id.buttonCancelGeolocation);
        this.validate = getView().findViewById(R.id.buttonSetGeolocation);

        // Setup recycler view
        this.locationList = (RecyclerView) getView().findViewById(R.id.locationMatchesList);
        this.locationList.setLayoutManager(new LinearLayoutManager(getActivity()));


        ////////////////////////////////
        //         Logic setup        //
        ////////////////////////////////

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

        // Results listeners
        phoneLocation.observe(getViewLifecycleOwner(), namedCoordinates -> updateResults());
        geocodingMatches.observe(getViewLifecycleOwner(), namedCoordinates -> updateResults());

        // Location listener
        this.locationProvider.addObserver((subject, value) -> {
            Optional<NamedCoordinates> current = this.phoneLocation.getValue();
            if(!current.isPresent() || !current.get().areCloseTo(value, COORDINATES_TOLERANCE)) {
                // We don't update GPS location if the offset is too small, in order not to spam
                // the geocoding service.
                geocoding.getBestNamedCoordinates(value).thenAccept(coordinates ->
                        this.phoneLocation.postValue(Optional.of(coordinates))
                );
            }
        });

        // Text edit listener
        locationQuery.addTextChangedListener(onTextChanged(s ->
                setCurrentRequest(
                        generateGeocodingRequest(s)
                )
        ));

        usePhoneLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                locationProvider.startLocationTracking();
            }
            else{
                locationProvider.stopLocationTracking();
            }
        });

        cancel.setOnClickListener(v -> {
            model.useGeolocation.postValue(false);
            gotoMain();
        });

        validate.setOnClickListener(v -> {
            model.useGeolocation.postValue(true);
            gotoMain();
        });
    }

    private Consumer<NamedCoordinates> generateLocationSelectionCallback() {
        return coordinates -> model.coordinates.postValue(Optional.of(coordinates));
    }

    private void updateResults(){
        List<NamedCoordinates> ls = new ArrayList<>(geocodingMatches.getValue());
        if(usePhoneLocation.isChecked() && phoneLocation.getValue().isPresent()){
            ls.add(phoneLocation.getValue().get());
        }

        locationList.setAdapter(new LocationsAdapter(ls, generateLocationSelectionCallback()));
    }

    synchronized private void setCurrentRequest(CompletableFuture<List<NamedCoordinates>> request) {
        lastRequest.cancel(true);
        requestLoading.setVisibility(View.VISIBLE);

        model.incrementLoad();
        lastRequest = request.whenComplete((o, t) -> {
            if(!(t instanceof CancellationException)){
                requestLoading.setVisibility(View.INVISIBLE);
            }
            model.decrementLoad();
        });
    }

    private CompletableFuture<List<NamedCoordinates>> generateGeocodingRequest(String query){
        return geocoding.getNamedCoordinates(query, SEARCH_COUNT)
                .whenComplete((coordinates, throwable) -> {
                    if(coordinates != null) {
                        this.geocodingMatches.postValue(coordinates);
                    }
                });
    }

    private void gotoMain() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.eventCreatorFragmentContainer, EventCreatorMainFragment.class, null)
                .setReorderingAllowed(true)
                .commit();
    }
}