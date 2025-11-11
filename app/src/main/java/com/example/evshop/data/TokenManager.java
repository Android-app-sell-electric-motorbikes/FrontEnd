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
    private static final String PREFS_NAME = "auth_prefs";

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

    public void clear() {
        prefs.edit().clear().apply();
    }

    // --- JWT thật ---
    private JWT getDecodedJwt() {
        String token = getAccessToken();
        if (token == null || token.isEmpty()) return null;
        try {
            return new JWT(token);
        } catch (Exception e) {
            Log.e("TokenManager", "Failed to decode JWT", e);
            return null;
        }
    }

    public String getUserId() {
        JWT jwt = getDecodedJwt();
        if (jwt != null) {
            Claim idClaim = jwt.getClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier");
            return idClaim.asString();
        }
        // Demo fallback
        return prefs.getString("demo_user_id", null);
    }

    public String getUsername() {
        JWT jwt = getDecodedJwt();
        if (jwt != null) {
            Claim nameClaim = jwt.getClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name");
            return nameClaim.asString();
        }
        return prefs.getString("demo_username", null);
    }

    public String getUserRole() {
        JWT jwt = getDecodedJwt();
        if (jwt != null) {
            Claim roleClaim = jwt.getClaim("http://schemas.microsoft.com/ws/2008/06/identity/claims/role");
            return roleClaim.asString();
        }
        return prefs.getString("demo_role", null);
    }

    // --- Chế độ demo ---
    public void setDemoUser(String userId, String username, String role) {
        prefs.edit()
                .putString("demo_user_id", userId)
                .putString("demo_username", username)
                .putString("demo_role", role)
                .apply();
    }
}
