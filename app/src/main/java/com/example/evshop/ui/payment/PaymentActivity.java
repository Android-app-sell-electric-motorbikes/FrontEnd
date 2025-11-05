package com.example.evshop.ui.payment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.evshop.R;
import com.google.android.material.appbar.MaterialToolbar;

public class PaymentActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "PAYMENT_URL";
    public static final String RETURN_URL_SUCCESS_KEY = "/Checkout"; // TỪ API CỦA BẠN

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        WebView webView = findViewById(R.id.webView);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        toolbar.setNavigationOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        String paymentUrl = getIntent().getStringExtra(EXTRA_URL);

        if (paymentUrl == null || paymentUrl.isEmpty()) {
            Toast.makeText(this, "URL thanh toán không hợp lệ", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

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

                if (url.contains(RETURN_URL_SUCCESS_KEY)) {
                    // Bạn có thể kiểm tra thêm các tham số như vnp_ResponseCode nếu cần
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Toast.makeText(PaymentActivity.this, "Lỗi tải trang: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        webView.loadUrl(paymentUrl);
    }
}
