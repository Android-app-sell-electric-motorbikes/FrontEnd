package com.example.evshop.ui;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.evshop.R;
import com.example.evshop.ui.main.MainActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 2500; // 2.5 seconds
    private static final long ANIMATION_DURATION = 1000; // 1 second

    private ImageView ivLogo;
    private TextView tvAppName;
    private TextView tvTagline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);
        tvTagline = findViewById(R.id.tvTagline);

        // Start animations
        startAnimations();

        // Navigate to MainActivity after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateToMain();
        }, SPLASH_DURATION);
    }

    private void startAnimations() {
        // Animate logo: fade in + scale
        ObjectAnimator logoFadeIn = ObjectAnimator.ofFloat(ivLogo, "alpha", 0f, 1f);
        logoFadeIn.setDuration(ANIMATION_DURATION);
        logoFadeIn.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(ivLogo, "scaleX", 0.5f, 1f);
        logoScaleX.setDuration(ANIMATION_DURATION);

        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(ivLogo, "scaleY", 0.5f, 1f);
        logoScaleY.setDuration(ANIMATION_DURATION);

        // Start logo animations
        logoFadeIn.start();
        logoScaleX.start();
        logoScaleY.start();

        // Animate app name after logo (with delay)
        ObjectAnimator nameFadeIn = ObjectAnimator.ofFloat(tvAppName, "alpha", 0f, 1f);
        nameFadeIn.setStartDelay(400);
        nameFadeIn.setDuration(800);
        nameFadeIn.start();

        // Animate tagline after app name
        ObjectAnimator taglineFadeIn = ObjectAnimator.ofFloat(tvTagline, "alpha", 0f, 1f);
        taglineFadeIn.setStartDelay(700);
        taglineFadeIn.setDuration(800);
        taglineFadeIn.start();
    }

    private void navigateToMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Disable back button during splash
    }
}

