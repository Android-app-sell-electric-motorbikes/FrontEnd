package com.example.evshop.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.evshop.R;
import com.example.evshop.domain.models.CartItem;
import com.example.evshop.util.CartManager;
import com.example.evshop.util.Formatters;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItems;
    private final Runnable onCartUpdated;

    public CartAdapter(List<CartItem> cartItems, Runnable onCartUpdated) {
        this.cartItems = cartItems;
        this.onCartUpdated = onCartUpdated;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view, this);
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
    
    public void removeItem(int position) {
        if (position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
            notifyItemRemoved(position);
            onCartUpdated.run();
        }
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvQuantity, tvTotal;
        Button btnMinus, btnPlus;
        ImageButton btnRemove;
        private final CartAdapter adapter;

        public CartViewHolder(@NonNull View itemView, CartAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
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
            tvName.setText(item.getVehicle().getVersion().getVersionName());
            tvPrice.setText("Giá: " + Formatters.currency(item.getVehicle().getPrice()));
            updateItemView(item);

            if (item.getVehicle().getImgUrl() != null && !item.getVehicle().getImgUrl().isEmpty()){
                Glide.with(itemView.getContext()).load(item.getVehicle().getImgUrl().get(0)).into(imgProduct);
            }

            btnPlus.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() + 1;
                CartManager.getInstance().updateQuantity(item.getVehicle().getId(), newQuantity);
                item.setQuantity(newQuantity);
                updateItemView(item);
                adapter.onCartUpdated.run();
            });

            btnMinus.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    int newQuantity = item.getQuantity() - 1;
                    CartManager.getInstance().updateQuantity(item.getVehicle().getId(), newQuantity);
                    item.setQuantity(newQuantity);
                    updateItemView(item);
                    adapter.onCartUpdated.run();
                }
            });

            btnRemove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION){
                    CartManager.getInstance().removeFromCart(item.getVehicle().getId());
                    adapter.removeItem(position);
                }
            });
        }

        private void updateItemView(CartItem item) {
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            tvTotal.setText("Tổng: " + Formatters.currency(item.getTotalPrice()));
        }
    }
}
