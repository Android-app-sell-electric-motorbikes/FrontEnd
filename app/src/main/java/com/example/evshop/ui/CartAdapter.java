package com.example.evshop.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.domain.models.CartItem;
import com.example.evshop.util.CartManager;

import java.util.List;

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
        Button btnMinus, btnPlus, btnRemove;

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
            imgProduct.setImageResource(item.getProduct().getImageUrl());
            tvName.setText(item.getProduct().getName());
            tvPrice.setText("Giá: " + item.getProduct().getPriceVnd() + "₫");
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            tvTotal.setText("Tổng: " + (item.getProduct().getPriceVnd() * item.getQuantity()) + "₫");

            // Nút tăng
            btnPlus.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() + 1;
                item.setQuantity(newQuantity);
                CartManager.getInstance().updateQuantity(item.getProduct().getId(), item.getQuantity());
                notifyItemChanged(getAdapterPosition());
                onCartUpdated.run();
            });

            // Nút giảm
            btnMinus.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    int newQuantity = item.getQuantity() - 1;
                    item.setQuantity(newQuantity);
                    CartManager.getInstance().updateQuantity(item.getProduct().getId(), item.getQuantity());
                    notifyItemChanged(getAdapterPosition());
                    onCartUpdated.run();
                }
            });

            // Nút xoá
            btnRemove.setOnClickListener(v -> {
                CartManager.getInstance().removeFromCart(item.getProduct().getId());
                cartItems.remove(getAdapterPosition());
                notifyItemRemoved(getAdapterPosition());
                onCartUpdated.run();
            });
        }
    }
}
