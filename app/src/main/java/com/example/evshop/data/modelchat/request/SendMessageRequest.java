package com.example.evshop.data.modelchat.request;

import com.google.gson.annotations.SerializedName;

/**
 * Lớp này đại diện cho đối tượng request được gửi đi khi người dùng gửi một tin nhắn.
 */
public class SendMessageRequest {

    @SerializedName("roomId")
    private String roomId;

    @SerializedName("message")
    private String message;

    @SerializedName("attachmentUrl")
    private String attachmentUrl;

    @SerializedName("attachmentType")
    private String attachmentType;

    @SerializedName("replyToMessageId")
    private String replyToMessageId;

    // Constructor mặc định (cần thiết cho một số thư viện)
    public SendMessageRequest() {}

    // ✅ CONSTRUCTOR MỚI ĐƯỢC THÊM VÀO ĐỂ SỬA LỖI
    /**
     * Constructor đơn giản để gửi tin nhắn văn bản.
     * @param roomId ID của phòng chat.
     * @param message Nội dung tin nhắn.
     */
    public SendMessageRequest(String roomId, String message) {
        this.roomId = roomId;
        this.message = message;
        this.attachmentUrl = null; // Mặc định là null
        this.attachmentType = null; // Mặc định là null
        this.replyToMessageId = null; // Mặc định là null
    }

    // Constructor đầy đủ (giữ lại để sử dụng trong tương lai khi có file đính kèm)
    public SendMessageRequest(String roomId, String message, String attachmentUrl,
                              String attachmentType, String replyToMessageId) {
        this.roomId = roomId;
        this.message = message;
        this.attachmentUrl = attachmentUrl;
        this.attachmentType = attachmentType;
        this.replyToMessageId = replyToMessageId;
    }

    // --- Getters and Setters ---
    // (Giữ nguyên không thay đổi)
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }

    public String getReplyToMessageId() { return replyToMessageId; }
    public void setReplyToMessageId(String replyToMessageId) { this.replyToMessageId = replyToMessageId; }
}
