package com.example.evshop.data.network;

import com.example.evshop.data.chat.model.ChatMessage;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatApiService {

    // Lấy danh sách tin nhắn trong phòng chat
    @GET("api/chat/rooms/{roomId}/messages")
    Call<List<ChatMessage>> getMessages(@Path("roomId") String roomId);

    // Gửi tin nhắn mới
    @POST("api/chat/send")
    Call<Void> sendMessage(@Body ChatMessage message);

    // Tạo phòng chat mới nếu chưa tồn tại, hoặc trả về roomId đã có
    @GET("api/chat/room")
    Call<String> getOrCreateRoom(
            @Query("user1") String user1,
            @Query("user2") String user2
    );
}
