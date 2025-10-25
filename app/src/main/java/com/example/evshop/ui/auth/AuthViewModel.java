package com.example.evshop.ui.auth;

// D:/PRM/FrontEnd/app/src/main/java/com/example/evshop/ui/auth/AuthViewModel.javapackage com.example.evshop.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import javax.inject.Inject;
import com.example.evshop.data.auth.AuthRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    // LiveData này sẽ được các Fragment/Activity khác lắng nghe
    public final LiveData<Boolean> isLoggedIn;

    @Inject
    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
        // Lấy trạng thái đăng nhập từ Repository
        this.isLoggedIn = authRepository.getIsLoggedInState();
    }

    public void logout() {
        authRepository.logout();
    }
}
