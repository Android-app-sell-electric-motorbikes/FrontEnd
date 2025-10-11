package com.example.evshop.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.auth.AuthRepository;
import com.example.evshop.domain.models.LoginResult;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class LoginViewModel extends ViewModel {
    public static class UiState {
        public final boolean loading;
        public final String error;
        public final boolean success;
        private UiState(boolean l, String e, boolean s){ loading=l; error=e; success=s; }
        public static UiState idle(){ return new UiState(false,null,false); }
        public static UiState loading(){ return new UiState(true,null,false); }
        public static UiState success(){ return new UiState(false,null,true); }
        public static UiState error(String m){ return new UiState(false,m,false); }
    }

    private final AuthRepository repo;
    private final MutableLiveData<UiState> state = new MutableLiveData<>(UiState.idle());

    @Inject
    public LoginViewModel(AuthRepository repo) {
        this.repo = repo;
    }

    public LiveData<UiState> getState(){ return state; }

    public void login(String email, String pass){
        if (email==null || email.trim().isEmpty()){
            state.setValue(UiState.error("Vui lòng nhập email"));
            return;
        }
        if (pass==null || pass.trim().isEmpty()){
            state.setValue(UiState.error("Vui lòng nhập mật khẩu"));
            return;
        }
        state.setValue(UiState.loading());
        repo.login(email.trim(), pass.trim(), new AuthRepository.Callback<LoginResult>() {
            @Override public void onSuccess(LoginResult data) {
                state.postValue(UiState.success());
            }
            @Override public void onError(String message) {
                state.postValue(UiState.error("Đăng nhập thất bại: " + message));
            }
        });
    }
}
