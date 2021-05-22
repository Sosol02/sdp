package com.github.onedirection;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.authentication.AuthenticationService;
import com.github.onedirection.authentication.exceptions.FailedLoginException;
import com.github.onedirection.authentication.exceptions.FailedRegistrationException;
import com.github.onedirection.authentication.FirebaseAuthentication;
import com.github.onedirection.authentication.exceptions.NoUserLoggedInException;
import com.github.onedirection.authentication.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class FirebaseAuthenticationTest {

    // Note: those tests aren't the cleanest...
    // Once we are able to set up a dang Firebase Auth emulator,
    // those tests should use that instead...

    private static final Context ctx = ApplicationProvider.getApplicationContext();

    private static final String TEST_EMAIL = ctx.getString(R.string.test_account);
    private static final String DISABLED_EMAIL = ctx.getString(R.string.test_disabled_account);
    private static final String TEST_PSW = ctx.getString(R.string.test_password);
    private static final String TEST_NAME1 = "TEST1";
    private static final String TEST_NAME2 = "TEST2";


    @Before
    public void logout() {
        FirebaseAuthentication.getInstance().logoutUser();
    }

    @Test
    public void userCanLogin() {
        AuthenticationService auth = FirebaseAuthentication.getInstance();
        assertFalse(auth.getCurrentUser().isPresent());

        try {
            CompletableFuture<User> fUser = auth.loginUser(TEST_EMAIL, TEST_PSW);
            User user = fUser.join();
            assertTrue(auth.getCurrentUser().isPresent());
            assertThat(auth.getCurrentUser().get(), is(user));
            assertThat(user.getEmail(), is(TEST_EMAIL));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void authFails(CompletableFuture<User> fUser, AuthenticationService auth, Class<?> expected) {
        try {
            User user = fUser.get();
            fail("User " + user + " should not have been able to login.");
        } catch (Exception e) {
            assertThat(e.getCause(), is(instanceOf(expected)));
            assertFalse(auth.getCurrentUser().isPresent());
        }
    }

    @Test
    public void disabledUserCantLogin() {
        AuthenticationService auth = FirebaseAuthentication.getInstance();
        assertFalse(auth.getCurrentUser().isPresent());

        try {
            authFails(auth.loginUser(DISABLED_EMAIL, TEST_PSW), auth, FailedLoginException.class);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void accountsCannotBeRegisteredWithUsedEmail() {
        AuthenticationService auth = FirebaseAuthentication.getInstance();
        assertFalse(auth.getCurrentUser().isPresent());

        try {
            authFails(auth.registerUser(DISABLED_EMAIL, TEST_PSW), auth, FailedRegistrationException.class);
            authFails(auth.registerUser(TEST_EMAIL, TEST_PSW), auth, FailedRegistrationException.class);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void userCanChangeDisplayName() {
        AuthenticationService auth = FirebaseAuthentication.getInstance();
        assertThat(TEST_NAME1, not(TEST_NAME2));

        try {
            auth.loginUser(TEST_EMAIL, TEST_PSW).get();
            assertThat(auth.updateDisplayName(TEST_NAME1).get().getName(), is(TEST_NAME1));
            assertThat(auth.getCurrentUser().get().getName(), is(TEST_NAME1));
            assertThat(auth.updateDisplayName(TEST_NAME2).get().getName(), is(TEST_NAME2));
            assertThat(auth.getCurrentUser().get().getName(), is(TEST_NAME2));
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    @Test
    public void profileCannotBeUpdatedWithoutUser() {
        AuthenticationService auth = FirebaseAuthentication.getInstance();
        try {
            auth.updateDisplayName(TEST_NAME1).get();
            fail("Display name update should have failed.");
        } catch (Exception e) {
            assertThat(e.getCause(), is(instanceOf(NoUserLoggedInException.class)));
        }
    }

    @Test
    public void firebaseAuthenticationIsDefaultAuthentication() {
        assertThat(AuthenticationService.getDefaultInstance(), is(FirebaseAuthentication.getInstance()));
    }
}
