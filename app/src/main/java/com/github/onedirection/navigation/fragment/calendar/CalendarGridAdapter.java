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
import com.github.onedirection.event.model.Event;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Adapter used by the calendar to display the date and number of events on that day
 */
public class CalendarGridAdapter extends ArrayAdapter {
    private final List<Event> events;
    private final LayoutInflater inflater;
    private final List<Date> dates;
    private final int currentMonth;



    public CalendarGridAdapter(@NonNull Context context, List<Date> dates, Calendar currentDate, List<Event> events) {
        super(context, R.layout.single_cell_layout);
        this.events = events;
        this.inflater = LayoutInflater.from(context);
        this.dates = dates;
        this.currentMonth = currentDate.get(Calendar.MONTH) + 1;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Date monthDate = dates.get(position);
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(monthDate);

        int dayNumber = dateCalendar.get(Calendar.DAY_OF_MONTH);
        int displayMonth = dateCalendar.get(Calendar.MONTH) + 1;
        int displayYear = dateCalendar.get(Calendar.YEAR);

        View view = convertView;

        if (view == null) {
            view = inflater.inflate(R.layout.single_cell_layout, parent, false);
        }

        if (displayMonth == currentMonth) {
            TextView textDayNumber = view.findViewById(R.id.calendar_day);
            TextView eventNumber = view.findViewById(R.id.nb_events);

            textDayNumber.setText(String.valueOf(dayNumber));
            Calendar eventCalendar = Calendar.getInstance();
            int nbOfEventsInDay = 0;
            for (int i = 0; i < events.size(); ++i) {
                eventCalendar.setTime(Date.from(Instant.ofEpochSecond(events.get(i).getStartTime().toEpochSecond())));
                if (dayNumber == eventCalendar.get(Calendar.DAY_OF_MONTH) && displayMonth == eventCalendar.get(Calendar.MONTH) + 1
                        && displayYear == eventCalendar.get(Calendar.YEAR)) {
                    nbOfEventsInDay++;
                }
            }
            if(nbOfEventsInDay > 0) {
                eventNumber.setText(String.valueOf(nbOfEventsInDay));
                eventNumber.setBackgroundResource(R.drawable.rounded_corner);
            } else {
                eventNumber.setBackgroundResource(android.R.color.transparent);
            }
        }
        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        Date monthDate = dates.get(position);
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(monthDate);
        int displayMonth = dateCalendar.get(Calendar.MONTH) + 1;
        return currentMonth == displayMonth;
    }

    @Override
    public int getCount() {
        return dates.size();
    }

    @Override
    public Object getItem(int position) {
        return dates.get(position);
    }


}




