package com.github.onedirection.event;


import android.content.Intent;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.R;
import com.github.onedirection.event.ui.EventCreator;
import com.github.onedirection.geolocation.Coordinates;
import com.github.onedirection.geolocation.NamedCoordinates;
import com.github.onedirection.geolocation.location.DeviceLocationProviderActivity;
import com.github.onedirection.utils.Id;
import com.github.onedirection.utils.ObserverPattern;
import com.github.onedirection.utils.Pair;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.onedirection.testhelpers.ViewChild.nthChild;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class EventCreatorTest {

    private final static Id ID = Id.generateRandom();
    private final static String NAME = "Event name";
    private final static String LOCATION_NAME = "Location name";
    private final static NamedCoordinates LOCATION = new NamedCoordinates(0, 0, LOCATION_NAME);
    private final static ZonedDateTime START_TIME = ZonedDateTime.now().plusDays(1);
    private final static ZonedDateTime END_TIME = ZonedDateTime.now().plusDays(2);

    private final static Duration REC_DURATION = ChronoUnit.DAYS.getDuration();
    private final static ZonedDateTime REC_END = START_TIME.plusYears(1);
    private final static Recurrence RECURRENCE = new Recurrence(ID, REC_DURATION, REC_END);

    private final static String EPFL_QUERY = "EPFL";
    private final static String EPFL_CANTON = "Vaud";

    private final static Event EVENT = new Event(ID, NAME, LOCATION, START_TIME, END_TIME);
    private final static Event REC_EVENT = new Event(ID, NAME, LOCATION_NAME, START_TIME, END_TIME, RECURRENCE);

    public static class Wrapper<T> {
        public T val;

        public Wrapper(T val) {
            this.val = val;
        }
    }

    // Utilities

    public void test(Function<Intent, Intent> setup, Runnable test, BiConsumer<Event, Boolean> eventChecks) {
        final Wrapper<IdlingResource> idling = new Wrapper<>(null);
        final Wrapper<Pair<Event, Boolean>> result = new Wrapper<>(new Pair<>(null, null));

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreator.class);
        setup.apply(intent);

        ActivityScenario.launch(intent).onActivity(a -> {
            EventCreator activity = (EventCreator) a;
            idling.val = activity.getIdlingResource();
            IdlingRegistry.getInstance().register(idling.val);
            activity.setCreationCallback((event, aBoolean) -> {
                result.val = new Pair<>(event, aBoolean);
                return CompletableFuture.completedFuture(true);
            });
        });

        test.run();

        eventChecks.accept(result.val.first, result.val.second);

        IdlingRegistry.getInstance().unregister(idling.val);
    }

    // Those do not work on cirrus...
    private void setTimePicker(int startId, ZonedDateTime time) {
        onView(withId(startId)).perform(ViewActions.click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(time.getHour(), time.getMinute()));
        onView(withId(android.R.id.button1)).perform(ViewActions.click());
    }

    private void setDatePicker(int startId, ZonedDateTime date) {
        onView(withId(startId)).perform(ViewActions.click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth()));
        onView(withId(android.R.id.button1)).perform(ViewActions.click());
    }

    public void testIsMainFragment() {
        onView(ViewMatchers.withId(R.id.textEventCreatorTitle)).check(matches(withText(containsString("CREATE"))));
    }

    public void testIsGeolocationFragment() {
        onView(withId(R.id.textEventCreatorTitle)).check(matches(withText(containsString("LOCATION"))));
    }

    ////////////////////////////////////////////////////////////
    // Behavioral tests (correct layouts are enabled/visible) //
    ////////////////////////////////////////////////////////////

    @Test
    public void geolocationTabIsOpenedWhenNoGeolocationIsSet() {
        test(
                i -> i,
                () -> {
                    onView(withId(R.id.checkGeolocation)).perform(scrollTo(), click());

                    testIsGeolocationFragment();
                    onView(withId(R.id.buttonSetGeolocation)).check(matches(not(isEnabled())));
                },
                (event, edit) -> {
                }
        );
    }

    @Test
    public void geolocationTabNotOpenedWhenGeolocationSet() {
        test(
                i -> EventCreator.putEventExtra(i, EVENT),
                () -> {
                    testIsMainFragment();
                    onView(withId(R.id.checkGeolocation)).perform(scrollTo(), click());
                    testIsMainFragment();
                },
                (event, edit) -> {
                }
        );
    }

    @Test
    public void recurrencePeriodCanBeSet(){
        test(
                i -> i,
                () -> {
                    testIsMainFragment();
                    onView(withId(R.id.checkEventRecurrence)).check(matches(not(isChecked())));
                    onView(withId(R.id.checkEventRecurrence)).perform(scrollTo(), click());

                    onView(withId(R.id.recurrencePeriod)).check(matches(isDisplayed()));
                    onView(withId(R.id.recurrencePeriod)).check(matches(isEnabled()));
                    onView(withId(R.id.recurrenceUntil)).check(matches(isDisplayed()));
                },
                (event, edit) -> {}
        );
    }

    @Test
    public void recurrencePeriodNotEditableWhenAlreadyRecurrent() {
        test(
                i -> EventCreator.putEventExtra(i, REC_EVENT),
                () -> {
                    testIsMainFragment();
                    onView(withId(R.id.checkEventRecurrence)).check(matches(isChecked()));

                    onView(withId(R.id.recurrencePeriod)).check(matches(isDisplayed()));
                    onView(withId(R.id.recurrencePeriod)).check(matches(not(isEnabled())));
                    onView(withId(R.id.recurrenceUntil)).check(matches(isDisplayed()));
                },
                (event, edit) -> {}
        );
    }

    @Test
    public void recurrencePeriodEditableWhenNotRecurrent() {
        test(
                i -> EventCreator.putEventExtra(i, EVENT),
                () -> {
                    testIsMainFragment();
                    onView(withId(R.id.checkEventRecurrence)).check(matches(not(isChecked())));
                    onView(withId(R.id.checkEventRecurrence)).perform(scrollTo(), click());

                    onView(withId(R.id.recurrencePeriod)).check(matches(isDisplayed()));
                    onView(withId(R.id.recurrencePeriod)).check(matches(isEnabled()));
                    onView(withId(R.id.recurrenceUntil)).check(matches(isDisplayed()));
                },
                (event, edit) -> {}
        );
    }

    ////////////////////////////////////////////////////////////
    //            Arguments tests (Intent extras)             //
    ////////////////////////////////////////////////////////////

    @Test
    public void dateCanBeSpecifiedAsInput() {
        final LocalDate date = LocalDate.of(1000, 10, 1);
        final String eventName = "6chars";

        test(
                i -> EventCreator.putDateExtra(i, date),
                () -> {
                    onView(withId(R.id.editEventName)).perform(
                            scrollTo(),
                            click(),
                            clearText(),
                            typeText(eventName)
                    );
                    onView(allOf(withId(R.id.date), hasSibling(withText(containsString("Start")))))
                            .check(matches(withText(date.toString())));

                    onView(withId(R.id.buttonEventAdd)).perform(scrollTo(), click());
                },
                (event, edit) -> {
                    assertThat(event.getStartTime().toLocalDate(), is(date));
                    assertThat(edit, is(false));
                }
        );
    }

    @Test
    public void eventCanBeSpecifiedAsInput() {
        test(
                i -> EventCreator.putEventExtra(i, EVENT),
                () -> {
                    onView(withId(R.id.editEventName)).check(matches(withText(EVENT.getName())));

                    onView(withId(R.id.checkGeolocation)).check(matches(isChecked()));

                    onView(allOf(withId(R.id.date), hasSibling(withText(containsString("Start")))))
                            .check(matches(withText(EVENT.getStartTime().toLocalDate().toString())));
                    onView(allOf(withId(R.id.time), hasSibling(withText(containsString("Start")))))
                            .check(matches(withText(EVENT.getStartTime().toLocalTime().toString())));

                    onView(allOf(withId(R.id.date), hasSibling(withText(containsString("End")))))
                            .check(matches(withText(EVENT.getEndTime().toLocalDate().toString())));
                    onView(allOf(withId(R.id.time), hasSibling(withText(containsString("End")))))
                            .check(matches(withText(EVENT.getEndTime().toLocalTime().toString())));

                    onView(withId(R.id.buttonGotoGeolocation)).perform(scrollTo(), click());
                    onView(withId(R.id.textSelectedLocationFull)).check(matches(withText(EVENT.getLocation().get().toString())));
                    onView(withId(R.id.buttonSetGeolocation)).perform(scrollTo(), click());

                    onView(withId(R.id.buttonEventAdd)).perform(scrollTo(), click());
                },
                (event, edit) -> {
                    assertThat(event, is(EVENT));
                    assertThat(edit, is(true));
                }
        );
    }

    ////////////////////////////////////////////////////////////
    //                    Functionality tests                 //
    ////////////////////////////////////////////////////////////

    @Test
    public void baseInfoCanBeSet() {
        String name = "EVENT NAME";
        String location = "EVENT LOCATION";

        test(
                i -> i,
                () -> {
                    onView(withId(R.id.editEventName)).perform(
                            scrollTo(),
                            click(),
                            clearText(),
                            typeText(name)
                    );
                    closeSoftKeyboard();
                    onView(withId(R.id.editEventLocationName)).perform(
                            scrollTo(),
                            click(),
                            clearText(),
                            typeText(location)
                    );
                    closeSoftKeyboard();

                    onView(withId(R.id.buttonEventAdd)).perform(scrollTo(), click());
                },
                (event, edit) -> {
                    assertThat(event.getName(), is(name));
                    assertThat(event.getLocationName(), is(location));
                }
        );
    }

    @Ignore("Cirrus' reject")
    @Test
    public void phoneLocationCanBeUsed() {
        // Not reimplemented yet
        // Original :
//        List<String> expected = List.of("lat", "lon");
//
//        onView(withId(R.id.checkGeolocation)).perform(scrollTo(), click());
//        testIsGeolocationFragment();
//
//        onView(withId(R.id.buttonUseCurrentLocation)).perform(scrollTo(), click());
//        onView(withId(R.id.textSelectedLocationFull)).check(matches(withText(stringContainsInOrder(expected))));
        test(
                i -> i,
                () -> {

                },
                (event, edit) -> {
                }
        );
    }

    @Test
    public void geocodingCanBeUsed() {
        final String eventName = "6chars";

        test(
                i -> i,
                () -> {
                    onView(withId(R.id.editEventName)).perform(
                            scrollTo(),
                            click(),
                            clearText(),
                            typeText(eventName)
                    );
                    onView(withId(R.id.checkGeolocation)).perform(scrollTo(), click());
                    testIsGeolocationFragment();

                    onView(withId(R.id.editLocationQuery)).perform(scrollTo(), clearText(), typeText(EPFL_QUERY));
                    closeSoftKeyboard();
                    onView(nthChild(withId(R.id.locationMatchesList), 0)).perform(scrollTo(), click());
                    onView(withId(R.id.buttonSetGeolocation)).perform(scrollTo(), click());
                    onView(withId(R.id.buttonEventAdd)).perform(scrollTo(), click());
                },
                (event, edit) -> {
                    assertThat(event.getLocationName(), containsString(EPFL_CANTON));
                }
        );
    }

    @Test
    public void recurrenceCanBeUsed() {
        ChronoUnit unit = ChronoUnit.YEARS;
        int amount = 2;
        final String eventName = "6chars";

        test(
                i -> i,
                () -> {
                    onView(withId(R.id.editEventName)).perform(
                            scrollTo(),
                            click(),
                            clearText(),
                            typeText(eventName)
                    );
                    onView(withId(R.id.checkEventRecurrence)).perform(scrollTo(), click());
                    onView(withId(R.id.editRecurrenceAmount)).perform(
                            scrollTo(),
                            click(),
                            clearText(),
                            typeText(Integer.toString(amount))
                    );
                    onView(withId(R.id.spinnerRecurrencePeriodType)).perform(scrollTo(), click());
                    onData(is(unit)).perform(click());

                    onView(withId(R.id.buttonEventAdd)).perform(scrollTo(), click());
                },
                (event, edit) -> {
                    assertThat(event.isRecurrent(), is(true));
                    assertThat(
                            event.getRecurrence().get().getPeriod(),
                            is(unit.getDuration().multipliedBy(amount))
                    );
                }
        );
    }


    @Test
    public void editorIsDisabledDuringCallback(){
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreator.class);
        EventCreator.putEventExtra(intent, EVENT);

        ActivityScenario.launch(intent).onActivity(a -> {
            EventCreator activity = (EventCreator) a;
            activity.setCreationCallback((event, aBoolean) -> new CompletableFuture<>());
        });

        onView(withId(R.id.buttonEventAdd)).perform(scrollTo(), click());
        onView(withId(R.id.eventCreatorMainFragment)).check(matches(allOf(
                isDisplayed(),
                not(isEnabled())
        )));
    }

    ////////////////////////////////////////////////////////////
    //                    Edition tests                       //
    ////////////////////////////////////////////////////////////

    @Test
    public void eventCanBeEdited() {
        final Event eventBis = new Event(
                ID,
                "Other name",
                "Other location",
                START_TIME,
                END_TIME
        );

        test(
                i -> EventCreator.putEventExtra(i, EVENT),
                () -> {
                    onView(withId(R.id.editEventName)).perform(
                            scrollTo(),
                            click(),
                            clearText(),
                            typeText(eventBis.getName())
                    );
                    closeSoftKeyboard();
                    onView(withId(R.id.checkGeolocation)).perform(
                            scrollTo(),
                            click()
                    );
                    onView(withId(R.id.editEventLocationName)).perform(
                            scrollTo(),
                            click(),
                            clearText(),
                            typeText(eventBis.getLocationName())
                    );
                    closeSoftKeyboard();

                    onView(withId(R.id.buttonEventAdd)).perform(scrollTo(), click());
                },
                (event, edit) -> {
                    assertThat(event, is(eventBis));
                    assertThat(edit, is(true));
                }
        );
    }


    ////////////////////////////////////////////////////////////
    //               "Should-not-be-here" tests               //
    ////////////////////////////////////////////////////////////
    @Test
    public void phoneLocationCanBeUsedWithoutUI() throws ExecutionException, InterruptedException {
        final DeviceLocationProviderActivity[] testClass = new DeviceLocationProviderActivity/*The array is so that it works*/[1];
        ActivityScenario.launch(EventCreator.class).onActivity(activity -> testClass[0] = activity);

        ObserverPattern.Observer<Coordinates> observer = (subject, value) -> {
        };

        assertThat(testClass[0].addObserver(observer), is(true));
        assertThat(testClass[0].removeObserver(observer), is(true));

        // For some reason, the location is never updated on cirrus, so we can't
        // test it...
        assertThat(testClass[0].startLocationTracking().get(), not(nullValue()));
        try {
            testClass[0].getLastLocation();
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IllegalStateException.class)));
        }
    }

}
