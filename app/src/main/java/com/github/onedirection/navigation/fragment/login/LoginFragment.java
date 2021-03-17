package com.github.onedirection.navigation.fragment.login;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.onedirection.R;
import com.github.onedirection.authentication.User;
import com.google.android.material.navigation.NavigationView;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private boolean register;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        final EditText usernameEditText = view.findViewById(R.id.username);
        final EditText passwordEditText = view.findViewById(R.id.password);
        final Button loginButton = view.findViewById(R.id.login);
        final Switch switchRegister = view.findViewById(R.id.login_switch);

        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        MenuItem logoutItem = navigationView.getMenu().findItem(R.id.nav_logout);
        MenuItem loginItem = navigationView.getMenu().findItem(R.id.nav_login);
        MenuItem homeItem = navigationView.getMenu().findItem(R.id.nav_home);
        View headerView = navigationView.getHeaderView(0);
        TextView drawerUsername = (TextView) headerView.findViewById(R.id.nav_header_username);
        TextView drawerEmail = (TextView) headerView.findViewById(R.id.nav_header_email);



        logoutItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //loginViewModel.logout();
                drawerUsername.setText("Guest");
                drawerEmail.setText("");
                loginItem.setVisible(true);
                logoutItem.setVisible(false);
                return false;
            }
        });

        loginViewModel.getLoginFormState().observe(getViewLifecycleOwner(), new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getUserResult().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                if (user != null) {
                    drawerUsername.setText(user.getName());
                    drawerEmail.setText(user.getName());
                    logoutItem.setVisible(true);
                    loginItem.setVisible(false);
                    updateUiWithUser(user);
                } else {
                    showLoginFailed(R.string.login_failed);
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString(), register);
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(), register);
            }
        });

        switchRegister.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                register = b;
            }
        });
    }

    private void updateUiWithUser(User model) {
        String welcome = getString(R.string.welcome) + " " + model.getName();
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(getContext().getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        }
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        if (getContext() != null && getContext().getApplicationContext() != null) {
            //do something
        }
    }
}