package com.example.evshop.data;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

// Sử dụng Singleton để đảm bảo chỉ có một TokenManager trong toàn ứng dụng
@Singleton
public class TokenManager {
    private final SharedPreferences prefs;

    // Yêu cầu Hilt cung cấp ApplicationContext một cách an toàn
    @Inject
    public TokenManager(@ApplicationContext Context context) {
        prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
    }

    public void saveAccessToken(String t) {
        prefs.edit().putString("access_token", t).apply();
    }

    public String getAccessToken() {
        return prefs.getString("access_token", null);
    }

    public void saveRefreshToken(String t) {
        prefs.edit().putString("refresh_token", t).apply();
    }

    public String getRefreshToken() {
        return prefs.getString("refresh_token", null);
    }

    // Hàm này sẽ được gọi khi cần xóa token
    public void clear() {
        prefs.edit().clear().apply();
    }

    // KHÔNG CÒN HÀM LOGOUT Ở ĐÂY NỮA
}
