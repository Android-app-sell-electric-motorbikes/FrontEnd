package com.example.evshop.ui.cart;

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
import com.example.evshop.domain.models.TemplateVehicle; // THÊM IMPORT
import com.example.evshop.util.Formatters;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItems;
    private final Runnable onCartUpdate;

    public CartAdapter(List<CartItem> cartItems, Runnable onCartUpdate) {
        this.cartItems = cartItems;
        this.onCartUpdate = onCartUpdate;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.bind(cartItem);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvQuantity, tvTotalItem;
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
            tvTotalItem = itemView.findViewById(R.id.tvTotalItem);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(final CartItem cartItem) {
            if (cartItem == null || cartItem.getVehicle() == null) {
                // Nếu item hoặc vehicle bị null, ẩn toàn bộ view để tránh crash
                itemView.setVisibility(View.GONE);
                return;
            }
            itemView.setVisibility(View.VISIBLE);

            TemplateVehicle vehicle = cartItem.getVehicle();
            TemplateVehicle.Version version = vehicle.getVersion();

            // ** KIỂM TRA NULL AN TOÀN **
            if (version != null && version.getVersionName() != null) {
                tvName.setText(version.getVersionName());
            } else {
                tvName.setText("Tên xe đang cập nhật");
            }

            tvPrice.setText("Giá: " + Formatters.currency(vehicle.getPrice()));
            updateItemView(cartItem);

            if (vehicle.getImgUrl() != null && !vehicle.getImgUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(vehicle.getImgUrl().get(0))
                        .placeholder(R.drawable.placeholder_vehicle)
                        .error(R.drawable.ic_placeholder) // Ảnh lỗi
                        .into(imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.placeholder_vehicle);
            }

            btnPlus.setOnClickListener(v -> {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                CartManager.getInstance().updateQuantity(vehicle.getId(), cartItem.getQuantity());
                updateItemView(cartItem);
                adapter.onCartUpdate.run();
            });

            btnMinus.setOnClickListener(v -> {
                if (cartItem.getQuantity() > 1) {
                    cartItem.setQuantity(cartItem.getQuantity() - 1);
                    CartManager.getInstance().updateQuantity(vehicle.getId(), cartItem.getQuantity());
                    updateItemView(cartItem);
                    adapter.onCartUpdate.run();
                }
            });

            btnRemove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    CartManager.getInstance().removeFromCart(vehicle.getId());
                    adapter.removeItem(position);
                    adapter.onCartUpdate.run();
                }
            });
        }

        private void updateItemView(CartItem cartItem) {
            tvQuantity.setText(String.valueOf(cartItem.getQuantity()));
            tvTotalItem.setText("Tổng: " + Formatters.currency(cartItem.getTotalPrice()));
        }
    }
}
