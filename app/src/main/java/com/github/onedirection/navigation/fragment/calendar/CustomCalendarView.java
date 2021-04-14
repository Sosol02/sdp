package com.github.onedirection.navigation.fragment.calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

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
    }
}
