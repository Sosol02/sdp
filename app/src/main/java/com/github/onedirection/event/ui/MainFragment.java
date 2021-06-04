package com.github.onedirection.event.ui;

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
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.github.onedirection.R;
import com.github.onedirection.event.model.Event;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.onedirection.utils.OnTextChanged.onTextChanged;

/**
 * Main fragment of the Event creator.
 * Allow to set values for most fields.
 */
public class MainFragment extends Fragment {

    @VisibleForTesting
    final static int MAX_STRING_LENGTH = 50;

    private final static List<TemporalUnit> PERIODS = Collections.unmodifiableList(Arrays.asList(
            ChronoUnit.DAYS,
            ChronoUnit.WEEKS,
            ChronoUnit.MONTHS,
            ChronoUnit.YEARS
    ));

    private ViewModel model;
    private EditText name;
    private EditText customLocation;
    private Button geolocation;
    private CheckBox useGeolocation;
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

        this.model = new ViewModelProvider(requireActivity()).get(ViewModel.class);
        this.name = getView().findViewById(R.id.editEventName);
        this.customLocation = getView().findViewById(R.id.editEventLocationName);
        this.geolocation = getView().findViewById(R.id.buttonGotoGeolocation);
        this.useGeolocation = getView().findViewById(R.id.checkGeolocation);
        CheckBox isRecurrent = getView().findViewById(R.id.checkEventRecurrence);
        this.recurrencePeriodType = getView().findViewById(R.id.spinnerRecurrencePeriodType);
        this.recurrencePeriodAmount = getView().findViewById(R.id.editRecurrenceAmount);

        // Model listeners
        model.name.observe(getViewLifecycleOwner(), str -> {
            if (!name.getText().toString().equals(str))
                name.setText(str);
        });
        model.customLocation.observe(getViewLifecycleOwner(), str -> {
            if (!customLocation.getText().toString().equals(str))
                customLocation.setText(str);
        });

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

        // 'Add Event' button listener
        getView().findViewById(R.id.buttonEventAdd).setOnClickListener(this::addEventCallback);

        // Text listeners
        name.addTextChangedListener(onTextChanged(s -> model.name.postValue(s)));
        customLocation.addTextChangedListener(onTextChanged(s -> model.customLocation.postValue(s)));


        // Checkbox listeners
        useGeolocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            model.useGeolocation.postValue(isChecked);
        });

        // Recurrence setup
        isRecurrent.setChecked(model.isRecurrent.getValue());
        isRecurrent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            model.isRecurrent.postValue(isChecked);
        });


        getView().findViewById(R.id.recurrencePeriod).setEnabled(!(model.isEditing && model.isRecurrent.getValue()));
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
                Log.wtf(EventCreator.LOGCAT_TAG, "Nothing should not be a selectable.");
            }
        });
        recurrencePeriodAmount.setOnFocusChangeListener((v, hasFocus) -> updateRecurrencePeriod());

        // Setup final button
        if (model.isEditing) {
            Button btn = getView().findViewById(R.id.buttonEventAdd);
            btn.setText(R.string.update_event);
        }
    }

    private void gotoGeolocation() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.eventCreatorFragmentContainer, GeolocationFragment.class, null)
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
                time.getValue().getMonthValue() - 1,
                time.getValue().getDayOfMonth()
        );
        datePicker.show();
    }

    private Event generateEvent() {
        return model.generateEvent();
    }

    private boolean fieldsAreValid() {
        String checkMsg = null;
        if (model.name.getValue() == null || model.name.getValue().equals("")) {
            checkMsg = getContext().getString(R.string.empty_event_name);
        } else if (model.name.getValue().length() > MAX_STRING_LENGTH) {
            checkMsg = String.format(getContext().getString(R.string.event_name_too_long), MAX_STRING_LENGTH);
        } else if (model.startTime.getValue().toEpochSecond() > model.endTime.getValue().toEpochSecond()) {
            checkMsg = getContext().getString(R.string.end_before_start);
        } else if (model.endTime.getValue().toEpochSecond() - model.startTime.getValue().toEpochSecond() > ChronoUnit.DAYS.getDuration().getSeconds()) {
            checkMsg = getContext().getString(R.string.event_time_too_long);
        } else if (model.customLocation.getValue() != null && model.customLocation.getValue().length() > MAX_STRING_LENGTH) {
            checkMsg = String.format(getContext().getString(R.string.location_name_too_long), MAX_STRING_LENGTH);
        } else if (model.isRecurrent.getValue()) {
            if (model.recurrenceEnd.getValue().toEpochSecond() < model.startTime.getValue().toEpochSecond()) {
                checkMsg = getContext().getString(R.string.recurrence_end_too_soon);
            }
        } else if (model.useGeolocation.getValue() && !model.coordinates.getValue().isPresent()) {
            checkMsg = getContext().getString(R.string.no_geoloc_set);
        }

        if (checkMsg != null) {
            TextView checksText = getView().findViewById(R.id.checkArgsText);
            checksText.setText(checkMsg);
            checksText.setVisibility(View.VISIBLE);
            return false;
        } else {
            getView().findViewById(R.id.checkArgsText).setVisibility(View.GONE);
            return true;
        }
    }

    private void addEventCallback(View v) {
        if (fieldsAreValid()) {
            model.incrementLoad();
            requireActivity().findViewById(R.id.eventCreatorMainFragment).setEnabled(false);
            model.callback.apply(generateEvent(), model.isEditing).whenComplete((o, throwable) -> {
                if (throwable != null) {
                    Log.d(EventCreator.LOGCAT_TAG, "Event callback failed: " + throwable);
                }
                model.decrementLoad();
                requireActivity().finish();
            });
        }
    }
}