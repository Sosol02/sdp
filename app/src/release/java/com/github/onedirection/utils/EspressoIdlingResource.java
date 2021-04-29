package com.github.onedirection.utils;

public class EspressoIdlingResource {

    private static final EspressoIdlingResource instance = new EspressoIdlingResource();

    private EspressoIdlingResource() {}

    public static EspressoIdlingResource getInstance() {
        return instance;
    }

    public void lockIdlingResource() {}

    public void unlockIdlingResource() {}
}
