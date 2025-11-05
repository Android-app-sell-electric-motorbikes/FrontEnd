package com.example.evshop.ui.transaction;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.repository.TransactionRepository;
import com.example.evshop.domain.models.Transaction;
import com.example.evshop.domain.models.TransactionResult;

import java.util.List;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TransactionViewModel extends ViewModel {

    private final TransactionRepository repository;
    private final MutableLiveData<List<Transaction>> _transactions = new MutableLiveData<>();
    public LiveData<List<Transaction>> transactions = _transactions;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>();
    public LiveData<Boolean> loading = _loading;

    @Inject
    public TransactionViewModel(TransactionRepository repository) {
        this.repository = repository;
    }

    public void fetchTransactions() {
        _loading.setValue(true);
        repository.getTransactions(1, 20, new TransactionRepository.TransactionCallback() {
            @Override
            public void onSuccess(TransactionResult result) {
                _transactions.postValue(result.getData());
                _loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                _error.postValue(message);
                _loading.postValue(false);
            }
        });
    }
}
