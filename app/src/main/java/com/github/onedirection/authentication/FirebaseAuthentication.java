package com.github.onedirection.authentication;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

final public class FirebaseAuthentication implements AuthenticationService {
    private final static FirebaseAuth auth = FirebaseAuth.getInstance();
    private final static FirebaseAuthentication self = new FirebaseAuthentication();

    public final static FirebaseAuthentication getInstance() {
        return self;
    }

    private FirebaseAuthentication() {
    }

    private final static Optional<User> convertUser(FirebaseUser user) {
        if (user == null) {
            return Optional.empty();
        } else {
            return Optional.of(new User(user.getDisplayName(), user.getEmail()));
        }
    }

    private final static CompletableFuture<User> convertAuthTask(Task<AuthResult> auth) {
        CompletableFuture<User> result = new CompletableFuture<>();

        auth.addOnCompleteListener(
                res -> {
                    if (res.isSuccessful()) {
                        // We are sure that the user exists (he just logged in)
                        result.complete(convertUser(res.getResult().getUser()).get());
                    } else {
                        result.completeExceptionally(res.getException());
                    }
                }
        );
        return result;
    }

    @Override
    public Optional<User> getCurrentUser() {
        return convertUser(auth.getCurrentUser());
    }

    @Override
    public CompletableFuture<User> registerUser(String identifier, String credentials) {
        return convertAuthTask(auth.createUserWithEmailAndPassword(identifier, credentials));
    }

    @Override
    public CompletableFuture<User> loginUser(String identifier, String credentials) {
        return convertAuthTask(auth.signInWithEmailAndPassword(identifier, credentials));
    }

    @Override
    public void logoutUser() {
        auth.signOut();
    }
}
