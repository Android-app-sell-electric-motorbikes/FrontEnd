package com.example.evshop.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.auth0.android.jwt.JWT;
import com.example.evshop.data.TokenManager;
import com.example.evshop.data.chat.model.ChatMessage;
import com.example.evshop.databinding.ActivityChatBinding;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    public static final String EXTRA_ROOM_ID = "extra_room_id";
    public static final String EXTRA_TARGET_ID = "extra_target_id";
    private ActivityChatBinding binding;
    private String currentUserId;
    private String roomId;

    private ChatAdapter adapter;
    private ChatViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy token
        String token = new TokenManager(this).getAccessToken();
        if (token == null) { finish(); return; }
        currentUserId = new JWT(token).getClaim("nameid").asString();

        // Lấy roomId từ intent
        roomId = getIntent().getStringExtra("ROOM_ID");

        setupRecycler();
        setupViewModel();
        setupSend();
    }

    private void setupRecycler() {
        adapter = new ChatAdapter(currentUserId);
        binding.recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerChat.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this, new ChatViewModelFactory(roomId)).get(ChatViewModel.class);
        viewModel.getMessages().observe(this, this::updateMessages);
    }

    private void updateMessages(List<ChatMessage> list) {
        adapter.submitList(list);
        if (!list.isEmpty()) binding.recyclerChat.scrollToPosition(list.size() - 1);
    }

    private void setupSend() {
        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etMessage.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;

            ChatMessage msg = new ChatMessage(roomId, currentUserId, text, System.currentTimeMillis());
            viewModel.sendMessage(msg);
            binding.etMessage.setText("");
        });
    }
}
