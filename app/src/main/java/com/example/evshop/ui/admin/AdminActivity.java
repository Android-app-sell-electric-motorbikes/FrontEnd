package com.example.evshop.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
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
import com.example.evshop.ui.transaction.TransactionHistoryActivity;
import com.google.android.material.appbar.MaterialToolbar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private UserData currentUserData;
    private MenuItem accountMenuItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupToolbar();
        setupButtonClickListeners();
        observeUserData();
        setupBackButtonHandler();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void observeUserData() {
        authViewModel.getCurrentUser().observe(this, userData -> {
            if (userData != null) {
                this.currentUserData = userData;
                if (accountMenuItem != null) {
                    accountMenuItem.setEnabled(true);
                }
            } else {
                if (!isFinishing()) {
                    Toast.makeText(this, "Phiên đăng xuất hoặc không hợp lệ.", Toast.LENGTH_SHORT).show();
                    logout();
                }
            }
        });
    }

    private void setupButtonClickListeners() {
        findViewById(R.id.btn_go_to_add_template).setOnClickListener(v ->
                startActivity(new Intent(AdminActivity.this, AddTemplateVehicleActivity.class)));

        findViewById(R.id.btn_manage_inventory).setOnClickListener(v ->
                startActivity(new Intent(AdminActivity.this, VehicleInventoryActivity.class)));

        // ** KÍCH HOẠT NÚT MỚI **
        findViewById(R.id.btnTransactionHistory).setOnClickListener(v ->
                startActivity(new Intent(AdminActivity.this, TransactionHistoryActivity.class)));

        findViewById(R.id.btnManageVehicles).setOnClickListener(v ->
                Toast.makeText(this, "Chức năng Quản lý Xe sắp ra mắt", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnManageUsers).setOnClickListener(v ->
                Toast.makeText(this, "Chức năng Quản lý Người dùng sắp ra mắt", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnManageOrders).setOnClickListener(v ->
                Toast.makeText(this, "Chức năng Quản lý Đơn hàng sắp ra mắt", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_toolbar_menu, menu);
        accountMenuItem = menu.findItem(R.id.action_admin_account_menu);
        accountMenuItem.setEnabled(currentUserData != null);
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
        if (currentUserData == null) {
            Toast.makeText(this, "Đang tải thông tin...", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_profile, null);
        TextView tvAdminName = dialogView.findViewById(R.id.tvAdminName);
        TextView tvAdminEmail = dialogView.findViewById(R.id.tvAdminEmail);
        Button btnClose = dialogView.findViewById(R.id.btnCloseDialog);

        tvAdminName.setText(currentUserData.fullName);
        tvAdminEmail.setText(currentUserData.email);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void logout() {
        authViewModel.logout();
        Intent intent = new Intent(AdminActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBackButtonHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(AdminActivity.this, "Vui lòng đăng xuất để thoát", Toast.LENGTH_SHORT).show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}
