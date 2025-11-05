package com.example.evshop.util;

import com.example.evshop.ui.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppNotificationManager {

    private static AppNotificationManager instance;
    private final List<Notification> notifications = new ArrayList<>();
    private int nextId = 1;

    private AppNotificationManager() {}

    public static synchronized AppNotificationManager getInstance() {
        if (instance == null) {
            instance = new AppNotificationManager();
        }
        return instance;
    }

    public void addNotification(String title, String message) {
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());

        Notification newNotification = new Notification(
                nextId++,
                0, // Giả sử userID là 0 cho thông báo hệ thống
                title,
                message,
                currentTime,
                false,
                currentDate
        );
        notifications.add(0, newNotification); // Thêm vào đầu danh sách
    }

    public List<Notification> getAllNotifications() {
        return new ArrayList<>(notifications);
    }
}
