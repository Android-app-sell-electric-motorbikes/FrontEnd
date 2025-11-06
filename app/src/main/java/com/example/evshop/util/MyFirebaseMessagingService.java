package com.example.evshop.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.evshop.R;
import com.example.evshop.data.chat.ChatRepositoryHolder;
import com.example.evshop.data.chat.model.ChatMessage;
import com.example.evshop.ui.chat.ChatActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "chat_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getData() != null && !remoteMessage.getData().isEmpty()) {
            String messageJson = remoteMessage.getData().get("message");
            if (messageJson != null) {
                ChatMessage chatMessage = new Gson().fromJson(messageJson, ChatMessage.class);
                if (ChatRepositoryHolder.getInstance().getRepository() != null) {
                    ChatRepositoryHolder.getInstance().getRepository().handleIncomingMessage(chatMessage);
                }
                showNotification(chatMessage);
                return;
            }
        }

        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            showBasicNotification(title, body);
        }
    }

    private void showNotification(ChatMessage chatMessage) {
        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_ROOM_ID, chatMessage.getRoomId());
        intent.putExtra(ChatActivity.EXTRA_TARGET_ID, chatMessage.getSenderId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_round_chat_24)
                .setContentTitle("Tin nhắn mới")
                .setContentText(chatMessage.getMessage())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(chatMessage.getMessage()));

        NotificationManagerCompat.from(this)
                .notify((int) (System.currentTimeMillis() % 10000), builder.build());
    }

    private void showBasicNotification(String title, String body) {
        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_round_chat_24)
                .setContentTitle(title != null ? title : "Tin nhắn mới")
                .setContentText(body != null ? body : "")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat.from(this)
                .notify((int) (System.currentTimeMillis() % 10000), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Tin nhắn trò chuyện",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Thông báo tin nhắn mới giữa khách hàng và admin");
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Lưu hoặc gửi token lên server nếu cần
    }
}
