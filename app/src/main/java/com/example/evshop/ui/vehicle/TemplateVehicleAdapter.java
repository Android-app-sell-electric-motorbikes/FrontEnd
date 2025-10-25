package com.example.evshop.ui.vehicle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
// Import model TemplateVehicle mà chúng ta đã tạo
import com.example.evshop.domain.models.TemplateVehicle;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TemplateVehicleAdapter extends RecyclerView.Adapter<TemplateVehicleAdapter.ViewHolder> {

    private final List<TemplateVehicle> vehicles;

    /**
     * Constructor của Adapter.
     * @param vehicles Danh sách ban đầu (thường là một danh sách rỗng).
     */
    public TemplateVehicleAdapter(List<TemplateVehicle> vehicles) {
        this.vehicles = vehicles;
    }

    /**
     * Hàm này được gọi từ Activity/Fragment để cập nhật dữ liệu mới cho Adapter
     * sau khi gọi API thành công.
     * @param newVehicles Danh sách xe mới lấy từ API.
     */
    public void updateVehicles(List<TemplateVehicle> newVehicles) {
        this.vehicles.clear();
        this.vehicles.addAll(newVehicles);
        notifyDataSetChanged(); // Rất quan trọng: Báo cho RecyclerView biết dữ liệu đã thay đổi để vẽ lại UI.
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tạo ra View cho mỗi item từ file layout item_vehicle.xml
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Lấy dữ liệu của item tại vị trí `position`
        TemplateVehicle vehicle = vehicles.get(position);
        if (vehicle == null) {
            Log.e("TemplateVehicleAdapter", "Dữ liệu xe tại vị trí " + position + " là null.");
            return;
        }

        // Bắt đầu gán dữ liệu vào các View trong ViewHolder
        holder.bind(vehicle);
    }

    @Override
    public int getItemCount() {
        // Trả về tổng số item trong danh sách
        return vehicles != null ? vehicles.size() : 0;
    }

    /**
     * Lớp ViewHolder chứa các View của một item trong danh sách.
     * Việc này giúp tối ưu hiệu năng bằng cách không cần gọi findViewById() nhiều lần.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtPrice;
        final ImageView imgVehicle;
        final TextView txtModel;
        final TextView txtColor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các View từ layout item_vehicle.xml
            imgVehicle = itemView.findViewById(R.id.imgVehicle);
            txtModel = itemView.findViewById(R.id.txtModel);
            txtColor = itemView.findViewById(R.id.txtColor);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }

        /**
         * Gán dữ liệu từ một đối tượng TemplateVehicle vào các View.
         * @param vehicle Đối tượng xe cần hiển thị.
         */
        void bind(TemplateVehicle vehicle) {
            // 1. Hiển thị tên xe (Model + Version)
            if (vehicle.getVersion() != null) {
                String fullName = vehicle.getVersion().getVersionName();//+ " " + vehicle.getVersion().getModelName();
                txtModel.setText(fullName);
            } else {
                txtModel.setText("Không có tên");
            }

            // 2. Hiển thị màu sắc
            if (vehicle.getColor() != null) {
                txtColor.setText(vehicle.getColor().getColorName());
            } else {
                txtColor.setText(""); // Ẩn đi nếu không có màu
            }

            // 3. Định dạng và hiển thị giá tiền
            // Sử dụng NumberFormat để có định dạng tiền tệ đẹp và đúng chuẩn (ví dụ: 5.242.422 ₫)
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            txtPrice.setText(currencyFormatter.format(vehicle.getPrice()));

            // 4. Tải hình ảnh bằng Picasso
            List<String> images = vehicle.getImgUrl();
            if (images != null && !images.isEmpty()) {
                String imageUrl = images.get(0); // Lấy ảnh đầu tiên trong danh sách
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder) // Ảnh hiển thị trong lúc đang tải
                        .error(R.drawable.ic_placeholder)       // Ảnh hiển thị nếu tải lỗi
                        .into(imgVehicle);
            } else {
                // Nếu không có URL ảnh, hiển thị một ảnh mặc định
                imgVehicle.setImageResource(R.drawable.ic_placeholder);
            }
        }
    }
}
