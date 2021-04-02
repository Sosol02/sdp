package com.github.onedirection.events;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.github.onedirection.EventsView;
import com.github.onedirection.R;
import com.github.onedirection.geocoding.LocationProvider;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EventCreatorMainFragment extends Fragment {

    private EventCreatorViewModel model;
    private ProgressBar requestLoading;
    private CompletableFuture<?> lastRequest;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_creator_main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.model = new ViewModelProvider(requireActivity()).get(EventCreatorViewModel.class);
        this.requestLoading = getView().findViewById(R.id.progressBarEventCreatorLoading);
        this.lastRequest = CompletableFuture.completedFuture(null);

        model.startTime.observe(getViewLifecycleOwner(), zonedDateTime -> {
            Button startTimeBtn = getView().findViewById(R.id.buttonStartTime);
            Button startDateBtn = getView().findViewById(R.id.buttonStartDate);
            startTimeBtn.setText(LocalTime.of(zonedDateTime.getHour(), zonedDateTime.getMinute()).toString());
            startDateBtn.setText(zonedDateTime.toLocalDate().toString());
        });

        model.endTime.observe(getViewLifecycleOwner(), zonedDateTime -> {
            Button endTimeBtn = getView().findViewById(R.id.buttonEndTime);
            Button endDateBtn = getView().findViewById(R.id.buttonEndDate);
            endTimeBtn.setText(LocalTime.of(zonedDateTime.getHour(), zonedDateTime.getMinute()).toString());
            endDateBtn.setText(zonedDateTime.toLocalDate().toString());
        });

        // Click listeners

        getView().findViewById(R.id.buttonStartTime).setOnClickListener(v -> showTimePicker(v, model.startTime));
        getView().findViewById(R.id.buttonEndTime).setOnClickListener(v -> showTimePicker(v, model.endTime));
        getView().findViewById(R.id.buttonStartDate).setOnClickListener(v -> showDatePicker(v, model.startTime));
        getView().findViewById(R.id.buttonEndTime).setOnClickListener(v -> showDatePicker(v, model.endTime));

        getView().findViewById(R.id.buttonUsePhoneLocation).setOnClickListener(v -> {
            lastRequest.cancel(true);
            requestLoading.setVisibility(View.VISIBLE);

            model.incrementLoad();
            lastRequest = LocationProvider.getCurrentLocation(requireActivity()).whenComplete((coordinates, throwable) -> {
                if(coordinates != null){
                    model.coordinates.setValue(Optional.of(coordinates));
                    requestLoading.setVisibility(View.INVISIBLE);
                }
                model.decrementLoad();
            });
        });

        getView().findViewById(R.id.buttonEventAdd).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), EventsView.class);
            EventCreator.putEventExtra(intent, generateEvent());
            startActivity(intent);
        });
    }

    private void showTimePicker(View v, MutableLiveData<ZonedDateTime> time){
        TimePickerDialog timePicker = new TimePickerDialog(
                v.getContext(),
                (view, hourOfDay, minute) -> {
                    ZonedDateTime startTime = time.getValue();
                    time.setValue(ZonedDateTime.of(startTime.toLocalDate(), LocalTime.of(hourOfDay, minute), startTime.getZone()));
                },
                time.getValue().getHour(),
                time.getValue().getMinute(),
                true);
        timePicker.show();
    }

    private void showDatePicker(View v, MutableLiveData<ZonedDateTime> time){
        DatePickerDialog datePicker = new DatePickerDialog(
                v.getContext(),
                (view, year, month, dayOfMonth) -> {
                    ZonedDateTime startTime = time.getValue();
                    time.setValue(ZonedDateTime.of(
                            LocalDate.of(year, month + 1, dayOfMonth),
                            startTime.toLocalTime(),
                            startTime.getZone()
                    ));
                },
                time.getValue().getYear(),
                time.getValue().getMonthValue(),
                time.getValue().getDayOfMonth()
        );
        datePicker.show();
    }

    private Event generateEvent() {
        EditText name = getView().findViewById(R.id.editEventName);
        EditText loc = getView().findViewById(R.id.editEventLocationName);

        return model.generateEvent(name.getText().toString(), loc.getText().toString());
    }

}