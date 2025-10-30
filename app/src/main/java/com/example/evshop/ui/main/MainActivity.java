package com.example.evshop.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View; // <-- QUAN TRỌNG: Import View

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.evshop.R;
import com.example.evshop.ui.admin.AdminActivity;
import com.example.evshop.databinding.ActivityMainBinding;
import com.example.evshop.ui.auth.AuthViewModel;
import com.example.evshop.ui.map.VietMapMapViewActivity;
import com.example.evshop.util.NotificationHelper;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host);
        navController = host.getNavController();

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Lắng nghe sự thay đổi trạng thái đăng nhập để cập nhật menu
        // Chúng ta sẽ không điều khiển toolbar ở đây nữa, mà để cho Fragment tự quyết định
        authViewModel.getIsLoggedInState().observe(this, isLoggedIn -> {
            invalidateOptionsMenu();
        });

        NavigationUI.setupWithNavController(binding.toolbar, navController);
        requestNotificationPermission();
    }

    // ==========================================================
    // *** HÀM MỚI QUAN TRỌNG NHẤT ***
    // ==========================================================
    /**
     * Hàm này cho phép các Fragment con điều khiển việc ẩn/hiện Toolbar.
     * @param show true để hiện Toolbar, false để ẩn Toolbar.
     */
    public void showToolbar(boolean show) {
        if (binding != null) {
            binding.toolbar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    // ==========================================================


    // --- CÁC HÀM KHÁC GIỮ NGUYÊN ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem loginItem = menu.findItem(R.id.login);
        MenuItem logoutItem = menu.findItem(R.id.action_logout);

        if (loginItem != null && logoutItem != null) {
            Boolean isLoggedIn = authViewModel.getIsLoggedInState().getValue();
            boolean loggedIn = isLoggedIn != null && isLoggedIn;

            loginItem.setVisible(!loggedIn);
            logoutItem.setVisible(loggedIn);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.login) {
            navController.navigate(R.id.loginFragment);
            return true;
        } else if (itemId == R.id.action_logout) {
            authViewModel.logout();
            // Sau khi logout, navController sẽ tự động quay về màn hình phù hợp
            return true;
        }

        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
