package com.github.onedirection.interoperability.gcalendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.onedirection.R;

/**
 * Fragment which allows to import a Google Calendar into the calendar.
 */
public class ImportFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.gcalendar_fragment_import, container, false);

        view.findViewById(R.id.buttonGCalendarImport).setOnClickListener(this::doSomething);

        return view;
    }

    private void doSomething(View ignored) {
    }
}
