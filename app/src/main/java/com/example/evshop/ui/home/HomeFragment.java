package com.example.evshop.ui.home;

import android.content.Intent;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.data.Analytics;
import com.example.evshop.data.HomeRepository;
// import com.example.evshop.data.TokenManager; // <-- KHÔNG CẦN DÙNG TRỰC TIẾP NỮA
import com.example.evshop.databinding.FragmentHomeBinding;
import com.example.evshop.ui.auth.AuthViewModel; // <-- **BƯỚC 1: IMPORT AUTHVIEWMODEL**
import com.example.evshop.ui.map.VietMapMapViewActivity;
import com.example.evshop.ui.vehicle.TemplateVehicleListActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;

import java.util.*;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class HomeFragment extends Fragment {
    private FragmentHomeBinding b;
    private HomeViewModel vm;
    private AuthViewModel authViewModel; // <-- **BƯỚC 2: KHAI BÁO AUTHVIEWMODEL**
    private ProductAdapter adapter;
    private Handler bannerHandler;
    private Runnable bannerRunnable;
    private static final double STORE_LAT = 16.047079;
    private static final double STORE_LNG = 108.206230;


    @Inject
    Analytics analytics;
    // @Inject TokenManager tokenManager; // <-- KHÔNG CẦN INJECT TOKENMANAGER NỮA

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentHomeBinding.inflate(inflater, container, false);
        return b.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cho phép Fragment xử lý menu items
        setHasOptionsMenu(true);

        // --- **BƯỚC 3: KHỞI TẠO CÁC VIEWMODEL** ---
        vm = new ViewModelProvider(this).get(HomeViewModel.class);
        // Lấy AuthViewModel được chia sẻ từ Activity
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // -- Giữ lại các setup cũ của bạn --
        setupToolbar();
        setupBanner();
        setupChips();
        setupGrid();
        setupSwipe();
        observe(); // Observe HomeViewModel
        vm.refresh();

        // Gán sự kiện cho các nút
        b.btnSignIn.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_loginFragment));
        b.btnSignUp.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_registerFragment)); // Đi đến login trước
        b.btnMap.setOnClickListener(v -> openVietMapActivity());
        
        // Profile chip - hiển thị menu profile
        b.chipUser.setOnClickListener(v -> showProfileMenu());
        
        b.btnViewAllLoggedIn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TemplateVehicleListActivity.class);
            startActivity(intent);
        });


        // --- **BƯỚC 4: LẮNG NGHE TRẠNG THÁI ĐĂNG NHẬP** ---
        // Hàm observe() này sẽ thay thế cho việc gọi updateAuthUi() thủ công
        authViewModel.isLoggedIn.observe(getViewLifecycleOwner(), this::updateUiBasedOnAuthState);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.action_search) {
            toggleSearch();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    // --- **BƯỚC 5: HÀM CẬP NHẬT UI MỚI, THAY THẾ updateAuthUi()** ---
    private void updateUiBasedOnAuthState(Boolean isLoggedIn) {
        boolean loggedIn = isLoggedIn != null && isLoggedIn;

        // Toggle giữa 2 panels
        b.panelAuth.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        b.panelLoggedIn.setVisibility(loggedIn ? View.VISIBLE : View.GONE);

        if (loggedIn) {
            // TODO: Nâng cấp để lấy tên người dùng từ một nguồn đáng tin cậy hơn (ví dụ: một User object trong AuthViewModel)
            // Hiện tại, có thể tạm thời hiển thị một chuỗi chào mừng chung.
            b.chipUser.setText(getString(R.string.welcome));
        }
    }


    // ===================================================================
    // CÁC HÀM KHÁC CỦA BẠN (GIỮ NGUYÊN, KHÔNG CẦN THAY ĐỔI)
    // ===================================================================

    private void openVietMapActivity() {
        if (getContext() == null) return;
        Intent i = new Intent(getContext(), VietMapMapViewActivity.class);
        i.putExtra("STORE_LAT", STORE_LAT);
        i.putExtra("STORE_LNG", STORE_LNG);
        startActivity(i);
    }

    private MaterialToolbar toolbar;

    private void setupToolbar() {
        toolbar = requireActivity().findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }
        toolbar.setNavigationIcon(null);
        toolbar.setTitle(R.string.title_evshop);
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

    private void setupGrid() {
        GridLayoutManager glm = new GridLayoutManager(getContext(), 2);
        b.rvProducts.setLayoutManager(glm);

        adapter = new ProductAdapter(p -> {
            analytics.viewProduct(p.getId());
            Toast.makeText(getContext(), "Xem " + p.getName(), Toast.LENGTH_SHORT).show();
            // TODO: Nav to product detail when available
            Intent intent = new Intent(requireContext(), com.example.evshop.ui.ProductDetailsActivity.class);
            intent.putExtra("product_name", p.getName());
            intent.putExtra("product_price", p.getPriceVnd());
            intent.putExtra("product_image", p.getImageUrl());
            startActivity(intent);

        });
        b.rvProducts.setAdapter(adapter);

        b.rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                if (dy > 0) {
                    int visible = glm.getChildCount();
                    int total = glm.getItemCount();
                    int first = glm.findFirstVisibleItemPosition();
                    if (visible + first >= total - 2) {
                        vm.loadMore();
                    }
                }
            }
        });
    }

    private void setupSwipe() {
        b.swipeRefresh.setOnRefreshListener(vm::refresh);
    }

    private void observe() {
        vm.items.observe(getViewLifecycleOwner(), list -> {
            adapter.submit(list);
            b.swipeRefresh.setRefreshing(false);
        });
        vm.loading.observe(getViewLifecycleOwner(), isLoading -> {
            adapter.setLoading(Boolean.TRUE.equals(isLoading));
            b.swipeRefresh.setRefreshing(Boolean.TRUE.equals(isLoading));
        });
        vm.error.observe(getViewLifecycleOwner(), isError -> {
            adapter.setError(Boolean.TRUE.equals(isError), vm::refresh);
        });
    }

    private void openFilterSheet() {
        if (getContext() == null) return;
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(getContext()).inflate(R.layout.sheet_filter_sort, null);
        dialog.setContentView(v);

        // Sort
        RadioGroup rg = v.findViewById(R.id.rgSort);
        rg.check(R.id.rbPopular);

        // Brands (mock)
        LinearLayout brandContainer = v.findViewById(R.id.brandContainer);
        String[] brands = {"VoltX", "EVM", "GreenGo", "Thunder", "EcoRide"};
        List<CheckBox> brandChecks = new ArrayList<>();
        for (String br : brands) {
            CheckBox cb = new CheckBox(getContext());
            cb.setText(br);
            brandContainer.addView(cb);
            brandChecks.add(cb);
        }

        // Price
        SeekBar seekPrice = v.findViewById(R.id.seekPrice);
        TextView txtPrice = v.findViewById(R.id.txtPriceValue);
        seekPrice.setProgress(150);
        txtPrice.setText("≤ " + com.example.evshop.util.Formatters.currency(150_000_000));
        seekPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtPrice.setText("≤ " + com.example.evshop.util.Formatters.currency(progress * 1_000_000L));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Rating
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
            HomeRepository.Filters f = new HomeRepository.Filters();
            int checked = rg.getCheckedRadioButtonId();
            if (checked == R.id.rbPriceAsc) f.sort = HomeRepository.Filters.Sort.PRICE_ASC;
            else if (checked == R.id.rbPriceDesc) f.sort = HomeRepository.Filters.Sort.PRICE_DESC;
            else if (checked == R.id.rbRating) f.sort = HomeRepository.Filters.Sort.RATING;
            else f.sort = HomeRepository.Filters.Sort.POPULAR;

            for (CheckBox cb : brandChecks)
                if (cb.isChecked()) f.brands.add(cb.getText().toString());
            f.maxPriceVnd = seekPrice.getProgress() * 1_000_000L;
            f.minRating = seekRating.getProgress() / 10f;

            vm.applyFilters(f);
            analytics.applyFilter("sort=" + f.sort + ", brands=" + f.brands + ", price<=" + f.maxPriceVnd + ", rating>=" + f.minRating);
            dialog.dismiss();
        });

        // Hiển thị dialog
        dialog.show();
    }

    private void showProfileMenu() {
        if (getContext() == null) return;
        
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 48);
        
        // Tiêu đề
        TextView title = new TextView(getContext());
        title.setText("Tài khoản");
        title.setTextSize(22);
        title.setTextColor(getResources().getColor(R.color.black, null));
        title.setPadding(0, 0, 0, 32);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(title);
        
        // Nút View Profile (tạm thời)
        com.google.android.material.button.MaterialButton btnProfile = 
            new com.google.android.material.button.MaterialButton(getContext());
        btnProfile.setText("Thông tin cá nhân");
        btnProfile.setIcon(getResources().getDrawable(R.drawable.ic_round_person_24, null));
        btnProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.setMargins(0, 0, 0, 16);
        btnProfile.setLayoutParams(btnParams);
        layout.addView(btnProfile);
        
        // Nút View Map
        com.google.android.material.button.MaterialButton btnMap = 
            new com.google.android.material.button.MaterialButton(getContext());
        btnMap.setText("Xem bản đồ");
        btnMap.setIcon(getResources().getDrawable(R.drawable.ic_round_map_24, null));
        btnMap.setOnClickListener(v -> {
            openVietMapActivity();
            dialog.dismiss();
        });
        btnMap.setLayoutParams(btnParams);
        layout.addView(btnMap);
        
        // Nút Logout
        com.google.android.material.button.MaterialButton btnLogout = 
            new com.google.android.material.button.MaterialButton(getContext());
        btnLogout.setText("Đăng xuất");
        btnLogout.setIcon(getResources().getDrawable(R.drawable.ic_logout, null));
        btnLogout.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
        btnLogout.setIconTint(android.content.res.ColorStateList.valueOf(
            getResources().getColor(android.R.color.holo_red_dark, null)));
        btnLogout.setOnClickListener(v -> {
            authViewModel.logout();
            Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        btnLogout.setLayoutParams(btnParams);
        layout.addView(btnLogout);
        
        dialog.setContentView(layout);
        dialog.show();
    }

    @Override 
    public void onResume() {
        super.onResume();
        // Banner auto-scroll
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
