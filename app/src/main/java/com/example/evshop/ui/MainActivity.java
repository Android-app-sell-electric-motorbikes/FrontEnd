package com.example.evshop.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import android.os.Bundle;

import com.example.evshop.R;
import com.example.evshop.databinding.ActivityMainBinding;
import com.example.evshop.util.NotificationHelper;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host);
        NavController navController = host.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            } else {
                NotificationHelper helper = new NotificationHelper(this);
                helper.updateAppBadge(5);
            }
        } else {
            NotificationHelper helper = new NotificationHelper(this);
            helper.updateAppBadge(5);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host);
        return host.getNavController().navigateUp() || super.onSupportNavigateUp();
    }
}
