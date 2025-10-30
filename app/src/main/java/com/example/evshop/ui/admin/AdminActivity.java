package com.example.evshop.ui.admin;

import android.content.Intent;
import android.os.Bundle;import android.view.Menu;
import android.view.MenuItem;
import android.view.View; // <-- QUAN TRỌNG: Import View
import android.widget.PopupMenu; // <-- QUAN TRỌNG: Import PopupMenu
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.evshop.R;
import com.example.evshop.domain.models.UserData;
import com.example.evshop.ui.main.MainActivity;
import com.example.evshop.ui.auth.AuthViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.stream.Collectors; // Dùng để nối chuỗi role

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

        // Xử lý nút back theo cách mới
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(AdminActivity.this, "Vui lòng đăng xuất để thoát", Toast.LENGTH_SHORT).show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_toolbar_menu, menu);
        return true;
    }

    // *** SỬA ĐỔI QUAN TRỌNG NHẤT NẰM Ở ĐÂY ***
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_admin_account_menu) {
            // Tìm view của item trên toolbar để PopupMenu có thể hiển thị đúng vị trí
            View menuItemView = findViewById(R.id.action_admin_account_menu);
            // Gọi hàm hiển thị PopupMenu
            showAdminPopupMenu(menuItemView);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Hàm mới để hiển thị PopupMenu cho Admin
     * @param anchorView View mà menu sẽ "neo" vào (chính là icon tài khoản)
     */
    private void showAdminPopupMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        // "Thổi phồng" menu với 2 lựa chọn
        popupMenu.getMenu().add("Tài khoản của tôi");
        popupMenu.getMenu().add("Đăng xuất");

        // Xử lý sự kiện khi một item trong popup được nhấn
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            String title = menuItem.getTitle().toString();
            if ("Tài khoản của tôi".equals(title)) {
                // Nếu nhấn "Tài khoản của tôi", thì hiển thị Dialog thông tin
                showAdminProfileDialog();
                return true;
            } else if ("Đăng xuất".equals(title)) {
                // Nếu nhấn "Đăng xuất", thì thực hiện logout
                logout();
                return true;
            }
            return false;
        });

        // Hiển thị menu lên
        popupMenu.show();
    }


    private void showAdminProfileDialog() {
        UserData currentUser = authViewModel.getCurrentUser().getValue();

        if (currentUser == null) {
            Toast.makeText(this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Tên: ").append(currentUser.fullName).append("\n\n");
        messageBuilder.append("Email: ").append(currentUser.email).append("\n\n");

        // Xử lý hiển thị danh sách roles
        String rolesString = "N/A";
        if (currentUser.roles != null && !currentUser.roles.isEmpty()) {
            // Nối các role lại với nhau, phân cách bởi dấu phẩy
            rolesString = currentUser.roles.stream().collect(Collectors.joining(", "));
        }
        messageBuilder.append("Vai trò: ").append(rolesString);

        new AlertDialog.Builder(this)
                .setTitle("Thông tin Admin")
                .setMessage(messageBuilder.toString())
                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void logout() {
        authViewModel.logout();
        Intent intent = new Intent(AdminActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
