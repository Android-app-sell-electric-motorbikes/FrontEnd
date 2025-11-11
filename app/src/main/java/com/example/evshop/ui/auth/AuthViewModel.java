package com.example.evshop.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.TokenManager;
import com.example.evshop.data.auth.AuthRepository;
import com.example.evshop.data.repository.UserRepository;
import com.example.evshop.domain.models.LoginResult;
import com.example.evshop.domain.models.RegisterRequest;
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

    public LiveData<UserData> getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public void login(String email, String password) {
        _loading.setValue(true);
        _error.setValue(null);

        authRepository.login(email, password, new AuthRepository.Callback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult data) {
                _loading.postValue(false);

                if (data == null || data.userData == null || data.accessToken == null) {
                    _error.postValue("Dữ liệu đăng nhập không hợp lệ.");
                    return;
                }

                tokenManager.saveAccessToken(data.accessToken);
                String role = tokenManager.getUserRole();
                data.userData.role = role;
                userRepository.setCurrentUser(data.userData);

                if (data.userData.isAdmin()) {
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

    // ========================================================
    // SỬA: THÊM PHƯƠNG THỨC REGISTER VÀO ĐÂY
    // ========================================================
    public void register(RegisterRequest request) {
        _loading.setValue(true);
        _error.setValue(null);

        // SỬA: Đổi kiểu Callback từ <String> thành <UserData>
        authRepository.register(request, new AuthRepository.Callback<UserData>() {
            @Override
            // SỬA: Tham số onSuccess bây giờ là một đối tượng UserData
            public void onSuccess(UserData createdUser) {
                _loading.postValue(false);
                // Sau khi đăng ký thành công, điều hướng người dùng về trang đăng nhập
                // để họ có thể tự đăng nhập lại.
                _navigationEvent.postValue(NavigationEvent.GO_TO_HOME); // Dùng GO_TO_HOME để trigger navigation về Login
            }

            @Override
            public void onError(String message) {
                _loading.postValue(false);
                _error.postValue(message);
            }
        });
    }
    // ========================================================

    public void onNavigationComplete() {
        _navigationEvent.setValue(NavigationEvent.STAY);
    }

    public LiveData<Boolean> getIsLoggedInState() {
        return authRepository.getIsLoggedInState();
    }

    public void logout() {
        authRepository.logout();
        userRepository.clearCurrentUser();
    }
}
