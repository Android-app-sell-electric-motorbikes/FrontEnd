package com.example.evshop.data.chat;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.evshop.data.chat.model.ChatMessage; // <--- SỬ DỤNG ĐÚNG PACKAGE
import com.example.evshop.data.network.ChatApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {
    private final ChatApiService api;
    private final MutableLiveData<List<ChatMessage>> messagesLive = new MutableLiveData<>();
    private final MutableLiveData<ChatMessage> incomingMessageLive = new MutableLiveData<>();

    public ChatRepository(ChatApiService api) {
        this.api = api;
    }

    public LiveData<List<ChatMessage>> getMessagesLive() {
        return messagesLive;
    }

    public LiveData<ChatMessage> getIncomingMessageLive() {
        return incomingMessageLive;
    }

    public void loadMessages(String roomId) {
        api.getMessages(roomId).enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messagesLive.postValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                Log.e("ChatRepository", "loadMessages failed", t);
            }
        });
    }

    public void sendMessage(ChatMessage message, Callback<Void> callback) {
        api.sendMessage(message).enqueue(callback);
    }

    // Gọi từ FCM service để push message vào UI nếu đang mở
    public void handleIncomingMessage(ChatMessage message) {
        incomingMessageLive.postValue(message);
    }
}
