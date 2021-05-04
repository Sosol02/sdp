package com.github.onedirection.events;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.github.onedirection.EventQueries;
import com.github.onedirection.R;
import com.github.onedirection.database.Database;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventsListAdapter extends ArrayAdapter {
    private Context context;
    private List<Event> events;
    private LayoutInflater layoutInflater;
    private Runnable onEditEvent;
    private Runnable onDeleteEvent;

    public EventsListAdapter(Context applicationContext, List<Event> events, Runnable onEditEvent, Runnable onDeleteEvent){
        super(applicationContext, R.layout.event_view_in_list);
        this.context = applicationContext;
        this.onEditEvent = onEditEvent;
        this.onDeleteEvent = onDeleteEvent;
        this.events = events;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = layoutInflater.inflate(R.layout.event_view_in_list, null);
        TextView eventName = (TextView) convertView.findViewById(R.id.eventName);
        TextView eventDate = (TextView) convertView.findViewById(R.id.eventDate);
        TextView eventStartTime = (TextView) convertView.findViewById(R.id.eventStartTime);
        TextView eventEndTime = (TextView) convertView.findViewById(R.id.eventEndTime);
        TextView eventLocation = (TextView) convertView.findViewById(R.id.eventLocation);
        Button eventEditButton = (Button) convertView.findViewById(R.id.eventEditButton);
        Button eventDeleteButton = (Button) convertView.findViewById(R.id.eventDeleteButton);



        Event event = events.get(position);
        eventName.setText(event.getName());
        eventDate.setText("Date: " + event.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        eventStartTime.setText("From: " + event.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        eventEndTime.setText("To: " + event.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        eventLocation.setText(event.getLocationName());

        eventEditButton.setOnClickListener(v -> {
            if(onEditEvent != null){
                onEditEvent.run();
            }
        });

        Database database = Database.getDefaultInstance();
        EventQueries queryManager = new EventQueries(database);
        eventDeleteButton.setOnClickListener(v -> {
            queryManager.removeEvent(event.getId());
            if(onDeleteEvent != null){
                onDeleteEvent.run();
            }
        });
        return convertView;



    }
}
