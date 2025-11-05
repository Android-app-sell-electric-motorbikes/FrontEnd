package com.example.evshop.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.repository.PaymentRepository;
import com.example.evshop.domain.models.VnpayResponse;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PaymentViewModel extends ViewModel {

    private final PaymentRepository paymentRepository;
    private final MutableLiveData<String> _paymentUrl = new MutableLiveData<>();
    public LiveData<String> paymentUrl = _paymentUrl;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    @Inject
    public PaymentViewModel(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // ** SỬA LẠI: CHỈ CẦN AMOUNT **
    public void createPayment(long amount) {
        paymentRepository.createVnpayPayment(amount, new PaymentRepository.Callback<VnpayResponse>() {
            @Override
            public void onSuccess(VnpayResponse response) {
                if (response != null && response.isSuccess() && response.getResult() != null) {
                    _paymentUrl.postValue(response.getResult());
                } else {
                    String errorMessage = (response != null) ? response.getMessage() : "Không nhận được URL thanh toán.";
                    _error.postValue(errorMessage);
                }
            }

            @Override
            public void onError(String message) {
                _error.postValue("Lỗi tạo thanh toán: " + message);
            }
        });
    }
}
