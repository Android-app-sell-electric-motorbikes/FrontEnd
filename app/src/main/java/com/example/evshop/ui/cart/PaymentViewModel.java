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
    public final LiveData<String> paymentUrl = _paymentUrl;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    @Inject
    public PaymentViewModel(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public void createPayment(long amount) {
        paymentRepository.createVnpayPayment(amount, new PaymentRepository.Callback<VnpayResponse>() {
            @Override
            public void onSuccess(VnpayResponse response) {
                if (response.isSuccess() && response.getResult() != null) {
                    _paymentUrl.postValue(response.getResult());
                } else {
                    _error.postValue(response.getMessage() != null ? response.getMessage() : "Lỗi không xác định từ server.");
                }
            }

            @Override
            public void onError(String message) {
                _error.postValue(message);
            }
        });
    }

    // ** PHƯƠNG THỨC MỚI ĐỂ RESET URL **
    public void onPaymentUrlHandled() {
        _paymentUrl.setValue(null);
    }
}
