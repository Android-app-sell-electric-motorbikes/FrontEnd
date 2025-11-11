package com.example.evshop.data.service;

import android.util.Log;

import com.example.evshop.data.TokenManager;
import com.example.evshop.data.modelchat.ChatMessage;
import com.example.evshop.data.modelchat.ChatRoom;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChatService {

    private static final String TAG = "ChatService";

    private final TokenManager tokenManager;
    private final FirebaseDatabase database;

    public ChatService(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
        this.database = FirebaseDatabase.getInstance("https://your-evshop-project-rtdb.firebaseio.com/");
    }

    // --- CALLBACK INTERFACES ---
    public interface ChatRoomsCallback {
        void onSuccess(List<ChatRoom> chatRooms);
        void onError(String error);
    }

    public interface MessagesCallback {
        void onSuccess(List<ChatMessage> messages);
        void onError(String error);
    }

    public interface SendMessageCallback {
        void onSuccess();
        void onError(String error);
    }

    // --- LẤY DANH SÁCH CHAT ROOM ---
    public void getChatRooms(ChatRoomsCallback callback) {
        DatabaseReference ref = database.getReference("Conversations");
        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<ChatRoom> rooms = new ArrayList<>();
                DataSnapshot snapshot = task.getResult();
                if (snapshot != null) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        ChatRoom room = child.getValue(ChatRoom.class);
                        if (room != null) rooms.add(room);
                    }
                }
                callback.onSuccess(rooms);
            } else {
                callback.onError("Failed to fetch chat rooms: " + task.getException());
            }
        });
    }

    // --- LẤY TIN NHẮN TRONG ROOM ---
    public void getMessagesForRoom(String roomId, MessagesCallback callback) {
        if (roomId == null) {
            callback.onError("RoomId is null");
            return;
        }
        DatabaseReference ref = database.getReference("Messages").child(roomId);
        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<ChatMessage> messages = new ArrayList<>();
                DataSnapshot snapshot = task.getResult();
                if (snapshot != null) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        ChatMessage msg = child.getValue(ChatMessage.class);
                        if (msg != null) {
                            msg.setMessageId(child.getKey());
                            msg.setMe(tokenManager.getUserId() != null &&
                                    tokenManager.getUserId().equals(msg.getSenderId()));
                            messages.add(msg);
                        }
                    }
                }
                callback.onSuccess(messages);
            } else {
                callback.onError("Failed to fetch messages: " + task.getException());
            }
        });
    }

    // --- GỬI TIN NHẮN ---
    public void sendMessage(String roomId, String message, SendMessageCallback callback) {
        if (roomId == null || message == null || message.isEmpty()) {
            callback.onError("Invalid room or message");
            return;
        }
        DatabaseReference ref = database.getReference("Messages").child(roomId).push();
        ChatMessage msg = new ChatMessage();
        msg.setMessage(message);
        msg.setSenderId(tokenManager.getUserId());
        msg.setSenderName(tokenManager.getUsername());
        msg.setTimestampFromLong(System.currentTimeMillis());
        msg.setMe(true);

        ref.setValue(msg).addOnCompleteListener(task -> {
            if (task.isSuccessful()) callback.onSuccess();
            else callback.onError("Failed to send message: " + task.getException());
        });
    }

    // --- ĐÁNH DẤU ĐÃ ĐỌC ---
    public void markMessagesAsRead(String roomId, SendMessageCallback callback) {
        // Demo: chỉ log, chưa thực sự đánh dấu
        Log.d(TAG, "Mark messages as read for room: " + roomId);
        callback.onSuccess();
    }
}
