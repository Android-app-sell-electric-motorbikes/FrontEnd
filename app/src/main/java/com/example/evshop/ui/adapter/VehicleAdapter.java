package com.example.evshop.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
// Thêm import cho RatingBar
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.evshop.R;
import com.example.evshop.databinding.ItemVehicleBinding; // Giả sử bạn dùng item_vehicle.xml và ViewBinding
import com.example.evshop.domain.models.TemplateVehicle;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class VehicleAdapter extends ListAdapter<TemplateVehicle, VehicleAdapter.VehicleViewHolder> {

    private final OnItemClickListener onItemClicked;

    public interface OnItemClickListener {
        void onItemClick(TemplateVehicle template);
    }

    public VehicleAdapter(OnItemClickListener onItemClicked) {
        super(DIFF_CALLBACK);
        this.onItemClicked = onItemClicked;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Đảm bảo rằng layout này chính là file item_vehicle.xml đã được sửa
        ItemVehicleBinding binding = ItemVehicleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VehicleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    // =========================================================
    // ***           CẬP NHẬT VIEW HOLDER Ở ĐÂY             ***
    // =========================================================
    class VehicleViewHolder extends RecyclerView.ViewHolder {
        private final ItemVehicleBinding binding;

        VehicleViewHolder(ItemVehicleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TemplateVehicle template) {
            if (template == null) return;

            // Gán dữ liệu cho các view đã có
            binding.txtModel.setText(template.getVersion().getVersionName());
            binding.txtColor.setText(template.getColor().getColorName());
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.txtPrice.setText(currencyFormatter.format(template.getPrice()));

            List<String> images = template.getImgUrl();
            if (images != null && !images.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(images.get(0))
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(binding.imgVehicle);
            } else {
                binding.imgVehicle.setImageResource(R.drawable.ic_placeholder);
            }

            // *** THÊM LOGIC GÁN GIÁ TRỊ CHO RATINGBAR ***
            binding.ratingBar.setRating((float) template.getRating());

            // Xử lý sự kiện click
            itemView.setOnClickListener(v -> onItemClicked.onItemClick(template));
        }
    }

    private static final DiffUtil.ItemCallback<TemplateVehicle> DIFF_CALLBACK = new DiffUtil.ItemCallback<TemplateVehicle>() {
        @Override
        public boolean areItemsTheSame(@NonNull TemplateVehicle oldItem, @NonNull TemplateVehicle newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull TemplateVehicle oldItem, @NonNull TemplateVehicle newItem) {
            return oldItem.equals(newItem);
        }
    };
}
