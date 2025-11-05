package com.example.evshop.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.domain.models.InventoryItem;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private List<InventoryItem> inventoryList;

    public InventoryAdapter(List<InventoryItem> inventoryList) {
        this.inventoryList = inventoryList;
    }

    // --- 1. SỬA LẠI VIEWHOLDER ---
    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView modelName;      // MỚI
        TextView colorName;      // MỚI
        TextView versionName;
        TextView quantity;
        TextView warehouseNames;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các View mới từ layout
            modelName = itemView.findViewById(R.id.tv_model_name);
            colorName = itemView.findViewById(R.id.tv_color_name);
            // Bỏ đi tv_price
            versionName = itemView.findViewById(R.id.tv_version_name);
            quantity = itemView.findViewById(R.id.tv_quantity);
            warehouseNames = itemView.findViewById(R.id.tv_warehouse_names);
        }
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_vehicle, parent, false);
        return new InventoryViewHolder(view);
    }

    // --- 2. SỬA LẠI ONBINDVIEWHOLDER ---
    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem currentItem = inventoryList.get(position);

        // Gán dữ liệu vào các TextView
        holder.modelName.setText(currentItem.modelName);
        holder.versionName.setText(currentItem.versionName);
        holder.colorName.setText("Màu: " + currentItem.colorName); // Thêm chữ "Màu: " cho rõ
        holder.quantity.setText(String.valueOf(currentItem.quantity));

        // Bỏ phần xử lý giá tiền

        // Xử lý logic để hiển thị tên các kho hàng (giữ nguyên)
        if (currentItem.vehicles != null && !currentItem.vehicles.isEmpty()) {
            String warehouses = currentItem.vehicles.stream()
                    .map(vehicle -> vehicle.warehouseName)
                    .distinct()
                    .map(name -> "- " + name)
                    .collect(Collectors.joining("\n"));
            holder.warehouseNames.setText(warehouses);
        } else {
            holder.warehouseNames.setText("Không có thông tin kho");
        }
    }

    @Override
    public int getItemCount() {
        return inventoryList != null ? inventoryList.size() : 0;
    }

    public void updateData(List<InventoryItem> newInventoryList) {
        this.inventoryList = newInventoryList;
        notifyDataSetChanged();
    }
}
