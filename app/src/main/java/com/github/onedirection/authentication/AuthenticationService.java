package com.github.onedirection.authentication;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** General interface implemented by all login services. */
public interface AuthenticationService {

    /** @return The user which is currently logged in.  */
    Optional<User> getCurrentUser();

    /** Attempt to register the user.
     *
     * Potential exceptional completion: FailedLoginException If the registration attempt fails (e.g. identifier already in use).
     * @param identifier The user's identifier.
     * @param credentials The user's password.
     * @return The user, is the registration succeeds·
     */
    CompletableFuture<User> registerUser(String identifier, String credentials);

    /** Attempt to login the user.
     *
     * Potential exceptional completion: FailedLoginException If the login attempt fails (e.g. wrong credentials).
     * @param identifier The user's identifier.
     * @param credentials The user's password.
     * @return The user, if the login succeeds.
     */
    CompletableFuture<User> loginUser(String identifier, String credentials);

    /** Change the display name of the currently logged in user.
     *
     * Potential exceptional completion: NoUserLoggedInException If no user is currently logged in.
     * @param newName The user's new display name.
     * @return The updated user, if the operation succeeds.
     */
    CompletableFuture<User> updateDisplayName(String newName);

    /** Disconnects the current user. */
    void logoutUser();

}
