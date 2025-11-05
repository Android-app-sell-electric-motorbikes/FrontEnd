package com.example.evshop.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evshop.R;
import com.example.evshop.databinding.FragmentLoginBinding;
import com.example.evshop.ui.admin.AdminActivity;
import com.example.evshop.ui.main.MainActivity;
import com.google.android.material.snackbar.Snackbar;

import javax.annotation.Nullable;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {
    private FragmentLoginBinding b;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentLoginBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // *** THAY ĐỔI CỰC KỲ QUAN TRỌNG Ở ĐÂY ***
        // Lấy ViewModel được chia sẻ từ Activity, thay vì tạo mới.
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showToolbarItems(false);
        }

        b.btnLogin.setOnClickListener(v -> {
            String email = String.valueOf(b.etEmail.getText());
            String pass = String.valueOf(b.etPassword.getText());
            authViewModel.login(email, pass);
        });

        b.tvSignup.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_loginFragment_to_registerFragment);
        });

        observeViewModelStates();
        observeNavigationEvents();
    }

    private void observeViewModelStates() {
        authViewModel._loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                b.loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                b.btnLogin.setEnabled(!isLoading);
                b.etEmail.setEnabled(!isLoading);
                b.etPassword.setEnabled(!isLoading);
            }
        });

        authViewModel._error.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(b.getRoot(), error, Snackbar.LENGTH_LONG)
                        .setAnchorView(b.btnLogin).show();
                authViewModel._error.setValue(null);
            }
        });
    }

    private void observeNavigationEvents() {
        authViewModel.getNavigationEvent().observe(getViewLifecycleOwner(), event -> {
            if (event == null || event == AuthViewModel.NavigationEvent.STAY) {
                return;
            }

            switch (event) {
                case GO_TO_ADMIN:
                    Snackbar.make(b.getRoot(), "Đăng nhập với quyền Admin thành công", Snackbar.LENGTH_SHORT)
                            .setAnchorView(b.btnLogin).show();

                    Intent adminIntent = new Intent(getActivity(), AdminActivity.class);
                    adminIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(adminIntent);
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                    break;

                case GO_TO_HOME:
                Snackbar.make(b.getRoot(), "Đăng nhập thành công", Snackbar.LENGTH_SHORT)
                        .setAnchorView(b.btnLogin).show();

                // ========================================================
                // ***           THAY ĐỔI CỐT LÕI NẰM Ở ĐÂY           ***
                // ========================================================
                // THAY THẾ: NavHostFragment.findNavController(this).navigateUp();
                // BẰNG LỆNH MỚI:
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_loginFragment_to_homeFragment);
                break;
            }

            authViewModel.onNavigationComplete();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
