package com.example.evshop.data.network;

import com.example.evshop.data.chat.model.ChatMessage;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatApiService {

    @GET("chat/rooms/{roomId}/messages")
    Call<List<ChatMessage>> getMessages(@Path("roomId") String roomId);

    @POST("chat/messages")
    Call<Void> sendMessage(@Body ChatMessage message);

    @POST("chat/rooms")
    Call<String> getOrCreateRoom(@Body ChatRoomRequest request);

    class ChatRoomRequest {
        public String senderId;
        public String receiverId;
        public ChatRoomRequest(String senderId, String receiverId) {
            this.senderId = senderId;
            this.receiverId = receiverId;
        }
    }
}
