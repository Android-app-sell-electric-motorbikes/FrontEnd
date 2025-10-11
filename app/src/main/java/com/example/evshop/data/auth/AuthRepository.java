package com.example.evshop.data.auth;

import com.example.evshop.data.ApiService;
import com.example.evshop.data.TokenManager;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.LoginRequest;
import com.example.evshop.domain.models.LoginResult;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import retrofit2.Call;
import retrofit2.Response;
    @Singleton
    public class AuthRepository {
        private final ApiService api;
        private final TokenManager tokenManager;

        @Inject
        public AuthRepository(ApiService api, TokenManager tm) {
            this.api = api;
            this.tokenManager = tm;
        }

        public interface Callback<T> {
            void onSuccess(T data);
            void onError(String message);
        }

        public void login(String email, String pass, Callback<LoginResult> cb) {
            api.login(new LoginRequest(email, pass, true))
                    .enqueue(new retrofit2.Callback<ApiEnvelope<LoginResult>>() {
                        @Override public void onResponse(Call<ApiEnvelope<LoginResult>> call,
                                                         Response<ApiEnvelope<LoginResult>> resp) {
                            if (resp.isSuccessful() && resp.body()!=null && resp.body().isSuccess && resp.body().result!=null) {
                                LoginResult r = resp.body().result;
                                // Lưu token tại data-layer
                                tokenManager.saveAccessToken(r.accessToken);
                                tokenManager.saveRefreshToken(r.refreshToken);
                                cb.onSuccess(r);
                            } else {
                                String msg = (resp.body()!=null && resp.body().message!=null)
                                        ? resp.body().message
                                        : ("HTTP " + resp.code());
                                cb.onError(msg);
                            }
                        }
                        @Override public void onFailure(Call<ApiEnvelope<LoginResult>> call, Throwable t) {
                            cb.onError(t.getMessage()!=null ? t.getMessage() : "Lỗi mạng");
                        }
                    });
        }
    }
