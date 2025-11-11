package com.example.evshop.data.modelchat.request;

import com.google.gson.annotations.SerializedName;

/**
 * Lớp này đại diện cho đối tượng request được gửi lên server
 * để cập nhật Firebase Cloud Messaging (FCM) token.
 */
public class UpdateFCMTokenRequest {

    // Tên trường này ("fcmToken") phải khớp với key mà API backend mong đợi.
    // Sử dụng @SerializedName để đảm bảo tên trong JSON luôn đúng,
    // ngay cả khi bạn đổi tên biến trong Java.
    @SerializedName("fcmToken")
    private String fcmToken;

    /**
     * Constructor để tạo một request cập nhật FCM token.
     * @param fcmToken Đoạn mã token mới nhất lấy từ Firebase.
     */
    public UpdateFCMTokenRequest(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    // --- (Tùy chọn) Thêm Getter và Setter để truy cập an toàn ---

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
