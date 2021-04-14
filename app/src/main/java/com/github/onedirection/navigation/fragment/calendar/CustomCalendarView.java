package com.github.onedirection.navigation.fragment.calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.github.onedirection.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomCalendarView extends LinearLayout {

    private static final int MAX_CALENDAR_DAYS = 42;
    ImageButton NextButton, PreviousButton;
    TextView CurrentDate;
    GridView gridView;


    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    Context context;
    List<Date> dates = new ArrayList<>();
    List<CalendarEvents> eventsList = new ArrayList<>();


    public CustomCalendarView(Context context) {
        super(context);
    }

    public CustomCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        InitializeLayout();
        SetUpCalendar();

        PreviousButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, -1);
                SetUpCalendar();
            }
        });

        NextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, 1);
                SetUpCalendar();
            }
        });
    }

    public CustomCalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void InitializeLayout(){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_calendar_view, this);
        NextButton = view.findViewById(R.id.nextBtn);
        PreviousButton = view.findViewById(R.id.previousBtn);
        CurrentDate = view.findViewById(R.id.currentDate);
        gridView = view.findViewById(R.id.gridView);
    }

    private void SetUpCalendar(){
        Date currentDate = calendar.getTime();
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        CurrentDate.setText(dayOfMonth + " / " + monthOfYear + " / " + year);
    }
}
