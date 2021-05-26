package com.github.onedirection.interoperability.gcalendar;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import com.github.onedirection.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.Objects;

import static com.github.onedirection.interoperability.gcalendar.GoogleCalendar.LOGCAT_TAG;
import static com.github.onedirection.interoperability.gcalendar.Login.createLogger;
import static com.github.onedirection.interoperability.gcalendar.Login.logIn;
import static com.github.onedirection.interoperability.gcalendar.Login.logOut;

/**
 * Fragment which allows to import the calendar into Google Calendar.
 */
public class ImportFragment extends Fragment {

    private ActivityResultLauncher<Intent> loginGoogle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.loginGoogle = createLogger(this, this::importEvents);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.gcalendar_fragment_import, container, false);

        view.findViewById(R.id.buttonGCalendarImport).setOnClickListener(this::googleLogin);

        return view;
    }


    private void googleLogin(View ignored) {
        logIn(requireContext(), loginGoogle);
    }

    private void importEvents(GoogleSignInAccount account) {
        Account a = Objects.requireNonNull(Objects.requireNonNull(account).getAccount());

        Log.d(LOGCAT_TAG, "Events will be imported from: " + a.name);
        GoogleCalendar.importEvent(
                requireContext(),
                a
        ).whenComplete((ignored1, ignored2) -> logOut(requireContext()));
    }
}
