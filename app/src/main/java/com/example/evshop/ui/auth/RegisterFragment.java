package com.example.evshop.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evshop.R;
import com.example.evshop.databinding.FragmentRegisterBinding;
import com.example.evshop.domain.models.RegisterRequest;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding b;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentRegisterBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        b.btnRegister.setOnClickListener(v -> handleRegistration());

        b.tvGoToLogin.setOnClickListener(v -> 
            NavHostFragment.findNavController(this).navigate(R.id.action_registerFragment_to_loginFragment)
        );

        observeViewModel();
    }

    private void handleRegistration() {
        String username = b.etUsername.getText().toString().trim();
        String password = b.etPassword.getText().toString().trim();
        String confirmPassword = b.etConfirmPassword.getText().toString().trim();
        String email = b.etEmail.getText().toString().trim();
        String phone = b.etPhoneNumber.getText().toString().trim();
        String address = b.etAddress.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            showError("Tên đăng nhập và mật khẩu là bắt buộc");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu không khớp");
            return;
        }

        RegisterRequest request = new RegisterRequest(username, password, email, phone, address);
        authViewModel.register(request);
    }

    private void observeViewModel() {
        authViewModel._loading.observe(getViewLifecycleOwner(), isLoading -> {
            b.loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            b.btnRegister.setEnabled(!isLoading);
        });

        authViewModel._error.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showError(error);
                authViewModel._error.postValue(null); // Reset lỗi sau khi hiển thị
            }
        });

        authViewModel.getNavigationEvent().observe(getViewLifecycleOwner(), event -> {
            if (event == AuthViewModel.NavigationEvent.GO_TO_HOME) {
                // Sau khi đăng ký thành công, điều hướng về trang đăng nhập
                NavHostFragment.findNavController(this).navigate(R.id.action_registerFragment_to_loginFragment);
                authViewModel.onNavigationComplete();
            }
        });
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}