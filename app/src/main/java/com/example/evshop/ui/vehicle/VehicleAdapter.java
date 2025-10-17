package com.example.evshop.ui.vehicle;

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

import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.ViewHolder> {

    private final List<Vehicle> vehicles;

    public VehicleAdapter(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
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
        holder.txtModel.setText(v.version != null ? v.version.modelName : "N/A");
        holder.txtColor.setText(v.color != null ? v.color.colorName : "N/A");

        if (v.imageUrl != null && !v.imageUrl.isEmpty()) {
            Picasso.get().load(v.imageUrl).into(holder.imgVehicle);
        } else {
            holder.imgVehicle.setImageResource(R.drawable.placeholder_vehicle);
        }
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgVehicle;
        TextView txtModel, txtColor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgVehicle = itemView.findViewById(R.id.imgVehicle);
            txtModel = itemView.findViewById(R.id.txtModel);
            txtColor = itemView.findViewById(R.id.txtColor);
        }
    }
}
