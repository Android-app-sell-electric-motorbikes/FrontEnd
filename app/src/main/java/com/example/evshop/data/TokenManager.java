package com.example.evshop.data;

import android.content.Context;
import android.content.Intent; // <-- THÊM DÒNG NÀY
import android.content.SharedPreferences;
import com.example.evshop.ui.auth.LoginActivity; // <-- THÊM DÒNG NÀY

public class TokenManager {
    private final SharedPreferences prefs;
    private final Context context; // <-- SỬA: Thêm Context

    public TokenManager(Context ctx) {
        this.context = ctx.getApplicationContext(); // <-- SỬA: Thêm dòng này
        prefs = this.context.getSharedPreferences("auth", Context.MODE_PRIVATE);
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


    public void logout() {
        this.clear();
        // 2. Tạo Intent để quay về màn hình Login
        Intent intent = new Intent(context, LoginActivity.class);
        // 3. Thêm các cờ (flags) để xóa hết các Activity khác trong stack
        // và đảm bảo người dùng không thể nhấn "Back" để quay lại màn hình cũ.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 4. Bắt đầu LoginActivity
        context.startActivity(intent);
    }
    // ========================================================
}
