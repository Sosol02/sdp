package com.github.onedirection;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.authentication.AuthenticationService;
import com.github.onedirection.authentication.FailedLoginException;
import com.github.onedirection.authentication.FailedRegistrationException;
import com.github.onedirection.authentication.FirebaseAuthentication;
import com.github.onedirection.authentication.NoUserLoggedInException;
import com.github.onedirection.authentication.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class FirebaseAuthenticationTest {

    // Note: those tests are dirty !
    // Once we are able to set up a dang Firebase Auth emulator,
    // those tests should use that instead...
    // Or at least hide the psw somewhere safe.

    private static final String TEST_EMAIL = "onedirection.nottheband@gmail.com";
    private static final String TEST_PSW = "123456";
    private static final String TEST_NAME1 = "TEST1";
    private static final String TEST_NAME2 = "TEST2";

    private static final String DISABLED_EMAIL = "disable@disabled.disabled";

    @Before
    public void logout() {
        FirebaseAuthentication.getInstance().logoutUser();
    }

    @Test
    public void userCanLogin() {
        AuthenticationService auth = FirebaseAuthentication.getInstance();
        assertFalse(auth.getCurrentUser().isPresent());

        CompletableFuture<User> fUser = auth.loginUser(TEST_EMAIL, TEST_PSW);
        try{
            User user = fUser.join();
            assertTrue(auth.getCurrentUser().isPresent());
            assertEquals(auth.getCurrentUser().get(), user);
            assertEquals(user.getEmail(), TEST_EMAIL);
        }
        catch (Exception e){
            fail(e.getMessage());
        }
    }

    private void cannotLogin(CompletableFuture<User> fUser, AuthenticationService auth, Class<?> expected){
        try{
            User user = fUser.get();
            fail("User " + user + " should not have been able to login.");
        }
        catch(Exception e){
            if(expected.isInstance(e.getCause())){
                assertFalse(auth.getCurrentUser().isPresent());
            }
            else{
                fail("Unexpected exception:\n" + e.getMessage());
            }
        }
    }

    @Test
    public void disabledUserCantLogin() {
        AuthenticationService auth = FirebaseAuthentication.getInstance();
        assertFalse(auth.getCurrentUser().isPresent());

        cannotLogin(auth.loginUser(DISABLED_EMAIL, TEST_PSW), auth, FailedLoginException.class);
    }

    @Test
    public void accountsCannotBeRegisteredWithUsedEmail() {
        AuthenticationService auth = FirebaseAuthentication.getInstance();
        assertFalse(auth.getCurrentUser().isPresent());

        cannotLogin(auth.registerUser(DISABLED_EMAIL, TEST_PSW), auth, FailedRegistrationException.class);
        cannotLogin(auth.registerUser(TEST_EMAIL, TEST_PSW), auth, FailedRegistrationException.class);
    }

    @Test
    public void userCanChangeDisplayName() {
        AuthenticationService auth = FirebaseAuthentication.getInstance();
        assertNotEquals(TEST_NAME1, TEST_NAME2);

        try {
            auth.loginUser(TEST_EMAIL, TEST_PSW).get();
            assertEquals(TEST_NAME1, auth.updateDisplayName(TEST_NAME1).get().getName());
            assertEquals(TEST_NAME1, auth.getCurrentUser().get().getName());
            assertEquals(TEST_NAME2, auth.updateDisplayName(TEST_NAME2).get().getName());
            assertEquals(TEST_NAME2, auth.getCurrentUser().get().getName());
        }
        catch(Exception e){
            fail(e.getMessage());
        }

    }

    @Test
    public void profileCannotBeUpdatedWithoutUser() {
        AuthenticationService auth = FirebaseAuthentication.getInstance();
        try{
            auth.updateDisplayName(TEST_NAME1);
        }
        catch(NoUserLoggedInException e){
            ; // That's what we expect
        }
        catch(Exception e){
            fail(e.getMessage());
        }

    }
}
