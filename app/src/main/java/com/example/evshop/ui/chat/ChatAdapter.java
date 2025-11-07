package com.example.evshop.ui.chat;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.data.chat.model.ChatMessage;
import com.example.evshop.databinding.ItemChatMessageInBinding;
import com.example.evshop.databinding.ItemChatMessageOutBinding;

public class ChatAdapter extends ListAdapter<ChatMessage, RecyclerView.ViewHolder> {

    private final String currentUserId;
    private static final int TYPE_IN = 0;
    private static final int TYPE_OUT = 1;

    public ChatAdapter(String currentUserId) {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
    }

    // So sánh dữ liệu cũ và mới để cập nhật UI tối ưu
    private static final DiffUtil.ItemCallback<ChatMessage> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ChatMessage>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
                    // Nếu có id riêng cho tin nhắn thì dùng id, nếu không thì fallback timestamp
                    if (oldItem.getId() != null && newItem.getId() != null)
                        return oldItem.getId().equals(newItem.getId());
                    else
                        return oldItem.getTimestamp() == newItem.getTimestamp();
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = getItem(position);
        return msg.getSenderId().equals(currentUserId) ? TYPE_OUT : TYPE_IN;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_IN) {
            ItemChatMessageInBinding binding = ItemChatMessageInBinding.inflate(inflater, parent, false);
            return new InViewHolder(binding);
        } else {
            ItemChatMessageOutBinding binding = ItemChatMessageOutBinding.inflate(inflater, parent, false);
            return new OutViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = getItem(position);
        if (holder instanceof InViewHolder) {
            ((InViewHolder) holder).bind(msg);
        } else if (holder instanceof OutViewHolder) {
            ((OutViewHolder) holder).bind(msg);
        }
    }

    static class InViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageInBinding binding;

        public InViewHolder(ItemChatMessageInBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ChatMessage msg) {
            binding.tvMessage.setText(msg.getText());
            // Nếu bạn có thêm thời gian, có thể hiển thị ở đây:
            // binding.tvTime.setText(DateFormat.format("HH:mm", msg.getTimestamp()));
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
            // binding.tvTime.setText(DateFormat.format("HH:mm", msg.getTimestamp()));
        }
    }
}
