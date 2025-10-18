package com.example.evshop.ui.vehicle;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.domain.models.Vehicle;
import com.squareup.picasso.Picasso;

import java.text.BreakIterator;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.ViewHolder> {

    private final List<Vehicle> vehicles;
    private final Context context;

    public VehicleAdapter(List<Vehicle> vehicles, Context context) {
        this.vehicles = vehicles;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vehicle v = vehicles.get(position);
        if(v == null){
            Log.e("VehicleAdapter", "Vehicle object at position " + position + " is null.");
            return;
        }
        // 1. Đặt tên và màu sắc
        if(v.version != null) {
            String fullName = v.version.modelName + " " + v.version.versionName;
            holder.txtModel.setText(fullName);
        }
        // 2. Định dạng giá tiền
        holder.txtPrice.setText(String.format("%,.0f đ",v.costPrice));
        // 3. Tải hình ảnh bằng Picasso
        List<String> images = v.imgUrl;

        if(images != null && !images.isEmpty()){
            String imageUrl = images.get(0);
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(holder.imgVehicle);
        } else {
            holder.imgVehicle.setImageResource(R.drawable.ic_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPrice;
        ImageView imgVehicle;
        TextView txtModel, txtColor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgVehicle = itemView.findViewById(R.id.imgVehicle);
            txtModel = itemView.findViewById(R.id.txtModel);
            txtColor = itemView.findViewById(R.id.txtColor);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }
    }
}
