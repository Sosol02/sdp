package com.github.onedirection.testhelpers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

// Hamcrest's anything but typed
public class Any {
    public static <T> Matcher<T> any() {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(final Object item) {
                return true;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Any");
            }
        };
    }
}
