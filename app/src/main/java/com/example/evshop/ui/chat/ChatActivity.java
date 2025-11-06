package com.example.evshop.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.data.RetrofitClient;
import com.example.evshop.data.TokenManager;
import com.example.evshop.data.chat.ChatRepository;
import com.example.evshop.data.chat.ChatRepositoryHolder;
import com.example.evshop.data.chat.model.ChatMessage;
import com.example.evshop.data.network.ChatApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatActivity: màn hình trò chuyện thời gian thực với người dùng khác
 * - Tự động load room chat từ server
 * - Gửi và nhận tin nhắn realtime (Firebase + Retrofit)
 */
public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_ID = "extra_room_id";
    public static final String EXTRA_TARGET_ID = "extra_target_id";

    private ChatViewModel viewModel;
    private ChatAdapter adapter;
    private final List<ChatMessage> list = new ArrayList<>();

    private String roomId;
    private String currentUserId;
    private String targetUserId;

    private EditText etMessage;
    private ImageButton btnSend;
    private RecyclerView rvChat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        initChat();
    }

    private void initViews() {
        rvChat = findViewById(R.id.recycler_chat);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);

        rvChat.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initChat() {
        // ✅ Lấy userId thật từ TokenManager (giải mã từ JWT)
        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String accessToken = tokenManager.getAccessToken();
        if (accessToken == null) {
            finish();
            return;
        }

        try {
            com.auth0.android.jwt.JWT jwt = new com.auth0.android.jwt.JWT(accessToken);
            currentUserId = jwt.getClaim("nameid").asString(); // hoặc "sub" tuỳ backend trả về
        } catch (Exception e) {
            e.printStackTrace();
            currentUserId = null;
        }

        Intent intent = getIntent();
        roomId = intent.getStringExtra(EXTRA_ROOM_ID);
        targetUserId = intent.getStringExtra(EXTRA_TARGET_ID);

        adapter = new ChatAdapter(list, currentUserId);
        rvChat.setAdapter(adapter);

        // ✅ Setup ViewModel & Repository
        ChatApiService chatApi = RetrofitClient.getChatApi(getApplicationContext());        ChatRepository repo = new ChatRepository(chatApi);
        ChatRepositoryHolder.getInstance().setRepository(repo);
        viewModel = new ViewModelProvider(this, new ChatViewModelFactory(repo)).get(ChatViewModel.class);

        // Quan sát tin nhắn realtime
        viewModel.getMessages().observe(this, messages -> {
            list.clear();
            if (messages != null) list.addAll(messages);
            adapter.notifyDataSetChanged();
            if (!list.isEmpty()) rvChat.scrollToPosition(list.size() - 1);
        });

        // ✅ Load room hoặc tạo mới nếu chưa có
        if (!TextUtils.isEmpty(roomId)) {
            viewModel.loadMessages(roomId);
        } else if (!TextUtils.isEmpty(currentUserId) && !TextUtils.isEmpty(targetUserId)) {
            chatApi.getOrCreateRoom(currentUserId, targetUserId).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        roomId = response.body();
                        viewModel.loadMessages(roomId);
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }

        // ✅ Gửi tin nhắn
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (TextUtils.isEmpty(text) || roomId == null) return;

            long ts = System.currentTimeMillis();
            ChatMessage msg = new ChatMessage(roomId, currentUserId, targetUserId, text, ts);

            viewModel.sendMessage(msg, new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // có thể hiện loading hoặc toast
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    t.printStackTrace();
                }
            });

            etMessage.setText("");
        });
    }
}
