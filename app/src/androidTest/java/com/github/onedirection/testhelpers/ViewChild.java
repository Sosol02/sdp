package com.github.onedirection.testhelpers;

import android.view.View;
import android.view.ViewGroup;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ViewChild extends TypeSafeMatcher<View> {
    public static Matcher<View> nthChild(Matcher<View> parent, int childIdx){
        return new ViewChild(parent, childIdx);
    }

    private final Matcher<View> parentMatcher;
    private final int index;

    public ViewChild(Matcher<View> parent, int index) {
        this.parentMatcher = parent;
        this.index = index;
    }

    @Override
    protected boolean matchesSafely(View view) {
        if (!(view.getParent() instanceof ViewGroup)) return false;
        ViewGroup parent = (ViewGroup) view.getParent();

        return parentMatcher.matches(parent)
                && parent.getChildCount() > index
                && parent.getChildAt(index).equals(view);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(index + "th child of parent");
        parentMatcher.describeTo(description);
    }
}
