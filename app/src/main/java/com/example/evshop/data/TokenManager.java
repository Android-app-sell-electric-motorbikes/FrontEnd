package com.example.evshop.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log; // Thêm import cho việc debug

import com.auth0.android.jwt.JWT;
import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Lớp này quản lý việc lưu và truy xuất token xác thực (access & refresh token).
 * Nó cũng cung cấp phương thức để giải mã JWT token và lấy vai trò người dùng.
 * Được cung cấp dưới dạng Singleton bởi Hilt.
 */
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

    // Hàm này sẽ được gọi khi cần xóa token (logout)
    public void clear() {
        prefs.edit().clear().apply();
    }


    // =================================================================
    //  HÀM QUAN TRỌNG NHẤT: GIẢI MÃ TOKEN ĐỂ LẤY VAI TRÒ (ROLE)
    // =================================================================
    /**
     * Giải mã Access Token (JWT) để lấy ra vai trò (role) của người dùng.
     * @return Chuỗi "Admin", "User", hoặc null nếu token không hợp lệ hoặc không có vai trò.
     */
    public String getUserRole() {
        String token = getAccessToken();

        // Nếu không có token, không thể có vai trò
        if (token == null || token.isEmpty()) {
            return null;
        }

        try {
            // Sử dụng thư viện đã thêm để giải mã token
            JWT jwt = new JWT(token);

            // Lấy "claim" (thông tin) bên trong token.
            // Dựa trên JWT của bạn, tên claim cho vai trò là tiêu chuẩn của ASP.NET Core Identity.
            String role = jwt.getClaim("http://schemas.microsoft.com/ws/2008/06/identity/claims/role").asString();

            Log.d("TokenManager", "Decoded role from JWT: " + role);
            return role;

        } catch (Exception e) {
            // Lỗi xảy ra nếu token không hợp lệ, hết hạn, hoặc không có claim cần tìm.
            Log.e("TokenManager", "Failed to decode JWT or claim not found", e);
            return null;
        }
    }
}
