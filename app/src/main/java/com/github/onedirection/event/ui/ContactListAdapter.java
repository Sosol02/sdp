package com.github.onedirection.event.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.github.onedirection.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContactListAdapter extends ArrayAdapter {
    private final Context context;
    private final LayoutInflater layoutInflater;
    private final MutableLiveData<List<String>> contacts;


    public ContactListAdapter(@NonNull Context context, MutableLiveData<List<String>> contactList) {
        super(context, R.layout.contact_email_list);
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        Objects.requireNonNull(contactList);
        this.contacts= contactList;
    }

    @Override
    public int getCount() {
        return contacts.getValue().size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return contacts.getValue().get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.single_contact_view, parent, false);
        }

        Button deleteContact = (Button) convertView.findViewById(R.id.contactDeleteButton);
        TextView emailView = (TextView) convertView.findViewById(R.id.contactEmailView);
        emailView.setText(contacts.getValue().get(position));

        deleteContact.setOnClickListener(v -> {
            List<String> newContactList = contacts.getValue();
            newContactList.remove(position);
            contacts.setValue(newContactList);
            this.notifyDataSetChanged();
        });

        return convertView;
    }
}
