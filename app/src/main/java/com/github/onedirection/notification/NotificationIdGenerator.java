package com.github.onedirection.notification;

/**
 * A simple ID generator for the notifications
 */
public class NotificationIdGenerator {
    private static Integer counter = 0;
    public static int getUniqueId() {
        synchronized (counter) {
            counter += 1;
            return counter;
        }
    }
}
