// File: D:/PRM391/FrontEnd/app/src/main/java/com/example/evshop/ui/vehicle/TemplateVehicleAdapter.java
package com.example.evshop.ui.vehicle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
// *** BƯỚC 4.1: THÊM IMPORT CHO RATINGBAR ***
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.evshop.R;
import com.example.evshop.domain.models.TemplateVehicle;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TemplateVehicleAdapter extends RecyclerView.Adapter<TemplateVehicleAdapter.ViewHolder> {

    // ... (phần interface, constructor, và các phương thức chính không đổi) ...
    public interface OnItemClickListener {
        void onItemClick(TemplateVehicle vehicle);
    }

    private List<TemplateVehicle> vehicles = new ArrayList<>();
    private final OnItemClickListener listener;

    public TemplateVehicleAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateVehicles(List<TemplateVehicle> newVehicles) {
        this.vehicles.clear();
        this.vehicles.addAll(newVehicles);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(vehicles.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    // =========================================================================
    // ***           BƯỚC 4.2: CẬP NHẬT VIEW HOLDER                        ***
    // =========================================================================
    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgVehicle;
        final TextView txtModel, txtColor, txtPrice, txtDescription;
        // Khai báo thêm RatingBar
        final RatingBar ratingBar;
        private final Context context;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();

            // Ánh xạ các View ID đã có
            imgVehicle = itemView.findViewById(R.id.imgVehicle);
            txtModel = itemView.findViewById(R.id.txtModel);
            txtColor = itemView.findViewById(R.id.txtColor);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtDescription = itemView.findViewById(R.id.txtDescription);

            // Ánh xạ ID của RatingBar
            ratingBar = itemView.findViewById(R.id.rating_bar);
        }

        void bind(final TemplateVehicle vehicle, final OnItemClickListener listener) {
            if (vehicle == null) return;

            // ... (Code gán dữ liệu cho imgVehicle, txtModel, txtColor, txtPrice không đổi)
            if (vehicle.getVersion() != null) {
                txtModel.setText(vehicle.getVersion().getVersionName());
            }
            if (vehicle.getColor() != null) {
                txtColor.setText(vehicle.getColor().getColorName());
            }
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            txtPrice.setText(currencyFormatter.format(vehicle.getPrice()));
            List<String> images = vehicle.getImgUrl();
            if (images != null && !images.isEmpty()) {
                Glide.with(context)
                        .load(images.get(0))
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(imgVehicle);
            } else {
                imgVehicle.setImageResource(R.drawable.ic_placeholder);
            }

            // *** BƯỚC 4.3: GÁN DỮ LIỆU CHO RATINGBAR ***
            // Ép kiểu `double` của rating thành `float` mà RatingBar yêu cầu
            ratingBar.setRating((float) vehicle.getRating());

            // ... (Code gán dữ liệu cho txtDescription và setOnClickListener không đổi)
            if (vehicle.getDescription() != null && !vehicle.getDescription().isEmpty()) {
                txtDescription.setVisibility(View.VISIBLE);
                txtDescription.setText(vehicle.getDescription());
            } else {
                txtDescription.setVisibility(View.GONE);
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(vehicle);
                }
            });
        }
    }
}
