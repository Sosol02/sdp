package com.github.onedirection.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.github.onedirection.R;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EventCreatorMainFragment extends Fragment {

    private final static List<TemporalUnit> PERIODS = Collections.unmodifiableList(Arrays.asList(
            ChronoUnit.MINUTES,
            ChronoUnit.HOURS,
            ChronoUnit.WEEKS,
            ChronoUnit.YEARS
    ));
    public static final String LOGCAT_TAG = "EventCreator";

    private EventCreatorViewModel model;
    private EditText name;
    private EditText customLocation;
    private Button geolocation;
    private CheckBox useGeolocation;
    private CheckBox isRecurrent;
    private Spinner recurrencePeriodType;
    private EditText recurrencePeriodAmount;

    private void setupDateTimeButtons(MutableLiveData<ZonedDateTime> data, int nameId, int viewId) {
        View dateTime = getView().findViewById(viewId);
        ((TextView) dateTime.findViewById(R.id.label)).setText(nameId);
        data.observe(getViewLifecycleOwner(), zonedDateTime -> {
            Button startTimeBtn = dateTime.findViewById(R.id.time);
            Button startDateBtn = dateTime.findViewById(R.id.date);
            startTimeBtn.setText(LocalTime.of(zonedDateTime.getHour(), zonedDateTime.getMinute()).toString());
            startDateBtn.setText(zonedDateTime.toLocalDate().toString());
        });

        dateTime.findViewById(R.id.time).setOnClickListener(v -> showTimePicker(v, data));
        dateTime.findViewById(R.id.date).setOnClickListener(v -> showDatePicker(v, data));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_creator_main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.model = new ViewModelProvider(requireActivity()).get(EventCreatorViewModel.class);
        this.name = getView().findViewById(R.id.editEventName);
        this.customLocation = getView().findViewById(R.id.editEventLocationName);
        this.geolocation = getView().findViewById(R.id.buttonGotoGeolocation);
        this.useGeolocation = getView().findViewById(R.id.checkGeolocation);
        this.isRecurrent = getView().findViewById(R.id.checkEventRecurrence);
        this.recurrencePeriodType = getView().findViewById(R.id.spinnerRecurrencePeriodType);
        this.recurrencePeriodAmount = getView().findViewById(R.id.editRecurrenceAmount);

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

        setupDateTimeButtons(model.startTime, R.string.start_time_text, R.id.startDateTime);
        setupDateTimeButtons(model.endTime, R.string.end_time_text, R.id.endDateTime);

        geolocation.setOnClickListener(v -> gotoGeolocation());

        getView().findViewById(R.id.buttonEventAdd).setOnClickListener(v -> {
            model.callback.accept(generateEvent(), model.isEditing);
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

        // Recurrence setup
        isRecurrent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            model.isRecurrent.postValue(isChecked);
        });


        getView().findViewById(R.id.recurrencePeriod).setEnabled(!model.isEditing);
        model.isRecurrent.observe(getViewLifecycleOwner(), aBoolean -> {
            getView().findViewById(R.id.recurrencePeriod).setVisibility(aBoolean ? View.VISIBLE : View.GONE);
            getView().findViewById(R.id.recurrenceUntil).setVisibility(aBoolean ? View.VISIBLE : View.GONE);
        });

        setupDateTimeButtons(model.recurrenceEnd, R.string.recurrence_end_text, R.id.recurrenceUntil);

        recurrencePeriodType.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, PERIODS));
        recurrencePeriodType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRecurrencePeriod();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.wtf(LOGCAT_TAG, "Nothing should not be a selectable.");
            }
        });

    }

    private void gotoGeolocation() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.eventCreatorFragmentContainer, EventCreatorGeolocationFragment.class, null)
                .setReorderingAllowed(true)
                .commit();
    }

    private void updateRecurrencePeriod() {
        TemporalUnit unit = PERIODS.get(recurrencePeriodType.getSelectedItemPosition());
        int amount = 1;
        try {
            amount = Integer.parseUnsignedInt(recurrencePeriodAmount.getText().toString());
        } catch (NumberFormatException e) {
            recurrencePeriodAmount.setText(String.format("%s", amount));
        }

        model.recurrencePeriod.postValue(unit.getDuration().multipliedBy(amount));
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