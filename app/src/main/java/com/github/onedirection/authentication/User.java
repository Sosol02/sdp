package com.github.onedirection.authentication;

public class User {
    private final String name;
    private final String email;

    User(String name, String email){
        this.name = name;
        this.email = email;
    }

    final public String getName(){
        return this.name;
    }

    final public String getEmail(){
        return this.email;
    }

}
