package com.example.evshop.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.evshop.R;
import com.example.evshop.databinding.ActivityMainBinding;
import com.example.evshop.ui.auth.LoginActivity; // <-- QUAN TRỌNG: Thay bằng Activity đăng nhập của bạn (ví dụ: LoginActivity)
import com.example.evshop.ui.auth.AuthViewModel;
import com.example.evshop.ui.map.VietMapMapViewActivity;
import com.example.evshop.util.NotificationHelper;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;

    // Sử dụng AuthViewModel làm "nguồn chân lý" cho trạng thái đăng nhập
    private AuthViewModel authViewModel;

    private static final double STORE_LAT = 16.047079;
    private static final double STORE_LNG = 108.206230;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host);
        navController = host.getNavController();

        // 1. Khởi tạo AuthViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 2. Lắng nghe sự thay đổi trạng thái đăng nhập
        // Khi trạng thái thay đổi, yêu cầu hệ thống vẽ lại menu ngay lập tức
        authViewModel.isLoggedIn.observe(this, isLoggedIn -> {
            invalidateOptionsMenu();
        });

        // 3. Tích hợp thanh công cụ (Toolbar) với Navigation Component
        // Tự động xử lý tiêu đề và nút quay lại
        NavigationUI.setupWithNavController(binding.toolbar, navController);

        requestNotificationPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Gắn layout menu vào thanh công cụ
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Hàm này được gọi ngay trước khi menu hiển thị
        // Đây là nơi hoàn hảo để ẩn/hiện các nút
        MenuItem loginItem = menu.findItem(R.id.login);
        MenuItem logoutItem = menu.findItem(R.id.action_logout);

        if (loginItem != null && logoutItem != null) {
            // Lấy trạng thái đăng nhập mới nhất từ ViewModel
            Boolean isLoggedIn = authViewModel.isLoggedIn.getValue();
            boolean loggedIn = isLoggedIn != null && isLoggedIn;

            // Ẩn/hiện các nút dựa trên trạng thái
            loginItem.setVisible(!loggedIn);
            logoutItem.setVisible(loggedIn);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Hàm này xử lý sự kiện khi người dùng nhấn vào một item trong menu
        int itemId = item.getItemId();

        if (itemId == R.id.login) {
            // Chuyển sang màn hình đăng nhập
            startActivity(new Intent(this, LoginActivity.class)); // Thay AuthActivity nếu cần
            return true;
        } else if (itemId == R.id.action_logout) {
            // Gọi logout từ ViewModel
            authViewModel.logout();

            // Chuyển về màn hình Login và xóa hết các màn hình cũ trong stack
            Intent intent = new Intent(this, LoginActivity.class); // Thay AuthActivity nếu cần
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            return true;
        }

        // Nếu không phải các nút trên, để cho NavigationUI tự xử lý (ví dụ: search, cart, filter)
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    private void openVietMapActivity() {
        Intent i = new Intent(this, VietMapMapViewActivity.class);
        i.putExtra("STORE_LAT", STORE_LAT);
        i.putExtra("STORE_LNG", STORE_LNG);
        startActivity(i);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            } else {
                new NotificationHelper(this).updateAppBadge(5);
            }
        } else {
            new NotificationHelper(this).updateAppBadge(5);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Xử lý nút "Back" trên thanh công cụ
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
