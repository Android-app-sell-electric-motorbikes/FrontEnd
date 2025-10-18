package com.example.evshop.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.evshop.R;
import com.example.evshop.util.CartManager;
import com.example.evshop.domain.models.Product;

import java.util.Arrays;
import java.util.List;

public class ProductDetailsActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ImageButton btnPrev, btnNext;
    private Button btnPlus, btnMinus, btnAddToCart;
    private EditText quantityInput;
    private TextView tvTitle, tvPrice;
    private RatingBar ratingBar;

    private Product currentProduct; // sản phẩm đang xem

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.product_details);

        // Áp dụng insets cho status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Liên kết view
        viewPager = findViewById(R.id.viewPager);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnPlus = findViewById(R.id.btn_plus);
        btnMinus = findViewById(R.id.btn_minus);
        btnAddToCart = findViewById(R.id.add_to_cart_button);
        quantityInput = findViewById(R.id.quantity_input);
        tvTitle = findViewById(R.id.product_title);
        tvPrice = findViewById(R.id.product_price);
        ratingBar = findViewById(R.id.product_rating);

        // Giả lập sản phẩm (sau này sẽ nhận qua Intent)
        currentProduct = new Product(
                "1",
                "Xe Máy Điện NISPA VERA X",
                "NISPA",
                R.drawable.ev_scooter,
                18990000,
                4.5f,
                "Xe máy điện"
        );

        // Set dữ liệu hiển thị
        tvTitle.setText(currentProduct.getName());
        tvPrice.setText(currentProduct.getPriceVnd() + "đ");
        ratingBar.setRating(currentProduct.getRating());

        // Danh sách ảnh (demo)
        List<Integer> images = Arrays.asList(
                R.drawable.ev_scooter,
                R.drawable.ev_scooter2,
                R.drawable.ev_scooter3
        );
        viewPager.setAdapter(new ImageAdapter(images));

        // Nút chuyển ảnh
        btnPrev.setOnClickListener(v -> {
            int prev = viewPager.getCurrentItem() - 1;
            if (prev >= 0) viewPager.setCurrentItem(prev, true);
        });
        btnNext.setOnClickListener(v -> {
            int next = viewPager.getCurrentItem() + 1;
            if (next < images.size()) viewPager.setCurrentItem(next, true);
        });

        // Nút cộng trừ
        btnPlus.setOnClickListener(v -> {
            int quantity = getQuantity();
            quantityInput.setText(String.valueOf(quantity + 1));
        });
        btnMinus.setOnClickListener(v -> {
            int quantity = getQuantity();
            if (quantity > 1) quantityInput.setText(String.valueOf(quantity - 1));
        });

        // 🛒 Nút thêm vào giỏ
        btnAddToCart.setOnClickListener(v -> {
            int quantity = getQuantity();
            CartManager.getInstance().addToCart(currentProduct, quantity);
            Toast.makeText(this,
                    "Đã thêm " + quantity + " sản phẩm vào giỏ hàng!",
                    Toast.LENGTH_SHORT).show();
        });
    }

    /** Lấy số lượng hợp lệ từ input */
    private int getQuantity() {
        String text = quantityInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return 1;
        try {
            int q = Integer.parseInt(text);
            return Math.max(q, 1);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
