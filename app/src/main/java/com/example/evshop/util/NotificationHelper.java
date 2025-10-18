package com.example.evshop.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.evshop.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "evshop_channel_id";
    private static final String CHANNEL_NAME = "EVShop Notifications";
    private static final String CHANNEL_DESC = "Show notifications for EVShop app";

    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    /** Tạo channel cho notification (Android 8.0+) */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.enableVibration(true);
            channel.setShowBadge(true); // ⚡ QUAN TRỌNG: bật badge ngoài app

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /** Hiển thị notification cơ bản */
    public void showNotification(String title, String message, int notificationId) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.circle_bg)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    /** Notification riêng cho giỏ hàng */
    public void showCartBadgeNotification(int cartCount) {
        String message = "Bạn có " + cartCount + " sản phẩm trong giỏ hàng.";
        showNotification("Giỏ hàng của bạn", message, 1001);
    }

    /** Cập nhật badge ngoài icon (thông qua Notification) */
    public void updateAppBadge(int count) {
        android.util.Log.d("BadgeTest", "Badge count updated: " + count);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.circle_bg)
                .setContentTitle("Giỏ hàng")
                .setContentText("Bạn có " + count + " sản phẩm trong giỏ hàng.")
                .setNumber(count) // ⚡ Launcher đọc số này để hiển thị badge
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(context).notify(2002, builder.build());
    }

    /** Xoá badge (bằng cách huỷ notification) */
    public void clearAppBadge() {
        NotificationManagerCompat.from(context).cancel(2002);
    }
}
