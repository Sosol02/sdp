package com.github.onedirection.authentication.exceptions;

/**
 * Used when an operation require a user logged in, and
 * no user is logged in.
 */
public class NoUserLoggedInException extends Exception {

    public NoUserLoggedInException(String operation) {
        super(operation + " failed: no user was logged in.");
    }

    public NoUserLoggedInException(String operation, Throwable cause) {
        this(operation + " failed: no user was logged in.");
        initCause(cause);
    }

}
