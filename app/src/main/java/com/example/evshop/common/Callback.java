package com.example.evshop.common;


public interface Callback<T> {
    void onSuccess(T value, boolean hasMore);
    void onError(Throwable t);
}