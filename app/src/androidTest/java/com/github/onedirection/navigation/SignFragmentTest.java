package com.github.onedirection.navigation;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.R;
import com.github.onedirection.testhelpers.WaitAction;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

@RunWith(AndroidJUnit4.class)
public class SignFragmentTest {

    @Rule
    public ActivityScenarioRule<NavigationActivity> testRule = new ActivityScenarioRule<>(NavigationActivity.class);

    private final Context ctx = ApplicationProvider.getApplicationContext();

    @Before
    public void openSignFragment() {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_sign)).perform(click());
    }

    @Test
    public void testNormalSignInAndLogout() {
        onView(withId(R.id.nav_header_email)).check(matches(withText(ctx.getString(R.string.nav_header_email))));

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.menu_sign)));

        onView(withId(R.id.email)).perform(ViewActions.clearText(), ViewActions.typeText(ctx.getString(R.string.test_account)));
        onView(withId(R.id.password)).perform(ViewActions.typeText(ctx.getString(R.string.test_password)));
        onView(withId(R.id.sign)).perform(click());

        onView(withId(R.id.drawer_layout)).perform(new WaitAction(1000));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_header_email)).check(matches(withText(ctx.getString(R.string.test_account))));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_logout)).perform(click());
        onView(withText(R.string.dialog_logout_no)).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_logout)).check(matches(isDisplayed()));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_logout)).perform(click());
        onView(withText(R.string.dialog_logout_yes)).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_header_email)).check(matches(withText(ctx.getString(R.string.nav_header_email))));
    }

    @Test
    public void testSignToggle() {
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.menu_sign)));

        onView(withId(R.id.sign_toggle)).perform(click());
        onView(withId(R.id.sign_toggle)).check(matches(withText(R.string.clickable_text_to_sign_in)));

        onView(withId(R.id.sign_toggle)).perform(click());
        onView(withId(R.id.sign_toggle)).check(matches(withText(R.string.clickable_text_to_register)));
    }

    @Test
    public void testRegisterFailed() {
        onView(withId(R.id.sign_toggle)).perform(click());
        onView(withId(R.id.email)).perform(ViewActions.clearText(), ViewActions.typeText(ctx.getString(R.string.test_account)));
        onView(withId(R.id.password)).perform(ViewActions.typeText(ctx.getString(R.string.test_password)));
        onView(withId(R.id.sign)).perform(click());
        closeSoftKeyboard();

        onView(withId(R.id.drawer_layout)).perform(new WaitAction(1000));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_header_email)).check(matches(withText(ctx.getString(R.string.nav_header_email))));
    }

    @Test
    public void testIAmDoneActionOnSign() {
        onView(withId(R.id.nav_header_email)).check(matches(withText(ctx.getString(R.string.nav_header_email))));

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.menu_sign)));

        onView(withId(R.id.email)).perform(ViewActions.clearText(), ViewActions.typeText(ctx.getString(R.string.test_account)));
        onView(withId(R.id.password)).perform(ViewActions.typeText(ctx.getString(R.string.test_password)));
        onView(withId(R.id.password)).perform(pressImeActionButton());

        onView(withId(R.id.drawer_layout)).perform(new WaitAction(1000));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_header_email)).check(matches(withText(ctx.getString(R.string.test_account))));
    }

    @Test
    public void testLoginFailed() {
        onView(withId(R.id.email)).perform(ViewActions.clearText(), ViewActions.typeText(ctx.getString(R.string.test_disabled_account)));
        onView(withId(R.id.password)).perform(ViewActions.typeText(ctx.getString(R.string.test_password)));
        onView(withId(R.id.sign)).perform(click());
        closeSoftKeyboard();

        onView(withId(R.id.drawer_layout)).perform(new WaitAction(1000));

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_header_email)).check(matches(withText(ctx.getString(R.string.nav_header_email))));
    }
}
