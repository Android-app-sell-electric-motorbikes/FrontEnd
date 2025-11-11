package com.example.evshop.ui.admin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Bạn cần cấp quyền truy cập để chọn ảnh", Toast.LENGTH_SHORT).show();
                }
            });

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
        setupClickListeners();
        observeViewModel();
    }

    private void setupToolbar() {
        b.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        b.btnSelectImage.setOnClickListener(v -> {
            String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? Manifest.permission.READ_MEDIA_IMAGES
                    : Manifest.permission.READ_EXTERNAL_STORAGE;

            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                requestPermissionLauncher.launch(permission);
            }
        });

        b.btnAddTemplate.setOnClickListener(v -> attemptToCreateTemplate());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void attemptToCreateTemplate() {
        Object versionObj = b.spinnerVersion.getSelectedItem();
        Object colorObj = b.spinnerColor.getSelectedItem();

        String description = b.etDescription.getText().toString().trim();
        String priceStr = b.etPrice.getText().toString().trim();

        if (versionObj == null || !(versionObj instanceof Version)) {
            Toast.makeText(this, "Vui lòng chọn một phiên bản hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (colorObj == null || !(colorObj instanceof Color)) {
            Toast.makeText(this, "Vui lòng chọn một màu sắc hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

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

        // ** SỬA LẠI: GỌI ĐÚNG TÊN PHƯƠNG THỨC **
        String versionId = ((Version) versionObj).getVersionId();
        String colorId = ((Color) colorObj).getColorId();

        viewModel.createTemplateVehicle(
                this,
                versionId,
                colorId,
                description,
                price,
                selectedImageUri
        );
    }


    private void observeViewModel() {
        viewModel.versions.observe(this, versions -> {
            if (versions != null && !versions.isEmpty()) {
                ArrayAdapter<Version> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, versions);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                b.spinnerVersion.setAdapter(adapter);
            }
        });

        viewModel.colors.observe(this, colors -> {
            if (colors != null && !colors.isEmpty()) {
                ArrayAdapter<Color> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colors);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                b.spinnerColor.setAdapter(adapter);
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
            b.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            b.btnAddTemplate.setEnabled(!isLoading);
            b.btnSelectImage.setEnabled(!isLoading);
        });

        viewModel.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.createSuccess.observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Thêm mẫu xe thành công!", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
