package com.example.evshop.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

// *** 1. THÊM IMPORT CHO NÚT ***
import com.google.android.material.button.MaterialButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.evshop.R;
import com.example.evshop.domain.models.UserData;
// *** 2. THÊM IMPORT CHO ACTIVITY MỚI ***
import com.example.evshop.ui.admin.AddTemplateVehicleActivity;
import com.example.evshop.ui.main.MainActivity;
import com.example.evshop.ui.auth.AuthViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private MaterialToolbar toolbar;

    // *** 3. KHAI BÁO BIẾN CHO CÁC NÚT ***
    private MaterialButton btnGoToAddTemplate;
    private MaterialButton btnManageVehicles;
    private MaterialButton btnManageUsers;
    private MaterialButton btnManageOrders;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // --- Thiết lập Toolbar (giữ nguyên) ---
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

        // ========================================================
        // *** 4. GỌI HÀM THIẾT LẬP SỰ KIỆN CLICK ***
        // ========================================================
        setupButtonClickListeners();

        // --- Xử lý nút back (giữ nguyên) ---
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(AdminActivity.this, "Vui lòng đăng xuất để thoát", Toast.LENGTH_SHORT).show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    // ========================================================
    // *** 5. HÀM MỚI ĐỂ TÌM VÀ GÁN SỰ KIỆN CHO CÁC NÚT ***
    // ========================================================
    /**
     * Hàm này sẽ tìm các nút trong layout và gán sự kiện click cho chúng.
     * Đây chính là phần logic còn thiếu trong code cũ của bạn.
     */
    private void setupButtonClickListeners() {
        // Tìm các nút trong layout bằng ID
        btnGoToAddTemplate = findViewById(R.id.btn_go_to_add_template);
        btnManageVehicles = findViewById(R.id.btnManageVehicles);
        btnManageUsers = findViewById(R.id.btnManageUsers);
        btnManageOrders = findViewById(R.id.btnManageOrders);

        // Gắn sự kiện click cho nút "Thêm Mẫu Xe Mới"
        btnGoToAddTemplate.setOnClickListener(v -> {
            // Tạo Intent để mở màn hình AddTemplateVehicleActivity
            Intent intent = new Intent(AdminActivity.this, AddTemplateVehicleActivity.class);
            startActivity(intent);
        });

        // Gắn sự kiện cho các nút khác
        btnManageVehicles.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Quản lý Xe sắp ra mắt", Toast.LENGTH_SHORT).show();
        });

        btnManageUsers.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Quản lý Người dùng sắp ra mắt", Toast.LENGTH_SHORT).show();
        });

        btnManageOrders.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Quản lý Đơn hàng sắp ra mắt", Toast.LENGTH_SHORT).show();
        });
    }

    // --- CÁC HÀM CÒN LẠI GIỮ NGUYÊN (KHÔNG THAY ĐỔI) ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_admin_account_menu) {
            View menuItemView = findViewById(R.id.action_admin_account_menu);
            if (menuItemView != null) {
                showAdminPopupMenu(menuItemView);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAdminPopupMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenu().add("Tài khoản của tôi");
        popupMenu.getMenu().add("Đăng xuất");

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            String title = menuItem.getTitle().toString();
            if ("Tài khoản của tôi".equals(title)) {
                showAdminProfileDialog();
                return true;
            } else if ("Đăng xuất".equals(title)) {
                logout();
                return true;
            }
            return false;
        });
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
        String rolesString = "N/A";
        if (currentUser.roles != null && !currentUser.roles.isEmpty()) {
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
