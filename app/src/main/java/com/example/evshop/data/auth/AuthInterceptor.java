package com.example.evshop.data.auth;

import com.example.evshop.data.TokenManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final TokenManager tokenManager;
    // Danh sách các đường dẫn không cần token
    private final List<String> publicPaths = Arrays.asList(
            "/api/Auth/login-mobile",
            "/api/Auth/register-mobile",
            "/api/Payment/create-vnpay-mobile"
    );

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder();

        // Kiểm tra xem đường dẫn có cần token hay không
        boolean needsAuth = publicPaths.stream().noneMatch(path -> originalRequest.url().encodedPath().contains(path));

        if (needsAuth) {
            String token = tokenManager.getAccessToken();
            if (token != null && !token.isEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer " + token);
            }
        }

        return chain.proceed(requestBuilder.build());
    }
}
