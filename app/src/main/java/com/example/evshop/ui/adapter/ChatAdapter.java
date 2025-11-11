package com.example.evshop.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.data.modelchat.ChatMessage;

import java.util.List;
import java.util.Objects;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_LEFT = 0;
    private static final int TYPE_RIGHT = 1;
    private final List<ChatMessage> messages;
    private final String currentUserId;

    public ChatAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        // Ưu tiên cờ isMe nếu có, nếu không thì so sánh senderId
        if (message.isMe()) {
            return TYPE_RIGHT;
        }
        if (message.getSenderId() != null && message.getSenderId().equals(currentUserId)) {
            return TYPE_RIGHT;
        }
        return TYPE_LEFT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_RIGHT) {
            View view = inflater.inflate(R.layout.item_message_right, parent, false);
            return new RightViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_left, parent, false);
            return new LeftViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        String time = message.getTime(); // Dùng phương thức getTime đã được chuẩn hóa

        if (holder.getItemViewType() == TYPE_RIGHT) {
            RightViewHolder vh = (RightViewHolder) holder;
            vh.message.setText(message.getMessage());
            vh.time.setText(time);
        } else {
            LeftViewHolder vh = (LeftViewHolder) holder;
            vh.message.setText(message.getMessage());
            vh.time.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class LeftViewHolder extends RecyclerView.ViewHolder {
        TextView message, time;
        public LeftViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.messageLeft);
            time = itemView.findViewById(R.id.timeLeft);
        }
    }

    public static class RightViewHolder extends RecyclerView.ViewHolder {
        TextView message, time;
        public RightViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.messageRight);
            time = itemView.findViewById(R.id.timeRight);
        }
    }
}

