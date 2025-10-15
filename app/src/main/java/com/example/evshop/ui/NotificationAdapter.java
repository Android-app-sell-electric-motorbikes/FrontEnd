package com.example.evshop.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.ui.Notification;
import com.example.evshop.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> items;


    public NotificationAdapter(List<Object> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new NotificationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position
    ) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) items.get(position));
        } else if (holder instanceof NotificationViewHolder) {
            ((NotificationViewHolder) holder).bind((Notification) items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Header ViewHolder
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView headerText;

        HeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.headerText);
        }

        void bind(String text) {
            headerText.setText(text);
        }
    }

    // Notification ViewHolder
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView titleText, messageText, timeText;

        NotificationViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timeText);
        }

        void bind(Notification notification) {
            titleText.setText(notification.getTitle());
            messageText.setText(notification.getMessage());
            timeText.setText(notification.getTime());
        }
    }
}
