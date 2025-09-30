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
import com.example.evshop.models.ApiEnvelope;
import com.example.evshop.models.LoginRequest;
import com.example.evshop.models.LoginResult;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etUser, etPass;
    private Button btnLogin;
    private ProgressBar loading;
    private TokenManager tokenManager;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
        String email = String.valueOf(etUser.getText()).trim();      // ô username đổi thành email
        String pass  = String.valueOf(etPass.getText()).trim();

        RetrofitClient.getApi(this).login(new LoginRequest(email, pass, true))
                .enqueue(new Callback<ApiEnvelope<LoginResult>>() {
                    @Override
                    public void onResponse(Call<ApiEnvelope<LoginResult>> call,
                                           Response<ApiEnvelope<LoginResult>> resp) {
                        setLoading(false);
                        if (resp.isSuccessful() && resp.body()!=null && resp.body().isSuccess && resp.body().result!=null){
                            LoginResult r = resp.body().result;
                            tokenManager.saveAccessToken(r.accessToken);
                            tokenManager.saveRefreshToken(r.refreshToken);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Snackbar.make(btnLogin, "Đăng nhập thất bại: " +
                                    (resp.body()!=null ? resp.body().message : "Lỗi"), Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiEnvelope<LoginResult>> call, Throwable t) {
                        setLoading(false);
                        Snackbar.make(btnLogin, "Lỗi mạng: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });

    }

    private void setLoading(boolean b){
        loading.setVisibility(b? View.VISIBLE: View.GONE);
        btnLogin.setEnabled(!b);
    }
}
