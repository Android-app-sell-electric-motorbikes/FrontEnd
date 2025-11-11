package com.example.evshop.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.data.modelchat.ChatRoom;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private final List<ChatRoom> chatRooms;
    private final OnChatRoomClickListener listener;

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom);
    }

    public ChatRoomAdapter(List<ChatRoom> chatRooms, OnChatRoomClickListener listener) {
        this.chatRooms = chatRooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRooms.get(position);

        holder.customerName.setText(chatRoom.getCustomerName() != null ? chatRoom.getCustomerName() : "Customer");
        holder.lastMessage.setText(chatRoom.getLastMessage() != null ? chatRoom.getLastMessage() : "No messages yet");

        // Format time
        String timeText = "";
        if (chatRoom.getLastMessageTime() != null && !chatRoom.getLastMessageTime().isEmpty()) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(chatRoom.getLastMessageTime());
                timeText = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception e) {
                timeText = ""; // Để trống nếu không parse được
            }
        }
        holder.lastMessageTime.setText(timeText);

        holder.onlineIndicator.setVisibility(chatRoom.isOnline() ? View.VISIBLE : View.GONE);

        if (chatRoom.getUnreadCount() > 0) {
            holder.unreadCount.setText(String.valueOf(chatRoom.getUnreadCount()));
            holder.unreadCount.setVisibility(View.VISIBLE);
        } else {
            holder.unreadCount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatRoomClick(chatRoom);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    public static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView customerName, lastMessage, lastMessageTime, unreadCount;
        View onlineIndicator;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.customerName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            lastMessageTime = itemView.findViewById(R.id.lastMessageTime);
            unreadCount = itemView.findViewById(R.id.unreadCount);
            onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
        }
    }
}
