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
    private static final String PREFS_NAME = "auth_prefs"; // Đổi tên để tránh xung đột

    @Inject
    public TokenManager(@ApplicationContext Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveAccessToken(String t) {
        prefs.edit().putString("access_token", t).apply();
    }

    public String getAccessToken() {
        return prefs.getString("access_token", null);
    }

    // ... các hàm save/get RefreshToken và clear() giữ nguyên

    public void clear() {
        prefs.edit().clear().apply();
    }

    // --- CÁC HÀM MỚI ĐỂ LẤY THÔNG TIN USER ---

    private JWT getDecodedJwt() {
        String token = getAccessToken();
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            return new JWT(token);
        } catch (Exception e) {
            Log.e("TokenManager", "Failed to decode JWT", e);
            return null;
        }
    }

    public String getUserRole() {
        JWT jwt = getDecodedJwt();
        if (jwt == null) return null;
        Claim roleClaim = jwt.getClaim("http://schemas.microsoft.com/ws/2008/06/identity/claims/role");
        return roleClaim.asString();
    }

    public String getUserId() {
        JWT jwt = getDecodedJwt();
        if (jwt == null) return null;
        // Chú ý: claim name có thể khác, "nameid" là một claim phổ biến cho User ID
        Claim idClaim = jwt.getClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier");
        return idClaim.asString();
    }

    public String getUsername() {
        JWT jwt = getDecodedJwt();
        if (jwt == null) return null;
        // Chú ý: claim name có thể khác, "name" là một claim phổ biến cho Username
        Claim nameClaim = jwt.getClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name");
        return nameClaim.asString();
    }
}
