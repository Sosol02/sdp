package com.github.onedirection.notifs;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.R;
import com.github.onedirection.database.Database;
import com.github.onedirection.events.Event;
import com.github.onedirection.navigation.NavigationActivity;
import com.github.onedirection.testhelpers.WaitAction;
import com.github.onedirection.utils.Id;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class NotificationTest {

    private static final Context ctx = ApplicationProvider.getApplicationContext();

    @Rule
    public ActivityScenarioRule<NavigationActivity> testRule = new ActivityScenarioRule<>(NavigationActivity.class);

    @Before
    public void init() {

    }

    @Test
    public void notificationForCloseEventIsTriggered() throws InterruptedException, ExecutionException {
        Database db = Database.getDefaultInstance();
        //final ArrayList<Event> events = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            db.store(new Event(Id.generateRandom(), "Event" + i, "Place" + i, Optional.empty(),
                    ZonedDateTime.now().plusSeconds(10 * i),
                    ZonedDateTime.now().plusSeconds(10 * i + 1),
                    Optional.empty())).get();
        }



        Notifications notifs = Notifications.getInstance(ctx);
        notifs.scheduleClosestEvent(ctx);

        onView(withId(R.id.textExampleHome)).perform(new WaitAction(60000));

    }

}
