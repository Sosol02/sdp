package com.github.onedirection.navigation.fragment.sign;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.onedirection.R;
import com.github.onedirection.authentication.service.User;
import com.google.android.material.navigation.NavigationView;

/**
 * Fragment for the SignIn/Register view
 */
public class SignFragment extends Fragment {

    private SignViewModel signViewModel;
    private boolean register;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        signViewModel = new ViewModelProvider(this).get(SignViewModel.class);
        register = false;

        EditText emailEditText = view.findViewById(R.id.email);
        EditText passwordEditText = view.findViewById(R.id.password);
        Button signButton = view.findViewById(R.id.sign);
        TextView signToggle = view.findViewById(R.id.sign_toggle);

        NavigationView navigationView = requireActivity().findViewById(R.id.nav_view);
        MenuItem signMenuItem = navigationView.getMenu().findItem(R.id.nav_sign);
        MenuItem logoutMenuItem = navigationView.getMenu().findItem(R.id.nav_logout);

        View headerView = navigationView.getHeaderView(0);
        TextView drawerUsername = headerView.findViewById(R.id.nav_header_username);
        TextView drawerEmail = headerView.findViewById(R.id.nav_header_email);


        signViewModel.getLoginFormState().observe(getViewLifecycleOwner(), loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            signButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getEmailError() != null) {
                emailEditText.setError(getString(loginFormState.getEmailError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        signViewModel.getUserResult().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                showSignFailed(register);
            } else {
                drawerUsername.setText(user.getName());
                drawerEmail.setText(user.getEmail());
                signMenuItem.setVisible(false);
                logoutMenuItem.setVisible(true);
                showSignSuccess(user);
                requireActivity().findViewById(R.id.nav_home).performClick();

                // Close the keyboard
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
                signViewModel.loginDataChanged(emailEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        emailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                signViewModel.sign(emailEditText.getText().toString(),
                        passwordEditText.getText().toString(), register);
            }
            return false;
        });

        signButton.setOnClickListener(v -> signViewModel.sign(emailEditText.getText().toString(),
                passwordEditText.getText().toString(), register));

        signToggle.setOnClickListener(v -> {
            register = !register;
            if (register) {
                signButton.setText(R.string.button_register_text);
                signToggle.setText(R.string.clickable_text_to_sign_in);
            } else {
                signButton.setText(R.string.button_sign_in_text);
                signToggle.setText(R.string.clickable_text_to_register);
            }
        });
    }

    private void showSignSuccess(User user) {
        String welcome = getString(R.string.welcome) + " " + user.getName() + "!";
        Toast.makeText(requireContext().getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showSignFailed(boolean register) {
        String failed;
        if (register) {
            failed = getString(R.string.register_failed);
        } else {
            failed = getString(R.string.sign_in_failed);
        }
        Toast.makeText(requireContext().getApplicationContext(), failed, Toast.LENGTH_LONG).show();
    }
}