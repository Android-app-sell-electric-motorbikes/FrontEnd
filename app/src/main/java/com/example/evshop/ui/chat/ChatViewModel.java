package com.example.evshop.ui.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.evshop.data.chat.model.ChatMessage;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {

    private final MutableLiveData<List<ChatMessage>> _messages = new MutableLiveData<>();
    public LiveData<List<ChatMessage>> getMessages() { return _messages; }

    private final List<ChatMessage> messageList = new ArrayList<>();
    private final DatabaseReference dbRef;

    private final ChildEventListener listener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
            ChatMessage msg = snapshot.getValue(ChatMessage.class);
            if (msg != null) {
                messageList.add(msg);
                _messages.setValue(new ArrayList<>(messageList));
            }
        }
        @Override public void onChildChanged(DataSnapshot snapshot, String previousChildName) {}
        @Override public void onChildRemoved(DataSnapshot snapshot) {}
        @Override public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}
        @Override public void onCancelled(DatabaseError error) {}
    };

    public ChatViewModel(String roomId) {
        dbRef = FirebaseDatabase.getInstance().getReference("chat_rooms").child(roomId).child("messages");
        dbRef.addChildEventListener(listener);
    }

    public void sendMessage(ChatMessage msg) {
        dbRef.push().setValue(msg);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        dbRef.removeEventListener(listener);
    }
}
