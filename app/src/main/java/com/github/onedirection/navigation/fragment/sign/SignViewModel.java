package com.github.onedirection.navigation.fragment.sign;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.onedirection.R;
import com.github.onedirection.authentication.AuthenticationService;
import com.github.onedirection.authentication.FirebaseAuthentication;
import com.github.onedirection.authentication.User;

public class SignViewModel extends ViewModel {

    private final MutableLiveData<SignFormState> signFormState = new MutableLiveData<>();
    private final MutableLiveData<User> userResult = new MutableLiveData<>();

    LiveData<SignFormState> getLoginFormState() {
        return signFormState;
    }

    LiveData<User> getUserResult() {
        return userResult;
    }

    public void sign(String username, String password, boolean register) {
        AuthenticationService auth = AuthenticationService.getDefaultInstance();
        if (register) {
            auth.registerUser(username, password).thenAccept(userResult::setValue)
                    .exceptionally(error -> {userResult.setValue(null); return null; });
        } else {
            auth.loginUser(username, password).thenAccept(userResult::setValue)
                    .exceptionally(error -> {userResult.setValue(null); return null; });;
        }
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            signFormState.setValue(new SignFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            signFormState.setValue(new SignFormState(null, R.string.invalid_password));
        } else {
            signFormState.setValue(new SignFormState(true));
        }
    }

    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        }
        return false;
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}