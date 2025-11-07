package com.example.evshop.data.chat.model;

import androidx.annotation.NonNull;
import java.util.Objects;

public class ChatMessage {
    private String id;
    private String roomId;
    private String senderId;
    private String text;
    private long timestamp;

    public ChatMessage() {
        // Firebase cần constructor rỗng
    }

    public ChatMessage(String roomId, String senderId, String text, long timestamp) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage)) return false;
        ChatMessage that = (ChatMessage) o;

        // Ưu tiên so sánh theo id (nếu có)
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }

        // Nếu id chưa được Firebase set, fallback so sánh theo nội dung
        return timestamp == that.timestamp &&
                Objects.equals(roomId, that.roomId) &&
                Objects.equals(senderId, that.senderId) &&
                Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, roomId, senderId, text, timestamp);
    }

    @NonNull
    @Override
    public String toString() {
        return "ChatMessage{" +
                "id='" + id + '\'' +
                ", roomId='" + roomId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
