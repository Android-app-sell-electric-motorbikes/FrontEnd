package com.example.evshop.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.evshop.R;
import com.example.evshop.databinding.ActivityMainBinding;
import com.example.evshop.ui.auth.AuthViewModel;

import java.util.HashSet;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AuthViewModel authViewModel;

    // Launcher để yêu cầu quyền thông báo
    private final ActivityResultLauncher<String> requestPermissionLauncher = 
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            // Bạn có thể xử lý kết quả ở đây nếu cần, ví dụ: hiển thị thông báo cảm ơn
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupNavigation();
        observeLoginStatus();
        askNotificationPermission(); // Yêu cầu quyền khi khởi động
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            Set<Integer> topLevelDestinations = new HashSet<>();
            topLevelDestinations.add(R.id.homeFragment);

            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();

            NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                invalidateOptionsMenu();
            });
        }
    }

    private void observeLoginStatus() {
        authViewModel.getIsLoggedInState().observe(this, isLoggedIn -> {
            invalidateOptionsMenu();
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (navController != null) {
            int currentDestinationId = navController.getCurrentDestination().getId();
            Boolean isLoggedIn = authViewModel.getIsLoggedInState().getValue();
            boolean loggedIn = isLoggedIn != null && isLoggedIn;

            boolean shouldShowMenu = (currentDestinationId == R.id.homeFragment);

            MenuItem cartItem = menu.findItem(R.id.cartFragment);
            if (cartItem != null) {
                cartItem.setVisible(shouldShowMenu);
            }
            
            MenuItem accountItem = menu.findItem(R.id.action_account_menu);
            if (accountItem != null) {
                accountItem.setVisible(shouldShowMenu && loggedIn);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }
}
