package com.example.evshop.ui.main;

import android.Manifest;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.evshop.R;
import com.example.evshop.databinding.ActivityMainBinding;
import com.example.evshop.ui.auth.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AuthViewModel authViewModel; // Giữ lại để các Fragment có thể truy cập
    private Menu toolbarMenu; // Biến để lưu trữ menu của toolbar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập Toolbar
        setSupportActionBar(binding.toolbar);

        // Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Thiết lập Navigation Component
        setupNavigation();
    }

    // Trong file: D:/PRM391/FrontEnd/app/src/main/java/com/example/evshop/ui/main/MainActivity.java

    private void setupNavigation() {
        // Tìm NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host); // ID của NavHostFragment trong activity_main.xml
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // *** SỬA LẠI HOÀN TOÀN ĐOẠN CẤU HÌNH APPBAR ***
            //
            // Khai báo các màn hình nào là "top-level" (không có nút back).
            // Trong trường hợp này, chỉ có HomeFragment.
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.homeFragment // <-- Thêm ID của HomeFragment tại đây
            ).build();

            // Thiết lập Toolbar với cấu hình mới
            // Dòng này sẽ làm 2 việc:
            // 1. Tự động thay đổi tiêu đề Toolbar.
            // 2. Tự động hiển thị/ẩn nút back dựa trên cấu hình bạn vừa cung cấp.
            NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // "Thổi phồng" layout của menu vào
        getMenuInflater().inflate(R.menu.menu_home, menu); // Đảm bảo bạn có file res/menu/main_toolbar_menu.xml
        // Lưu lại tham chiếu đến menu
        this.toolbarMenu = menu;
        // Rất quan trọng: Mặc định ban đầu sẽ ẩn hết các icon.
        // Fragment hiện tại sẽ quyết định có hiện chúng lên hay không.
        showToolbarItems(false);
        return true;
    }

    // ====================================================================
    // ***           HÀM CÔNG KHAI DUY NHẤT ĐỂ ĐIỀU KHIỂN TOOLBAR          ***
    // ====================================================================

    /**
     * Hàm này được các Fragment gọi để ra lệnh ẩn/hiện các icon trên Toolbar.
     *
     * @param show true để hiển thị, false để ẩn.
     */
    public void showToolbarItems(boolean show) {
        if (toolbarMenu != null) {
            // Menu của chúng ta giờ chỉ có một item duy nhất.
            MenuItem accountItem = toolbarMenu.findItem(R.id.action_account_menu);
            if (accountItem != null) {
                accountItem.setVisible(show);
            }
        }
    }
}
