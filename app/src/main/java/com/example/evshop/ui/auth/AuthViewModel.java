package com.example.evshop.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.TokenManager;
import com.example.evshop.data.auth.AuthRepository;
import com.example.evshop.data.repository.UserRepository;
import com.example.evshop.domain.models.LoginResult;
import com.example.evshop.domain.models.UserData;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final TokenManager tokenManager;

    public final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public final MutableLiveData<String> _error = new MutableLiveData<>();

    public enum NavigationEvent {
        GO_TO_HOME,
        GO_TO_ADMIN,
        STAY
    }
    private final MutableLiveData<NavigationEvent> _navigationEvent = new MutableLiveData<>(NavigationEvent.STAY);
    public LiveData<NavigationEvent> getNavigationEvent() {
        return _navigationEvent;
    }

    @Inject
    public AuthViewModel(
            AuthRepository authRepository,
            UserRepository userRepository,
            TokenManager tokenManager
    ) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.tokenManager = tokenManager;
    }

    // ==========================================================
    // *** BƯỚC 1: THÊM HÀM NÀY ĐỂ CUNG CẤP THÔNG TIN USER ***
    // ==========================================================
    /**
     * Trả về LiveData chứa thông tin của người dùng đang đăng nhập.
     * AdminActivity sẽ gọi hàm này.
     */
    public LiveData<UserData> getCurrentUser() {
        return userRepository.getCurrentUser();
    }
    // ==========================================================


    public void login(String email, String password) {
        _loading.setValue(true);
        _error.setValue(null);

        authRepository.login(email, password, new AuthRepository.Callback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult data) {
                _loading.postValue(false);

                if (data == null || data.userData == null) {
                    _error.postValue("Dữ liệu đăng nhập không hợp lệ.");
                    return;
                }

                // Lưu thông tin người dùng lại để các màn hình khác dùng
                userRepository.setCurrentUser(data.userData);

                // Logic phân quyền (giữ nguyên)
                if (data.accessToken != null && data.accessToken.contains("QWRtaW4")) {
                    _navigationEvent.postValue(NavigationEvent.GO_TO_ADMIN);
                } else {
                    _navigationEvent.postValue(NavigationEvent.GO_TO_HOME);
                }
            }

            @Override
            public void onError(String message) {
                _loading.postValue(false);
                _error.postValue(message);
            }
        });
    }

    public void onNavigationComplete() {
        _navigationEvent.setValue(NavigationEvent.STAY);
    }

    public LiveData<Boolean> getIsLoggedInState() {
        return authRepository.getIsLoggedInState();
    }



    public void logout() {
        tokenManager.clear();
        userRepository.clearCurrentUser();
        authRepository.logout();
    }
}
