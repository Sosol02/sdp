package com.github.onedirection.notification;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.R;
import com.github.onedirection.database.ConcreteDatabase;
import com.github.onedirection.database.Database;
import com.github.onedirection.database.store.EventStorer;
import com.github.onedirection.event.Event;
import com.github.onedirection.navigation.NavigationActivity;
import com.github.onedirection.testhelpers.WaitAction;
import com.github.onedirection.utils.Id;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class NotificationTest {

    private static final Context ctx = ApplicationProvider.getApplicationContext();

    @Rule
    public ActivityScenarioRule<NavigationActivity> testRule = new ActivityScenarioRule<>(NavigationActivity.class);

    @Before
    public void deleteAllEvents() throws ExecutionException, InterruptedException {
        ConcreteDatabase db = ConcreteDatabase.getDatabase();
        List<Event> events = db.retrieveAll(EventStorer.getInstance()).get();
        for(Event e : events) {
            Id id = db.remove(e.getId(), EventStorer.getInstance()).get();
            assertEquals(e.getId(), id);
        }
        NotificationPublisher.SLACK = 5;
    }

    @Test
    public void notificationForCloseEventIsTriggered() throws InterruptedException, ExecutionException {
        Database db = Database.getDefaultInstance();
        //final ArrayList<Event> events = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            db.store(new Event(Id.generateRandom(), "Event" + i, "Place" + i, Optional.empty(),
                    ZonedDateTime.now().plusSeconds(5 * i),
                    ZonedDateTime.now().plusSeconds(5 * i + 1),
                    Optional.empty())).get();
        }

        // now notifs should eventually show up

        onView(withId(R.id.textExampleHome)).perform(new WaitAction(NotificationPublisher.SLACK * 4000));
    }
}
