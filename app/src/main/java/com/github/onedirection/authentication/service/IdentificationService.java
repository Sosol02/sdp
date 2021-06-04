package com.github.onedirection.authentication.service;

import android.content.Context;
import android.util.Log;

import com.github.onedirection.utils.Monads;
import com.google.firebase.installations.FirebaseInstallations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IdentificationService {
    private static final String LOG_TAG = "IdentificationService";

    private static File fileDir;
    private static String firebaseInstallationId = null;
    private static String deviceId = null;

    public static void initService(Context context) {
        fileDir = context.getFilesDir();
        File file = new File(fileDir, "device_id");
        if (!file.exists()) {
            UUID uuid = UUID.randomUUID();
            deviceId = uuid.toString();
            try {
                Files.write(file.toPath(), deviceId.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            byte[] bytes;
            try {
                bytes = Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            deviceId = new String(bytes, StandardCharsets.UTF_8);
        }
    }

    public static String getDeviceId() {
        return deviceId;
    }

    public static void renewDeviceId() {
        File file = new File(fileDir, "device_id");
        UUID uuid = UUID.randomUUID();
        deviceId = uuid.toString();
        try {
            Files.write(file.toPath(), deviceId.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
