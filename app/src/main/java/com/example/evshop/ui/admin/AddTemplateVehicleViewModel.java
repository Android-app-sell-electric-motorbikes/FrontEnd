package com.example.evshop.ui.admin;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.network.requests.CreateTemplateVehicleRequest;
import com.example.evshop.data.network.requests.GetUploadUrlRequest;
import com.example.evshop.data.network.responses.UploadUrlResponse;
import com.example.evshop.data.repository.VehicleAdminRepository;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.Color;
import com.example.evshop.domain.models.Version;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class AddTemplateVehicleViewModel extends ViewModel {

    private final VehicleAdminRepository repository;

    private final MutableLiveData<List<Version>> _versions = new MutableLiveData<>();
    public LiveData<List<Version>> versions = _versions;

    private final MutableLiveData<List<Color>> _colors = new MutableLiveData<>();
    public LiveData<List<Color>> colors = _colors;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _createSuccess = new MutableLiveData<>(false);
    public LiveData<Boolean> createSuccess = _createSuccess;

    private final AtomicInteger apiCallCounter = new AtomicInteger(2);

    @Inject
    public AddTemplateVehicleViewModel(VehicleAdminRepository repository) {
        this.repository = repository;
        loadInitialData();
    }

    public void loadInitialData() {
        _isLoading.setValue(true);
        apiCallCounter.set(2);
        loadVersions();
        loadColors();
    }

    private void loadVersions() {
        repository.getVersions().enqueue(new Callback<ApiEnvelope<List<Version>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiEnvelope<List<Version>>> call, @NonNull Response<ApiEnvelope<List<Version>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().result != null) {
                    _versions.postValue(response.body().result);
                } else {
                    _error.postValue("Lỗi tải danh sách phiên bản: " + response.code());
                }
                checkIfLoadingComplete();
            }

            @Override
            public void onFailure(@NonNull Call<ApiEnvelope<List<Version>>> call, @NonNull Throwable t) {
                _error.postValue("Lỗi mạng (versions): " + t.getMessage());
                checkIfLoadingComplete();
            }
        });
    }

    private void loadColors() {
        repository.getColors().enqueue(new Callback<ApiEnvelope<List<Color>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiEnvelope<List<Color>>> call, @NonNull Response<ApiEnvelope<List<Color>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().result != null) {
                    _colors.postValue(response.body().result);
                } else {
                    _error.postValue("Lỗi tải danh sách màu sắc: " + response.code());
                }
                checkIfLoadingComplete();
            }

            @Override
            public void onFailure(@NonNull Call<ApiEnvelope<List<Color>>> call, @NonNull Throwable t) {
                _error.postValue("Lỗi mạng (colors): " + t.getMessage());
                checkIfLoadingComplete();
            }
        });
    }

    private void checkIfLoadingComplete() {
        if (apiCallCounter.decrementAndGet() == 0) {
            _isLoading.postValue(false);
        }
    }

    public void createTemplateVehicle(Context context, String versionId, String colorId, String description, double price, Uri imageUri) {
        _isLoading.setValue(true);
        _error.setValue(null);

        String fileName = getFileName(context.getContentResolver(), imageUri);
        String contentType = getMimeType(context, imageUri);

        if (fileName == null || contentType == null) {
            _error.postValue("Không thể lấy thông tin file từ Uri.");
            _isLoading.postValue(false);
            return;
        }

        GetUploadUrlRequest getUrlRequest = new GetUploadUrlRequest(fileName, contentType);
        repository.getUploadUrl(getUrlRequest).enqueue(new Callback<UploadUrlResponse>() {
            @Override
            public void onResponse(@NonNull Call<UploadUrlResponse> call, @NonNull Response<UploadUrlResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getResult() != null) {
                    UploadUrlResponse.Result uploadInfo = response.body().getResult();
                    uploadImageToS3(context, uploadInfo.getUploadUrl(), uploadInfo.getObjectKey(), imageUri, versionId, colorId, description, price);
                } else {
                    String errorMsg = (response.body() != null) ? response.body().getMessage() : ("Lỗi " + response.code());
                    _error.postValue("Lỗi khi lấy URL upload: " + errorMsg);
                    _isLoading.postValue(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UploadUrlResponse> call, @NonNull Throwable t) {
                _error.postValue("Lỗi mạng (getUploadUrl): " + t.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    private void uploadImageToS3(Context context, String uploadUrl, String objectKey, Uri imageUri, String versionId, String colorId, String description, double price) {
        String mimeType = getMimeType(context, imageUri);
        if (mimeType == null) {
            _error.postValue("Không thể xác định loại file ảnh.");
            _isLoading.postValue(false);
            return;
        }

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) throw new Exception("InputStream is null");
            byte[] fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);
            inputStream.close();
            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), fileBytes);

            repository.uploadImageToS3(uploadUrl, requestFile).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        createFinalTemplate(versionId, colorId, description, price, objectKey);
                    } else {
                        _error.postValue("Lỗi upload ảnh lên S3: " + response.code());
                        _isLoading.postValue(false);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    _error.postValue("Lỗi mạng (uploadToS3): " + t.getMessage());
                    _isLoading.postValue(false);
                }
            });
        } catch (Exception e) {
            _error.postValue("Lỗi đọc file ảnh: " + e.getMessage());
            _isLoading.postValue(false);
        }
    }

    private void createFinalTemplate(String versionId, String colorId, String description, double price, String objectKey) {

        List<String> objectKeys = Collections.singletonList(objectKey);

        CreateTemplateVehicleRequest request = new CreateTemplateVehicleRequest(
                versionId,
                colorId,
                description,
                price,
                objectKeys,
                true // isActive
        );

        repository.createTemplateVehicle(request).enqueue(new Callback<ApiEnvelope<Boolean>>() {
            @Override
            public void onResponse(@NonNull Call<ApiEnvelope<Boolean>> call, @NonNull Response<ApiEnvelope<Boolean>> response) {
                _isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    _createSuccess.postValue(true);
                } else {
                    String msg = (response.body() != null) ? response.body().message : ("Lỗi " + response.code());
                    _error.postValue("Tạo mẫu xe thất bại: " + msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiEnvelope<Boolean>> call, @NonNull Throwable t) {
                _isLoading.postValue(false);
                _error.postValue("Lỗi mạng (createFinalTemplate): " + t.getMessage());
            }
        });
    }

    private String getFileName(ContentResolver resolver, Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) { /* Ignore */ }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    private String getMimeType(Context context, Uri uri) {
        String mimeType;
        if (uri.getScheme() != null && ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            mimeType = context.getContentResolver().getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType;
    }
}
