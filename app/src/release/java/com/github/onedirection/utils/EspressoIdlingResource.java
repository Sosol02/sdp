package com.github.onedirection.utils;

public class EspressoIdlingResource {

    /*private final String RESOURCE_TAG = "MAP_IDLING_RESOURCE";
    private final CountingIdlingResource countingIdlingResource = new CountingIdlingResource(RESOURCE_TAG);*/
    private static final EspressoIdlingResource instance = new EspressoIdlingResource();

    private EspressoIdlingResource() {}

    public static EspressoIdlingResource getInstance() {
        return instance;
    }

    /*public CountingIdlingResource getCountingIdlingResource() {
        return countingIdlingResource;
    }*/

    public void lockIdlingResource() {
        /*countingIdlingResource.increment();*/
    }

    public void unlockIdlingResource() {
        /*if (!countingIdlingResource.isIdleNow()) {
            countingIdlingResource.decrement();
        }*/
    }
}
