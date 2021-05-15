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
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;

import com.github.onedirection.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

import java.util.Objects;

/**
 * Fragment which allow to export the calendar into Google Calendar.
 */
public class ExportFragment extends Fragment {

    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";
    private static final String LOGCAT_TAG = "GCalendar";

    private ActivityResultLauncher<Intent> loginGoogle;
    private boolean performExport = true;

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

    private void googleLogin(View ignored){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(CALENDAR_SCOPE))
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
        googleSignInClient.signOut();

        Log.d(LOGCAT_TAG, "User needs to login into its Google Account.");

        Intent signInIntent = googleSignInClient.getSignInIntent();
        loginGoogle.launch(signInIntent);
    }

    private void exportEvents(GoogleSignInAccount account){
        if(!this.performExport){
            return;
        }

        Account a = Objects.requireNonNull(Objects.requireNonNull(account).getAccount());

        Log.d(LOGCAT_TAG, a.name);
        Log.d(LOGCAT_TAG, "Exporting events...");
    }

    @VisibleForTesting
    public void setExport(boolean v){
        this.performExport = v;
    }
}