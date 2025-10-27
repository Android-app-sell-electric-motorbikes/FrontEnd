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
import com.example.evshop.domain.models.Product;
import com.example.evshop.util.CartManager; // Giả sử bạn có lớp này để quản lý giỏ hàng

import java.util.Arrays;
import java.util.List;

public class ProductDetailsActivity extends AppCompatActivity {

    // --- SỬA: Khai báo tất cả các view cần dùng ở cấp độ class ---
    private ViewPager2 viewPager;
    private ImageButton btnPrev, btnNext, btnBack;
    private TextView txtName, txtPrice;
    private RatingBar ratingBar;
    private EditText quantityInput;
    private ImageButton btnPlus, btnMinus;
    private Button btnAddToCart;

    private Product currentProduct; // Sản phẩm đang xem, để thêm vào giỏ hàng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.product_details);

        // Áp dụng padding cho system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- SỬA: Gom tất cả việc liên kết View vào một nơi duy nhất ---
        initViews();

        // --- SỬA: Lấy dữ liệu từ Intent và hiển thị ---
        displayProductInfo();

        // --- Thiết lập các thành phần khác ---
        setupImageSlider();
        setupQuantityButtons();
        setupAddToCartButton();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        txtName = findViewById(R.id.product_title);
        txtPrice = findViewById(R.id.product_price);
        ratingBar = findViewById(R.id.product_rating);
        quantityInput = findViewById(R.id.quantity_input);
        btnPlus = findViewById(R.id.btn_plus);
        btnMinus = findViewById(R.id.btn_minus);
        btnAddToCart = findViewById(R.id.add_to_cart_button);
        
        // Setup nút back
        btnBack.setOnClickListener(v -> finish());
    }

    private void displayProductInfo() {
        // Lấy dữ liệu từ Intent
        String name = getIntent().getStringExtra("product_name");
        // Giá tiền nên được truyền dưới dạng số (long hoặc double) để xử lý
        long price = getIntent().getLongExtra("product_price", 0);
        float rating = getIntent().getFloatExtra("product_rating", 4.5f);
        String productId = getIntent().getStringExtra("product_id"); // Cần có ID để thêm vào giỏ hàng

        // Hiển thị dữ liệu
        txtName.setText(name);
        txtPrice.setText(String.format("%,dđ", price)); // Định dạng giá tiền cho đẹp
        ratingBar.setRating(rating);

        // Tạo một đối tượng Product tạm thời để dùng cho việc thêm vào giỏ hàng
        // (Lưu ý: ImageUrl ở đây chỉ là giả lập, vì ta không truyền ảnh qua Intent)
        currentProduct = new Product(productId, name, "Brand", R.drawable.ev_scooter, price, rating, "Category");
    }

    private void setupImageSlider() {
        // Danh sách ảnh (demo)
        List<Integer> images = Arrays.asList(
                R.drawable.ev_scooter,
                R.drawable.ev_scooter2,
                R.drawable.ev_scooter3
        );
        ImageAdapter adapter = new ImageAdapter(images);
        viewPager.setAdapter(adapter);

        // Nút chuyển ảnh
        btnPrev.setOnClickListener(v -> {
            int prev = viewPager.getCurrentItem() - 1;
            if (prev >= 0) viewPager.setCurrentItem(prev, true);
        });

        btnNext.setOnClickListener(v -> {
            int next = viewPager.getCurrentItem() + 1;
            if (next < images.size()) viewPager.setCurrentItem(next, true);
        });
    }

    private void setupQuantityButtons() {
        // Xử lý nút cộng
        btnPlus.setOnClickListener(v -> {
            int current = getQuantity();
            quantityInput.setText(String.valueOf(current + 1));
        });

        // Xử lý nút trừ
        btnMinus.setOnClickListener(v -> {
            int current = getQuantity();
            if (current > 1) {
                quantityInput.setText(String.valueOf(current - 1));
            }
        });
    }

    private void setupAddToCartButton() {
        btnAddToCart.setOnClickListener(v -> {
            if (currentProduct != null) {
                int quantity = getQuantity();
                // Sử dụng CartManager để thêm sản phẩm
                CartManager.getInstance().addToCart(currentProduct, quantity);
                Toast.makeText(this, "Đã thêm " + quantity + " sản phẩm vào giỏ!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lỗi: Không tìm thấy thông tin sản phẩm.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private int getQuantity() {
        String text = quantityInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return 1;
        try {
            int q = Integer.parseInt(text);
            return Math.max(q, 1);
        } catch (NumberFormatException e) {
            return 1; // Nếu người dùng nhập chữ, mặc định là 1
        }
    }

}
