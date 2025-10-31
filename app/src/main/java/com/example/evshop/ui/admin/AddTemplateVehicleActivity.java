package com.example.evshop.ui.admin;

// *** THÊM MỚI CÁC IMPORT CẦN THIẾT ***
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils; // Thêm mới
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.util.Log; // <--- SỬA LẠI THÀNH DÒNG NÀY
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Thêm mới
import androidx.lifecycle.ViewModelProvider;

import com.example.evshop.databinding.ActivityAddTemplateVehicleBinding;
import com.example.evshop.domain.models.Color;
import com.example.evshop.domain.models.Version;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddTemplateVehicleActivity extends AppCompatActivity {

    private ActivityAddTemplateVehicleBinding b;
    private AddTemplateVehicleViewModel viewModel;
    private Uri selectedImageUri;

    // *** THÊM MỚI: LAUNCHER ĐỂ XIN QUYỀN TRUY CẬP BỘ NHỚ ***
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Nếu người dùng đồng ý, mở thư viện ảnh
                    openImagePicker();
                } else {
                    // Nếu từ chối, thông báo cho người dùng
                    Toast.makeText(this, "Bạn cần cấp quyền truy cập để chọn ảnh", Toast.LENGTH_SHORT).show();
                }
            });

    // Launcher chọn ảnh cũ của bạn (giữ nguyên)
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    b.ivPreview.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAddTemplateVehicleBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        viewModel = new ViewModelProvider(this).get(AddTemplateVehicleViewModel.class);

        setupToolbar();
        setupClickListeners(); // Hàm này sẽ được sửa lại
        observeViewModel(); // Hàm này sẽ được bổ sung
    }

    private void setupToolbar() {
        b.toolbar.setNavigationOnClickListener(v -> finish());
    }

    // *** SỬA LẠI: HÀM SETUPCLICKLISTENERS ĐỂ HOÀN THIỆN LOGIC ***
    private void setupClickListeners() {
        // --- Sửa logic nút chọn ảnh để kiểm tra quyền ---
        b.btnSelectImage.setOnClickListener(v -> {
            // Xác định quyền cần xin tùy theo phiên bản Android
            String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? Manifest.permission.READ_MEDIA_IMAGES
                    : Manifest.permission.READ_EXTERNAL_STORAGE;

            // Kiểm tra xem đã được cấp quyền chưa
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                // Nếu đã có, mở luôn
                openImagePicker();
            } else {
                // Nếu chưa, yêu cầu quyền
                // Dòng này rất quan trọng để gọi hộp thoại xin quyền
                requestPermissionLauncher.launch(permission);
            }
        });

        // --- Sửa logic nút "Thêm Mẫu Xe" ---
        b.btnAddTemplate.setOnClickListener(v -> {
            // Thay vì hiển thị Toast, gọi hàm xử lý chính
            attemptToCreateTemplate();
        });
    }

    // *** THÊM MỚI: HÀM MỞ THƯ VIỆN ẢNH ĐỂ TÁI SỬ DỤNG ***
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    // *** THÊM MỚI: HÀM KIỂM TRA DỮ LIỆU VÀ GỌI VIEWMODEL ***
    private void attemptToCreateTemplate() {
        // 1. Lấy các đối tượng được chọn từ Spinner
        Object versionObj = b.spinnerVersion.getSelectedItem();
        Object colorObj = b.spinnerColor.getSelectedItem();

        // 2. Lấy dữ liệu từ các trường khác
        String description = b.etDescription.getText().toString().trim();
        String priceStr = b.etPrice.getText().toString().trim();

        // ===================================================================
        //                      *** BƯỚC KIỂM TRA TOÀN DIỆN ***
        // ===================================================================

        // KIỂM TRA SPINNER
        if (versionObj == null) {
            Toast.makeText(this, "Chưa chọn phiên bản hoặc dữ liệu đang tải.", Toast.LENGTH_SHORT).show();
            return;
        }
        // **Thêm bước kiểm tra kiểu dữ liệu an toàn**
        if (!(versionObj instanceof Version)) {
            Toast.makeText(this, "Dữ liệu phiên bản không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (colorObj == null) {
            Toast.makeText(this, "Chưa chọn màu sắc hoặc dữ liệu đang tải.", Toast.LENGTH_SHORT).show();
            return;
        }
        // **Thêm bước kiểm tra kiểu dữ liệu an toàn**
        if (!(colorObj instanceof Color)) {
            Toast.makeText(this, "Dữ liệu màu sắc không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // KIỂM TRA CÁC TRƯỜNG CÒN LẠI (giữ nguyên)
        if (TextUtils.isEmpty(description)) {
            b.etDescription.setError("Mô tả không được để trống");
            b.etDescription.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(priceStr)) {
            b.etPrice.setError("Giá bán không được để trống");
            b.etPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            b.etPrice.setError("Giá bán không hợp lệ");
            b.etPrice.requestFocus();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn một hình ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        // ===================================================================
        //  NẾU ĐẾN ĐƯỢC ĐÂY, MỌI THỨ ĐỀU HỢP LỆ. BẮT ĐẦU GỌI VIEWMODEL
        // ===================================================================

        // Ép kiểu an toàn và lấy ID (chắc chắn không null)
        String versionId = ((Version) versionObj).getId();
        String colorId = ((Color) colorObj).getId();

        // Gọi hàm trong ViewModel với dữ liệu đã được xác thực
        viewModel.createTemplateVehicle(
                this,
                versionId,
                colorId,
                description,
                price,
                selectedImageUri
        );
    }


    // *** SỬA LẠI: BỔ SUNG LOGIC CHO HÀM OBSERVEVIEWMODEL ***
    private void observeViewModel() {
        // Lắng nghe danh sách Versions (giữ nguyên)
        viewModel.versions.observe(this, versions -> {
            if (versions != null && !versions.isEmpty()) {
                ArrayAdapter<Version> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, versions);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                b.spinnerVersion.setAdapter(adapter);
            }
        });

        // Lắng nghe danh sách Colors (giữ nguyên)
        viewModel.colors.observe(this, colors -> {
            if (colors != null && !colors.isEmpty()) {
                ArrayAdapter<Color> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colors);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                b.spinnerColor.setAdapter(adapter);
            }
        });

        // Lắng nghe trạng thái loading (sửa lại một chút để vô hiệu hóa cả 2 nút)
        viewModel.isLoading.observe(this, isLoading -> {
            b.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            // Vô hiệu hóa các nút khi đang xử lý để tránh người dùng nhấn nhiều lần
            b.btnAddTemplate.setEnabled(!isLoading);
            b.btnSelectImage.setEnabled(!isLoading);
        });

        // Lắng nghe lỗi (giữ nguyên)
        viewModel.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        // *** THÊM MỚI: LẮNG NGHE SỰ KIỆN TẠO THÀNH CÔNG ***
        viewModel.createSuccess.observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Thêm mẫu xe thành công!", Toast.LENGTH_LONG).show();
                finish(); // Đóng màn hình này và quay về màn hình trước đó
            }
        });
    }
}
