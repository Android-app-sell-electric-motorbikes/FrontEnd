package com.example.evshop.ui.home;

import android.view.*;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.evshop.R;
import java.util.List;


public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.VH> {
    private final List<String> images;
    public BannerAdapter(List<String> images){ this.images = images; }


    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new VH(v);
    }


    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Glide.with(h.img.getContext()).load(images.get(pos)).into(h.img);
        h.img.setContentDescription("Banner "+pos);
    }


    @Override public int getItemCount() { return images.size(); }


    static class VH extends RecyclerView.ViewHolder {
        ImageView img; VH(View v){ super(v); img = v.findViewById(R.id.imgBanner);} }
}
