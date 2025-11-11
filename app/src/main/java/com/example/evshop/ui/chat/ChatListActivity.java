package com.example.evshop.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.data.TokenManager;
import com.example.evshop.data.api.ChatApi;
import com.example.evshop.data.modelchat.ChatRoom;
import com.example.evshop.data.service.ChatService;
import com.example.evshop.data.service.RealtimeChatService;
import com.example.evshop.ui.adapter.ChatRoomAdapter;
import com.example.evshop.ui.auth.LoginFragment; // SỬA: Chuyển hướng đến AuthActivity

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatListActivity extends AppCompatActivity {

    private static final String TAG = "ChatListActivity";

    @Inject
    ChatApi chatApi;
    @Inject
    TokenManager tokenManager;

    private RecyclerView chatRoomsRecyclerView;
    private ChatRoomAdapter chatRoomAdapter;
    private List<ChatRoom> chatRoomList;
    private List<ChatRoom> filteredChatRoomList;

    private ChatService chatService;
    private RealtimeChatService realtimeChatService;
    private EditText searchEditText;
    private LinearLayout emptyStateLayout;
    private TextView totalChatsText, unreadChatsText, onlineChatsText;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        if (!isUserLoggedIn()) {
            redirectToLogin();
            return;
        }

        initServices();
        initViews();
        setupRecyclerView();
        loadChatRooms();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private boolean isUserLoggedIn() {
        String token = tokenManager.getAccessToken();
        return token != null && !token.isEmpty();
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Please log in to use chat feature", Toast.LENGTH_LONG).show();
        // SỬA: Chuyển hướng đến AuthActivity
        Intent intent = new Intent(this, LoginFragment.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        chatRoomsRecyclerView = findViewById(R.id.chatRoomsRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        totalChatsText = findViewById(R.id.totalChatsText);
        unreadChatsText = findViewById(R.id.unreadChatsText);
        onlineChatsText = findViewById(R.id.onlineChatsText);
        backButton = findViewById(R.id.backButton);

        chatRoomList = new ArrayList<>();
        filteredChatRoomList = new ArrayList<>();

        setupSearch();
        setupBackButton();
    }

    private void initServices() {
        chatService = new ChatService(chatApi, this);
        realtimeChatService = RealtimeChatService.getInstance(this, chatApi);
    }

    private void setupRecyclerView() {
        chatRoomAdapter = new ChatRoomAdapter(filteredChatRoomList, chatRoom -> {
            chatRoomsRecyclerView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            Intent intent = new Intent(ChatListActivity.this, ChatRoomActivity.class);
            intent.putExtra("roomId", chatRoom.getRoomId());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        chatRoomsRecyclerView.setHasFixedSize(true);
        chatRoomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRoomsRecyclerView.setAdapter(chatRoomAdapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterChatRooms(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupBackButton() {
        backButton.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void filterChatRooms(String query) {
        filteredChatRoomList.clear();
        String lowerCaseQuery = query.toLowerCase();

        if (lowerCaseQuery.isEmpty()) {
            filteredChatRoomList.addAll(chatRoomList);
        } else {
            List<ChatRoom> filtered = chatRoomList.stream()
                    .filter(chatRoom -> (chatRoom.getCustomerName() != null && chatRoom.getCustomerName().toLowerCase().contains(lowerCaseQuery)) ||
                            (chatRoom.getLastMessage() != null && chatRoom.getLastMessage().toLowerCase().contains(lowerCaseQuery)))
                    .collect(Collectors.toList());
            filteredChatRoomList.addAll(filtered);
        }
        chatRoomAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        emptyStateLayout.setVisibility(filteredChatRoomList.isEmpty() ? View.VISIBLE : View.GONE);
        chatRoomsRecyclerView.setVisibility(filteredChatRoomList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateStats() {
        totalChatsText.setText(String.valueOf(chatRoomList.size()));
        int unreadCount = chatRoomList.stream().mapToInt(ChatRoom::getUnreadCount).sum();
        long onlineCount = chatRoomList.stream().filter(ChatRoom::isOnline).count();
        unreadChatsText.setText(String.valueOf(unreadCount));
        onlineChatsText.setText(String.valueOf(onlineCount));
    }

    private void loadChatRooms() {
        chatService.getChatRooms(new ChatService.ChatRoomsCallback() {
            @Override
            public void onSuccess(List<ChatRoom> chatRooms) {
                runOnUiThread(() -> handleRoomUpdate(chatRooms));
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatListActivity.this, "Failed to load chats: " + error, Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
            }
        });
        realtimeChatService.startListeningForChatRooms(this::handleRoomUpdate);
    }

    private void handleRoomUpdate(List<ChatRoom> updatedRooms) {
        runOnUiThread(() -> {
            chatRoomList.clear();
            chatRoomList.addAll(updatedRooms);
            chatRoomList.sort((r1, r2) -> {
                try {
                    return r2.getLastMessageTime().compareTo(r1.getLastMessageTime());
                } catch (Exception e) {
                    return 0;
                }
            });
            filterChatRooms(searchEditText.getText().toString());
            updateStats();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (realtimeChatService != null) {
            realtimeChatService.stopListeningForChatRooms();
        }
    }
}
