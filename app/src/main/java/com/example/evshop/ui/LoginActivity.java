// ui/LoginActivity.java
package com.example.evshop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.evshop.R;
import com.example.evshop.data.RetrofitClient;
import com.example.evshop.data.TokenManager;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.LoginRequest;
import com.example.evshop.domain.models.LoginResult;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etUser, etPass;
    private Button btnLogin;
    private ProgressBar loading;
    private View root;
    private TokenManager tokenManager;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        tokenManager = new TokenManager(this);
        if (tokenManager.getAccessToken() != null){
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        etUser = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        loading = findViewById(R.id.loading);

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin(){
        String email = String.valueOf(etUser.getText()).trim();
        String pass  = String.valueOf(etPass.getText()).trim();

        if (email.isEmpty()) { snack("Vui lòng nhập email"); return; }
        if (pass.isEmpty())  { snack("Vui lòng nhập mật khẩu"); return; }

        setLoading(true);

        RetrofitClient.getApi(this).login(new LoginRequest(email, pass, true))
                .enqueue(new Callback<ApiEnvelope<LoginResult>>() {
                    @Override
                    public void onResponse(Call<ApiEnvelope<LoginResult>> call,
                                           Response<ApiEnvelope<LoginResult>> resp) {
                        setLoading(false);
                        if (resp.isSuccessful() && resp.body()!=null) {
                            ApiEnvelope<LoginResult> body = resp.body();
                            if (body.isSuccess && body.result != null) {
                                LoginResult r = body.result;
                                tokenManager.saveAccessToken(r.accessToken);
                                tokenManager.saveRefreshToken(r.refreshToken);
                                snack("Đăng nhập thành công");
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                snack("Đăng nhập thất bại: " + (body.message != null ? body.message : "Thông tin không hợp lệ"));
                            }
                        } else {
                            String err = "HTTP " + resp.code();
                            try {
                                if (resp.errorBody()!=null) err += " - " + resp.errorBody().string();
                            } catch (Exception ignored) {}
                            snack("Đăng nhập thất bại: " + err);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiEnvelope<LoginResult>> call, Throwable t) {
                        setLoading(false);
                        snack("Lỗi mạng: " + (t.getMessage()!=null ? t.getMessage() : "Không rõ nguyên nhân"));
                    }
                });
    }

    private void setLoading(boolean b){
        loading.setVisibility(b? View.VISIBLE: View.GONE);
        btnLogin.setEnabled(!b);
        etUser.setEnabled(!b);
        etPass.setEnabled(!b);
    }

    private void snack(String msg){
        Snackbar.make(root, msg, Snackbar.LENGTH_LONG).setAnchorView(btnLogin).show();
    }
}
