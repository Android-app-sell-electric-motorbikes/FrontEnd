// data/ApiService.java
package com.example.evshop.data;

import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.LoginRequest;
import com.example.evshop.domain.models.LoginResult;
import com.example.evshop.domain.models.Vehicle;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
public interface ApiService {
        @Headers("Accept: application/json")
        @POST("/api/Auth/login-user")
        Call<ApiEnvelope<LoginResult>> login(@Body LoginRequest body);

        //---Vehicle----
        @GET("/api/ElectricVehicle/get-all-vehicles")
        Call<ApiEnvelope<List<Vehicle>>> getAllVehicles();
    }
