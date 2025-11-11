package com.example.evshop.data.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.evshop.data.TokenManager;
import com.example.evshop.data.api.ChatApi;
import com.example.evshop.data.modelchat.ChatMessage;
import com.example.evshop.data.modelchat.ChatRoom;
// Import chính xác ApiEnvelope
import com.example.evshop.domain.models.ApiEnvelope;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RealtimeChatService {
    private static final String TAG = "RealtimeChatService";

    // ĐỊNH NGHĨA URL FIREBASE CHO DỰ ÁN EVSHOP
    // NHỚ THAY BẰNG URL FIREBASE CỦA BẠN
    private static final String FIREBASE_DATABASE_URL = "https://your-evshop-project-rtdb.firebaseio.com";

    private static RealtimeChatService instance;

    private final ChatApi chatApi;
    private final Handler mainHandler;
    private final ScheduledExecutorService scheduler;
    private final TokenManager tokenManager; // SỬ DỤNG TOKEN MANAGER

    // Event listeners
    private final Map<String, OnNewMessageListener> messageListeners = new HashMap<>();
    private final Map<String, OnChatRoomUpdateListener> roomListeners = new HashMap<>();

    // Firebase listeners
    private final Map<String, ChildEventListener> firebaseChildListeners = new HashMap<>();

    // Polling backup
    private ScheduledFuture<?> chatRoomsPollingFuture;
    private final AtomicBoolean chatRoomsPollInFlight = new AtomicBoolean(false);


    private RealtimeChatService(Context context, ChatApi chatApi) {
        this.chatApi = chatApi;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.tokenManager = new TokenManager(context);
    }

    public static synchronized RealtimeChatService getInstance(Context context, ChatApi chatApi) {
        if (instance == null) {
            instance = new RealtimeChatService(context, chatApi);
        }
        return instance;
    }

    public void startListeningForMessages(String roomId, OnNewMessageListener listener) {
        if (roomId == null || roomId.isEmpty()) return;
        Log.d(TAG, "Starting real-time listening for room: " + roomId);
        messageListeners.put(roomId, listener);
        startFirebaseMessageListener(roomId);
    }

    public void startListeningForChatRooms(OnChatRoomUpdateListener listener) {
        Log.d(TAG, "Starting real-time listening for chat rooms");
        roomListeners.put("global", listener);
        startChatRoomsFirebaseListener();
        startChatRoomPolling();
    }

    private void startFirebaseMessageListener(String roomId) {
        stopListeningForMessages(roomId);
        OnNewMessageListener existingListener = messageListeners.get(roomId);
        messageListeners.put(roomId, existingListener);

        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL);
            DatabaseReference messagesRef = database.getReference("Messages").child(roomId);

            ChildEventListener childListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                    handleNewMessage(snapshot, roomId);
                }
                @Override
                public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                    handleNewMessage(snapshot, roomId);
                }
                @Override public void onChildRemoved(DataSnapshot snapshot) {}
                @Override public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}
                @Override public void onCancelled(DatabaseError error) {
                    Log.e(TAG, "Firebase message listener cancelled for room " + roomId + ": " + error.getMessage());
                }
            };

            messagesRef.addChildEventListener(childListener);
            firebaseChildListeners.put(roomId, childListener);
            Log.d(TAG, "Attached Firebase message listener for room: " + roomId);
        } catch (Exception e) {
            Log.e(TAG, "Error starting Firebase message listener: " + e.getMessage());
        }
    }

    private void handleNewMessage(DataSnapshot snapshot, String roomId) {
        try {
            ChatMessage message = parseMessageFromSnapshot(snapshot, roomId);
            if (message != null) {
                OnNewMessageListener currentListener = messageListeners.get(roomId);
                if (currentListener != null) {
                    mainHandler.post(() -> currentListener.onNewMessage(message));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling new message: " + e.getMessage());
        }
    }

    private void startChatRoomsFirebaseListener() {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL);
            DatabaseReference chatRoomsRef = database.getReference("Conversations");

            chatRoomsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Log.d(TAG, "Conversations data changed, triggering immediate poll.");
                    pollForChatRooms();
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w(TAG, "ChatRooms Firebase listener cancelled: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error starting chat rooms Firebase listener: " + e.getMessage());
        }
    }

    private void startChatRoomPolling() {
        if (chatRoomsPollingFuture != null && !chatRoomsPollingFuture.isCancelled()) {
            return;
        }
        chatRoomsPollingFuture = scheduler.scheduleWithFixedDelay(() -> {
            if (!chatRoomsPollInFlight.get()) {
                pollForChatRooms();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    // Mã nguồn ở đây giờ đã hoàn toàn chính xác
    private void pollForChatRooms() {
        if (!chatRoomsPollInFlight.compareAndSet(false, true)) {
            return;
        }

        chatApi.getChatRooms().enqueue(new Callback<ApiEnvelope<List<ChatRoom>>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<List<ChatRoom>>> call, Response<ApiEnvelope<List<ChatRoom>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ChatRoom> rooms = response.body().getData();
                    OnChatRoomUpdateListener listener = roomListeners.get("global");
                    if (listener != null && rooms != null) {
                        mainHandler.post(() -> listener.onChatRoomsUpdated(rooms));
                    }
                } else {
                    Log.e(TAG, "Failed to poll chat rooms. Code: " + response.code());
                }
                chatRoomsPollInFlight.set(false);
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ChatRoom>>> call, Throwable t) {
                Log.e(TAG, "Error polling chat rooms: " + t.getMessage());
                chatRoomsPollInFlight.set(false);
            }
        });
    }

    private ChatMessage parseMessageFromSnapshot(DataSnapshot snapshot, String roomId) {
        try {
            ChatMessage message = snapshot.getValue(ChatMessage.class);
            if (message == null) return null;
            message.setMessageId(snapshot.getKey());
            message.setRoomId(roomId);

            String currentUserId = tokenManager.getUserId();
            if (currentUserId != null) {
                message.setMe(currentUserId.equals(message.getSenderId()));
            }
            return message;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message from snapshot: " + e.getMessage());
            return null;
        }
    }

    public void stopListeningForMessages(String roomId) {
        if (roomId == null) return;
        Log.d(TAG, "Stopping real-time listening for room: " + roomId);
        ChildEventListener listener = firebaseChildListeners.remove(roomId);
        if (listener != null) {
            try {
                FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL)
                        .getReference("Messages")
                        .child(roomId)
                        .removeEventListener(listener);
            } catch (Exception e) {
                Log.e(TAG, "Error removing Firebase message listener: " + e.getMessage());
            }
        }
        messageListeners.remove(roomId);
    }

    public void stopListeningForChatRooms() {
        Log.d(TAG, "Stopping real-time listening for chat rooms");
        if (chatRoomsPollingFuture != null && !chatRoomsPollingFuture.isCancelled()) {
            chatRoomsPollingFuture.cancel(true);
            chatRoomsPollingFuture = null;
        }
        roomListeners.clear();
    }

    public void cleanup() {
        Log.d(TAG, "Cleaning up RealtimeChatService.");
        stopListeningForChatRooms();
        new HashMap<>(firebaseChildListeners).keySet().forEach(this::stopListeningForMessages);
        scheduler.shutdown();
        instance = null;
    }

    public interface OnNewMessageListener {
        void onNewMessage(ChatMessage newMessage);
    }

    public interface OnChatRoomUpdateListener {
        void onChatRoomsUpdated(List<ChatRoom> chatRooms);
    }
}
