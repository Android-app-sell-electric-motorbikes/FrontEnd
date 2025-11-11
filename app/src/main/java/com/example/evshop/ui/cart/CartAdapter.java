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
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.util.Formatters;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private final CartItemListener listener;

    // ** ĐỊNH NGHĨA INTERFACE CÒN THIẾU **
    public interface CartItemListener {
        void onIncreaseQuantity(CartItem item);
        void onDecreaseQuantity(CartItem item);
        void onRemoveItem(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, CartItemListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.bind(cartItem, listener);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    // ** PHƯƠNG THỨC ĐỂ ACTIVITY CẬP NHẬT DỮ LIỆU **
    public void updateCartItems(List<CartItem> newItems) {
        this.cartItems.clear();
        this.cartItems.addAll(newItems);
        notifyDataSetChanged();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvQuantity, tvTotalItem;
        Button btnMinus, btnPlus;
        ImageButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotalItem = itemView.findViewById(R.id.tvTotalItem);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(final CartItem cartItem, final CartItemListener listener) {
            if (cartItem == null || cartItem.getVehicle() == null) {
                itemView.setVisibility(View.GONE);
                return;
            }
            itemView.setVisibility(View.VISIBLE);

            TemplateVehicle vehicle = cartItem.getVehicle();
            tvName.setText(vehicle.getVersion().getVersionName());
            tvPrice.setText("Giá: " + Formatters.currency(vehicle.getPrice()));
            updateItemView(cartItem);

            if (vehicle.getImgUrl() != null && !vehicle.getImgUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(vehicle.getImgUrl().get(0))
                        .placeholder(R.drawable.placeholder_vehicle)
                        .error(R.drawable.ic_placeholder)
                        .into(imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.placeholder_vehicle);
            }

            // ** GỌI LISTENER KHI BẤM NÚT **
            btnPlus.setOnClickListener(v -> listener.onIncreaseQuantity(cartItem));
            btnMinus.setOnClickListener(v -> listener.onDecreaseQuantity(cartItem));
            btnRemove.setOnClickListener(v -> listener.onRemoveItem(cartItem));
        }

        private void updateItemView(CartItem cartItem) {
            tvQuantity.setText(String.valueOf(cartItem.getQuantity()));
            tvTotalItem.setText("Tổng: " + Formatters.currency(cartItem.getTotalPrice()));
        }
    }
}
