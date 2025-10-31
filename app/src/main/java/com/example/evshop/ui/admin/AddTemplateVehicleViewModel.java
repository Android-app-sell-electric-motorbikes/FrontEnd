package com.example.evshop.ui.admin; // Gói này khớp với Activity của bạn

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.network.AdminRepository;
import com.example.evshop.domain.models.Color;
import com.example.evshop.domain.models.Version;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class AddTemplateVehicleViewModel extends ViewModel {

    private final AdminRepository adminRepository;

    // LiveData để giữ danh sách Version và Color
    private final MutableLiveData<List<Version>> _versions = new MutableLiveData<>();
    public LiveData<List<Version>> versions = _versions;

    private final MutableLiveData<List<Color>> _colors = new MutableLiveData<>();
    public LiveData<List<Color>> colors = _colors;

    // LiveData cho trạng thái (loading, lỗi, thành công)
    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public LiveData<Boolean> loading = _loading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    @Inject
    public AddTemplateVehicleViewModel(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
        // Ngay khi ViewModel được tạo, tải danh sách Version và Color
        loadInitialData();
    }

    // Hàm gọi API để lấy danh sách
    public void loadInitialData() {
        _loading.setValue(true);
        // Lấy danh sách version
        adminRepository.getVersions().enqueue(new Callback<List<Version>>() {
            @Override
            public void onResponse(Call<List<Version>> call, Response<List<Version>> response) {
                if (response.isSuccessful()) {
                    _versions.postValue(response.body());
                } else {
                    _error.postValue("Lỗi tải danh sách phiên bản");
                }
                // Dù thành công hay thất bại cũng gọi tiếp API color
                loadColors();
            }

            @Override
            public void onFailure(Call<List<Version>> call, Throwable t) {
                _error.postValue("Lỗi mạng: " + t.getMessage());
                loadColors(); // Vẫn gọi để không bị treo
            }
        });
    }

    private void loadColors() {
        // Lấy danh sách color
        adminRepository.getColors().enqueue(new Callback<List<Color>>() {
            @Override
            public void onResponse(Call<List<Color>> call, Response<List<Color>> response) {
                _loading.postValue(false); // Kết thúc loading sau khi gọi xong cả 2 API
                if (response.isSuccessful()) {
                    _colors.postValue(response.body());
                } else {
                    _error.postValue("Lỗi tải danh sách màu sắc");
                }
            }

            @Override
            public void onFailure(Call<List<Color>> call, Throwable t) {
                _loading.postValue(false);
                _error.postValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }
}
    