package com.example.evshop.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.example.evshop.ui.main.MainActivity;

public class TokenManager {
    private final SharedPreferences prefs;
    private final Context context; // <-- SỬA: Thêm Context

    // ⚠️ TEST MODE: Bật để test UI mà không cần đăng nhập thật
    private static final boolean TEST_MODE = true; // Đổi thành false khi deploy production
    private static final String FAKE_TOKEN = "test_token_for_ui_testing_only";

    public TokenManager(Context ctx) {
        this.context = ctx.getApplicationContext(); // <-- SỬA: Thêm dòng này
        prefs = this.context.getSharedPreferences("auth", Context.MODE_PRIVATE);
    }

    public void saveAccessToken(String t) {
        prefs.edit().putString("access_token", t).apply();
    }

    public String getAccessToken() {
        if (TEST_MODE) {
            // Test mode: trả về fake token để test UI
            return FAKE_TOKEN;
        }
        return prefs.getString("access_token", null);
    }

    public void saveRefreshToken(String t) {
        prefs.edit().putString("refresh_token", t).apply();
    }

    public String getRefreshToken() {
        if (TEST_MODE) {
            // Test mode: trả về fake token
            return FAKE_TOKEN;
        }
        return prefs.getString("refresh_token", null);
    }

    public void clear() {
        if (TEST_MODE) {
            // Test mode: không xóa token (để giữ trạng thái đăng nhập)
            return;
        }
        prefs.edit().clear().apply();
    }


    public void logout() {
        this.clear();
        // 2. Tạo Intent để quay về MainActivity (sẽ tự navigate đến LoginFragment)
        Intent intent = new Intent(context, MainActivity.class);
        // 3. Thêm các cờ (flags) để xóa hết các Activity khác trong stack
        // và đảm bảo người dùng không thể nhấn "Back" để quay lại màn hình cũ.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Thêm extra để báo MainActivity cần navigate đến login
        intent.putExtra("navigate_to_login", true);

        // 4. Bắt đầu MainActivity
        context.startActivity(intent);
    }
    // ========================================================
}
