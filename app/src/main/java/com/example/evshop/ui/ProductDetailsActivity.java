package com.example.evshop.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.evshop.R;

import java.util.Arrays;
import java.util.List;

public class ProductDetailsActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ImageButton btnPrev, btnNext;
    private TextView txtName,txtPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.product_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Liên kết View
        viewPager = findViewById(R.id.viewPager);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        txtName = findViewById(R.id.product_title);
        txtPrice = findViewById(R.id.product_price);

        // --- Tăng giảm số lượng ---
        android.widget.EditText quantityInput = findViewById(R.id.quantity_input);
        android.widget.ImageButton btnPlus = findViewById(R.id.btn_plus);
        android.widget.ImageButton btnMinus = findViewById(R.id.btn_minus);

        // Xử lý nút cộng
        btnPlus.setOnClickListener(v -> {
            int current = Integer.parseInt(quantityInput.getText().toString());
            current++;
            quantityInput.setText(String.valueOf(current));
        });

        // Xử lý nút trừ
        btnMinus.setOnClickListener(v -> {
            int current = Integer.parseInt(quantityInput.getText().toString());
            if (current > 1) current--;
            quantityInput.setText(String.valueOf(current));
        });


        // Lấy dữ liệu từ Internet
        String name = getIntent().getStringExtra("product_name");
        String price = getIntent().getStringExtra("product_price");
        if(name != null) txtName.setText(name);
        if(price != null) txtPrice.setText(price);



        // Danh sách ảnh (trong drawable)
        List<Integer> images = Arrays.asList(
                R.drawable.ev_scooter,
                R.drawable.ev_scooter2,
                R.drawable.ev_scooter3
        );

        // Adapter
        ImageAdapter adapter = new ImageAdapter(images);
        viewPager.setAdapter(adapter);

        // Nút mũi tên
        btnPrev.setOnClickListener(v -> {
            int prev = viewPager.getCurrentItem() - 1;
            if (prev >= 0) viewPager.setCurrentItem(prev, true);
        });

        btnNext.setOnClickListener(v -> {
            int next = viewPager.getCurrentItem() + 1;
            if (next < images.size()) viewPager.setCurrentItem(next, true);
        });
    }
}
