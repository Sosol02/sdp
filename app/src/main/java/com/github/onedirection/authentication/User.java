package com.github.onedirection.authentication;

import java.util.Objects;

public class User {
    private final String name;
    private final String email;

    User(String name, String email) {
        Objects.requireNonNull(email, "Email cannot be null");

        this.name = name == null ? "" : name;
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
        if (other != null && other instanceof User) {
            User that = (User) other;
            return this.name.equals(that.name) && this.email.equals(that.email);
        } else {
            return false;
        }
    }

}
