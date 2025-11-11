package com.example.evshop.data.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

    private final MutableLiveData<Boolean> isLoggedInState = new MutableLiveData<>();

    @Inject
    public AuthRepository(ApiService apiService, TokenManager tm) {
        this.apiService = apiService;
        this.tokenManager = tm;
        checkLoginStatus();
    }

    public LiveData<Boolean> getIsLoggedInState() {
        return isLoggedInState;
    }

    private void checkLoginStatus() {
        String token = tokenManager.getAccessToken();
        isLoggedInState.postValue(token != null && !token.isEmpty());
    }

    public void logout() {
        tokenManager.clear();
        isLoggedInState.postValue(false);
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
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess() && resp.body().getData() != null) {
                    LoginResult r = resp.body().getData();
                    tokenManager.saveAccessToken(r.accessToken);
                    isLoggedInState.postValue(true);
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

    // ========================================================
    // SỬA: KÍCH HOẠT LẠI PHƯƠNG THỨC REGISTER
    // ========================================================
    public void register(RegisterRequest request, Callback<UserData> cb) {
        // Gọi đến phương thức register trong ApiService đã được định nghĩa
        apiService.register(request).enqueue(new retrofit2.Callback<ApiEnvelope<UserData>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<UserData>> call, Response<ApiEnvelope<UserData>> response) {
                // Kiểm tra xem request có thành công và có dữ liệu trả về không
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Trả về đối tượng UserData cho ViewModel
                    cb.onSuccess(response.body().getData());
                } else {
                    // Xử lý lỗi nếu có
                    handleErrorResponse(response, cb);
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<UserData>> call, Throwable t) {
                cb.onError("Lỗi mạng, vui lòng kiểm tra kết nối.");
            }
        });
    }
    // ========================================================


    private void handleErrorResponse(Response<?> response, Callback<?> callback) {
        if (response.code() == 401) {
            logout();
            callback.onError("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
            return;
        }

        String errorMessage = "Đã có lỗi xảy ra. Vui lòng thử lại.";
        if (response.errorBody() != null) {
            try {
                String errorBodyString = response.errorBody().string();
                JSONObject errorJson = new JSONObject(errorBodyString);
                // Ưu tiên parse lỗi từ backend trả về
                if (errorJson.has("message")) {
                    errorMessage = errorJson.getString("message");
                } else if (errorJson.has("title")){
                    errorMessage = errorJson.getString("title");
                } else if (errorJson.has("errors")) {
                    // Xử lý lỗi validation từ ASP.NET Core
                    JSONObject errors = errorJson.getJSONObject("errors");
                    StringBuilder errorBuilder = new StringBuilder();
                    for (java.util.Iterator<String> keys = errors.keys(); keys.hasNext();) {
                        String key = keys.next();
                        String error = errors.getJSONArray(key).getString(0);
                        errorBuilder.append(error).append("\n");
                    }
                    errorMessage = errorBuilder.toString().trim();
                }
            } catch (Exception e) {
                errorMessage = "Lỗi xử lý phản hồi từ máy chủ.";
            }
        } else if (response.body() != null && ((ApiEnvelope<?>)response.body()).message != null) {
            // Lấy message từ body nếu có
            errorMessage = ((ApiEnvelope<?>)response.body()).message;
        } else {
            errorMessage = "Lỗi không xác định: " + response.code();
        }
        callback.onError(errorMessage);
    }
}
