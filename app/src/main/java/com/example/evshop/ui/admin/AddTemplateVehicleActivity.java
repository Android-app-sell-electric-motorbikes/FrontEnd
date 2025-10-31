package com.example.evshop.ui.admin; // Gói gốc của file bạn

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter; // QUAN TRỌNG
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.evshop.databinding.ActivityAddTemplateVehicleBinding;
import com.example.evshop.domain.models.Color;    // QUAN TRỌNG
import com.example.evshop.domain.models.Version;  // QUAN TRỌNG

import java.util.List; // QUAN TRỌNG

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddTemplateVehicleActivity extends AppCompatActivity {

    private ActivityAddTemplateVehicleBinding b;
    private AddTemplateVehicleViewModel viewModel; // *** MỞ KHÓA ***
    private Uri selectedImageUri;

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

        // *** MỞ KHÓA VÀ KHỞI TẠO VIEWMODEL ***
        viewModel = new ViewModelProvider(this).get(AddTemplateVehicleViewModel.class);

        setupToolbar();
        setupClickListeners();
        observeViewModel(); // *** MỞ KHÓA ***
    }

    private void setupToolbar() {
        b.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        b.btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        b.btnAddTemplate.setOnClickListener(v -> {
            // Sẽ hoàn thiện ở bước cuối
            Toast.makeText(this, "Chức năng đang được phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    // ==========================================================
    // *** HÀM QUAN TRỌNG NHẤT: LẮNG NGHE DỮ LIỆU TỪ VIEWMODEL ***
    // ==========================================================
    private void observeViewModel() {
        // Lắng nghe danh sách Versions
        viewModel.versions.observe(this, versions -> {
            if (versions != null && !versions.isEmpty()) {
                // Tạo Adapter để đổ dữ liệu vào Spinner
                ArrayAdapter<Version> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, versions);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Gán adapter cho spinner
                b.spinnerVersion.setAdapter(adapter);
            }
        });

        // Lắng nghe danh sách Colors
        viewModel.colors.observe(this, colors -> {
            if (colors != null && !colors.isEmpty()) {
                ArrayAdapter<Color> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colors);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                b.spinnerColor.setAdapter(adapter);
            }
        });

        // Lắng nghe trạng thái loading
        viewModel.loading.observe(this, isLoading -> {
            if (isLoading) {
                b.progressBar.setVisibility(View.VISIBLE);
                b.btnAddTemplate.setEnabled(false); // Không cho nhấn nút khi đang tải
            } else {
                b.progressBar.setVisibility(View.GONE);
                b.btnAddTemplate.setEnabled(true);
            }
        });

        // Lắng nghe lỗi
        viewModel.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
    