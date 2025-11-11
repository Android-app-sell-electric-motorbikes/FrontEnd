package com.example.evshop.data.auth;

import com.example.evshop.data.TokenManager;

import java.io.IOException;
// Xóa import không cần thiết: import java.util.Arrays;
// Xóa import không cần thiết: import java.util.List;

import okhttp3.HttpUrl; // Thêm import này
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final TokenManager tokenManager;
    // *** BƯỚC 1: XÁC ĐỊNH HOST API CỦA BẠN ***
    private final String apiHost = "api.electricvehiclesystem.click";

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        HttpUrl requestUrl = originalRequest.url(); // Lấy URL của request
        Request.Builder requestBuilder = originalRequest.newBuilder();

        // ===================================================================
        //        *** BƯỚC 2: SỬA LẠI TOÀN BỘ LOGIC KIỂM TRA ***
        // ===================================================================
        // Chỉ thêm token nếu request đang gọi đến ĐÚNG host API của bạn
        if (requestUrl.host().equals(apiHost)) {
            // Lấy token
            String token = tokenManager.getAccessToken();
            // Thêm header nếu có token
            if (token != null && !token.isEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer " + token);
            }
        }
        // --> Nếu request KHÔNG gọi đến apiHost (ví dụ: gọi đến S3),
        // thì khối if này sẽ bị bỏ qua và token sẽ KHÔNG được thêm vào.
        // Đây chính là điều chúng ta cần!

        return chain.proceed(requestBuilder.build());
    }
}
