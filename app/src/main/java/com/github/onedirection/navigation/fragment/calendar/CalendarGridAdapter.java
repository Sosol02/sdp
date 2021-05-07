package com.github.onedirection.navigation.fragment.calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.onedirection.R;
import com.github.onedirection.event.Event;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarGridAdapter extends ArrayAdapter {
    private final Calendar currentDate;
    private final List<Event> events;
    private final LayoutInflater inflater;
    private final List<Date> dates;


    public CalendarGridAdapter(@NonNull Context context, List<Date> dates, Calendar currentDate, List<Event> events) {
        super(context, R.layout.single_cell_layout);
        this.currentDate = currentDate;
        this.events = events;
        this.inflater = LayoutInflater.from(context);
        this.dates = dates;
    }


    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Date monthDate = dates.get(position);
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(monthDate);

        int dayNumber = dateCalendar.get(Calendar.DAY_OF_MONTH);
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
            TextView EventNumber = view.findViewById(R.id.events_id);

            Day_Number.setText(String.valueOf(dayNumber));
            Calendar eventCalendar = Calendar.getInstance();
            int nbOfEventsInDay = 0;
            for (int i = 0; i < events.size(); ++i) {
                eventCalendar.setTime(Date.from(Instant.ofEpochSecond(events.get(i).getStartTime().toEpochSecond())));
                if (dayNumber == eventCalendar.get(Calendar.DAY_OF_MONTH) && displayMonth == eventCalendar.get(Calendar.MONTH) + 1
                        && displayYear == eventCalendar.get(Calendar.YEAR)) {
                    nbOfEventsInDay++;
                    if (nbOfEventsInDay == 1) {
                        EventNumber.setText(nbOfEventsInDay + " Event");
                    } else {
                        EventNumber.setText(nbOfEventsInDay + " Events");
                    }
                }
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




