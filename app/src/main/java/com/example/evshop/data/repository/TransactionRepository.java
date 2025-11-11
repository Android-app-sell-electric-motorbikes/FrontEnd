package com.example.evshop.data.repository;

import com.example.evshop.data.ApiService;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TransactionResult;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class TransactionRepository {

    private final ApiService apiService;

    @Inject
    public TransactionRepository(ApiService apiService) { // **YÊU CẦU ApiService**
        this.apiService = apiService;
    }

    public interface TransactionCallback {
        void onSuccess(TransactionResult result);
        void onError(String message);
    }

    public void getTransactions(int page, int pageSize, TransactionCallback callback) {
        apiService.getAllTransactions(page, pageSize).enqueue(new Callback<ApiEnvelope<TransactionResult>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<TransactionResult>> call, Response<ApiEnvelope<TransactionResult>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    callback.onSuccess(response.body().result);
                } else {
                    callback.onError("Lỗi tải lịch sử giao dịch: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<TransactionResult>> call, Throwable t) {
                callback.onError("Lỗi mạng: " + t.getMessage());
            }
        });
    }
}
