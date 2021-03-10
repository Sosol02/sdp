package com.github.onedirection.authentication;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

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

    private final static CompletableFuture<User> convertAuthTask(Task<AuthResult> auth, String username) {
        CompletableFuture<User> result = new CompletableFuture<>();

        auth.addOnCompleteListener(
                res -> {
                    if (res.isSuccessful()) {
                        // We are sure that the user exists (he just logged in)
                        result.complete(convertUser(res.getResult().getUser()).get());
                    } else {
                        if (res.getException() instanceof FirebaseAuthInvalidUserException) {
                            result.completeExceptionally(new FailedLoginException(username, res.getException()));
                        } else if (res.getException() instanceof FirebaseAuthUserCollisionException) {
                            result.completeExceptionally(new FailedRegistrationException(username, res.getException()));
                        } else {
                            result.completeExceptionally(res.getException());
                        }
                    }
                }
        );
        return result;
    }

    private final static CompletableFuture<User> updateProfile(UserProfileChangeRequest profileUpdate) {
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
        return convertAuthTask(auth.createUserWithEmailAndPassword(identifier, credentials), identifier);
    }

    @Override
    public CompletableFuture<User> loginUser(String identifier, String credentials) {
        return convertAuthTask(auth.signInWithEmailAndPassword(identifier, credentials), identifier);
    }

    @Override
    public CompletableFuture<User> updateDisplayName(String newName) {
        UserProfileChangeRequest changes = (new UserProfileChangeRequest.Builder()).setDisplayName(newName).build();
        return updateProfile(changes);
    }

    @Override
    public void logoutUser() {
        auth.signOut();
    }
}
