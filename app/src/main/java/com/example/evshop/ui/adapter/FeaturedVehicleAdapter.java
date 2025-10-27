package com.example.evshop.ui.adapter;

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
import com.example.evshop.util.Formatters;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter này hiển thị danh sách xe (ví dụ: Xe Nổi Bật) theo chiều dọc.
 * Nó sử dụng layout `item_vehicle.xml` để mỗi item hiển thị to và rõ ràng.
 */
public class FeaturedVehicleAdapter extends RecyclerView.Adapter<FeaturedVehicleAdapter.FeaturedViewHolder> {

    private List<TemplateVehicle> vehicles = new ArrayList<>();

    // Không cần biến `context` riêng nữa vì có thể lấy từ itemView.

    // --- Interface để xử lý sự kiện click ---
    public interface OnItemClickListener {
        void onItemClick(TemplateVehicle vehicle);
    }
    private final OnItemClickListener listener;

    // --- Constructor ---
    public FeaturedVehicleAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Cập nhật danh sách xe và vẽ lại RecyclerView.
     */
    public void setVehicles(List<TemplateVehicle> newVehicles) {
        this.vehicles = (newVehicles != null) ? newVehicles : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Luôn sử dụng layout item_vehicle (layout to, đẹp)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle, parent, false);
        return new FeaturedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        TemplateVehicle vehicle = vehicles.get(position);
        holder.bind(vehicle, listener);
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    // ===============================================================================
    //  LỚP VIEW HOLDER
    // ===============================================================================

    /**
     * ViewHolder này chứa các View từ layout `item_vehicle.xml`.
     * Nó đã được tối ưu để chỉ ánh xạ những View cần thiết cho danh sách này.
     */
    static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        // Khai báo các View có trong layout `item_vehicle.xml`
        final ImageView imgVehicle;
        final TextView txtModel;
        final TextView txtColor;
        final TextView txtPrice;

        public FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các View từ layout
            imgVehicle = itemView.findViewById(R.id.imgVehicle);
            txtModel = itemView.findViewById(R.id.txtModel);
            txtColor = itemView.findViewById(R.id.txtColor);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }

        /**
         * Gán dữ liệu của một chiếc xe vào các View tương ứng.
         */
        public void bind(final TemplateVehicle vehicle, final OnItemClickListener listener) {
            // Kiểm tra dữ liệu null để tránh ứng dụng bị crash
            if (vehicle.getVersion() != null) {
                txtModel.setText(vehicle.getVersion().getVersionName());
            } else {
                txtModel.setText("N/A"); // Hoặc một giá trị mặc định
            }

            if (vehicle.getColor() != null) {
                txtColor.setText(vehicle.getColor().getColorName());
            } else {
                txtColor.setText(""); // Ẩn đi nếu không có màu
            }

            // Định dạng giá tiền
            txtPrice.setText(Formatters.currency(vehicle.getPrice()));

            // Tải ảnh bằng Glide
            if (vehicle.getImgUrl() != null && !vehicle.getImgUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(vehicle.getImgUrl().get(0)) // Lấy ảnh đầu tiên trong danh sách
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(imgVehicle);
            } else {
                // Nếu không có ảnh, hiển thị ảnh mặc định
                imgVehicle.setImageResource(R.drawable.ic_placeholder);
            }

            // Thiết lập sự kiện click cho toàn bộ item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(vehicle);
                }
            });
        }
    }
}
