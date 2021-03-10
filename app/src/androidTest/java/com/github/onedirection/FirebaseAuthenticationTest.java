package com.github.onedirection;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.authentication.AuthenticationService;
import com.github.onedirection.authentication.FirebaseAuthentication;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class FirebaseAuthenticationTest {

    // Note: those tests are dirty !
    // Once we were able to set up a dang Firebase Auth emulator,
    // those tests should use that instead...
    // Or at least hide the psw somewhere safe.

    private static final String TEST_EMAIL = "onedirection.nottheband@gmail.com";
    private static final String TEST_NAME = "";
    private static final String TEST_PSW = "123456";

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
            assertEquals(user.getName(), TEST_NAME);
        }
        catch (Exception e){
            fail(e.getMessage());
        }
    }

    private void cannotLogin(CompletableFuture<User> fUser, AuthenticationService auth, Class<?> cause){
        try{
            User user = fUser.get();
            fail("User " + user + " should not have been able to login.");
        }
        catch(Exception e){
            if(cause.isInstance(e.getCause())){
                assertFalse(auth.getCurrentUser().isPresent());
                ; //This is what we expect !
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

        cannotLogin(auth.loginUser(DISABLED_EMAIL, TEST_PSW), auth, FirebaseAuthInvalidUserException.class);
    }

    @Test
    public void cannotRegisterAlreadyExistingAccount() {
        AuthenticationService auth = FirebaseAuthentication.getInstance();
        assertFalse(auth.getCurrentUser().isPresent());

        cannotLogin(auth.registerUser(DISABLED_EMAIL, TEST_PSW), auth, FirebaseAuthUserCollisionException.class);
        cannotLogin(auth.registerUser(TEST_EMAIL, TEST_PSW), auth, FirebaseAuthUserCollisionException.class);
    }
}
