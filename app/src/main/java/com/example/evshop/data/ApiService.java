// data/ApiService.java
package com.example.evshop.data;

import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.LoginRequest;
import com.example.evshop.domain.models.LoginResult;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
public interface ApiService {
        @Headers("Accept: application/json")
        @POST("/api/Auth/login-user")
        Call<ApiEnvelope<LoginResult>> login(@Body LoginRequest body);
    }
