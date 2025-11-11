package com.example.evshop.data.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.evshop.data.TokenManager;
import com.example.evshop.data.modelchat.ChatMessage;
import com.example.evshop.data.modelchat.ChatRoom;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RealtimeChatService {

    private static final String TAG = "RealtimeChatService";
    private static RealtimeChatService instance;

    private final FirebaseDatabase database;
    private final TokenManager tokenManager;
    private final Handler mainHandler;

    // roomId -> listener
    private final Map<String, OnNewMessageListener> messageListeners = new HashMap<>();
    private final Map<String, ChildEventListener> firebaseListeners = new HashMap<>();

    private RealtimeChatService(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
        this.database = FirebaseDatabase.getInstance("https://your-evshop-project-rtdb.firebaseio.com/");
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized RealtimeChatService getInstance(TokenManager tokenManager) {
        if (instance == null) {
            instance = new RealtimeChatService(tokenManager);
        }
        return instance;
    }

    // --- LISTEN TIN NHẮN MỚI ---
    public void startListeningForMessages(String roomId, OnNewMessageListener listener) {
        if (roomId == null || listener == null) return;

        stopListeningForMessages(roomId); // remove listener cũ nếu có

        DatabaseReference ref = database.getReference("Messages").child(roomId);
        ChildEventListener childListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                handleMessage(snapshot, roomId);
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                handleMessage(snapshot, roomId);
            }

            @Override public void onChildRemoved(DataSnapshot snapshot) {}
            @Override public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Firebase listener cancelled: " + error.getMessage());
            }

            private void handleMessage(DataSnapshot snapshot, String roomId) {
                ChatMessage msg = snapshot.getValue(ChatMessage.class);
                if (msg != null) {
                    msg.setMessageId(snapshot.getKey());
                    msg.setMe(tokenManager.getUserId() != null &&
                            tokenManager.getUserId().equals(msg.getSenderId()));
                    mainHandler.post(() -> listener.onNewMessage(msg));
                }
            }
        };

        ref.addChildEventListener(childListener);
        messageListeners.put(roomId, listener);
        firebaseListeners.put(roomId, childListener);
    }

    public void stopListeningForMessages(String roomId) {
        if (roomId == null) return;

        ChildEventListener listener = firebaseListeners.remove(roomId);
        if (listener != null) {
            database.getReference("Messages").child(roomId).removeEventListener(listener);
        }
        messageListeners.remove(roomId);
    }

    public void cleanup() {
        for (String roomId : new ArrayList<>(firebaseListeners.keySet())) {
            stopListeningForMessages(roomId);
        }
        instance = null;
    }

    public interface OnNewMessageListener {
        void onNewMessage(ChatMessage newMessage);
    }
}
