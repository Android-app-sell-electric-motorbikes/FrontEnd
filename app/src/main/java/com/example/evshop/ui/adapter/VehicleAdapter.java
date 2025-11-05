package com.example.evshop.ui.adapter;

import android.view.LayoutInflater;
import android.view.View; // SỬA LỖI 1: Thêm import đúng
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.evshop.R;
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.databinding.ItemVehicleBinding;

import java.text.NumberFormat;
import java.util.Locale;

public class VehicleAdapter extends ListAdapter<TemplateVehicle, VehicleAdapter.VehicleViewHolder> {

    private final OnVehicleClickListener listener;

    public VehicleAdapter(OnVehicleClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVehicleBinding binding = ItemVehicleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VehicleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        TemplateVehicle currentVehicle = getItem(position);
        holder.bind(currentVehicle, listener);
    }

    class VehicleViewHolder extends RecyclerView.ViewHolder {
        private final ItemVehicleBinding binding;

        public VehicleViewHolder(ItemVehicleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final TemplateVehicle template, final OnVehicleClickListener listener) {
            // SỬA LỖI 2: Xóa 2 dòng code không an toàn ở đây

            // --- Code kiểm tra an toàn (Giữ lại) ---
            // 1. Kiểm tra và gán tên xe
            if (template.getVersion() != null && template.getVersion().getVersionName() != null) {
                binding.txtModel.setText(template.getVersion().getVersionName());
            } else {
                binding.txtModel.setText(R.string.vehicle_name_unavailable);
            }

            // 2. Kiểm tra và gán màu sắc
            if (template.getColor() != null && template.getColor().getColorName() != null) {
                binding.txtColor.setText(template.getColor().getColorName());
                binding.txtColor.setVisibility(View.VISIBLE); // SỬA LỖI 1: Dùng View chuẩn
            } else {
                binding.txtColor.setVisibility(View.GONE); // SỬA LỖI 1: Dùng View chuẩn
            }
            // --- Kết thúc code kiểm tra an toàn ---

            // Gán giá tiền
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.txtPrice.setText(currencyFormatter.format(template.getPrice()));

            // Tải hình ảnh
            if (template.getImgUrl() != null && !template.getImgUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(template.getImgUrl().get(0))
                        .placeholder(R.drawable.placeholder_vehicle)
                        .error(R.drawable.ic_round_error_24) // Đổi tên file placeholder error cho đúng
                        .into(binding.imgVehicle);
            } else {
                binding.imgVehicle.setImageResource(R.drawable.placeholder_vehicle);
            }

            // Gán sự kiện click
            binding.getRoot().setOnClickListener(v -> listener.onVehicleClick(template));
        }
    }

    public interface OnVehicleClickListener {
        void onVehicleClick(TemplateVehicle template);
    }

    private static final DiffUtil.ItemCallback<TemplateVehicle> DIFF_CALLBACK = new DiffUtil.ItemCallback<TemplateVehicle>() {
        @Override
        public boolean areItemsTheSame(@NonNull TemplateVehicle oldItem, @NonNull TemplateVehicle newItem) {
            // Lỗi tiềm ẩn: nếu API trả về item có id null thì sẽ crash. Sửa lại cho an toàn.
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull TemplateVehicle oldItem, @NonNull TemplateVehicle newItem) {
            // Lỗi tiềm ẩn: Code này sẽ crash nếu getVersion() trả về null.
            // Chỉ cần so sánh ID là đủ cho ListAdapter hoạt động cơ bản.
            // Khi dữ liệu thay đổi, chỉ cần ID giống nhau và submitList được gọi, item sẽ được cập nhật.
            return oldItem.equals(newItem); // Cách an toàn nhất là override equals() trong TemplateVehicle,
            // hoặc so sánh từng trường một cách an toàn.
            // Nhưng để đơn giản, chỉ so sánh ID là đủ.
        }
    };
}
