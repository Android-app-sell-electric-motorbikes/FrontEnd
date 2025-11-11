package com.example.evshop.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.data.TokenManager;
import com.example.evshop.data.modelchat.ChatMessage;
import com.example.evshop.data.service.ChatService;
import com.example.evshop.data.service.RealtimeChatService;
import com.example.evshop.ui.adapter.ChatAdapter;
import com.example.evshop.ui.auth.LoginFragment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatRoomActivity extends AppCompatActivity {

    @Inject TokenManager tokenManager;

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private EditText messageInput;
    private ImageButton sendButton;
    private ImageView backButton;
    private TextView chatTitle;

    private ChatService chatService;
    private RealtimeChatService realtimeChatService;

    private String roomId;
    private String currentUserId;
    private String currentUserName;

    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // Kiểm tra login
        if (!isUserLoggedIn()) {
            redirectToLogin();
            return;
        }

        initServices();
        initViews();
        setupRecyclerView();
        setupBackButton();
        setupSendButton();

        loadMessages();
        setupRealtimeListener();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { finish(); overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right); }
        });
    }

    private boolean isUserLoggedIn() {
        String token = tokenManager.getAccessToken();
        return token != null && !token.isEmpty();
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Please log in again.", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, LoginFragment.class));
        finish();
    }

    private void initServices() {
        chatService = new ChatService(tokenManager);
        realtimeChatService = RealtimeChatService.getInstance(tokenManager);
        currentUserId = tokenManager.getUserId();
        currentUserName = tokenManager.getUsername();
    }

    private void initViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backBtn);
        chatTitle = findViewById(R.id.chatName);

        messageList = new ArrayList<>();

        roomId = getIntent().getStringExtra("roomId");
        if (roomId == null) { finish(); return; }

        // Demo title
        chatTitle.setText("Chat Room");
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(messageList, currentUserId);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void setupBackButton() {
        backButton.setOnClickListener(v -> { v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY); finish(); });
    }

    private void setupSendButton() {
        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (text.isEmpty()) return;
            messageInput.setText("");
            sendMessage(text);
        });
    }

    private void loadMessages() {
        chatService.getMessagesForRoom(roomId, new ChatService.MessagesCallback() {
            @Override
            public void onSuccess(List<ChatMessage> messages) {
                runOnUiThread(() -> {
                    messageList.clear();
                    messageList.addAll(messages);
                    chatAdapter.notifyDataSetChanged();
                    scrollToBottom(false);
                });
            }
            @Override public void onError(String error) { runOnUiThread(() -> Toast.makeText(ChatRoomActivity.this, error, Toast.LENGTH_SHORT).show()); }
        });
    }

    private void setupRealtimeListener() {
        realtimeChatService.startListeningForMessages(roomId, this::processNewMessage);
    }

    private void processNewMessage(ChatMessage msg) {
        runOnUiThread(() -> {
            boolean exists = messageList.stream().anyMatch(m -> m.getMessageId().equals(msg.getMessageId()));
            if (!exists) {
                messageList.add(msg);
                chatAdapter.notifyDataSetChanged();
                scrollToBottom(true);
            }
        });
    }

    private void sendMessage(String text) {
        ChatMessage temp = new ChatMessage();
        temp.setMessageId("temp_" + System.currentTimeMillis());
        temp.setSenderId(currentUserId);
        temp.setSenderName(currentUserName);
        temp.setMessage(text);
        temp.setTimestampFromLong(System.currentTimeMillis());
        temp.setMe(true);

        messageList.add(temp);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom(true);

        chatService.sendMessage(roomId, text, new ChatService.SendMessageCallback() {
            @Override public void onSuccess() { /* Realtime sẽ cập nhật */ }
            @Override public void onError(String error) { runOnUiThread(() -> Toast.makeText(ChatRoomActivity.this, "Failed to send", Toast.LENGTH_SHORT).show()); }
        });
    }

    private void scrollToBottom(boolean smooth) {
        if (chatAdapter.getItemCount() > 0) {
            int last = chatAdapter.getItemCount() - 1;
            if (smooth) chatRecyclerView.smoothScrollToPosition(last);
            else chatRecyclerView.scrollToPosition(last);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        realtimeChatService.stopListeningForMessages(roomId);
    }
}
