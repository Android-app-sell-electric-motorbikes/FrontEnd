// ui/MainActivity.java
package com.example.evshop.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.evshop.R;
import com.example.evshop.data.RetrofitClient;
import com.example.evshop.models.Product;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipe;
    private RecyclerView rv;
    private ProductAdapter adapter;
    private String sort = "priceAsc";
    private String brand = null; // có thể set từ filter sau

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Xe máy điện");

        swipe = findViewById(R.id.swipe);
        rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        swipe.setOnRefreshListener(this::loadProducts);
        swipe.setRefreshing(true);
        loadProducts();
    }

    private void loadProducts(){
//        RetrofitClient.getApi(this).getProducts(sort, brand).enqueue(new Callback<List<Product>>() {
//            @Override public void onResponse(Call<List<Product>> call, Response<List<Product>> resp) {
//                swipe.setRefreshing(false);
//                if (resp.isSuccessful() && resp.body()!=null){
//                    adapter.setItems(resp.body());
//                } else {
//                    Snackbar.make(rv, "Không tải được danh sách", Snackbar.LENGTH_LONG).show();
//                }
//            }
//            @Override public void onFailure(Call<List<Product>> call, Throwable t) {
//                swipe.setRefreshing(false);
//                Snackbar.make(rv, "Lỗi mạng: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
//            }
//        });
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort_price_asc){
            sort = "priceAsc";
            swipe.setRefreshing(true);
            loadProducts();
            return true;
        } else if (id == R.id.action_sort_price_desc){
            sort = "priceDesc";
            swipe.setRefreshing(true);
            loadProducts();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
