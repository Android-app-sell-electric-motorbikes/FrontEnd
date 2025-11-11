package com.example.evshop.data.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.auth0.android.jwt.JWT;
import com.example.evshop.data.ApiService;
import com.example.evshop.data.TokenManager;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.LoginRequest;
import com.example.evshop.domain.models.LoginResult;
import com.example.evshop.domain.models.RegisterRequest;
import com.example.evshop.domain.models.UserData;

import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Response;

@Singleton
public class AuthRepository {
    private final ApiService apiService;
    private final TokenManager tokenManager;

    private final MutableLiveData<UserData> _currentUserData = new MutableLiveData<>();

    @Inject
    public AuthRepository(ApiService apiService, TokenManager tm) {
        this.apiService = apiService;
        this.tokenManager = tm;
        loadUserFromToken();
    }

    public LiveData<UserData> getCurrentUser() {
        return _currentUserData;
    }

    private void loadUserFromToken() {
        String token = tokenManager.getAccessToken();
        String role = tokenManager.getUserRole();
        if (token != null && role != null) {
            UserData userData = new UserData();
            userData.role = role;
            try {
                JWT jwt = new JWT(token);
                userData.username = jwt.getClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier").asString();
            } catch (Exception ignored) {}
            _currentUserData.postValue(userData);
        } else {
            _currentUserData.postValue(null);
        }
    }

    public void logout() {
        tokenManager.clear();
        _currentUserData.postValue(null);
    }

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public void login(String username, String pass, Callback<LoginResult> cb) {
        LoginRequest request = new LoginRequest(username, pass);
        apiService.login(request).enqueue(new retrofit2.Callback<ApiEnvelope<LoginResult>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<LoginResult>> call, Response<ApiEnvelope<LoginResult>> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess && resp.body().result != null) {
                    LoginResult r = resp.body().result;
                    tokenManager.saveAccessToken(r.accessToken);
                    tokenManager.saveRefreshToken(r.refreshToken);

                    try {
                        JWT jwt = new JWT(r.accessToken);
                        String role = jwt.getClaim("http://schemas.microsoft.com/ws/2008/06/identity/claims/role").asString();
                        tokenManager.saveUserRole(role);
                        if (r.userData != null) {
                            r.userData.role = role;
                            r.userData.username = username;
                        }
                        _currentUserData.postValue(r.userData);
                    } catch (Exception ignored) {}

                    cb.onSuccess(r);
                } else {
                    handleErrorResponse(resp, cb);
                }
            }
            @Override
            public void onFailure(Call<ApiEnvelope<LoginResult>> call, Throwable t) {
                cb.onError("Lỗi mạng, vui lòng kiểm tra kết nối.");
            }
        });
    }

    public void register(RegisterRequest request, Callback<String> cb) {
        apiService.register(request).enqueue(new retrofit2.Callback<ApiEnvelope<String>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<String>> call, Response<ApiEnvelope<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    cb.onSuccess(response.body().message);
                } else {
                    handleErrorResponse(response, cb);
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<String>> call, Throwable t) {
                cb.onError("Lỗi mạng, vui lòng kiểm tra kết nối.");
            }
        });
    }

    // ** NÂNG CẤP HÀM XỬ LÝ LỖI **
    private void handleErrorResponse(Response<?> response, Callback<?> callback) {
        // ** XỬ LÝ LỖI 401: TOKEN HẾT HẠN **
        if (response.code() == 401) {
            logout(); // Tự động đăng xuất
            callback.onError("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
            return;
        }

        String errorMessage = "Đã có lỗi xảy ra. Vui lòng thử lại."; // Mặc định
        if (response.errorBody() != null) {
            try {
                String errorBodyString = response.errorBody().string();
                JSONObject errorJson = new JSONObject(errorBodyString);
                if (errorJson.has("message")) {
                    String serverMessage = errorJson.getString("message");
                    if (serverMessage.toLowerCase().contains("existed")) {
                        errorMessage = "Tên đăng nhập hoặc email đã tồn tại.";
                    } else {
                        errorMessage = serverMessage;
                    }
                } else if (errorJson.has("title")){
                     errorMessage = errorJson.getString("title");
                }
            } catch (Exception e) {
                 errorMessage = "Lỗi máy chủ: " + response.code();
            }
        } else {
            errorMessage = "Lỗi không xác định: " + response.code();
        }
        callback.onError(errorMessage);
    }
}
