package com.example.evshop.data.api;

// SỬA: Import các model từ đúng package
import com.example.evshop.data.modelchat.request.SendMessageRequest;
import com.example.evshop.data.modelchat.request.UpdateFCMTokenRequest;
import com.example.evshop.data.modelchat.ChatMessage;
import com.example.evshop.data.modelchat.ChatRoom;
// SỬA: Import ApiEnvelope thay cho ApiResponse
import com.example.evshop.domain.models.ApiEnvelope;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ChatApi {

    // SỬA: Thay thế tất cả ApiResponse bằng ApiEnvelope
    @GET("api/chat/rooms")
    Call<ApiEnvelope<List<ChatRoom>>> getChatRooms();

    @GET("api/chat/rooms/{roomId}/messages")
    Call<ApiEnvelope<List<ChatMessage>>> getMessagesForRoom(@Path("roomId") String roomId);

    @POST("api/chat/messages")
    Call<ApiEnvelope<String>> sendMessage(@Body SendMessageRequest request);

    @POST("api/chat/rooms/{roomId}/read")
    Call<ApiEnvelope<String>> markMessagesAsRead(@Path("roomId") String roomId);

    @GET("api/chat/rooms/customer")
    Call<ApiEnvelope<String>> getOrCreateCustomerRoom();

    @PUT("api/users/fcm-token")
    Call<ApiEnvelope<String>> updateFCMToken(@Body UpdateFCMTokenRequest request);
}
