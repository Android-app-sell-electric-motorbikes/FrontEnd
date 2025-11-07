package com.example.evshop.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.evshop.databinding.FragmentRegisterBinding;
import com.example.evshop.ui.main.MainActivity;
import com.google.android.material.snackbar.Snackbar;
import dagger.hilt.android.AndroidEntryPoint;
import com.example.evshop.R;
@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    // private RegisterViewModel vm; // Sẽ mở ra khi có ViewModel

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // vm = new ViewModelProvider(this).get(RegisterViewModel.class); // Sẽ mở ra khi có ViewModel

        // Dòng mã gây lỗi đã được xóa. MainActivity sẽ tự động quản lý toolbar.

        // Sự kiện nhấn nút đăng ký
        binding.btnRegister.setOnClickListener(v -> {
            String fullName = String.valueOf(binding.etFullName.getText());
            String email = String.valueOf(binding.etEmail.getText());
            String password = String.valueOf(binding.etPassword.getText());
            String confirmPassword = String.valueOf(binding.etConfirmPassword.getText());

            // TODO: Khi có ViewModel, gọi vm.register(...)
            // Hiện tại chỉ hiển thị thông báo giả
            Snackbar.make(binding.getRoot(), "Chức năng đăng ký sẽ sớm được cập nhật!", Snackbar.LENGTH_SHORT).show();
        });

        // Sự kiện nhấn nút "Quay về đăng nhập"
        binding.tvGoToLogin.setOnClickListener(v -> {
            // Sử dụng action đã định nghĩa để quay về màn hình Login một cách tường minh
            NavHostFragment.findNavController(this).navigate(R.id.action_registerFragment_to_loginFragment);
        });


        // TODO: Lắng nghe trạng thái từ ViewModel để xử lý loading, error, success
        // vm.getState().observe(getViewLifecycleOwner(), state -> { ... });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Quan trọng để tránh rò rỉ bộ nhớ
    }
}