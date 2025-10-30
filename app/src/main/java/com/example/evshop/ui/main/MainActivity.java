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
import com.example.evshop.ui.auth.LoginActivity;
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

        // 2. Setup Bottom Navigation
        setupBottomNavigation();

        // 3. Tích hợp thanh công cụ (Toolbar) với Navigation Component
        // Tự động xử lý tiêu đề và nút quay lại
        NavigationUI.setupWithNavController(binding.toolbar, navController);

        requestNotificationPermission();
    }

    private void setupBottomNavigation() {
        // Kết nối Bottom Navigation với NavController
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        
        // Lắng nghe thay đổi destination để update UI
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Có thể ẩn/hiện bottom nav tùy theo màn hình
            int destId = destination.getId();
            if (destId == R.id.loginFragment || destId == R.id.registerFragment) {
                // Ẩn bottom nav khi ở màn hình login/register
                binding.bottomNavigation.setVisibility(android.view.View.GONE);
            } else {
                binding.bottomNavigation.setVisibility(android.view.View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Gắn layout menu vào thanh công cụ
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Hàm này xử lý sự kiện khi người dùng nhấn vào một item trong menu
        int itemId = item.getItemId();

        if (itemId == R.id.action_notifications) {
            // Mở màn hình Notifications
            startActivity(new Intent(this, com.example.evshop.ui.NotificationActivity.class));
            return true;
        }
        
        // Để các menu items khác (search) được xử lý bởi Fragment
        return super.onOptionsItemSelected(item);
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
