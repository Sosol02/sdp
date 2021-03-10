package com.github.onedirection.authentication;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** General interface implemented by all login services. */
public interface AuthenticationService {

    /** @return The user which is currently logged in.  */
    Optional<User> getCurrentUser();

    /** Attempt to register the user.
     *
     * @param identifier The user's identifier.
     * @param credentials The user's password.
     * @return The user, is the registration succeeds·
     * @throws FailedLoginException If the registration attempt fails (e.g. identifier already in use).
     */
    CompletableFuture<User> registerUser(String identifier, String credentials) throws FailedLoginException;

    /** Attempt to login the user.
     *
     * @param identifier The user's identifier.
     * @param credentials The user's password.
     * @return The user, if the login succeeds.
     * @throws FailedLoginException If the login attempt fails (e.g. wrong credentials).
     */
    CompletableFuture<User> loginUser(String identifier, String credentials) throws FailedLoginException;

    /** Change the display name of the currently logged in user.
     *
     * @param newName The user's new display name.
     * @return The updated user, if the operation succeeds.
     * @throws NoUserLoggedInException If no user is currently logged in.
     */
    CompletableFuture<User> updateDisplayName(String newName) throws NoUserLoggedInException;

    /** Disconnects the current user. */
    void logoutUser();

}
