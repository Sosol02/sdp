package com.github.onedirection;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

import org.hamcrest.Matcher;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

public class WaitAction implements ViewAction {

    private final long waitingTime;

    public WaitAction(long waitingTime) {
        this.waitingTime = waitingTime;
    }

    @Override
    public Matcher<View> getConstraints() {
        return isDisplayed();
    }

    @Override
    public String getDescription() {
        return "WaitActionFor" + waitingTime;
    }

    @Override
    public void perform(UiController uiController, View view) {
        uiController.loopMainThreadForAtLeast(waitingTime);
    }
}
