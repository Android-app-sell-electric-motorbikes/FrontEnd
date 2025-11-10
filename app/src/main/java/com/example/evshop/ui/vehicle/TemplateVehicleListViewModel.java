// File: D:/PRM391/FrontEnd/app/src/main/java/com/example/evshop/ui/vehicle/TemplateVehicleListViewModel.java

package com.example.evshop.ui.vehicle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.evshop.data.repository.VehicleRepository;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateResult;
import com.example.evshop.domain.models.TemplateVehicle;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class TemplateVehicleListViewModel extends ViewModel {

    private final VehicleRepository repository;

    private final MutableLiveData<List<TemplateVehicle>> _vehicles = new MutableLiveData<>();
    public LiveData<List<TemplateVehicle>> getVehicles() { return _vehicles; }

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>();
    public LiveData<Boolean> getLoading() { return _loading; }

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> getError() { return _error; }

    @Inject
    public TemplateVehicleListViewModel(VehicleRepository repository) {
        this.repository = repository;
        // Tải danh sách mặc định khi ViewModel được tạo lần đầu
        fetchVehicles(null, null, null, null);
    }

    /**
     * Phương thức chính để tải danh sách xe, có hỗ trợ tìm kiếm, lọc giá và sắp xếp.
     *
     * @param searchTerm     Từ khóa tìm kiếm (VD: "Aqua").
     * @param minPrice       Giá tối thiểu.
     * @param maxPrice       Giá tối đa.
     * @param sortByPriceAsc Sắp xếp theo giá tăng dần (true) hoặc giảm dần (false). Truyền null nếu không sắp xếp.
     */
    public void fetchVehicles(
            String searchTerm,
            Long minPrice,
            Long maxPrice,
            Boolean sortByPriceAsc
    ) {
        _loading.postValue(true);
        _error.postValue(null); // Xóa lỗi cũ

        repository.getAllTemplateVehicles(1, 100, searchTerm, minPrice, maxPrice, sortByPriceAsc)
                .enqueue(new Callback<ApiEnvelope<TemplateResult>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiEnvelope<TemplateResult>> call, @NonNull Response<ApiEnvelope<TemplateResult>> response) {
                        _loading.postValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            ApiEnvelope<TemplateResult> apiResponse = response.body();

                            // *** SỬA LẠI ĐỂ DÙNG GETTER ***
                            if (apiResponse.isSuccess && apiResponse.result != null && apiResponse.result.getData() != null) {
                                _vehicles.postValue(apiResponse.result.getData());
                            } else {
                                _vehicles.postValue(new ArrayList<>());
                                String message = apiResponse.message != null ? apiResponse.message : "Không có dữ liệu";
                                _error.postValue("Lỗi: " + message);
                            }
                        } else {
                            _vehicles.postValue(new ArrayList<>());
                            _error.postValue("Lỗi tải dữ liệu: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiEnvelope<TemplateResult>> call, @NonNull Throwable t) {
                        _loading.postValue(false);
                        _vehicles.postValue(new ArrayList<>());
                        _error.postValue("Lỗi mạng: " + t.getMessage());
                    }
                });
    }
}
