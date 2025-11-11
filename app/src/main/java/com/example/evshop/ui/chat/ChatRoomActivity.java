package com.example.evshop.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
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
import com.example.evshop.data.api.ChatApi;
import com.example.evshop.data.modelchat.ChatMessage;
import com.example.evshop.data.service.ChatService;
import com.example.evshop.data.service.RealtimeChatService;
import com.example.evshop.data.TokenManager;
import com.example.evshop.ui.adapter.ChatAdapter;
import com.example.evshop.ui.auth.LoginFragment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint // Báo cho Hilt biết đây là điểm để inject
public class ChatRoomActivity extends AppCompatActivity {

    private static final String TAG = "ChatRoomActivity";

    // --- Inject Dependencies bằng Hilt ---
    @Inject
    ChatApi chatApi; // Hilt sẽ cung cấp ChatApi từ NetworkModule
    @Inject
    TokenManager tokenManager; // Hilt sẽ cung cấp TokenManager
    // ------------------------------------

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private EditText messageInput;
    private ImageButton sendButton;

    private ChatService chatService;
    private RealtimeChatService realtimeChatService;
    private String roomId;
    private String currentUserId;
    private String currentUserName;

    private LinearLayoutManager layoutManager;
    private ImageView backButton;
    private TextView chatTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // --- KIỂM TRA TRẠNG THÁI ĐĂNG NHẬP NGAY LẬP TỨC ---
        if (!isUserLoggedIn()) {
            redirectToLogin();
            return; // Dừng thực thi nếu chưa đăng nhập
        }
        // ----------------------------------------------------

        initServices(); // Khởi tạo service trước để lấy thông tin user
        initViews();
        setupRecyclerView();
        setupBackButton();
        setupSendButton();

        // Tải dữ liệu và bắt đầu lắng nghe sự kiện
        loadMessages();
        setupFirebaseListener();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private boolean isUserLoggedIn() {
        // Người dùng được coi là đã đăng nhập nếu có token hợp lệ
        String token = tokenManager.getAccessToken();
        return token != null && !token.isEmpty();
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Your session has expired. Please log in again.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, LoginFragment.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Đóng Activity hiện tại
    }

    private void initViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backBtn);
        chatTitle = findViewById(R.id.chatName);
        messageList = new ArrayList<>();

        roomId = getIntent().getStringExtra("roomId");
        if (roomId == null) {
            Toast.makeText(this, "Room ID not found. Cannot open chat.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (chatTitle != null) {
            // Lấy userRole từ TokenManager đã được inject
            String userRole = tokenManager.getUserRole();
            if ("ADMIN".equals(userRole)) {
                String customerName = extractCustomerNameFromRoomId(roomId);
                chatTitle.setText(customerName != null ? customerName + " Chat" : "Customer Chat");
            } else {
                chatTitle.setText("EVShop Support");
            }
        }
    }

    private void initServices() {
        // Sử dụng các dependency đã được Hilt inject
        chatService = new ChatService(chatApi, this);
        realtimeChatService = RealtimeChatService.getInstance(this, chatApi);
        currentUserId = tokenManager.getUserId();
        currentUserName = tokenManager.getUsername();
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(messageList, currentUserId);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        chatRecyclerView.setHasFixedSize(true);
        chatRecyclerView.setItemAnimator(null);

        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);
    }

    // ... Các hàm còn lại (setupBackButton, loadMessages, setupFirebaseListener, ...) giữ nguyên như cũ ...
    private void setupBackButton() {
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        }
    }

    private void loadMessages() {
        if (roomId == null) return;
        chatService.getMessagesForRoom(roomId, new ChatService.MessagesCallback() {
            @Override
            public void onSuccess(List<ChatMessage> messages) {
                runOnUiThread(() -> {
                    messageList.clear();
                    messageList.addAll(messages);
                    messageList.sort((m1, m2) -> Long.compare(m1.getOriginalTimestamp(), m2.getOriginalTimestamp()));
                    chatAdapter.notifyDataSetChanged();
                    scrollToBottom(false);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatRoomActivity.this, "Failed to load messages: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupFirebaseListener() {
        if (roomId == null) return;
        realtimeChatService.startListeningForMessages(roomId, this::processNewMessage);
    }

    private void processNewMessage(ChatMessage newMessage) {
        runOnUiThread(() -> {
            boolean isDuplicate = messageList.stream().anyMatch(m -> m.getMessageId() != null && m.getMessageId().equals(newMessage.getMessageId()));
            if (isDuplicate) return;

            int tempIndex = -1;
            for (int i = 0; i < messageList.size(); i++) {
                ChatMessage msg = messageList.get(i);
                if (msg.getMessageId() != null && msg.getMessageId().startsWith("temp_") &&
                        msg.getMessage().equals(newMessage.getMessage()) &&
                        msg.getSenderId().equals(newMessage.getSenderId())) {
                    tempIndex = i;
                    break;
                }
            }

            if (tempIndex != -1) {
                messageList.set(tempIndex, newMessage);
                chatAdapter.notifyItemChanged(tempIndex);
            } else {
                messageList.add(newMessage);
                messageList.sort((m1, m2) -> Long.compare(m1.getOriginalTimestamp(), m2.getOriginalTimestamp()));
                chatAdapter.notifyDataSetChanged();
                scrollToBottom(true);
            }
        });
    }

    private void setupSendButton() {
        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (messageText.isEmpty()) return;

            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            messageInput.setText("");

            sendMessage(messageText);
        });
    }

    private void sendMessage(String messageText) {
        long currentTime = System.currentTimeMillis();
        ChatMessage tempMessage = new ChatMessage();
        tempMessage.setMessageId("temp_" + currentTime);
        tempMessage.setSenderId(currentUserId);
        tempMessage.setSenderName(currentUserName);
        tempMessage.setMessage(messageText);
        tempMessage.setTimestampFromLong(currentTime);
        tempMessage.setMe(true);
        tempMessage.setRoomId(roomId);

        messageList.add(tempMessage);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom(true);

        chatService.sendMessage(roomId, messageText, new ChatService.SendMessageCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Message sent successfully. Waiting for real-time update.");
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatRoomActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    int index = messageList.indexOf(tempMessage);
                    if (index != -1) {
                        messageList.remove(index);
                        chatAdapter.notifyItemRemoved(index);
                    }
                });
            }
        });
    }

    private void scrollToBottom(boolean smooth) {
        if (chatAdapter != null && chatAdapter.getItemCount() > 0) {
            int lastPos = chatAdapter.getItemCount() - 1;
            if (smooth) {
                chatRecyclerView.smoothScrollToPosition(lastPos);
            } else {
                chatRecyclerView.scrollToPosition(lastPos);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (roomId != null) {
            chatService.markMessagesAsRead(roomId, new ChatService.SendMessageCallback() {
                @Override public void onSuccess() { Log.d(TAG, "Messages marked as read."); }
                @Override public void onError(String error) { Log.e(TAG, "Failed to mark as read: " + error); }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (realtimeChatService != null && roomId != null) {
            realtimeChatService.stopListeningForMessages(roomId);
        }
    }

    private String extractCustomerNameFromRoomId(String roomId) {
        if (roomId != null && roomId.startsWith("chatRoom_")) {
            String[] parts = roomId.split("_");
            if (parts.length >= 2) {
                return parts[1];
            }
        }
        return "Customer";
    }
}
