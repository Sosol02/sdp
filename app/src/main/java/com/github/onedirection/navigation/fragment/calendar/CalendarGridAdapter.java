package com.github.onedirection.navigation.fragment.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.onedirection.R;
import com.github.onedirection.events.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarGridAdapter extends ArrayAdapter {
    private Calendar currentDate;
    private List<Event> events;
    private LayoutInflater inflater;
    private final List<Date> dates;

    public CalendarGridAdapter(@NonNull Context context, List<Date> dates, Calendar currentDate, List<Event> events) {
        super(context, R.layout.single_cell_layout);
        this.dates = dates;
        this.currentDate = currentDate;
        this.events = events;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Date monthDate = dates.get(position);
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(monthDate);

        int DayNumber = dateCalendar.get(Calendar.DAY_OF_MONTH);
        int displayMonth = dateCalendar.get(Calendar.MONTH) + 1;
        int displayYear = dateCalendar.get(Calendar.YEAR);
        int currentMonth = currentDate.get(Calendar.MONTH) + 1;
        int currentYear = currentDate.get(Calendar.YEAR);
        View view = convertView;


        if (view == null) {
            view = inflater.inflate(R.layout.single_cell_layout, parent, false);
        }


        if (displayMonth == currentMonth) {
            TextView Day_Number = view.findViewById(R.id.calendar_day);
            Day_Number.setText(String.valueOf(DayNumber));
            Calendar eventCalendar = Calendar.getInstance();
            ArrayList<String> arrayList = new ArrayList<>();
            for (int i = 0; i < events.size(); ++i) {

            }
        }
        return view;
    }


    @Override
    public int getCount() {
        return dates.size();
    }

    @Override
    public int getPosition(@Nullable Object item) {
        return dates.indexOf(item);
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return dates.get(position);
    }
}




