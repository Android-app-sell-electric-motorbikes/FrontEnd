package com.example.evshop.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.auth.AuthRepository;
import com.example.evshop.domain.models.LoginResult;
import com.example.evshop.domain.models.RegisterRequest;
import com.example.evshop.domain.models.UserData;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    public enum NavigationEvent { GO_TO_ADMIN, GO_TO_HOME, STAY }

    public final MutableLiveData<String> _error = new MutableLiveData<>();
    public final MutableLiveData<Boolean> _loading = new MutableLiveData<>();
    private final MutableLiveData<NavigationEvent> _navigationEvent = new MutableLiveData<>(NavigationEvent.STAY);

    @Inject
    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    // ** THAY ĐỔI: LẤY TRỰC TIẾP TỪ REPOSITORY **
    public LiveData<UserData> getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    public LiveData<NavigationEvent> getNavigationEvent() {
        return _navigationEvent;
    }

    public void onNavigationComplete() {
        _navigationEvent.setValue(NavigationEvent.STAY);
    }

    public void login(String username, String password) {
        _loading.setValue(true);
        authRepository.login(username, password, new AuthRepository.Callback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult data) {
                // Repository đã tự cập nhật LiveData, ViewModel chỉ cần xử lý điều hướng
                if (data.userData != null && data.userData.isAdmin()) {
                    _navigationEvent.postValue(NavigationEvent.GO_TO_ADMIN);
                } else {
                    _navigationEvent.postValue(NavigationEvent.GO_TO_HOME);
                }
                _loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                _error.postValue(message);
                _loading.postValue(false);
            }
        });
    }

    public void logout() {
        authRepository.logout(); // Chỉ cần gọi logout của repository
    }

    public void register(RegisterRequest request) {
        _loading.setValue(true);
        authRepository.register(request, new AuthRepository.Callback<String>() {
            @Override
            public void onSuccess(String data) {
                _error.postValue("Đăng ký thành công! Vui lòng đăng nhập.");
                _navigationEvent.postValue(NavigationEvent.GO_TO_HOME); // Điều hướng về trang đăng nhập
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
