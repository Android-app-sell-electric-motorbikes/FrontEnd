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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
// *** THÊM IMPORT NÀY ***
import androidx.appcompat.widget.Toolbar;


import com.example.evshop.R;
import com.example.evshop.databinding.FragmentHomeBinding;
import com.example.evshop.databinding.ItemBannerBinding;
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.domain.models.UserData;
import com.example.evshop.ui.adapter.VehicleAdapter;
import com.example.evshop.ui.auth.AuthViewModel;
import com.example.evshop.ui.main.MainActivity;
import com.example.evshop.ui.vehicle.TemplateVehicleListActivity;
import com.example.evshop.ui.vehicle.VehicleDetailActivity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        observeLoginState();
        setupClickListeners();
    }

    private void observeLoginState() {
        // Lắng nghe trạng thái đăng nhập từ AuthViewModel
        authViewModel.getIsLoggedInState().observe(getViewLifecycleOwner(), isLoggedIn -> {
            if (b == null) return; // Đảm bảo view binding còn tồn tại

            boolean loggedIn = isLoggedIn != null && isLoggedIn;

            // Cập nhật giao diện dựa trên trạng thái đăng nhập
            if (loggedIn) {
                // --- KHI ĐÃ ĐĂNG NHẬP ---

                // Ẩn panel đăng nhập/đăng ký
                b.panelAuth.setVisibility(View.GONE);

                // Lấy thông tin người dùng để hiển thị trên chip
                UserData user = authViewModel.getCurrentUser().getValue();
                if (user != null) {
                    b.chipUser.setText("Chào, " + user.fullName);
                }
                // Hiện chip chào mừng
                b.chipUser.setVisibility(View.VISIBLE);

                // ===> HIỆN NÚT "XEM TẤT CẢ SẢN PHẨM" <===
                b.btnViewAllLoggedIn.setVisibility(View.VISIBLE);

                // Tải lại dữ liệu trang chủ
                homeViewModel.refresh();

            } else {
                // --- KHI CHƯA ĐĂNG NHẬP ---

                // Hiện panel đăng nhập/đăng ký
                b.panelAuth.setVisibility(View.VISIBLE);

                // Ẩn các thành phần của người dùng đã đăng nhập
                b.chipUser.setVisibility(View.GONE);
                b.btnViewAllLoggedIn.setVisibility(View.GONE);
            }

            // Cập nhật icon trên Toolbar của Activity (đoạn này đã đúng)
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showToolbarItems(loggedIn);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null; // Tránh memory leak
    }

    private void setupBanner() {
        if (b == null) return;
        List<Integer> bannerImages = Arrays.asList(R.drawable.banner_xe3, R.drawable.banner_xe6, R.drawable.banner_xe5);
        BannerAdapter bannerAdapter = new BannerAdapter(bannerImages);
        b.viewPager.setAdapter(bannerAdapter);
    }

    private void setupRecyclerView() {
        if (b == null) return;
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
        if (b == null) return;
        homeViewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (b!=null && isLoading != null) b.swipeRefresh.setRefreshing(isLoading);
        });
        homeViewModel.getFeaturedVehicles().observe(getViewLifecycleOwner(), vehicles -> {
            if (b!=null && vehicles != null) vehicleAdapter.submitList(vehicles);
        });
        homeViewModel.error.observe(getViewLifecycleOwner(), hasError -> {
            if (b!=null && hasError != null && hasError) Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
        });
    }

    // ======================================================================
    // ***           HÀM SETUPCLICKLISTENERS ĐÃ ĐƯỢC VIẾT LẠI           ***
    // ======================================================================
    private void setupClickListeners() {
        if (b == null) return;

        // Giữ lại listener cho SwipeRefresh
        b.swipeRefresh.setOnRefreshListener(() -> homeViewModel.refresh());

        // Nút "Xem tất cả" vẫn hoạt động bình thường
        b.btnViewAllLoggedIn.setOnClickListener(v -> {
            if (getContext() == null) return;
            Intent intent = new Intent(getContext(), TemplateVehicleListActivity.class);
            startActivity(intent);
        });

        // Gắn listener vào Toolbar của Activity để bắt sự kiện click item menu
        if (getActivity() != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
            toolbar.setOnMenuItemClickListener(menuItem -> {
                // Kiểm tra xem có phải người dùng đã nhấn đúng vào icon tài khoản không
                if (menuItem.getItemId() == R.id.action_account_menu) {
                    // Tìm View của chính item đó để làm "điểm neo" cho PopupMenu
                    View menuItemView = getActivity().findViewById(R.id.action_account_menu);
                    if (menuItemView != null) {
                        showUserPopupMenu(menuItemView);
                    }
                    return true;
                }
                return false;
            });
        }
    }

    /**
     * Hàm mới để hiển thị PopupMenu, giống trong AdminActivity.
     * @param anchorView View mà menu sẽ "neo" vào (chính là icon tài khoản).
     */
    private void showUserPopupMenu(View anchorView) {
        if (getContext() == null) return;

        PopupMenu popupMenu = new PopupMenu(getContext(), anchorView);
        popupMenu.getMenu().add("Tài khoản của tôi");
        popupMenu.getMenu().add("Đăng xuất");

        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if ("Tài khoản của tôi".equals(title)) {
                // *** THAY ĐỔI Ở ĐÂY: GỌI HÀM HIỂN THỊ PROFILE MỚI ***
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
        // Luôn kiểm tra context trong Fragment
        if (getContext() == null) {
            return;
        }

        // Lấy dữ liệu người dùng hiện tại từ AuthViewModel
        UserData currentUser = authViewModel.getCurrentUser().getValue();

        // Kiểm tra nếu không có dữ liệu (chưa kịp tải hoặc lỗi)
        if (currentUser == null) {
            Toast.makeText(getContext(), "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xây dựng chuỗi thông tin để hiển thị
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Tên: ").append(currentUser.fullName).append("\n\n");
        messageBuilder.append("Email: ").append(currentUser.email).append("\n\n");
        // Bạn có thể thêm các thông tin khác như số điện thoại nếu có trong model UserData
        //messageBuilder.append("Ngày/Tháng/Năm Sinh: ").append(currentUser.dateOfBirth).append("\n\n");


        // Xử lý hiển thị danh sách roles (vai trò)
        String rolesString = "N/A";
        if (currentUser.roles != null && !currentUser.roles.isEmpty()) {
            // Nối các role lại với nhau, phân cách bởi dấu phẩy
            // Cần API 24+ để dùng stream, dự án của bạn có vẻ đã đáp ứng
            rolesString = currentUser.roles.stream().collect(Collectors.joining(", "));
        }
        messageBuilder.append("Vai trò: ").append(rolesString);

        // Tạo và hiển thị AlertDialog
        new AlertDialog.Builder(getContext()) // Sử dụng getContext() thay vì 'this'
                .setTitle("Thông tin Tài khoản") // Đổi tiêu đề cho phù hợp
                .setMessage(messageBuilder.toString())
                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                .show();
    }




    /**
     * Hàm private để gom logic đăng xuất cho gọn.
     */
    private void logout() {
        authViewModel.logout();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }


    // --- ADAPTER CHO BANNER GIỮ NGUYÊN ---
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
