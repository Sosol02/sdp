package com.github.onedirection.startup;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import com.github.onedirection.database.utils.FirebaseUtils;
import com.github.onedirection.geolocation.geocoding.GeocodingService;
import com.github.onedirection.notification.Notifications;

import java.util.Collections;
import java.util.List;

/**
 * An application initializer that initializes the notification system
 */
public class ApplicationInitializer implements Initializer<Notifications> {

    @NonNull
    @Override
    public Notifications create(@NonNull Context context) {
        GeocodingService.init(context);
        FirebaseUtils.init();
        // Starts the notifications handler.
        // DO NOT REMOVE THIS LINE otherwise notifications wont work!
        return Notifications.getInstance(context);
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }
}
