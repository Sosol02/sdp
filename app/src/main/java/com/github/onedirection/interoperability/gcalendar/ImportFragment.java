package com.github.onedirection.interoperability.gcalendar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.github.onedirection.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.Task;

import static com.github.onedirection.interoperability.gcalendar.GoogleCalendar.LOGCAT_TAG;

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
