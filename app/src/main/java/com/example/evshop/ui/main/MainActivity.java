package com.example.evshop.ui.main;

<<<<<<< HEAD:app/src/main/java/com/example/evshop/ui/MainActivity.java
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
=======
import android.content.Intent;
import android.os.Bundle;
>>>>>>> 4106a68aca52fbb28be23c5f52f0812b2aff348f:app/src/main/java/com/example/evshop/ui/main/MainActivity.java
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import android.os.Bundle;

import com.example.evshop.R;
import com.example.evshop.databinding.ActivityMainBinding;
<<<<<<< HEAD:app/src/main/java/com/example/evshop/ui/MainActivity.java
import com.example.evshop.util.NotificationHelper;
=======
import com.example.evshop.ui.map.VietMapMapViewActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import dagger.hilt.android.AndroidEntryPoint;
>>>>>>> 4106a68aca52fbb28be23c5f52f0812b2aff348f:app/src/main/java/com/example/evshop/ui/main/MainActivity.java

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private static final double STORE_LAT = 16.047079;
    private static final double STORE_LNG = 108.206230;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

<<<<<<< HEAD:app/src/main/java/com/example/evshop/ui/MainActivity.java
=======
        MaterialButton btnMap = findViewById(R.id.btnMap);
        Chip chipUser = findViewById(R.id.chipUser);

        btnMap.setOnClickListener(v -> openVietMapActivity());
        chipUser.setOnClickListener(v -> openVietMapActivity());

>>>>>>> 4106a68aca52fbb28be23c5f52f0812b2aff348f:app/src/main/java/com/example/evshop/ui/main/MainActivity.java
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

<<<<<<< HEAD:app/src/main/java/com/example/evshop/ui/MainActivity.java
    @Override
    public boolean onSupportNavigateUp() {
=======
    private void openVietMapActivity() {
        Intent i = new Intent(this, VietMapMapViewActivity.class);
        i.putExtra("STORE_LAT", STORE_LAT);
        i.putExtra("STORE_LNG", STORE_LNG);
        startActivity(i);
    }
    @Override public boolean onSupportNavigateUp() {
>>>>>>> 4106a68aca52fbb28be23c5f52f0812b2aff348f:app/src/main/java/com/example/evshop/ui/main/MainActivity.java
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host);
        return host.getNavController().navigateUp() || super.onSupportNavigateUp();
    }
}
