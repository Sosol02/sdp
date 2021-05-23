package com.github.onedirection.interoperability.gcalendar;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.github.onedirection.R;
import com.github.onedirection.database.implementation.Database;
import com.github.onedirection.database.queries.EventQueries;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

import java.util.Objects;

import static com.github.onedirection.interoperability.gcalendar.GoogleCalendar.LOGCAT_TAG;

/**
 * Fragment which allows to export the calendar into Google Calendar.
 */
public class ExportFragment extends Fragment {

    // From https://developers.google.com/identity/protocols/oauth2/scopes#calendar
    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";

    private static final GoogleSignInOptions GSI_OPTIONS =
            new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(new Scope(CALENDAR_SCOPE))
                    .build();

    private ActivityResultLauncher<Intent> loginGoogle;

    public ExportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.loginGoogle = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            task.addOnSuccessListener(this::exportEvents);
        });
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
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), GSI_OPTIONS);
        // The user is always logged out so that he can select to which account to login (and thus export).
        googleSignInClient.signOut();

        Log.d(LOGCAT_TAG, "User needs to login into its Google Account.");

        Intent signInIntent = googleSignInClient.getSignInIntent();
        loginGoogle.launch(signInIntent);
    }

    private void exportEvents(GoogleSignInAccount account) {
        Account a = Objects.requireNonNull(Objects.requireNonNull(account).getAccount());

        Log.d(LOGCAT_TAG, "Events will be exported to: " + a.name);
        GoogleCalendar.exportEvents(
                requireContext(),
                a,
                EventQueries.getAllEvents(Database.getDefaultInstance())
        ).whenComplete((ignored1, ignored2) -> {
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), GSI_OPTIONS);
            googleSignInClient.signOut();
            Log.d(LOGCAT_TAG, "User logged out.");
        });
    }
}