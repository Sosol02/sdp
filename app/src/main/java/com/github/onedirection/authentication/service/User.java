package com.github.onedirection.authentication.service;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

/**
 * Immutable class representing a user.
 */
@Immutable
public class User {
    private final String name;
    private final String email;

    /** Create a user.
     *
     * @param name The user's name (same as email if null).
     * @param email The user's email (cannot be null).
     */
    User(String name, String email) {
        Objects.requireNonNull(email, "Email cannot be null");

        this.name = name == null ? email : name;
        this.email = email;
    }

    final public String getName() {
        return this.name;
    }

    final public String getEmail() {
        return this.email;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof User) {
            User that = (User) other;
            return this.name.equals(that.name) && this.email.equals(that.email);
        } else {
            return false;
        }
    }

}
