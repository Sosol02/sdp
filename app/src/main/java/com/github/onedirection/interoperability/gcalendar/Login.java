package com.github.onedirection.interoperability.gcalendar;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import static com.github.onedirection.interoperability.gcalendar.GoogleCalendar.LOGCAT_TAG;

/**
 * Provide Google Login capabilities for use with Google Calendar.
 */
class Login {
    // From https://developers.google.com/identity/protocols/oauth2/scopes#calendar
    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";

    private static final GoogleSignInOptions GSI_OPTIONS =
            new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(new Scope(CALENDAR_SCOPE))
                    .build();

    public static ActivityResultLauncher<Intent> createLogger(ActivityResultCaller self, OnSuccessListener<? super GoogleSignInAccount> callback) {
        return self.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            task.addOnSuccessListener(callback);
            task.addOnFailureListener(e -> Log.d(LOGCAT_TAG, "Login failed: " + e.getMessage()));
        });
    }

    public static void logIn(Context ctx, ActivityResultLauncher<Intent> logger) {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(ctx, GSI_OPTIONS);
        // The user is always logged out so that he can select to which account to login (and thus export).
        googleSignInClient.signOut();

        Log.d(LOGCAT_TAG, "User needs to login into its Google Account.");

        Intent signInIntent = googleSignInClient.getSignInIntent();
        logger.launch(signInIntent);
    }

    public static void logOut(Context ctx) {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(ctx, GSI_OPTIONS);
        googleSignInClient.signOut();
        Log.d(LOGCAT_TAG, "User logged out.");
    }
}
