package com.github.onedirection;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.onedirection.authentication.AuthenticationService;
import com.github.onedirection.authentication.FirebaseAuthentication;
import com.github.onedirection.authentication.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class FirebaseAuthenticationTest {

    private static final String TEST_EMAIL = "onedirection.nottheband@gmail.com";
    private static final String TEST_PSW = "123456";

    private static final String DISABLED_EMAIL = " disable@disabled.disabled ";

    @Before
    public void logout() {
        FirebaseAuthentication.getInstance().logoutUser();
    }

    @Test
    public void userCanLogin() {
        AuthenticationService auth = FirebaseAuthentication.getInstance();
        assertFalse(auth.getCurrentUser().isPresent());

        auth.loginUser(TEST_EMAIL, TEST_PSW).handle(
                (u, t) -> {
                    assertNull(t);
                    assertTrue(auth.getCurrentUser().isPresent());
                    User user = auth.getCurrentUser().get();
                    assertEquals(user.getEmail(), TEST_EMAIL);
                    assertEquals(user.getName(), TEST_EMAIL);
                    assertEquals(u, user);
                    return 0;
                }
        );
    }

    @Test
    public void disabledUserCantLogin() {
        AuthenticationService auth = FirebaseAuthentication.getInstance();
        assertFalse(auth.getCurrentUser().isPresent());

        auth.loginUser(DISABLED_EMAIL, TEST_PSW).handle(
                (u, t) -> {
                    assertNull(u);
                    assertNotNull(t);
                    assertFalse(auth.getCurrentUser().isPresent());
                    return 0;
                }
        );
    }

    @Test
    public void cannotRegisterAlreadyExistingAccount() {
        AuthenticationService auth = FirebaseAuthentication.getInstance();
        assertFalse(auth.getCurrentUser().isPresent());

        auth.registerUser(DISABLED_EMAIL, TEST_PSW).handle(
                (u, t) -> {
                    assertNull(u);
                    assertNotNull(t);
                    assertFalse(auth.getCurrentUser().isPresent());
                    return 0;
                }
        );

        auth.registerUser(TEST_EMAIL, TEST_PSW).handle(
                (u, t) -> {
                    assertNull(u);
                    assertNotNull(t);
                    assertFalse(auth.getCurrentUser().isPresent());
                    return 0;
                }
        );
    }
}
