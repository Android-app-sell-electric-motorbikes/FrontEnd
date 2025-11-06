package com.example.evshop.data.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.evshop.data.chat.model.ChatMessage;
import com.example.evshop.data.network.ChatApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {

    private final ChatApiService apiService;
    private final MutableLiveData<List<ChatMessage>> messagesLiveData = new MutableLiveData<>();

    public ChatRepository(ChatApiService apiService) {
        this.apiService = apiService;
    }

    public LiveData<List<ChatMessage>> getMessagesLiveData() {
        return messagesLiveData;
    }

    public void loadMessages(String roomId) {
        apiService.getMessages(roomId).enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messagesLiveData.postValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void sendMessage(ChatMessage message, Callback<Void> callback) {
        apiService.sendMessage(message).enqueue(callback);
    }

    public void getOrCreateRoom(String senderId, String receiverId, Callback<String> callback) {
        ChatApiService.ChatRoomRequest request = new ChatApiService.ChatRoomRequest(senderId, receiverId);
        apiService.getOrCreateRoom(request).enqueue(callback);
    }
}
