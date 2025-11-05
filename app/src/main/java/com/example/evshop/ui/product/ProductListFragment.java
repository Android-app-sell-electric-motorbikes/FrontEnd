package com.example.evshop.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.databinding.FragmentProductListBinding;
import com.example.evshop.domain.models.Product;
import com.example.evshop.ui.ProductDetailsActivity;
import com.example.evshop.ui.home.HomeViewModel;
import com.example.evshop.ui.home.ProductAdapter;
import com.google.android.material.chip.Chip;

import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProductListFragment extends Fragment {
    private FragmentProductListBinding binding;
    private HomeViewModel viewModel;
    private ProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProductListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupChips();
        setupRecyclerView();
        setupSwipeRefresh();
        observeData();

        // Load data
        viewModel.refresh();
    }

    private void setupChips() {
        List<String> categories = Arrays.asList("Tất cả", "City", "Sport", "Off-road", "Eco");
        
        for (int i = 0; i < categories.size(); i++) {
            String cat = categories.get(i);
            Chip chip = new Chip(requireContext());
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.white);
            chip.setChipStrokeColorResource(R.color.black);
            chip.setChipStrokeWidth(2f);
            
            if (i == 0) {
                chip.setChecked(true);
            }
            
            chip.setOnClickListener(v -> {
                viewModel.setCategory(cat);
            });
            
            binding.chipGroup.addView(chip);
        }
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(this::onProductClick);
        binding.rvProducts.setAdapter(adapter);
        binding.rvProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
    }

    private void onProductClick(Product product) {
        // Mở màn hình chi tiết sản phẩm
        Intent intent = new Intent(requireContext(), ProductDetailsActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_name", product.getName());
        intent.putExtra("product_price", product.getPriceVnd());
        intent.putExtra("product_rating", product.getRating());
        intent.putExtra("product_image", product.getImageUrl());
        startActivity(intent);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());
    }

    private void observeData() {
        // TODO: HomeViewModel currently only manages featured vehicles, not products
        // Need to implement products list with items LiveData in HomeViewModel
        // viewModel.items.observe(getViewLifecycleOwner(), list -> {
        //     if (list != null) {
        //         adapter.submit(list);
        //         binding.tvProductCount.setText(list.size() + " sản phẩm");
        //     }
        // });
        
        // Observe featured vehicles instead (if needed)
        viewModel.getFeaturedVehicles().observe(getViewLifecycleOwner(), vehicles -> {
            // TODO: Convert TemplateVehicle to Product or use different adapter
            // For now, just stop refreshing
            binding.swipeRefresh.setRefreshing(false);
        });

        viewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            adapter.setLoading(Boolean.TRUE.equals(isLoading));
            binding.swipeRefresh.setRefreshing(Boolean.TRUE.equals(isLoading));
        });

        viewModel.error.observe(getViewLifecycleOwner(), isError -> {
            adapter.setError(Boolean.TRUE.equals(isError), () -> viewModel.refresh());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

