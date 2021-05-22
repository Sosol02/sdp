package com.github.onedirection.navigation.fragment.calendar;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.github.onedirection.R;
import com.github.onedirection.database.database.Database;
import com.github.onedirection.database.queries.EventQueries;
import com.github.onedirection.event.model.Event;
import com.github.onedirection.event.creator.EventCreator;
import com.github.onedirection.utils.Id;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Adapter used by views listing events that displays a concise description of the events
 */
public class EventsListAdapter extends ArrayAdapter {
    private final Context context;
    private final List<Event> events;
    private final LayoutInflater layoutInflater;
    private final Runnable onEditEvent;
    private final Runnable onDeleteEvent;

    public EventsListAdapter(Context applicationContext, List<Event> events, Runnable onEditEvent, Runnable onDeleteEvent) {
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
        return events.get(position);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.event_view_in_list, parent, false);
        }
        TextView eventName = (TextView) convertView.findViewById(R.id.eventName);
        TextView eventDate = (TextView) convertView.findViewById(R.id.eventDate);
        TextView eventStartTime = (TextView) convertView.findViewById(R.id.eventStartTime);
        TextView eventEndTime = (TextView) convertView.findViewById(R.id.eventEndTime);
        TextView eventLocation = (TextView) convertView.findViewById(R.id.eventLocation);
        Button eventEditButton = (Button) convertView.findViewById(R.id.eventEditButton);
        Button eventDeleteButton = (Button) convertView.findViewById(R.id.eventDeleteButton);


        Event event = events.get(position);
        eventName.setText(event.getName());
        eventDate.setText(String.format("Date: %s", event.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        eventStartTime.setText(String.format("From: %s", event.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
        eventEndTime.setText(String.format("To: %s", event.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
        eventLocation.setText(event.getLocationName());

        eventEditButton.setOnClickListener(v -> {
            if (onEditEvent != null) {
                Intent intent = new Intent(this.getContext(), EventCreator.class);
                EventCreator.putEventExtra(intent, event);
                this.getContext().startActivity(intent);
                onEditEvent.run();
            }
        });

        Database database = Database.getDefaultInstance();
        EventQueries queryManager = new EventQueries(database);
        eventDeleteButton.setOnClickListener(v -> {
            CompletableFuture<Id> eventDeleted = queryManager.removeEvent(event.getId());
            eventDeleted.whenComplete((id, throwable) -> {
                if (onDeleteEvent != null) {
                    onDeleteEvent.run();
                }
            });
        });
        return convertView;


    }
}
