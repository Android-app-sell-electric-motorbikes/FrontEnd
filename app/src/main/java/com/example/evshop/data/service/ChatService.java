package com.example.evshop.data.service;

import android.content.Context;
import android.util.Log;

import com.example.evshop.data.TokenManager;
import com.example.evshop.data.api.ChatApi;
import com.example.evshop.data.modelchat.request.SendMessageRequest;
import com.example.evshop.data.modelchat.ChatMessage;
import com.example.evshop.data.modelchat.ChatRoom;
import com.example.evshop.domain.models.ApiEnvelope;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatService {
    private static final String TAG = "ChatService";

    // NHỚ THAY BẰNG URL FIREBASE CỦA DỰ ÁN EVSHOP
    private static final String FIREBASE_DATABASE_URL = "https://your-evshop-project-rtdb.firebaseio.com";

    private final ChatApi chatApi;
    private final TokenManager tokenManager;

    public ChatService(ChatApi chatApi, Context context) {
        this.chatApi = chatApi;
        this.tokenManager = new TokenManager(context);
    }

    // --- INTERFACES ---
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

    // --- PHƯƠNG THỨC GỌI API ---

    public void getChatRooms(ChatRoomsCallback callback) {
        chatApi.getChatRooms().enqueue(new Callback<ApiEnvelope<List<ChatRoom>>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<List<ChatRoom>>> call, Response<ApiEnvelope<List<ChatRoom>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    String errorMsg = "Failed to get chat rooms";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ChatRoom>>> call, Throwable t) {
                Log.e(TAG, "Error getting chat rooms", t);
                callback.onError(t.getMessage());
            }
        });
    }

    public void getMessagesForRoom(String roomId, MessagesCallback callback) {
        chatApi.getMessagesForRoom(roomId).enqueue(new Callback<ApiEnvelope<List<ChatMessage>>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<List<ChatMessage>>> call, Response<ApiEnvelope<List<ChatMessage>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ChatMessage> messages = response.body().getData() != null ? response.body().getData() : new ArrayList<>();
                    String currentUserId = tokenManager.getUserId();
                    if (currentUserId != null) {
                        for (ChatMessage message : messages) {
                            message.setMe(currentUserId.equals(message.getSenderId()));
                        }
                    }
                    callback.onSuccess(messages);
                } else {
                    String errorMsg = "Failed to get messages";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ChatMessage>>> call, Throwable t) {
                Log.e(TAG, "Error getting messages", t);
                callback.onError(t.getMessage());
            }
        });
    }

    public void sendMessage(String roomId, String message, SendMessageCallback callback) {
        SendMessageRequest request = new SendMessageRequest(roomId, message);
        chatApi.sendMessage(request).enqueue(new Callback<ApiEnvelope<String>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<String>> call, Response<ApiEnvelope<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Failed to send message");
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<String>> call, Throwable t) {
                Log.e(TAG, "Error sending message", t);
                callback.onError(t.getMessage());
            }
        });
    }

    // ✅ BỔ SUNG LẠI PHƯƠNG THỨC NÀY
    public void markMessagesAsRead(String roomId, SendMessageCallback callback) {
        chatApi.markMessagesAsRead(roomId).enqueue(new Callback<ApiEnvelope<String>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<String>> call, Response<ApiEnvelope<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "API call to mark messages as read was successful.");
                    callback.onSuccess();
                } else {
                    String errorMsg = "Failed to mark messages as read";
                    if(response.body() != null && response.body().getMessage() != null){
                        errorMsg = response.body().getMessage();
                    }
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<String>> call, Throwable t) {
                Log.e(TAG, "Error marking messages as read", t);
                callback.onError(t.getMessage());
            }
        });
    }
}
