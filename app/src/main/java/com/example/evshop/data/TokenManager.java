package com.example.evshop.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

// SỬA: Thêm các import cần thiết để giải mã JWT
import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;

import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class TokenManager {
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private final SharedPreferences prefs;

    // SỬA: Sử dụng Hilt để inject Context, thay cho constructor cũ
    @Inject
    public TokenManager(@ApplicationContext Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveAccessToken(String token) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public void saveRefreshToken(String token) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply();
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }


    // =============================================================
    // ***           CÁC HÀM MỚI ĐỂ LẤY THÔNG TIN USER           ***
    // =============================================================

    /**
     * Hàm nội bộ để giải mã JWT từ access token đã lưu.
     * @return đối tượng JWT đã giải mã, hoặc null nếu token không hợp lệ.
     */
    private JWT getDecodedJwt() {
        String token = getAccessToken();
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            // Thư viện sẽ tự xử lý việc giải mã token
            return new JWT(token);
        } catch (Exception e) {
            Log.e("TokenManager", "Failed to decode JWT", e);
            return null;
        }
    }

    /**
     * Lấy vai trò (role) của người dùng từ token.
     * @return Chuỗi vai trò (ví dụ: "ADMIN", "CUSTOMER") hoặc null.
     */
    public String getUserRole() {
        JWT jwt = getDecodedJwt();
        if (jwt == null) return null;
        // Tên claim này phải khớp với tên mà Backend trả về trong token
        Claim roleClaim = jwt.getClaim("http://schemas.microsoft.com/ws/2008/06/identity/claims/role");
        return roleClaim.asString();
    }

    /**
     * Lấy ID của người dùng từ token.
     * @return Chuỗi ID người dùng hoặc null.
     */
    public String getUserId() {
        JWT jwt = getDecodedJwt();
        if (jwt == null) return null;
        // "nameidentifier" là một claim phổ biến cho User ID trong ASP.NET Core Identity
        Claim idClaim = jwt.getClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier");
        return idClaim.asString();
    }

    /**
     * Lấy tên đăng nhập (username) của người dùng từ token.
     * @return Chuỗi username hoặc null.
     */
    public String getUsername() {
        JWT jwt = getDecodedJwt();
        if (jwt == null) return null;
        // "name" là một claim phổ biến cho Username
        Claim nameClaim = jwt.getClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name");
        return nameClaim.asString();
    }

    // Các hàm saveUserRole và getUserRole cũ có thể bị loại bỏ
    // vì bây giờ chúng ta lấy trực tiếp từ token, đảm bảo dữ liệu luôn đúng.
}
