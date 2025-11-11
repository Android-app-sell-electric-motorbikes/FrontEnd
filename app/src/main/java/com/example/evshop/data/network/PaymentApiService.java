package com.example.evshop.data.network;

import com.example.evshop.domain.models.VnpayResponse;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PaymentApiService {
    @POST("api/Payment/create-vnpay-mobile/{amount}")
    Call<VnpayResponse> createVnpayPayment(@Path("amount") long amount);
}
