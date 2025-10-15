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
import com.example.evshop.data.TokenManager;
import com.example.evshop.databinding.FragmentHomeBinding;
import com.example.evshop.ui.auth.LoginActivity;
import com.example.evshop.ui.map.VietMapMapViewActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.badge.BadgeUtils;

import java.util.*;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class HomeFragment extends Fragment {
    private FragmentHomeBinding b;
    private HomeViewModel vm;
    private ProductAdapter adapter;
    private BadgeDrawable cartBadge;
    private Handler bannerHandler;
    private Runnable bannerRunnable;
    private static final double STORE_LAT = 16.047079;  // ví dụ Đà Nẵng
    private static final double STORE_LNG = 108.206230;


    @Inject
    Analytics analytics;
    @Inject
    TokenManager tokenManager;


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
        MaterialButton btnMap = view.findViewById(R.id.btnMap);
        Chip chipUser = view.findViewById(R.id.chipUser);
        setupToolbar();
        setupBanner();
        setupChips();
        setupGrid();
        setupSwipe();
        observe();
        vm.refresh();

        View.OnClickListener openMap = v -> openVietMapActivity();

        btnMap.setOnClickListener(openMap);
        chipUser.setOnClickListener(openMap);
        b.btnSignIn.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_loginFragment));

        updateAuthUi();
        toggleSearch();
        openFilterSheet();
    }

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
            // Không có toolbar -> bỏ qua tất cả, tránh crash
            return;
        }
        toolbar.setNavigationIcon(null);
        toolbar.setTitle(R.string.title_evshop);

        Menu menu = toolbar.getMenu();
        if (menu != null) {
            MenuItem loginItem = menu.findItem(R.id.login); // id của menu
            if (loginItem != null) {
                loginItem.setOnMenuItemClickListener(mi -> {
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                    return true;
                });
            }
        }

        cartBadge = BadgeDrawable.create(requireContext());
        cartBadge.setNumber(0);
        cartBadge.setVisible(true);
        // Gắn badge vào icon menu "cart"
        // (dùng post() để chắc chắn menu đã inflate xong)
        toolbar.post(() -> {
            if (toolbar.getMenu() != null && toolbar.getMenu().findItem(R.id.action_cart) != null) {
                try {
                    BadgeUtils.attachBadgeDrawable(cartBadge, toolbar, R.id.action_cart);
                } catch (Throwable t) {
                    // Không cho app crash
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

    private void setupGrid() {
        GridLayoutManager glm = new GridLayoutManager(getContext(), 2);
        b.rvProducts.setLayoutManager(glm);

        adapter = new ProductAdapter(p -> {
            analytics.viewProduct(p.getId());
            Toast.makeText(getContext(), "Xem " + p.getName(), Toast.LENGTH_SHORT).show();
            // TODO: Nav to product detail when available
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

    private void updateAuthUi() {
        String token = tokenManager != null ? tokenManager.getAccessToken() : null;
        boolean loggedIn = token != null;

        b.panelAuth.setVisibility(loggedIn ? View.GONE : View.VISIBLE);

        if (loggedIn) {
            String name = com.example.evshop.util.JwtUtils.getDisplayName(token);
            b.chipUser.setText(name != null && !name.isEmpty()
                    ? "Xin chào, " + name
                    : getString(R.string.welcome));
            b.chipUser.setVisibility(View.VISIBLE);
        } else {
            b.chipUser.setVisibility(View.GONE);
        }

        if (toolbar != null && toolbar.getMenu() != null) {
            MenuItem loginItem = toolbar.getMenu().findItem(R.id.login);
            if (loginItem != null) loginItem.setVisible(!loggedIn);
        }
    }

    private void openFilterSheet() {
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

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
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

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
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

        dialog.show();
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    private void incrementCartBadge() {
        if (cartBadge == null || toolbar == null) return;
        cartBadge.setNumber(cartBadge.getNumber() + 1);
        // Bảo đảm đang được attach (không crash nếu thiếu cart)
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
        updateAuthUi();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }
}
