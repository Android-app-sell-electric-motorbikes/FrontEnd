// data/AuthInterceptor.java
package com.example.evshop.data;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final TokenManager tokenManager;
    public AuthInterceptor(TokenManager tm){ this.tokenManager = tm; }

    @Override public Response intercept(Chain chain) throws IOException {
        Request req = chain.request();
        String token = tokenManager.getAccessToken();
        if (token != null) {
            req = req.newBuilder().addHeader("Authorization", "Bearer " + token).build();
        }
        return chain.proceed(req);
    }
}
