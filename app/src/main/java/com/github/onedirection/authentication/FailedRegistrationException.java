package com.github.onedirection.authentication;

/**
 * Represents a failed login attempt, due to
 * wrong identifier (mail, phone number,...)
 * or password.
 */
public class FailedRegistrationException extends RuntimeException {

    public FailedRegistrationException(String email) {
        super("Could not register `" + email + "`.");
    }

    public FailedRegistrationException(String email, Throwable cause) {
        this("Could not register `" + email + "`.");
        initCause(cause);
    }

}
