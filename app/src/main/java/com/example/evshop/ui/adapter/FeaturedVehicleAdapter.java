package com.example.evshop.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil; // <-- Import mới
import androidx.recyclerview.widget.ListAdapter; // <-- Import mới, thay cho RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.evshop.R;
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.util.Formatters;

// 1. THAY ĐỔI: Kế thừa từ ListAdapter thay vì RecyclerView.Adapter
public class FeaturedVehicleAdapter extends ListAdapter<TemplateVehicle, FeaturedVehicleAdapter.VehicleViewHolder> {

    private final OnVehicleClickListener listener;

    // Interface để xử lý sự kiện click
    public interface OnVehicleClickListener {
        void onVehicleClick(TemplateVehicle template);
    }

    // 2. THAY ĐỔI: Constructor nhận vào listener và một đối tượng DiffUtil.ItemCallback
    public FeaturedVehicleAdapter(OnVehicleClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        // `getItem(position)` là phương thức của ListAdapter để lấy item
        TemplateVehicle currentVehicle = getItem(position);
        holder.bind(currentVehicle, listener);
    }

    // 3. XÓA BỎ: Hàm `setVehicles` và `getItemCount` không còn cần thiết
    // ListAdapter sẽ tự quản lý kích thước danh sách.

    // ViewHolder class (GIỮ NGUYÊN, KHÔNG CẦN SỬA)
    static class VehicleViewHolder extends RecyclerView.ViewHolder {
        ImageView imgVehicle;
        TextView txtVersionName, txtColor, txtPrice;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            imgVehicle = itemView.findViewById(R.id.imgVehicle);
            txtVersionName = itemView.findViewById(R.id.txtModel);
            txtColor = itemView.findViewById(R.id.txtColor);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }

        public void bind(final TemplateVehicle template, final OnVehicleClickListener listener) {
            txtVersionName.setText(template.getVersion() != null ? template.getVersion().getVersionName() : "N/A");
            txtColor.setText(template.getColor() != null ? template.getColor().getColorName() : "N/A");
            txtPrice.setText(Formatters.currency(template.getPrice()));

            if (template.getImgUrl() != null && !template.getImgUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(template.getImgUrl().get(0))
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(imgVehicle);
            } else {
                imgVehicle.setImageResource(R.drawable.ic_placeholder);
            }

            itemView.setOnClickListener(v -> listener.onVehicleClick(template));
        }
    }

    // 4. THÊM VÀO: Đây là phần quan trọng nhất của ListAdapter
    // Nó giúp RecyclerView biết item nào đã thay đổi, thêm, xóa để cập nhật hiệu quả
    private static final DiffUtil.ItemCallback<TemplateVehicle> DIFF_CALLBACK = new DiffUtil.ItemCallback<TemplateVehicle>() {
        @Override
        public boolean areItemsTheSame(@NonNull TemplateVehicle oldItem, @NonNull TemplateVehicle newItem) {
            // So sánh ID để biết có phải là cùng một item hay không
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull TemplateVehicle oldItem, @NonNull TemplateVehicle newItem) {
            // So sánh nội dung để biết item có cần vẽ lại hay không
            // (Bạn có thể thêm các so sánh khác như giá, màu sắc nếu cần)
            return oldItem.getVersion().getVersionName().equals(newItem.getVersion().getVersionName()) &&
                    oldItem.getPrice() == newItem.getPrice();
        }
    };
}
