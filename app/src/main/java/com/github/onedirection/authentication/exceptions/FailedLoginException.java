package com.github.onedirection.authentication.exceptions;

/**
 * Represents a failed login attempt, due to
 * wrong identifier (mail, phone number,...)
 * or password.
 */
public class FailedLoginException extends Exception {

    public FailedLoginException(String email) {
        super("Could not login `" + email + "`.");
    }

    public FailedLoginException(String email, Throwable cause) {
        this("Could not login `" + email + "`.");
        initCause(cause);
    }

}
