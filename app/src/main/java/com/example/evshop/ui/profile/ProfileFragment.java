package com.example.evshop.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.evshop.databinding.FragmentProfileBinding;
import com.example.evshop.ui.auth.AuthViewModel;
import com.example.evshop.ui.auth.LoginActivity;
import com.example.evshop.ui.map.VietMapMapViewActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;

    private static final double STORE_LAT = 16.047079;
    private static final double STORE_LNG = 108.206230;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupUI();
        setupClickListeners();
    }

    private void setupUI() {
        // Có thể update thông tin user từ ViewModel sau
        binding.tvUserName.setText("Người dùng test");
        binding.tvUserEmail.setText("test@evshop.com");
    }

    private void setupClickListeners() {
        // Thông tin cá nhân
        binding.cardPersonalInfo.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Đơn hàng
        binding.cardOrders.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Xem bản đồ
        binding.cardMap.setOnClickListener(v -> {
            openMap();
        });

        // Đăng xuất
        binding.cardLogout.setOnClickListener(v -> {
            logout();
        });
    }

    private void openMap() {
        if (getContext() == null) return;
        Intent intent = new Intent(getContext(), VietMapMapViewActivity.class);
        intent.putExtra("STORE_LAT", STORE_LAT);
        intent.putExtra("STORE_LNG", STORE_LNG);
        startActivity(intent);
    }

    private void logout() {
        authViewModel.logout();
        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        
        // Chuyển về màn hình login
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

