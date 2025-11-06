package com.example.evshop.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.evshop.R;
import com.example.evshop.data.chat.model.ChatMessage;
import java.util.List;

/**
 * ChatAdapter hiển thị danh sách tin nhắn, gồm hai loại layout:
 * - Tin nhắn gửi đi (Sent)
 * - Tin nhắn nhận được (Received)
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> items;
    private final String currentUserId;

    // Loại view cho 2 kiểu tin nhắn
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> items, String currentUserId) {
        this.items = items;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = items.get(position);
        if (msg == null || msg.getSenderId() == null) return TYPE_RECEIVED;
        return currentUserId.equals(msg.getSenderId()) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_SENT) {
            View view = inflater.inflate(R.layout.item_message_sent, parent, false);
            return new SentHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_received, parent, false);
            return new ReceivedHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = items.get(position);
        if (msg == null) return;

        if (holder instanceof SentHolder) {
            ((SentHolder) holder).bind(msg);
        } else if (holder instanceof ReceivedHolder) {
            ((ReceivedHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    // ViewHolder cho tin nhắn gửi
    static class SentHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage;

        SentHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message_sent);
        }

        void bind(ChatMessage msg) {
            tvMessage.setText(msg.getMessage());
        }
    }

    // ViewHolder cho tin nhắn nhận
    static class ReceivedHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage;

        ReceivedHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message_received);
        }

        void bind(ChatMessage msg) {
            tvMessage.setText(msg.getMessage());
        }
    }
}
