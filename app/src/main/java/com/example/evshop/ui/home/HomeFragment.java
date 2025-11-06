package com.example.evshop.ui.home;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.databinding.FragmentHomeBinding;
import com.example.evshop.databinding.ItemBannerBinding;
import com.example.evshop.domain.models.UserData;
import com.example.evshop.ui.adapter.VehicleAdapter;
import com.example.evshop.ui.auth.AuthViewModel;
import com.example.evshop.ui.chat.ChatActivity;
import com.example.evshop.ui.map.VietMapMapViewActivity;
import com.example.evshop.ui.vehicle.TemplateVehicleListActivity;
import com.example.evshop.ui.vehicle.VehicleDetailActivity;

import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private AuthViewModel authViewModel;
    private HomeViewModel homeViewModel;
    private NavController navController;
    private VehicleAdapter vehicleAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupBanner();
        setupRecyclerView();
        setupClickListeners();
        observeLoginState();
        observeHomeViewModel();

        homeViewModel.refresh();
    }

    private void observeLoginState() {
        authViewModel.getIsLoggedInState().observe(getViewLifecycleOwner(), isLoggedIn -> {
            boolean loggedIn = isLoggedIn != null && isLoggedIn;

            binding.panelAuth.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
            binding.tvWelcome.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
            binding.btnLogout.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
            binding.fabChat.setVisibility(loggedIn ? View.VISIBLE : View.GONE);

            if (loggedIn) {
                UserData user = authViewModel.getCurrentUser().getValue();
                if (user != null) {
                    binding.tvWelcome.setText("Chào, " + user.fullName + "!");
                }
            }
        });
    }

    private void setupBanner() {
        List<Integer> bannerImages = Arrays.asList(R.drawable.banner_xe3, R.drawable.banner_xe6, R.drawable.banner_xe5);
        BannerAdapter bannerAdapter = new BannerAdapter(bannerImages);
        binding.viewPager.setAdapter(bannerAdapter);
    }

    private void setupRecyclerView() {
        vehicleAdapter = new VehicleAdapter(template -> {
            Intent intent = new Intent(getContext(), VehicleDetailActivity.class);
            intent.putExtra("VEHICLE_ID", template.getId());
            startActivity(intent);
        });

        binding.rvFeaturedVehicles.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvFeaturedVehicles.setAdapter(vehicleAdapter);
        binding.rvFeaturedVehicles.setNestedScrollingEnabled(false);
    }

    private void observeHomeViewModel() {
        homeViewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (binding.swipeRefresh != null && isLoading != null) {
                binding.swipeRefresh.setRefreshing(isLoading);
            }
        });

        homeViewModel.getFeaturedVehicles().observe(getViewLifecycleOwner(), vehicles -> {
            if (vehicles != null) {
                vehicleAdapter.submitList(vehicles);
            }
        });

        homeViewModel.error.observe(getViewLifecycleOwner(), hasError -> {
            if (hasError != null && hasError) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.swipeRefresh.setOnRefreshListener(homeViewModel::refresh);

        binding.btnSignIn.setOnClickListener(v -> navController.navigate(R.id.action_homeFragment_to_loginFragment));
        binding.btnSignUp.setOnClickListener(v -> navController.navigate(R.id.action_homeFragment_to_registerFragment));
        binding.btnLogout.setOnClickListener(v -> logout());

        binding.btnViewAll.setOnClickListener(v -> startActivity(new Intent(getContext(), TemplateVehicleListActivity.class)));
        binding.btnMap.setOnClickListener(v -> startActivity(new Intent(getContext(), VietMapMapViewActivity.class)));

        binding.fabChat.setOnClickListener(v -> {
            Boolean isLoggedIn = authViewModel.getIsLoggedInState().getValue();
            if (isLoggedIn != null && isLoggedIn) {
                startActivity(new Intent(getContext(), ChatActivity.class));
            } else {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để chat với cửa hàng", Toast.LENGTH_SHORT).show();
                navController.navigate(R.id.action_homeFragment_to_loginFragment);
            }
        });
    }

    private void logout() {
        authViewModel.logout();
        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // --- Banner Adapter ---
    public static class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

        private final List<Integer> images;

        public BannerAdapter(List<Integer> images) {
            this.images = images;
        }

        @NonNull
        @Override
        public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemBannerBinding itemBinding = ItemBannerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new BannerViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
            holder.binding.imgBanner.setImageResource(images.get(position));
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        static class BannerViewHolder extends RecyclerView.ViewHolder {
            ItemBannerBinding binding;

            public BannerViewHolder(ItemBannerBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
