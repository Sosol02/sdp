package com.github.onedirection.navigation;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.github.onedirection.R;
import com.github.onedirection.authentication.service.AuthenticationService;
import com.github.onedirection.authentication.service.FirebaseAuthentication;
import com.github.onedirection.authentication.service.User;
import com.github.onedirection.event.model.Event;
import com.github.onedirection.event.ui.EventCreator;
import com.github.onedirection.navigation.fragment.home.HomeFragment;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Activity to navigate into the app. The app start on this activity.
 */
public class NavigationActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_calendar,R.id.nav_home, R.id.nav_map, R.id.nav_account, R.id.nav_sign)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        navigationView.getMenu().findItem(R.id.nav_create_event).setOnMenuItemClickListener(menuItem -> {
            Intent intent = new Intent(getApplicationContext(), EventCreator.class);
            startActivity(intent);
            return false;
        });


        MenuItem signMenuItem = navigationView.getMenu().findItem(R.id.nav_sign);
        MenuItem logoutMenuItem = navigationView.getMenu().findItem(R.id.nav_logout);

        View headerView = navigationView.getHeaderView(0);
        TextView drawerUsername = headerView.findViewById(R.id.nav_header_username);
        TextView drawerEmail = headerView.findViewById(R.id.nav_header_email);

        logoutMenuItem.setOnMenuItemClickListener(menuItem -> {
            logout(signMenuItem, logoutMenuItem, drawerUsername, drawerEmail, drawer);
            return false;
        });

        //check if an user is already connected
        AuthenticationService auth = AuthenticationService.getDefaultInstance();
        Optional<User> userOptional = auth.getCurrentUser();
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            drawerUsername.setText(user.getName());
            drawerEmail.setText(user.getEmail());
            signMenuItem.setVisible(false);
            logoutMenuItem.setVisible(true);
            navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
            //needed to highlight home item instead of logout
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    private void logout(MenuItem signMenuItem, MenuItem logoutMenuItem, TextView drawerUsername,
                        TextView drawerEmail, DrawerLayout drawer) {
        AlertDialog.Builder confirmationWindows = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_logout_tittle)
                .setMessage(R.string.dialog_logout_body)
                .setPositiveButton(R.string.dialog_logout_yes, (dialogInterface, i) -> {
                    FirebaseAuthentication auth = FirebaseAuthentication.getInstance();
                    auth.logoutUser();
                    drawerUsername.setText(R.string.nav_header_username);
                    drawerEmail.setText(R.string.nav_header_email);
                    signMenuItem.setVisible(true);
                    logoutMenuItem.setVisible(false);
                    drawer.close();

                    // Back to home to avoid displaying outdated data
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.nav_home);
                }).setNegativeButton(R.string.dialog_logout_no, (dialogInterface, i) -> {});
        confirmationWindows.show();
    }
}