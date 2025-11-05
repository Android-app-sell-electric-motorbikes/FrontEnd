package com.example.evshop.data.repository;

import com.example.evshop.data.network.PaymentApiService;
import com.example.evshop.domain.models.VnpayResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Response;

@Singleton
public class PaymentRepository {

    private final PaymentApiService apiService;

    @Inject
    public PaymentRepository(PaymentApiService apiService) {
        this.apiService = apiService;
    }

    public interface Callback<T> {
        void onSuccess(T response);
        void onError(String message);
    }

    // ** SỬA LẠI: CHỈ CẦN AMOUNT **
    public void createVnpayPayment(long amount, Callback<VnpayResponse> callback) {
        apiService.createVnpayPayment(amount).enqueue(new retrofit2.Callback<VnpayResponse>() {
            @Override
            public void onResponse(Call<VnpayResponse> call, Response<VnpayResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Lỗi tạo thanh toán từ server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<VnpayResponse> call, Throwable t) {
                callback.onError("Lỗi mạng: " + t.getMessage());
            }
        });
    }
}
