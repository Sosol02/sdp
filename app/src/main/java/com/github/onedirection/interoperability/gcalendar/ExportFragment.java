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
import com.github.onedirection.database.implementation.Database;
import com.github.onedirection.database.queries.EventQueries;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.Objects;

import static com.github.onedirection.interoperability.gcalendar.GoogleCalendar.LOGCAT_TAG;
import static com.github.onedirection.interoperability.gcalendar.Login.createLogger;
import static com.github.onedirection.interoperability.gcalendar.Login.logIn;
import static com.github.onedirection.interoperability.gcalendar.Login.logOut;

/**
 * Fragment which allows to export the calendar into Google Calendar.
 */
public class ExportFragment extends Fragment {

    private ActivityResultLauncher<Intent> loginGoogle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.loginGoogle = createLogger(this, this::exportEvents);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.gcalendar_fragment_export, container, false);

        view.findViewById(R.id.buttonGCalendarExport).setOnClickListener(this::googleLogin);

        return view;
    }

    private void googleLogin(View ignored) {
        logIn(requireContext(), loginGoogle);
    }

    private void exportEvents(GoogleSignInAccount account) {
        Account a = Objects.requireNonNull(Objects.requireNonNull(account).getAccount());

        Log.d(LOGCAT_TAG, "Events will be exported to: " + a.name);
        GoogleCalendar.exportEvents(
                requireContext(),
                a,
                EventQueries.getAllEvents(Database.getDefaultInstance())
        ).whenComplete((ignored1, ignored2) -> logOut(requireContext()));
    }

}