package com.github.onedirection.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.github.onedirection.R;
import com.github.onedirection.database.Database;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;

public class EventCreatorMainFragment extends Fragment {

    private EventCreatorViewModel model;
    private CompletableFuture<?> lastRequest;
    private EditText name;
    private EditText customLocation;
    private Button geolocation;
    private CheckBox useGeolocation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_creator_main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.model = new ViewModelProvider(requireActivity()).get(EventCreatorViewModel.class);
        this.lastRequest = CompletableFuture.completedFuture(null);
        this.name = getView().findViewById(R.id.editEventName);
        this.customLocation = getView().findViewById(R.id.editEventLocationName);
        this.geolocation = getView().findViewById(R.id.buttonGotoGeolocation);
        this.useGeolocation = getView().findViewById(R.id.checkGeolocation);

        // Model listeners
        model.name.observe(getViewLifecycleOwner(), str -> name.setText(str));
        model.customLocation.observe(getViewLifecycleOwner(), str -> customLocation.setText(str));

        model.useGeolocation.observe(getViewLifecycleOwner(), b -> {
            useGeolocation.setChecked(b);
            if (b) {
                customLocation.setVisibility(View.GONE);
                geolocation.setVisibility(View.VISIBLE);
            } else {
                customLocation.setVisibility(View.VISIBLE);
                geolocation.setVisibility(View.GONE);
            }
        });

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

        geolocation.setOnClickListener(v -> gotoGeolocation());

        getView().findViewById(R.id.buttonEventAdd).setOnClickListener(v -> {
            Event event = generateEvent();
            Database.getDefaultInstance().store(event);
            requireActivity().finish();
        });

        // Text listeners
        name.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                model.name.postValue(name.getText().toString());
            }
        });
        customLocation.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                model.customLocation.postValue(customLocation.getText().toString());
            }
        });


        // Checkbox listeners
        useGeolocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (model.coordinates.getValue().isPresent()) {
                    model.useGeolocation.postValue(true);
                } else {
                    gotoGeolocation();
                }
            } else {
                model.useGeolocation.postValue(false);
            }
        });
    }

    private void gotoGeolocation() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.eventCreatorFragmentContainer, EventCreatorGeolocationFragment.class, null)
                .setReorderingAllowed(true)
                .commit();
    }

    private void showTimePicker(View v, MutableLiveData<ZonedDateTime> time) {
        TimePickerDialog timePicker = new TimePickerDialog(
                v.getContext(),
                (view, hourOfDay, minute) ->
                        time.postValue(ZonedDateTime.of(time.getValue().toLocalDate(), LocalTime.of(hourOfDay, minute), time.getValue().getZone()))
                ,
                time.getValue().getHour(),
                time.getValue().getMinute(),
                true);
        timePicker.show();
    }

    private void showDatePicker(View v, MutableLiveData<ZonedDateTime> time) {
        DatePickerDialog datePicker = new DatePickerDialog(
                v.getContext(),
                (view, year, month, dayOfMonth) ->
                        time.postValue(ZonedDateTime.of(
                                LocalDate.of(year, month + 1, dayOfMonth),
                                time.getValue().toLocalTime(),
                                time.getValue().getZone()
                        ))
                ,
                time.getValue().getYear(),
                time.getValue().getMonthValue(),
                time.getValue().getDayOfMonth()
        );
        datePicker.show();
    }

    private Event generateEvent() {
        return model.generateEvent();
    }

}