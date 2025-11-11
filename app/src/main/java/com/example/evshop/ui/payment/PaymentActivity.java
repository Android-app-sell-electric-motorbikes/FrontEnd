package com.example.evshop.ui.payment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.evshop.R;
import com.google.android.material.appbar.MaterialToolbar;

public class PaymentActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "EXTRA_URL";

    private WebView webView;
    private ProgressBar progressBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());

        String url = getIntent().getStringExtra(EXTRA_URL);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String currentUrl = request.getUrl().toString();
                // ** PHÁT HIỆN GIAO DỊCH THÀNH CÔNG **
                if (currentUrl.contains("vnp_ResponseCode=00")) {
                    // Gửi tín hiệu thành công về cho CartActivity
                    setResult(RESULT_OK);
                    finish();
                    return true; // Đã xử lý, không load URL này nữa
                }
                // Các trường hợp khác (thất bại, hủy) sẽ tự động đóng khi người dùng back
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        } else {
            finish(); // Đóng nếu không có URL
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
