package com.example.evshop.ui.auth;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evshop.databinding.FragmentLoginBinding;
import com.google.android.material.snackbar.Snackbar;

import javax.annotation.Nullable;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {
    private FragmentLoginBinding b;
    private LoginViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentLoginBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(this).get(LoginViewModel.class);

        b.btnLogin.setOnClickListener(v -> {
            String email = String.valueOf(b.etEmail.getText());
            String pass  = String.valueOf(b.etPassword.getText());
            vm.login(email, pass);
        });

        vm.getState().observe(getViewLifecycleOwner(), st -> {
            // loading
            b.loading.setVisibility(st.loading ? View.VISIBLE : View.GONE);
            b.btnLogin.setEnabled(!st.loading);
            b.etEmail.setEnabled(!st.loading);
            b.etPassword.setEnabled(!st.loading);

            // error -> Snackbar
            if (st.error != null){
                Snackbar.make(b.getRoot(), st.error, Snackbar.LENGTH_LONG)
                        .setAnchorView(b.btnLogin).show();
            }

            // success -> quay về home (hoặc close activity chứa fragment)
            if (st.success){
                Snackbar.make(b.getRoot(), "Đăng nhập thành công", Snackbar.LENGTH_SHORT)
                        .setAnchorView(b.btnLogin).show();
                NavHostFragment.findNavController(this).navigateUp(); // hoặc navigate đến Home
            }
        });
    }

    @Override public void onDestroyView() { super.onDestroyView(); b = null; }
}
