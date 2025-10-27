package com.example.evshop.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.evshop.R;
import com.example.evshop.domain.models.CartItem;
import com.example.evshop.domain.models.Product;
import com.example.evshop.util.CartManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItems;
    private final Runnable onCartUpdated; // callback dùng để cập nhật tổng tiền ở CartFragment

    public CartAdapter(List<CartItem> cartItems, Runnable onCartUpdated) {
        this.cartItems = cartItems;
        this.onCartUpdated = onCartUpdated;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvQuantity, tvTotal;
        ImageButton btnMinus, btnPlus, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotal = itemView.findViewById(R.id.tvTotalItem);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(CartItem item) {
            Product product = item.getProduct();
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            
            // Load image: Kiểm tra xem dùng URL string hay drawable resource
            if (product.hasImageUrlString()) {
                // Load từ URL (data từ API)
                Glide.with(itemView.getContext())
                    .load(product.getImageUrlString())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(imgProduct);
            } else {
                // Load từ drawable resource (mock data)
                imgProduct.setImageResource(product.getImageUrl());
            }
            
            tvName.setText(product.getName());
            tvPrice.setText(formatter.format(product.getPriceVnd()) + "₫");
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            tvTotal.setText(formatter.format(product.getPriceVnd() * item.getQuantity()) + "₫");

            // ===== TÍNH NĂNG MỚI: Click vào item để xem chi tiết sản phẩm =====
            itemView.setOnClickListener(v -> {
                Context ctx = v.getContext();
                Intent intent = new Intent(ctx, ProductDetailsActivity.class);
                intent.putExtra("product_id", product.getId());
                intent.putExtra("product_name", product.getName());
                intent.putExtra("product_price", product.getPriceVnd());
                intent.putExtra("product_rating", product.getRating());
                intent.putExtra("product_image", product.getImageUrl());
                ctx.startActivity(intent);
            });

            // Nút tăng số lượng
            btnPlus.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() + 1;
                item.setQuantity(newQuantity);
                CartManager.getInstance().updateQuantity(product.getId(), newQuantity);
                notifyItemChanged(getAdapterPosition());
                onCartUpdated.run();
            });

            // Nút giảm số lượng
            btnMinus.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    int newQuantity = item.getQuantity() - 1;
                    item.setQuantity(newQuantity);
                    CartManager.getInstance().updateQuantity(product.getId(), newQuantity);
                    notifyItemChanged(getAdapterPosition());
                    onCartUpdated.run();
                }
            });

            // Nút xóa sản phẩm khỏi giỏ hàng
            btnRemove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    CartManager.getInstance().removeFromCart(product.getId());
                    cartItems.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, cartItems.size());
                    onCartUpdated.run();
                }
            });
        }
    }
}
