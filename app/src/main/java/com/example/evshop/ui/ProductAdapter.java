// ui/ProductAdapter.java
package com.example.evshop.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.evshop.R;
import com.example.evshop.models.Product;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi","VN"));
    private List<Product> items;
    public ProductAdapter(List<Product> items){ this.items = items; }
    public void setItems(List<Product> list){ this.items = list; notifyDataSetChanged(); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        Product p = items.get(position);
        h.name.setText(p.name);
        h.brand.setText(p.brand);
        h.price.setText(currency.format(p.price));
        h.rating.setRating(p.rating);
        Glide.with(h.itemView).load(p.imageUrl).placeholder(R.drawable.ic_placeholder).into(h.image);
        h.btnAdd.setOnClickListener(v -> {
            // TODO: thêm vào giỏ (sau). Tạm thời toast/snackbar:
            // Snackbar.make(v, "Đã thêm " + p.name, Snackbar.LENGTH_SHORT).show();
        });
    }

    @Override public int getItemCount() { return items==null?0:items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image; TextView name, brand, price; RatingBar rating; Button btnAdd;
        VH(@NonNull View v){
            super(v);
            image = v.findViewById(R.id.img);
            name = v.findViewById(R.id.tvName);
            brand = v.findViewById(R.id.tvBrand);
            price = v.findViewById(R.id.tvPrice);
            rating = v.findViewById(R.id.ratingBar);
            btnAdd = v.findViewById(R.id.btnAddToCart);
        }
    }
}
