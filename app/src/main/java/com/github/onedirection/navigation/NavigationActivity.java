package com.github.onedirection.navigation;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.github.onedirection.authentication.FirebaseAuthentication;
import com.github.onedirection.database.Database;
import com.github.onedirection.database.queries.EventQueries;
import com.github.onedirection.event.Event;
import com.github.onedirection.event.ui.EventCreator;
import com.google.android.material.navigation.NavigationView;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class NavigationActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    List<Event> events = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ZonedDateTime date = ZonedDateTime.now();

        ZonedDateTime firstInstantOfMonth = ZonedDateTime.of(2021, 5, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        CompletableFuture<List<Event>> monthEventsFuture = getEventFromMonth(firstInstantOfMonth);


        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_calendar,R.id.nav_home, R.id.nav_map)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        navigationView.getMenu().findItem(R.id.nav_create_event).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(getApplicationContext(), EventCreator.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem signMenuItem = navigationView.getMenu().findItem(R.id.nav_sign);
        MenuItem logoutMenuItem = navigationView.getMenu().findItem(R.id.nav_logout);

        View headerView = navigationView.getHeaderView(0);
        TextView drawerUsername = headerView.findViewById(R.id.nav_header_username);
        TextView drawerEmail = headerView.findViewById(R.id.nav_header_email);

        logoutMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                logout(signMenuItem, logoutMenuItem, drawerUsername, drawerEmail, drawer);
                return false;
            }
        });
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
                .setPositiveButton(R.string.dialog_logout_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseAuthentication auth = FirebaseAuthentication.getInstance();
                        auth.logoutUser();
                        drawerUsername.setText(R.string.nav_header_username);
                        drawerEmail.setText(R.string.nav_header_email);
                        signMenuItem.setVisible(true);
                        logoutMenuItem.setVisible(false);
                        drawer.close();
                    }
                }).setNegativeButton(R.string.dialog_logout_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        confirmationWindows.show();
    }

    public CompletableFuture<List<Event>> getEventFromMonth(ZonedDateTime date) {
        Database db = Database.getDefaultInstance();
        EventQueries queryManager = new EventQueries(db);
        CompletableFuture<List<Event>> monthEventsFuture = queryManager.getEventsByMonth(date);
        return monthEventsFuture;
    }

    private List<Event> getEvents(){
        return this.events;
    }
}