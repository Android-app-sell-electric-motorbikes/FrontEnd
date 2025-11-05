package com.example.evshop.data.repository;

import com.example.evshop.data.ApiService;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.domain.models.VersionDetails;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import retrofit2.Call;

@Singleton
public class VehicleRepository {

    private final ApiService apiService;

    @Inject
    public VehicleRepository(@Named("AuthApiService") ApiService apiService) {
        this.apiService = apiService;
    }

    public Call<ApiEnvelope<List<TemplateVehicle>>> getAllTemplateVehicles() {
        return apiService.getAllTemplateVehicles();
    }

    public Call<ApiEnvelope<TemplateVehicle>> getVehicleById(String vehicleId) {
        return apiService.getVehicleById(vehicleId);
    }

    public Call<ApiEnvelope<VersionDetails>> getVersionDetails(String versionId) {
        return apiService.getVersionDetails(versionId);
    }
}
