package com.github.onedirection.authentication;

/**
 * Represents a failed registration attempt, due to
 * reuse of email address.
 */
public class FailedRegistrationException extends Exception {

    public FailedRegistrationException(String email) {
        super("Could not register `" + email + "`.");
    }

    public FailedRegistrationException(String email, Throwable cause) {
        this("Could not register `" + email + "`.");
        initCause(cause);
    }

}
