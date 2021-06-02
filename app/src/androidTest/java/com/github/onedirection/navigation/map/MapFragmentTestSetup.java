package com.github.onedirection.navigation.map;

import android.Manifest;
import android.widget.TextView;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.github.onedirection.R;
import com.github.onedirection.database.implementation.ConcreteDatabase;
import com.github.onedirection.database.implementation.DefaultDatabase;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.event.model.Event;
import com.github.onedirection.navigation.NavigationActivity;
import com.github.onedirection.navigation.fragment.map.MapFragment;
import com.github.onedirection.utils.EspressoIdlingResource;
import com.github.onedirection.utils.Id;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public abstract class MapFragmentTestSetup {

    protected MapboxMap mapboxMap;
    protected MapFragment fragment;
    private OnMapReadyIdlingResource onMapReadyIdlingResource;
    private EspressoIdlingResource espressoIdlingResource;
    private CountingIdlingResource countingIdlingResource;

    @Rule
    public ActivityScenarioRule<NavigationActivity> testRule = new ActivityScenarioRule<>(NavigationActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION);

    @Before
    public void setupForTest() {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_map)).perform(ViewActions.click());

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.menu_map)));
        testRule.getScenario().onActivity(activity -> {
            fragment = (MapFragment) activity.getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment)
                    .getChildFragmentManager().getPrimaryNavigationFragment();
            onMapReadyIdlingResource = new OnMapReadyIdlingResource(fragment);
        });

        espressoIdlingResource = EspressoIdlingResource.getInstance();
        countingIdlingResource = espressoIdlingResource.getCountingIdlingResource();

        IdlingRegistry.getInstance().register(onMapReadyIdlingResource);
        IdlingRegistry.getInstance().register(countingIdlingResource);
        onView(withId(R.id.mapView)).check(matches(isDisplayed()));
        mapboxMap = onMapReadyIdlingResource.getMapboxMap();
    }

    @Before
    public void deleteAllEvents() throws ExecutionException, InterruptedException {
        ConcreteDatabase db = DefaultDatabase.getDefaultConcreteInstance();
        List<Event> events = db.retrieveAll(EventStorer.getInstance()).get();
        for(Event e : events) {
            Id id = db.remove(e.getId(), EventStorer.getInstance()).get();
            assertEquals(e.getId(), id);
        }
    }

    @After
    public void AtEndTest() {
        IdlingRegistry.getInstance().unregister(onMapReadyIdlingResource);
        IdlingRegistry.getInstance().unregister(countingIdlingResource);
    }

    protected  <T> T getFragmentField(String fieldName, Class<T> classToCast) {
        return getAttributeField(fieldName, fragment, classToCast);
    }

    protected  <T, S> S getAttributeField(String fieldName, T attribute, Class<S> classToCast) {
        try {
            Field field = attribute.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return ((S) field.get(attribute));
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    protected  <T> void setFragmentField(String fieldName, T value) {
        setAttributeField(fieldName, fragment, value);
    }

    protected  <T, S> void setAttributeField(String fieldName, T attribute, S value) {
        try {
            Field field = attribute.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(attribute, value);
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    protected void runOnUiThreadAndWaitEndExecution(Runnable runnable) throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        fragment.requireActivity().runOnUiThread(() -> {
            runnable.run();
            semaphore.release();
        });
        semaphore.acquire();
    }
}
