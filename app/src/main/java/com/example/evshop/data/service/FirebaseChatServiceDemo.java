package com.example.evshop.data.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.evshop.data.modelchat.ChatMessage;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FirebaseChatServiceDemo {

    private static final String TAG = "FirebaseChatDemo";
    private static final String ROOM_ID = "room_demo";

    private final String currentUserId;
    private final DatabaseReference messagesRef;

    private final List<ChatMessage> messageList = new ArrayList<>();
    private ChildEventListener childEventListener;

    public interface MessageListener {
        void onNewMessage(ChatMessage message);
    }

    public FirebaseChatServiceDemo(String currentUserId) {
        this.currentUserId = currentUserId;
        this.messagesRef = FirebaseDatabase.getInstance()
                .getReference("Messages")
                .child(ROOM_ID);
    }

    // Gửi tin nhắn
    public void sendMessage(String messageText) {
        String messageId = messagesRef.push().getKey();
        if (messageId == null) return;

        ChatMessage message = new ChatMessage();
        message.setMessageId(messageId);
        message.setRoomId(ROOM_ID);
        message.setSenderId(currentUserId);
        message.setMessage(messageText);
        message.setEpochMillis(System.currentTimeMillis());

        messagesRef.child(messageId).setValue(message)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Message sent!"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send message", e));
    }

    // Lắng nghe tin nhắn realtime
    public void startListening(MessageListener listener) {
        stopListening();

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                handleNewMessage(snapshot, listener);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                handleNewMessage(snapshot, listener);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase listener cancelled: " + error.getMessage());
            }
        };

        messagesRef.addChildEventListener(childEventListener);
    }

    private void handleNewMessage(DataSnapshot snapshot, MessageListener listener) {
        ChatMessage message = snapshot.getValue(ChatMessage.class);
        if (message != null) {
            message.setMessageId(snapshot.getKey());
            message.setRoomId(ROOM_ID);
            message.setMe(currentUserId.equals(message.getSenderId()));

            messageList.add(message);

            if (listener != null) {
                listener.onNewMessage(message);
            }

            Log.d(TAG, "New message: " + message.getSenderId() + " -> " + message.getMessage());
        }
    }

    public void stopListening() {
        if (childEventListener != null) {
            messagesRef.removeEventListener(childEventListener);
            childEventListener = null;
        }
    }

    public List<ChatMessage> getMessageList() {
        return messageList;
    }
}
