package com.github.onedirection.navigation.fragment.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.onedirection.R;
import com.github.onedirection.authentication.FailedLoginException;
import com.github.onedirection.authentication.FailedRegistrationException;
import com.github.onedirection.authentication.FirebaseAuthentication;
import com.github.onedirection.authentication.NoUserLoggedInException;
import com.github.onedirection.authentication.User;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<User> userResult = new MutableLiveData<>();

    public LoginViewModel() {

    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }
    LiveData<User> getUserResult() {
        return userResult;
    }

    public void login(String username, String password, boolean register) {
        FirebaseAuthentication auth = FirebaseAuthentication.getInstance();
        if (register) {
            auth.registerUser(username, password).thenAccept(user -> userResult.setValue(user));
        } else {
            auth.loginUser(username, password).thenAccept(user -> userResult.setValue(user));
        }
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        return username.contains("@");
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}