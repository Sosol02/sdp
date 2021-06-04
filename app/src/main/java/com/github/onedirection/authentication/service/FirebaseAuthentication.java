package com.github.onedirection.authentication.service;

import android.util.Log;

import com.github.onedirection.authentication.exceptions.FailedLoginException;
import com.github.onedirection.authentication.exceptions.FailedRegistrationException;
import com.github.onedirection.authentication.exceptions.NoUserLoggedInException;
import com.github.onedirection.database.implementation.ConcreteDatabase;
import com.github.onedirection.database.implementation.DefaultDatabase;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.event.model.Event;
import com.github.onedirection.utils.Monads;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** A login service based on google's firebase. */
final public class FirebaseAuthentication implements AuthenticationService {
    private final static FirebaseAuth auth = FirebaseAuth.getInstance();
    private final static FirebaseAuthentication self = new FirebaseAuthentication();

    public static FirebaseAuthentication getInstance() {
        return self;
    }

    private FirebaseAuthentication() {
    }

    private static Optional<User> convertUser(FirebaseUser user) {
        if (user == null) {
            return Optional.empty();
        } else {
            return Optional.of(new User(user.getDisplayName(), user.getEmail()));
        }
    }

    private static CompletableFuture<User> convertAuthTask(Task<AuthResult> auth, String username) {
        CompletableFuture<User> result = new CompletableFuture<>();

        auth.addOnCompleteListener(
                res -> {
                    if (res.isSuccessful()) {
                        // We are sure that the user exists (he just logged in)
                        Log.d("FirebaseLogin", "User logged in: " + username);
                        result.complete(convertUser(res.getResult().getUser()).get());
                    } else {
                        if (res.getException() instanceof FirebaseAuthInvalidUserException) {
                            Log.d("FirebaseLogin", "Login failed - no such user: " + username);
                            result.completeExceptionally(new FailedLoginException(username, res.getException()));
                        } else if (res.getException() instanceof FirebaseAuthUserCollisionException) {
                            Log.d("FirebaseLogin", "Login failed - cannot register: " + username);
                            result.completeExceptionally(new FailedRegistrationException(username, res.getException()));
                        } else {
                            Log.d("FirebaseLogin", "Login failed - unknown: " + username);
                            result.completeExceptionally(res.getException());
                        }
                    }
                }
        );
        return result;
    }

    private static CompletableFuture<User> updateProfile(UserProfileChangeRequest profileUpdate) {
        CompletableFuture<User> result = new CompletableFuture<>();
        if (!sGetCurrentUser().isPresent()) {
            result.completeExceptionally(new NoUserLoggedInException("Profile update"));
        } else {
            auth.getCurrentUser().updateProfile(profileUpdate).addOnCompleteListener(
                    res -> {
                        if (res.isSuccessful()) {
                            Optional<User> user = sGetCurrentUser();
                            if (user.isPresent()) {
                                result.complete(user.get());
                            } else {
                                result.completeExceptionally(new RuntimeException("Successful profile update without logged in user."));
                            }
                        } else {
                            result.completeExceptionally(res.getException());
                        }
                    }
            );
        }
        return result;
    }

    private static Optional<User> sGetCurrentUser() {
        return convertUser(auth.getCurrentUser());
    }

    @Override
    public Optional<User> getCurrentUser() {
        return sGetCurrentUser();
    }

    @Override
    public CompletableFuture<User> registerUser(String identifier, String credentials) {
        DefaultDatabase.clearCaches();
        return convertAuthTask(auth.createUserWithEmailAndPassword(identifier, credentials), identifier);
    }

    @Override
    public CompletableFuture<User> loginUser(String identifier, String credentials) {
        ConcreteDatabase cdb = DefaultDatabase.getDefaultConcreteInstance();
        CompletableFuture<User> fut = cdb.retrieveAll(
                EventStorer.getInstance()).thenCompose(events ->
                        convertAuthTask(auth.signInWithEmailAndPassword(identifier, credentials), identifier)
                                .thenCompose(user -> cdb.storeAll(events).thenApply(ignore -> user))
        );
        IdentificationService.renewDeviceId();

        DefaultDatabase.clearCaches();
        return fut;
    }

    @Override
    public void logoutUser() {
        DefaultDatabase.clearCaches();
        auth.signOut();
    }

    @Override
    public CompletableFuture<User> updateDisplayName(String newName) {
        UserProfileChangeRequest changes = (new UserProfileChangeRequest.Builder()).setDisplayName(newName).build();
        return updateProfile(changes);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FirebaseAuthentication;
    }
}
