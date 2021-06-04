package com.github.onedirection.navigation.fragment.account;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.onedirection.R;
import com.github.onedirection.authentication.service.AuthenticationService;
import com.google.android.material.navigation.NavigationView;

/**
 * Fragment for the Account view
 */
public class AccountFragment extends Fragment {

    public final static int MAX_USERNAME_LENGTH = 20;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        NavigationView navigationView = requireActivity().findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView drawerUsername = headerView.findViewById(R.id.nav_header_username);
        TextView accountUsername = view.findViewById(R.id.account_username);
        EditText changeUsernameEdit = view.findViewById(R.id.change_username_account_display);
        Button changeUsernameButton = view.findViewById(R.id.button_change_username);

        accountUsername.setText(drawerUsername.getText());
        drawerUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                accountUsername.setText(drawerUsername.getText());
            }
        });
        changeUsernameButton.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //close the soft keyboard on the app

            AuthenticationService auth = AuthenticationService.getDefaultInstance();
            if (changeUsernameEdit.getText().toString().length() > MAX_USERNAME_LENGTH) {
                Toast.makeText(requireContext().getApplicationContext(), getString(R.string.changed_name_too_long), Toast.LENGTH_LONG).show();
            } else {
                auth.updateDisplayName(changeUsernameEdit.getText().toString()).thenAccept(user -> {
                    drawerUsername.setText(user.getName());
                    Toast.makeText(requireContext().getApplicationContext(), getString(R.string.changed_name_success) + " " + user.getName(),
                            Toast.LENGTH_LONG).show();
                }).exceptionally(error -> {
                    Toast.makeText(requireContext().getApplicationContext(), getString(R.string.changed_name_failure), Toast.LENGTH_LONG).show();
                    return null;
                });
            }
        });

        return view;
    }

}