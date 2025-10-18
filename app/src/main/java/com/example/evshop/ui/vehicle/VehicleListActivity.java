package com.example.evshop.ui.vehicle;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.data.RetrofitClient;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.Vehicle;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VehicleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_list);

        recyclerView = findViewById(R.id.recyclerVehicles);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadVehicles();
    }

    private void loadVehicles() {
        RetrofitClient.getApi(this).getAllVehicles().enqueue(new Callback<ApiEnvelope<List<Vehicle>>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<List<Vehicle>>> call, Response<ApiEnvelope<List<Vehicle>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    List<Vehicle> vehicles = response.body().result;
                    adapter = new VehicleAdapter(vehicles,VehicleListActivity.this);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(VehicleListActivity.this, "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<Vehicle>>> call, Throwable t) {
                Toast.makeText(VehicleListActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
        