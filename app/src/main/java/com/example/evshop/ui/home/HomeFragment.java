package com.example.evshop.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.example.evshop.ui.map.VietMapMapViewActivity;
import com.example.evshop.ui.vehicle.TemplateVehicleListActivity;
import com.example.evshop.ui.vehicle.VehicleDetailActivity;

import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding b;
    private AuthViewModel authViewModel;
    private NavController navController;
    private HomeViewModel homeViewModel;
    private VehicleAdapter vehicleAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentHomeBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupBanner();
        setupRecyclerView();
        setupClickListeners();
        observeLoginState();
        observeHomeViewModel();

        homeViewModel.refresh();
    }

    private void observeLoginState() {
        authViewModel.getIsLoggedInState().observe(getViewLifecycleOwner(), isLoggedIn -> {
            if (b == null) return;
            boolean loggedIn = isLoggedIn != null && isLoggedIn;

            b.panelAuth.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
            b.tvWelcome.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
            b.btnLogout.setVisibility(loggedIn ? View.VISIBLE : View.GONE);

            if (loggedIn) {
                UserData user = authViewModel.getCurrentUser().getValue();
                if (user != null) {
                    b.tvWelcome.setText("Chào, " + user.fullName + "!");
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }

    private void setupBanner() {
        if (b == null) return;
        List<Integer> bannerImages = Arrays.asList(R.drawable.banner_xe3, R.drawable.banner_xe6, R.drawable.banner_xe5);
        BannerAdapter bannerAdapter = new BannerAdapter(bannerImages);
        b.viewPager.setAdapter(bannerAdapter);
    }

    private void setupRecyclerView() {
        if (b == null) return;
        vehicleAdapter = new VehicleAdapter(template -> {
            if (getContext() == null) return;
            Intent intent = new Intent(getContext(), VehicleDetailActivity.class);
            intent.putExtra("VEHICLE_ID", template.getId());
            startActivity(intent);
        });
        b.rvFeaturedVehicles.setLayoutManager(new GridLayoutManager(getContext(), 2));
        b.rvFeaturedVehicles.setAdapter(vehicleAdapter);
        b.rvFeaturedVehicles.setNestedScrollingEnabled(false);
    }

    private void observeHomeViewModel() {
        if (b == null) return;
        homeViewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (b.swipeRefresh != null && isLoading != null) {
                b.swipeRefresh.setRefreshing(isLoading);
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
        if (b == null) return;

        b.swipeRefresh.setOnRefreshListener(() -> homeViewModel.refresh());

        b.btnSignIn.setOnClickListener(v -> navController.navigate(R.id.action_homeFragment_to_loginFragment));
        b.btnSignUp.setOnClickListener(v -> navController.navigate(R.id.action_homeFragment_to_registerFragment));

        b.btnLogout.setOnClickListener(v -> logout());

        b.btnViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), TemplateVehicleListActivity.class);
            startActivity(intent);
        });

        b.btnMap.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), VietMapMapViewActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_account_menu) {
            View menuItemView = requireActivity().findViewById(R.id.action_account_menu);
            if (menuItemView != null) {
                showUserPopupMenu(menuItemView);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUserPopupMenu(View anchorView) {
        if (getContext() == null) return;
        PopupMenu popupMenu = new PopupMenu(getContext(), anchorView);
        popupMenu.getMenu().add("Tài khoản của tôi");
        popupMenu.getMenu().add("Đăng xuất");

        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if ("Tài khoản của tôi".equals(title)) {
                showUserProfileDialog();
                return true;
            } else if ("Đăng xuất".equals(title)) {
                logout();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showUserProfileDialog() {
        if (getContext() == null) return;
        UserData currentUser = authViewModel.getCurrentUser().getValue();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Tên: ").append(currentUser.fullName).append("\n\n");
        messageBuilder.append("Email: ").append(currentUser.email).append("\n\n");
        String roleString = "N/A";
        if (currentUser.role != null && !currentUser.role.isEmpty()) {
            roleString = currentUser.role;
        }
        messageBuilder.append("Vai trò: ").append(roleString);
        new AlertDialog.Builder(getContext())
                .setTitle("Thông tin Tài khoản")
                .setMessage(messageBuilder.toString())
                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void logout() {
        authViewModel.logout();
        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    public static class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
        private final List<Integer> images;
        public BannerAdapter(List<Integer> images) { this.images = images; }
        @NonNull @Override
        public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BannerViewHolder(ItemBannerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
            holder.binding.imgBanner.setImageResource(images.get(position));
        }
        @Override public int getItemCount() { return images.size(); }
        static class BannerViewHolder extends RecyclerView.ViewHolder {
            ItemBannerBinding binding;
            public BannerViewHolder(ItemBannerBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}