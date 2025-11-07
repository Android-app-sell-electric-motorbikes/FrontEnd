package com.example.evshop.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class TokenManager {
    private final SharedPreferences prefs;

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

    public void clear() {
        prefs.edit().clear().apply();
    }

    public String getUserRole() {
        String token = getAccessToken();
        if (token == null || token.isEmpty()) {
            return null;
        }

        try {
            JWT jwt = new JWT(token);

            // ** SỬA LẠI CÚ PHÁP CHO ĐÚNG PHIÊN BẢN THƯ VIỆN **
            Claim roleClaim = jwt.getClaim("http://schemas.microsoft.com/ws/2008/06/identity/claims/role");
            String role = roleClaim.asString();

            Log.d("TokenManager", "Decoded role from JWT: " + role);
            return role;

        } catch (Exception e) {
            Log.e("TokenManager", "Failed to decode JWT or claim not found", e);
            return null;
        }
    }
}