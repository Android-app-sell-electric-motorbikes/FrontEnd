package com.example.evshop.data.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.evshop.data.ApiService;
import com.example.evshop.data.TokenManager;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.LoginRequest;
import com.example.evshop.domain.models.LoginResult;

import javax.inject.Inject;
import javax.inject.Named; // << THÊM IMPORT NÀY
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Response;

@Singleton // Đảm bảo chỉ có một AuthRepository duy nhất trong toàn app
public class AuthRepository {
    private final ApiService api;
    private final TokenManager tokenManager;

    private final MutableLiveData<Boolean> isLoggedInState = new MutableLiveData<>();

    @Inject
    public AuthRepository(@Named("AuthApiService") ApiService api, TokenManager tm) { // << SỬA Ở ĐÂY
        this.api = api;
        this.tokenManager = tm;
        checkInitialLoginStatus();
    }

    public LiveData<Boolean> getIsLoggedInState() {
        return isLoggedInState;
    }

    private void checkInitialLoginStatus() {
        // Dùng postValue để đảm bảo an toàn nếu được gọi từ background thread
        isLoggedInState.postValue(tokenManager.getAccessToken() != null);
    }

    public void logout() {
        tokenManager.clear();
        isLoggedInState.postValue(false); // Phát ra trạng thái "đã đăng xuất"
    }

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public void login(String email, String pass, Callback<LoginResult> cb) {
        api.login(new LoginRequest(email, pass, true))
                .enqueue(new retrofit2.Callback<ApiEnvelope<LoginResult>>() {
                    @Override
                    public void onResponse(Call<ApiEnvelope<LoginResult>> call,
                                           Response<ApiEnvelope<LoginResult>> resp) {
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess && resp.body().result != null) {
                            LoginResult r = resp.body().result;
                            tokenManager.saveAccessToken(r.accessToken);
                            tokenManager.saveRefreshToken(r.refreshToken);

                            // *** CẬP NHẬT QUAN TRỌNG NHẤT ***
                            isLoggedInState.postValue(true); // Phát ra trạng thái "đã đăng nhập"

                            cb.onSuccess(r);
                        } else {
                            String msg = (resp.body() != null && resp.body().message != null)
                                    ? resp.body().message
                                    : ("HTTP " + resp.code());
                            cb.onError(msg);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiEnvelope<LoginResult>> call, Throwable t) {
                        cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
                    }
                });
    }
}
