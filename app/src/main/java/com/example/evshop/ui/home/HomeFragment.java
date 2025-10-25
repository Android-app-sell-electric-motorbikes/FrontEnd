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
import com.example.evshop.ui.NotificationActivity;
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
        setHasOptionsMenu(true);  // Bật menu cho fragment
        vm = new ViewModelProvider(this).get(HomeViewModel.class);
        setupToolbar();
        setupBanner();
        setupChips();
        setupGrid();
        setupSwipe();
        setupQuickActions();
        observe();
        vm.refresh();

        updateAuthUi();
        setupSearchBar();
        openFilterSheet();
    }

    private void setupQuickActions() {
        // Quick Cart Action
        View quickCart = b.getRoot().findViewById(R.id.quickCart);
        if (quickCart != null) {
            quickCart.setOnClickListener(v -> 
                NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_cartFragment)
            );
        }

        // Quick Notification Action
        View quickNotification = b.getRoot().findViewById(R.id.quickNotification);
        if (quickNotification != null) {
            quickNotification.setOnClickListener(v -> 
                startActivity(new Intent(requireContext(), NotificationActivity.class))
            );
        }

        // Quick Map Action & Button
        MaterialButton btnMap = b.getRoot().findViewById(R.id.btnMap);
        View quickMap = b.getRoot().findViewById(R.id.quickMap);
        View.OnClickListener openMap = v -> openVietMapActivity();
        
        if (btnMap != null) btnMap.setOnClickListener(openMap);
        if (quickMap != null) quickMap.setOnClickListener(openMap);

        // Sign In Button
        if (b.btnSignIn != null) {
            b.btnSignIn.setOnClickListener(v -> 
                NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_loginFragment)
            );
        }

        // User Chip
        if (b.chipUser != null) {
            b.chipUser.setOnClickListener(openMap);
        }
    }

    private void setupSearchBar() {
        View searchCard = b.getRoot().findViewById(R.id.searchCard);
        if (searchCard != null) {
            searchCard.setOnClickListener(v -> toggleSearch());
        }
    }

    private void toggleSearch() {
        com.google.android.material.textfield.TextInputLayout tilSearch = 
            b.getRoot().findViewById(R.id.tilSearch);
        com.google.android.material.textfield.TextInputEditText etSearch = 
            b.getRoot().findViewById(R.id.etSearch);
            
        if (tilSearch == null || etSearch == null) return;
        
        int vis = (tilSearch.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE;
        tilSearch.setVisibility(vis);
        if (vis == View.VISIBLE) {
            etSearch.requestFocus();
            etSearch.setOnEditorActionListener((tv, actionId, event) -> {
                String q = tv.getText() != null ? tv.getText().toString() : "";
                vm.setQuery(q);
                tilSearch.setVisibility(View.GONE);
                return true;
            });
        }
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

        // Inflate menu vào toolbar
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_home);

        Menu menu = toolbar.getMenu();
        if (menu != null) {
            MenuItem loginItem = menu.findItem(R.id.login); // id của menu
            if (loginItem != null) {
                loginItem.setOnMenuItemClickListener(mi -> {
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                    return true;
                });
            }

            // Xử lý click cho Cart
            MenuItem cartItem = menu.findItem(R.id.action_cart);
            if (cartItem != null) {
                cartItem.setOnMenuItemClickListener(mi -> {
                    NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_cartFragment);
                    return true;
                });
            }

            // Xử lý click cho Notification
            MenuItem notificationItem = menu.findItem(R.id.action_notification);
            if (notificationItem != null) {
                notificationItem.setOnMenuItemClickListener(mi -> {
                    startActivity(new Intent(requireContext(), NotificationActivity.class));
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


    private void setupBanner() {
        androidx.viewpager2.widget.ViewPager2 viewPager = 
            b.getRoot().findViewById(R.id.viewPager);
        if (viewPager == null) return;
        
        List<Integer> banners = Arrays.asList(
                R.drawable.banner_xe3,
                R.drawable.banner_xe5,
                R.drawable.banner_xe6
        );
        viewPager.setAdapter(new BannerAdapter(banners));

        bannerHandler = new Handler(Looper.getMainLooper());
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewPager.getAdapter() == null || viewPager.getAdapter().getItemCount() == 0)
                    return;
                int next = (viewPager.getCurrentItem() + 1) % viewPager.getAdapter().getItemCount();
                viewPager.setCurrentItem(next, true);
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
        
        View chipGroupView = b.getRoot().findViewById(R.id.chipGroup);
        if (chipGroupView instanceof com.google.android.material.chip.ChipGroup) {
            com.google.android.material.chip.ChipGroup chipGroup = (com.google.android.material.chip.ChipGroup) chipGroupView;
            chipGroup.setSingleSelection(true);
            
            for (int i = 0; i < cats.length; i++) {
                Chip chip = new Chip(requireContext());
                chip.setText(cats[i]);
                chip.setCheckable(true);
                chip.setChipBackgroundColorResource(R.color.surface_variant);
                chip.setTextColor(getResources().getColor(R.color.on_surface, null));
                chip.setCheckedIconVisible(false);
                
                // Styling
                chip.setChipStrokeWidth(0f);
                chip.setTextSize(14);
                chip.setMinHeight(48);
                
                if (i == 0) {
                    chip.setChecked(true);
                    chip.setChipBackgroundColorResource(R.color.accent_purple);
                    chip.setTextColor(getResources().getColor(R.color.white, null));
                }
                
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        chip.setChipBackgroundColorResource(R.color.accent_purple);
                        chip.setTextColor(getResources().getColor(R.color.white, null));
                        vm.setCategory(chip.getText().toString());
                    } else {
                        chip.setChipBackgroundColorResource(R.color.surface_variant);
                        chip.setTextColor(getResources().getColor(R.color.on_surface, null));
                    }
                });
                
                chipGroup.addView(chip);
            }
        }
    }

    private void setupGrid() {
        RecyclerView rvProducts = b.getRoot().findViewById(R.id.rvProducts);
        if (rvProducts == null) return;
        
        GridLayoutManager glm = new GridLayoutManager(getContext(), 2);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1; // Each item takes 1 span
            }
        });
        rvProducts.setLayoutManager(glm);

        adapter = new ProductAdapter(p -> {
            analytics.viewProduct(p.getId());
            Toast.makeText(getContext(), "Xem " + p.getName(), Toast.LENGTH_SHORT).show();
            // TODO: Nav to product detail when available
        });
        rvProducts.setAdapter(adapter);

        rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh = 
            b.getRoot().findViewById(R.id.swipeRefresh);
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(vm::refresh);
            swipeRefresh.setColorSchemeResources(
                R.color.accent_purple,
                R.color.accent_blue,
                R.color.accent_green
            );
        }
    }

    private void observe() {
        androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh = 
            b.getRoot().findViewById(R.id.swipeRefresh);
            
        vm.items.observe(getViewLifecycleOwner(), list -> {
            adapter.submit(list);
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
        });
        vm.loading.observe(getViewLifecycleOwner(), isLoading -> {
            adapter.setLoading(Boolean.TRUE.equals(isLoading));
            if (swipeRefresh != null) swipeRefresh.setRefreshing(Boolean.TRUE.equals(isLoading));
        });
        vm.error.observe(getViewLifecycleOwner(), isError -> {
            adapter.setError(Boolean.TRUE.equals(isError), vm::refresh);
        });
    }

    private void updateAuthUi() {
        String token = tokenManager != null ? tokenManager.getAccessToken() : null;
        boolean loggedIn = token != null;

        if (b.panelAuth != null) {
            b.panelAuth.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        }

        // Update header username
        TextView txtUserName = b.getRoot().findViewById(R.id.txtUserName);
        if (txtUserName != null) {
            if (loggedIn) {
                String name = com.example.evshop.util.JwtUtils.getDisplayName(token);
                txtUserName.setText(name != null && !name.isEmpty() ? name : "Khách hàng");
            } else {
                txtUserName.setText("Khách hàng");
            }
        }

        if (b.chipUser != null) {
            if (loggedIn) {
                String name = com.example.evshop.util.JwtUtils.getDisplayName(token);
                b.chipUser.setText(name != null && !name.isEmpty()
                        ? "Xin chào, " + name
                        : getString(R.string.welcome));
                b.chipUser.setVisibility(View.VISIBLE);
            } else {
                b.chipUser.setVisibility(View.GONE);
            }
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
