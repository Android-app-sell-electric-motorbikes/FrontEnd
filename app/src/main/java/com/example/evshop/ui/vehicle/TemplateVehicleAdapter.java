// File: D:/PRM391/FrontEnd/app/src/main/java/com/example/evshop/ui/vehicle/TemplateVehicleAdapter.java
package com.example.evshop.ui.vehicle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

// Adapter được đơn giản hóa cho giao diện lưới
public class TemplateVehicleAdapter extends RecyclerView.Adapter<TemplateVehicleAdapter.ViewHolder> {

    // Interface để xử lý sự kiện click. Activity sẽ implement nó.
    public interface OnItemClickListener {
        void onItemClick(TemplateVehicle vehicle);
    }

    private List<TemplateVehicle> vehicles = new ArrayList<>();
    private final OnItemClickListener listener;

    public TemplateVehicleAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Phương thức cập nhật danh sách
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
        TemplateVehicle vehicle = vehicles.get(position);
        // Truyền cả vehicle và listener vào ViewHolder
        holder.bind(vehicle, listener);
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    // =========================================================================
    // ***           VIEW HOLDER ĐÃ ĐƯỢC CẬP NHẬT CHO ĐÚNG LAYOUT          ***
    // =========================================================================
    static class ViewHolder extends RecyclerView.ViewHolder {
        // Khai báo thêm TextView cho description
        final ImageView imgVehicle;
        final TextView txtModel, txtColor, txtPrice, txtDescription; // <<< THÊM TXTDESCRIPTION
        private final Context context;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();

            // Ánh xạ các View ID
            imgVehicle = itemView.findViewById(R.id.imgVehicle);
            txtModel = itemView.findViewById(R.id.txtModel);
            txtColor = itemView.findViewById(R.id.txtColor);
            txtPrice = itemView.findViewById(R.id.txtPrice);

            // Ánh xạ ID của TextView mới
            txtDescription = itemView.findViewById(R.id.txtDescription); // <<< THÊM DÒNG NÀY
        }

        void bind(final TemplateVehicle vehicle, final OnItemClickListener listener) {
            if (vehicle == null) return;

            // 1. Gán dữ liệu cho các View cơ bản (phần này không đổi)
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

            // ==================================================================
            // *** 2. GÁN DỮ LIỆU CHO TEXTVIEW DESCRIPTION (PHẦN QUAN TRỌNG) ***
            // ==================================================================
            // Giả sử model TemplateVehicle của bạn có phương thức `getDescription()`
            if (vehicle.getDescription() != null && !vehicle.getDescription().isEmpty()) {
                txtDescription.setVisibility(View.VISIBLE);
                txtDescription.setText(vehicle.getDescription());
            } else {
                // Nếu không có description, hãy ẩn TextView đi để không chiếm khoảng trống
                txtDescription.setVisibility(View.GONE);
            }

            // 3. Thiết lập sự kiện click cho toàn bộ item (không đổi)
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(vehicle);
                }
            });
        }
    }
    
}
