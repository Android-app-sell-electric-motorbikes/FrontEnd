package com.example.evshop.ui.home;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.evshop.R;
import com.example.evshop.domain.models.Product;
import com.example.evshop.util.Formatters;
import java.util.*;


public class ProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_PRODUCT = 1;
    private static final int TYPE_SHIMMER = 2;
    private static final int TYPE_EMPTY = 3;
    private static final int TYPE_ERROR = 4;


    private final List<Product> data = new ArrayList<>();
    private boolean showShimmer = false;
    private boolean showEmpty = false;
    private boolean showError = false;
    private Runnable retry;


    interface Listener { void onClick(Product p); }
    private final Listener listener;


    public ProductAdapter(Listener listener){ this.listener = listener; }


    public void submit(List<Product> items) {
        data.clear(); data.addAll(items);
        showEmpty = items.isEmpty(); showError = false; showShimmer=false;
        notifyDataSetChanged();
    }


    public void setLoading(boolean loading) { this.showShimmer = loading; notifyDataSetChanged(); }
    public void setError(boolean error, Runnable retry){ this.showError = error; this.retry=retry; notifyDataSetChanged(); }


    @Override public int getItemViewType(int position) {
        if (showError) return TYPE_ERROR;
        if (showEmpty) return TYPE_EMPTY;
        if (showShimmer) return TYPE_SHIMMER;
        return TYPE_PRODUCT;
    }


    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType==TYPE_PRODUCT) return new ProductVH(inf.inflate(R.layout.item_product, parent, false));
        if (viewType==TYPE_SHIMMER) return new ShimmerVH(inf.inflate(R.layout.item_shimmer, parent, false));
        if (viewType==TYPE_EMPTY) return new EmptyVH(inf.inflate(R.layout.item_empty, parent, false));
        return new ErrorVH(inf.inflate(R.layout.item_error, parent, false));
    }


    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        if (h instanceof ProductVH) {
            Product p = data.get(position);
            ProductVH vh = (ProductVH) h;
            Glide.with(vh.img.getContext()).load(p.getImageUrl()).into(vh.img);
            vh.name.setText(p.getName());
            vh.brand.setText(p.getBrand());
            vh.price.setText(Formatters.currency(p.getPriceVnd()));
            vh.rating.setText(String.format(Locale.getDefault(), "%.1f", p.getRating()));
            vh.itemView.setOnClickListener(v -> listener.onClick(p));
        } else if (h instanceof ErrorVH) {
            ((ErrorVH) h).btnRetry.setOnClickListener(v -> { if (retry!=null) retry.run(); });
        }
    }


    @Override public int getItemCount() {
        if (showError || showEmpty || showShimmer) return 4; // grid looks balanced
        return data.size();
    }


    static class ProductVH extends RecyclerView.ViewHolder {
        ImageView img; TextView name, brand, price, rating;
        ProductVH(View v){ super(v); img=v.findViewById(R.id.img); name=v.findViewById(R.id.txtName);
            brand=v.findViewById(R.id.txtBrand); price=v.findViewById(R.id.txtPrice); rating=v.findViewById(R.id.txtRating); }
    }
    static class ShimmerVH extends RecyclerView.ViewHolder { ShimmerVH(View v){ super(v);} }
    static class EmptyVH extends RecyclerView.ViewHolder { EmptyVH(View v){ super(v);} }
    static class ErrorVH extends RecyclerView.ViewHolder { Button btnRetry; ErrorVH(View v){ super(v); btnRetry=v.findViewById(R.id.btnRetry);} }
}
