package com.example.evshop.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.auth0.android.jwt.JWT;
import com.example.evshop.R;
import com.example.evshop.data.TokenManager;
import com.example.evshop.data.chat.model.ChatMessage;
import com.example.evshop.databinding.ActivityChatBinding;

import java.util.List;

/**
 * ChatActivity - Màn hình chat giữa người dùng và cửa hàng
 * Có thể dùng roomId mặc định "store_chat_room" nếu không truyền từ HomeFragment.
 */
public class ChatActivity extends AppCompatActivity {

    // Khóa intent để truyền dữ liệu vào Activity
    public static final String EXTRA_ROOM_ID = "extra_room_id";
    public static final String EXTRA_TARGET_ID = "extra_target_id";

    private ActivityChatBinding binding;
    private String currentUserId;
    private String roomId;
    private String targetId;

    private ChatAdapter adapter;
    private ChatViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        extractIntentData();
        setupRecycler();
        setupViewModel();
        setupSendButton();
    }

    /**
     * Thiết lập Toolbar với nút quay lại
     */
    private void setupToolbar() {
        setSupportActionBar(binding.toolbarChat);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chat với cửa hàng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbarChat.setNavigationOnClickListener(v -> finish());
    }


    /**
     * Lấy dữ liệu từ Intent và token người dùng
     */
    private void extractIntentData() {
        // Lấy access token từ TokenManager
        String token = new TokenManager(this).getAccessToken();
        if (token == null) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Giải mã token để lấy userId hiện tại
        currentUserId = new JWT(token).getClaim("nameid").asString();

        // Lấy roomId và targetId từ Intent (nếu có)
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        targetId = getIntent().getStringExtra(EXTRA_TARGET_ID);

        // Nếu không có roomId → tạo mặc định
        if (TextUtils.isEmpty(roomId)) {
            roomId = "store_chat_room";
        }

        // Nếu cần, có thể log ra để debug
        // Log.d("ChatActivity", "roomId=" + roomId + ", targetId=" + targetId);
    }

    /**
     * Cấu hình RecyclerView và Adapter
     */
    private void setupRecycler() {
        adapter = new ChatAdapter(currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // luôn hiển thị tin mới nhất phía dưới
        binding.recyclerChat.setLayoutManager(layoutManager);
        binding.recyclerChat.setAdapter(adapter);
    }

    /**
     * Thiết lập ViewModel và quan sát dữ liệu tin nhắn
     */
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this, new ChatViewModelFactory(roomId))
                .get(ChatViewModel.class);

        viewModel.getMessages().observe(this, this::updateMessages);
    }

    /**
     * Cập nhật danh sách tin nhắn trên RecyclerView
     */
    private void updateMessages(List<ChatMessage> list) {
        adapter.submitList(list);
        if (!list.isEmpty()) {
            binding.recyclerChat.scrollToPosition(list.size() - 1);
        }
    }

    /**
     * Xử lý gửi tin nhắn
     */
    private void setupSendButton() {
        binding.btnSend.setOnClickListener(v -> {
            String messageText = binding.etMessage.getText().toString().trim();
            if (TextUtils.isEmpty(messageText)) return;

            ChatMessage message = new ChatMessage(
                    roomId,
                    currentUserId,
                    messageText,
                    System.currentTimeMillis()
            );

            viewModel.sendMessage(message);
            binding.etMessage.setText("");
        });
    }
}
