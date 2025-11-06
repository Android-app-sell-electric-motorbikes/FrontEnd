package com.example.evshop.ui.chat;



import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.evshop.data.chat.ChatRepository;

public class ChatViewModelFactory implements ViewModelProvider.Factory {
    private final ChatRepository repo;
    public ChatViewModelFactory(ChatRepository repo) { this.repo = repo; }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChatViewModel.class)) {
            return (T) new ChatViewModel(repo);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}


