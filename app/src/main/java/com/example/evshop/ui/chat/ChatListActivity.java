package com.example.evshop.ui.chat;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.evshop.data.modelchat.ChatRoom;
import com.example.evshop.data.service.ChatService;
import com.example.evshop.data.service.RealtimeChatService;
import com.example.evshop.ui.adapter.ChatRoomAdapter;
import com.example.evshop.ui.auth.LoginFragment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatListActivity extends AppCompatActivity {

    @Inject TokenManager tokenManager;

    private RecyclerView chatRoomsRecyclerView;
    private ChatRoomAdapter adapter;
    private List<ChatRoom> rooms;

    private ChatService chatService;
    private RealtimeChatService realtimeChatService;

    private EditText searchEditText;
    private LinearLayout emptyStateLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        if (!isUserLoggedIn()) {
            redirectToLogin();
            return;
        }

        initViews();
        initServices();
        setupRecyclerView();
        loadChatRooms();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { finish(); }
        });
    }

    private boolean isUserLoggedIn() {
        return tokenManager.getAccessToken() != null;
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, LoginFragment.class));
        finish();
    }

    private void initViews() {
        chatRoomsRecyclerView = findViewById(R.id.chatRoomsRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        rooms = new ArrayList<>();
    }

    private void initServices() {
        chatService = new ChatService(tokenManager);
        realtimeChatService = RealtimeChatService.getInstance(tokenManager);
    }

    private void setupRecyclerView() {
        adapter = new ChatRoomAdapter(rooms, chatRoom -> {
            Intent intent = new Intent(ChatListActivity.this, ChatRoomActivity.class);
            intent.putExtra("roomId", chatRoom.getRoomId());
            startActivity(intent);
        });
        chatRoomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRoomsRecyclerView.setAdapter(adapter);
    }

    private void loadChatRooms() {
        chatService.getChatRooms(new ChatService.ChatRoomsCallback() {
            @Override public void onSuccess(List<ChatRoom> chatRooms) {
                runOnUiThread(() -> {
                    rooms.clear();
                    rooms.addAll(chatRooms);
                    adapter.notifyDataSetChanged();
                    emptyStateLayout.setVisibility(rooms.isEmpty() ? LinearLayout.VISIBLE : LinearLayout.GONE);
                });
            }
            @Override public void onError(String error) { runOnUiThread(() -> Toast.makeText(ChatListActivity.this, error, Toast.LENGTH_SHORT).show()); }
        });

        realtimeChatService.startListeningForMessages("global", msg -> {
            // TODO: cập nhật số lượng tin nhắn mới nếu muốn
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        realtimeChatService.cleanup();
    }
}
