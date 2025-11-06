package com.example.evshop.ui.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.evshop.data.chat.ChatRepository;
import com.example.evshop.data.chat.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Callback;

public class ChatViewModel extends ViewModel {

    private final ChatRepository repository;
    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>(new ArrayList<>());

    public ChatViewModel(ChatRepository repository) {
        this.repository = repository;

        // Cập nhật danh sách messages từ repository
        repository.getMessagesLive().observeForever(this::updateMessages);
        repository.getIncomingMessageLive().observeForever(this::addIncomingMessage);
    }

    private void updateMessages(List<ChatMessage> list) {
        messages.postValue(list != null ? new ArrayList<>(list) : new ArrayList<>());
    }

    private void addIncomingMessage(ChatMessage msg) {
        if (msg == null) return;
        List<ChatMessage> current = messages.getValue() != null ? new ArrayList<>(messages.getValue()) : new ArrayList<>();
        current.add(msg);
        messages.postValue(current);
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }

    public void loadMessages(String roomId) {
        repository.loadMessages(roomId);
    }

    public void sendMessage(ChatMessage message, Callback<Void> callback) {
        repository.sendMessage(message, callback);

        // Optimistic update: hiển thị ngay khi gửi
        addIncomingMessage(message);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Remove observer khi ViewModel bị destroy để tránh leak memory
        repository.getMessagesLive().removeObserver(this::updateMessages);
        repository.getIncomingMessageLive().removeObserver(this::addIncomingMessage);
    }

    // Factory để tạo ViewModel với repository
    public static class Factory implements ViewModelProvider.Factory {
        private final ChatRepository repository;

        public Factory(ChatRepository repository) {
            this.repository = repository;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ChatViewModel.class)) {
                return (T) new ChatViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
