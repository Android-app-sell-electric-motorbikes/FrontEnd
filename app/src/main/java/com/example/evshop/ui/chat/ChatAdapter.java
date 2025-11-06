package com.example.evshop.ui.chat;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.evshop.data.chat.model.ChatMessage;
import com.example.evshop.databinding.ItemChatMessageInBinding;
import com.example.evshop.databinding.ItemChatMessageOutBinding;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String currentUserId;
    private final List<ChatMessage> messages = new ArrayList<>();

    private static final int TYPE_IN = 0;
    private static final int TYPE_OUT = 1;

    public ChatAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void submitList(List<ChatMessage> list) {
        messages.clear();
        messages.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        return msg.getSenderId().equals(currentUserId) ? TYPE_OUT : TYPE_IN;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_IN) {
            ItemChatMessageInBinding binding = ItemChatMessageInBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false
            );
            return new InViewHolder(binding);
        } else {
            ItemChatMessageOutBinding binding = ItemChatMessageOutBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false
            );
            return new OutViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        if (holder instanceof InViewHolder) {
            ((InViewHolder) holder).bind(msg);
        } else if (holder instanceof OutViewHolder) {
            ((OutViewHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class InViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageInBinding binding;
        public InViewHolder(ItemChatMessageInBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        public void bind(ChatMessage msg) {
            binding.tvMessage.setText(msg.getText());
        }
    }

    static class OutViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageOutBinding binding;
        public OutViewHolder(ItemChatMessageOutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        public void bind(ChatMessage msg) {
            binding.tvMessage.setText(msg.getText());
        }
    }
}
