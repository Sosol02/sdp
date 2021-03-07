package com.github.onedirection.authentication;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AuthenticationService {

    Optional<User> getCurrentUser();

    CompletableFuture<User> registerUser(String identifier, String credentials);

    CompletableFuture<User> loginUser(String identifier, String credentials);

    void logoutUser();

}
