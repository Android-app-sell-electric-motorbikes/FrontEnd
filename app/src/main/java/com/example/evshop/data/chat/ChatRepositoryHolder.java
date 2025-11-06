package com.example.evshop.data.chat;



public class ChatRepositoryHolder {
    private static ChatRepositoryHolder instance;
    private ChatRepository repository;
    private ChatRepositoryHolder() {}
    public static synchronized ChatRepositoryHolder getInstance() {
        if (instance == null) instance = new ChatRepositoryHolder();
        return instance;
    }
    public void setRepository(ChatRepository repository) { this.repository = repository; }
    public ChatRepository getRepository() { return repository; }
}

