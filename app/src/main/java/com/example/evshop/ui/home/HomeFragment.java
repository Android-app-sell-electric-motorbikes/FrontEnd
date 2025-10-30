package com.example.evshop.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.databinding.FragmentHomeBinding;
import com.example.evshop.databinding.ItemBannerBinding;
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.ui.adapter.VehicleAdapter;
import com.example.evshop.ui.auth.AuthViewModel;
// *** BƯỚC 1: IMPORT MAINACTIVITY ***
import com.example.evshop.ui.main.MainActivity;
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
        observeHomeViewModel();
        observeLoginState(); // <-- Logic chính nằm ở đây
        setupClickListeners();

        homeViewModel.refresh();
    }

    // *** BƯỚC 2: CẬP NHẬT HÀM NÀY ***
    private void observeLoginState() {
        authViewModel.getIsLoggedInState().observe(getViewLifecycleOwner(), isLoggedIn -> {
            boolean loggedIn = isLoggedIn != null && isLoggedIn;

            // Cập nhật giao diện của HomeFragment
            b.panelAuth.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
            b.chipUser.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
            b.viewPager.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
            b.btnViewAllLoggedIn.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
            if (loggedIn) {
                b.chipUser.setText("Tài khoản");
            }

            // Ra lệnh cho MainActivity ẩn/hiện Toolbar
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showToolbar(loggedIn);
            }
        });
    }

    // *** BƯỚC 3: CẬP NHẬT HÀM NÀY ĐỂ ĐẢM BẢO AN TOÀN ***
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Khi rời khỏi HomeFragment, hãy đảm bảo Toolbar hiện lại
        // để không ảnh hưởng đến các màn hình khác.
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showToolbar(true);
        }
        b = null;
    }


    // --- CÁC HÀM CÒN LẠI GIỮ NGUYÊN, KHÔNG CẦN SỬA ---

    private void setupBanner() {
        List<Integer> bannerImages = Arrays.asList(
                R.drawable.banner_xe3,
                R.drawable.banner_xe6,
                R.drawable.banner_xe5
        );
        BannerAdapter bannerAdapter = new BannerAdapter(bannerImages);
        b.viewPager.setAdapter(bannerAdapter);
    }

    private void setupRecyclerView() {
        VehicleAdapter.OnVehicleClickListener listener = template -> {
            if (getContext() == null) return;
            Intent intent = new Intent(getContext(), VehicleDetailActivity.class);
            intent.putExtra("VEHICLE_ID", template.getId());
            startActivity(intent);
        };
        vehicleAdapter = new VehicleAdapter(listener);
        b.rvFeaturedVehicles.setLayoutManager(new LinearLayoutManager(getContext()));
        b.rvFeaturedVehicles.setAdapter(vehicleAdapter);
        b.rvFeaturedVehicles.setNestedScrollingEnabled(false);
    }

    private void observeHomeViewModel() {
        homeViewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) b.swipeRefresh.setRefreshing(isLoading);
        });
        homeViewModel.getFeaturedVehicles().observe(getViewLifecycleOwner(), vehicles -> {
            if (vehicles != null) vehicleAdapter.submitList(vehicles);
        });
        homeViewModel.error.observe(getViewLifecycleOwner(), hasError -> {
            if (hasError != null && hasError) Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupClickListeners() {
        b.swipeRefresh.setOnRefreshListener(() -> homeViewModel.refresh());
        b.btnSignIn.setOnClickListener(v -> navController.navigate(R.id.action_homeFragment_to_loginFragment));
        b.btnSignUp.setOnClickListener(v -> navController.navigate(R.id.action_homeFragment_to_registerFragment));
        b.chipUser.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), b.chipUser);
            popupMenu.getMenu().add("Tài khoản của tôi");
            popupMenu.getMenu().add("Đăng xuất");
            popupMenu.setOnMenuItemClickListener(item -> {
                if ("Đăng xuất".equals(item.getTitle().toString())) {
                    authViewModel.logout();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
        b.btnViewAllLoggedIn.setOnClickListener(v -> {
            if (getContext() == null) return;
            Intent intent = new Intent(getContext(), TemplateVehicleListActivity.class);
            startActivity(intent);
        });
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
