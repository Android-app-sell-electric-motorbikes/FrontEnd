package com.example.evshop.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager; // <-- ĐÃ THAY ĐỔI
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.data.Analytics;
import com.example.evshop.databinding.FragmentHomeBinding;
import com.example.evshop.ui.adapter.FeaturedVehicleAdapter; // <-- ĐÃ THAY ĐỔI
import com.example.evshop.ui.auth.AuthViewModel;
import com.example.evshop.ui.map.VietMapMapViewActivity;
import com.example.evshop.ui.vehicle.TemplateVehicleListActivity;
import com.example.evshop.ui.vehicle.VehicleDetailActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class HomeFragment extends Fragment {
    private FragmentHomeBinding b;
    private HomeViewModel vm;
    private AuthViewModel authViewModel;
    private FeaturedVehicleAdapter featuredVehicleAdapter; // <-- ĐÃ SỬA TÊN ADAPTER
    private BadgeDrawable cartBadge;
    private Handler bannerHandler;
    private Runnable bannerRunnable;
    private static final double STORE_LAT = 16.047079;
    private static final double STORE_LNG = 108.206230;


    @Inject
    Analytics analytics;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentHomeBinding.inflate(inflater, container, false);
        return b.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(this).get(HomeViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupToolbar();
        setupBanner();
        setupChips();
        setupFeaturedVehicleList(); // <-- ĐÃ ĐỔI TÊN HÀM CHO ĐÚNG
        setupSwipeRefresh();
        observeViewModel();
        vm.refresh();

        b.btnSignIn.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_loginFragment));
        b.btnSignUp.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_registerFragment));
        b.btnMap.setOnClickListener(v -> openVietMapActivity());
        b.chipUser.setOnClickListener(v -> openVietMapActivity());
        b.btnViewAllLoggedIn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TemplateVehicleListActivity.class);
            startActivity(intent);
        });

        authViewModel.isLoggedIn.observe(getViewLifecycleOwner(), this::updateUiBasedOnAuthState);
    }

    private void setupSwipeRefresh() {
        b.swipeRefresh.setOnRefreshListener(vm::refresh);
        authViewModel.isLoggedIn.observe(getViewLifecycleOwner(), isLoggedIn -> {
            b.swipeRefresh.setEnabled(isLoggedIn != null && isLoggedIn);
        });
    }

    // =================================================================================
    //  PHẦN CHỈNH SỬA QUAN TRỌNG NHẤT
    // =================================================================================
    private void setupFeaturedVehicleList() {
        // 1. Dùng LinearLayoutManager để hiển thị danh sách dọc
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        b.rvFeaturedVehicles.setLayoutManager(layoutManager);

        // 2. Tắt cuộn lồng nhau để toàn bộ màn hình cuộn mượt mà
        b.rvFeaturedVehicles.setNestedScrollingEnabled(false);

        // 3. Khởi tạo FeaturedVehicleAdapter
        featuredVehicleAdapter = new FeaturedVehicleAdapter(template -> {
            // ==========================================================
            // THAY ĐỔI LOGIC CLICK TẠI ĐÂY
            // ==========================================================

            // 1. Lấy Context một cách an toàn
            if (getContext() == null) {
                return;
            }

            // 2. Tạo một Intent để mở VehicleDetailActivity
            Intent intent = new Intent(getContext(), VehicleDetailActivity.class);

            // 3. Đặt ID của chiếc xe vào Intent. Dùng "VEHICLE_ID" làm chìa khóa (key).
            intent.putExtra("VEHICLE_ID", template.getId());

            // 4. Bắt đầu Activity mới
            startActivity(intent);

            // analytics.viewProduct(template.getId()); // Bạn có thể kích hoạt lại nếu cần
        });

        // 4. Gán adapter cho RecyclerView
        b.rvFeaturedVehicles.setAdapter(featuredVehicleAdapter);
    }

    private void observeViewModel() {
        // Lắng nghe LiveData chứa danh sách XE NỔI BẬT
        vm.featuredVehicles.observe(getViewLifecycleOwner(), vehicles -> {
            if (vehicles != null) {
                // Sử dụng phương thức setVehicles của FeaturedVehicleAdapter
                featuredVehicleAdapter.setVehicles(vehicles);
                b.tvFeaturedVehiclesTitle.setVisibility(vehicles.isEmpty() ? View.GONE : View.VISIBLE);
                b.rvFeaturedVehicles.setVisibility(vehicles.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        vm.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                b.swipeRefresh.setRefreshing(isLoading);
            }
        });

        vm.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null && error) {
                Toast.makeText(getContext(), "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUiBasedOnAuthState(Boolean isLoggedIn) {
        boolean loggedIn = isLoggedIn != null && isLoggedIn;

        b.panelAuth.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        b.chipUser.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        b.btnViewAllLoggedIn.setVisibility(loggedIn ? View.VISIBLE : View.GONE);

        if (loggedIn) {
            b.chipUser.setText(getString(R.string.welcome));
        }

        if (toolbar != null && toolbar.getMenu() != null) {
            MenuItem loginItem = toolbar.getMenu().findItem(R.id.login);
            MenuItem logoutItem = toolbar.getMenu().findItem(R.id.action_logout);
            if (loginItem != null) loginItem.setVisible(!loggedIn);
            if (logoutItem != null) logoutItem.setVisible(loggedIn);
        }
    }


    // ===================================================================
    // CÁC HÀM KHÁC GIỮ NGUYÊN (KHÔNG THAY ĐỔI)
    // ===================================================================

    private void openVietMapActivity() {
        if (getContext() == null) return;
        Intent i = new Intent(getContext(), VietMapMapViewActivity.class);
        i.putExtra("STORE_LAT", STORE_LAT);
        i.putExtra("STORE_LNG", STORE_LNG);
        startActivity(i);
    }

    private MaterialToolbar toolbar;

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    private void setupToolbar() {
        toolbar = requireActivity().findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }
        toolbar.setNavigationIcon(null);
        toolbar.setTitle(R.string.title_evshop);

        cartBadge = BadgeDrawable.create(requireContext());
        cartBadge.setNumber(0);
        cartBadge.setVisible(true);

        toolbar.post(() -> {
            if (toolbar.getMenu() != null && toolbar.getMenu().findItem(R.id.action_cart) != null) {
                try {
                    BadgeUtils.attachBadgeDrawable(cartBadge, toolbar, R.id.action_cart);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }


    private void toggleSearch() {
        int vis = (b.tilSearch.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE;
        b.tilSearch.setVisibility(vis);
        if (vis == View.VISIBLE) {
            b.etSearch.requestFocus();
            b.etSearch.setOnEditorActionListener((tv, actionId, event) -> {
                String q = tv.getText() != null ? tv.getText().toString() : "";
                vm.setQuery(q);
                return true;
            });
        }
    }

    private void setupBanner() {
        List<Integer> banners = Arrays.asList(
                R.drawable.banner_xe3,
                R.drawable.banner_xe5,
                R.drawable.banner_xe6
        );
        b.viewPager.setAdapter(new BannerAdapter(banners));

        bannerHandler = new Handler(Looper.getMainLooper());
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                if (b.viewPager.getAdapter() == null || b.viewPager.getAdapter().getItemCount() == 0)
                    return;
                int next = (b.viewPager.getCurrentItem() + 1) % b.viewPager.getAdapter().getItemCount();
                b.viewPager.setCurrentItem(next, true);
                bannerHandler.postDelayed(this, 3000);
            }
        };
    }

    private void setupChips() {
        String[] cats = {
                getString(R.string.chip_all),
                getString(R.string.chip_city),
                getString(R.string.chip_sport),
                getString(R.string.chip_offroad),
                getString(R.string.chip_eco)
        };
        b.chipGroup.setSingleSelection(true);
        for (int i = 0; i < cats.length; i++) {
            Chip chip = new Chip(requireContext());
            chip.setText(cats[i]);
            chip.setCheckable(true);
            if (i == 0) chip.setChecked(true);
            chip.setOnClickListener(v -> vm.setCategory(chip.getText().toString()));
            b.chipGroup.addView(chip);
        }
    }

    private void openFilterSheet() {
        if (getContext() == null) return;
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(getContext()).inflate(R.layout.sheet_filter_sort, null);
        dialog.setContentView(v);

        RadioGroup rg = v.findViewById(R.id.rgSort);
        rg.check(R.id.rbPopular);

        LinearLayout brandContainer = v.findViewById(R.id.brandContainer);
        String[] brands = {"VoltX", "EVM", "GreenGo", "Thunder", "EcoRide"};
        List<CheckBox> brandChecks = new ArrayList<>();
        for (String br : brands) {
            CheckBox cb = new CheckBox(getContext());
            cb.setText(br);
            brandContainer.addView(cb);
            brandChecks.add(cb);
        }

        SeekBar seekPrice = v.findViewById(R.id.seekPrice);
        TextView txtPrice = v.findViewById(R.id.txtPriceValue);
        seekPrice.setProgress(150);
        txtPrice.setText("≤ " + com.example.evshop.util.Formatters.currency(150_000_000L));
        seekPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtPrice.setText("≤ " + com.example.evshop.util.Formatters.currency(progress * 1_000_000L));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        SeekBar seekRating = v.findViewById(R.id.seekRating);
        TextView txtRating = v.findViewById(R.id.txtRatingMin);
        seekRating.setProgress(30);
        txtRating.setText("≥ 3.0");
        seekRating.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtRating.setText("≥ " + (progress / 10f));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        v.findViewById(R.id.btnCancel).setOnClickListener(btn -> dialog.dismiss());
        v.findViewById(R.id.btnApply).setOnClickListener(btn -> {
            HomeViewModel.Filters f = new HomeViewModel.Filters();
            int checked = rg.getCheckedRadioButtonId();

            if (checked == R.id.rbPriceAsc) f.sort = HomeViewModel.Filters.Sort.PRICE_ASC;
            else if (checked == R.id.rbPriceDesc) f.sort = HomeViewModel.Filters.Sort.PRICE_DESC;
            else if (checked == R.id.rbRating) f.sort = HomeViewModel.Filters.Sort.RATING;
            else f.sort = HomeViewModel.Filters.Sort.POPULAR;

            for (CheckBox cb : brandChecks)
                if (cb.isChecked()) f.brands.add(cb.getText().toString());
            f.maxPriceVnd = seekPrice.getProgress() * 1_000_000L;
            f.minRating = seekRating.getProgress() / 10f;

            vm.applyFilters(f);
            analytics.applyFilter("sort=" + f.sort + ", brands=" + f.brands + ", price<=" + f.maxPriceVnd + ", rating>=" + f.minRating);
            dialog.dismiss();
        });
    }


    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    private void incrementCartBadge() {
        if (cartBadge == null || toolbar == null) return;
        cartBadge.setNumber(cartBadge.getNumber() + 1);
        if (toolbar.getMenu() != null && toolbar.getMenu().findItem(R.id.action_cart) != null) {
            try {
                BadgeUtils.attachBadgeDrawable(cartBadge, toolbar, R.id.action_cart);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    @Override public void onResume() {
        super.onResume();
        if (toolbar != null) {
            toolbar.post(() -> {
                if (toolbar.getMenu() != null && toolbar.getMenu().findItem(R.id.action_cart) != null && cartBadge != null) {
                    try { BadgeUtils.attachBadgeDrawable(cartBadge, toolbar, R.id.action_cart); }
                    catch (Throwable t) { t.printStackTrace(); }
                }
            });
        }
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.postDelayed(bannerRunnable, 3000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }
}
