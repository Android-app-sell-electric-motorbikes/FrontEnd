// File: TemplateVehicleAdapter.java
package com.example.evshop.ui.vehicle;

import android.content.Context;
import android.util.Log; // <-- Quan trọng
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.evshop.R;
import com.example.evshop.domain.models.Product;
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.domain.models.VersionDetails;
import com.example.evshop.util.CartManager;
import com.example.evshop.util.ProductConverter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TemplateVehicleAdapter extends RecyclerView.Adapter<TemplateVehicleAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onFetchDetails(int position, String versionId);
    }

    private List<TemplateVehicle> vehicles = new ArrayList<>();
    private final OnItemClickListener listener;
    private final Map<String, Boolean> expandedState = new HashMap<>();
    private final Map<String, VersionDetails> fetchedDetails = new HashMap<>();

    public TemplateVehicleAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateVehicles(List<TemplateVehicle> newVehicles) {
        this.vehicles = newVehicles;
        expandedState.clear();
        fetchedDetails.clear();
        notifyDataSetChanged();
    }

    public void onDetailsFetched(int position, VersionDetails details) {
        if (details != null && position < vehicles.size()) {
            String versionId = vehicles.get(position).getVersion().getId();
            if (versionId != null) {
                fetchedDetails.put(versionId, details);
                notifyItemChanged(position);
            }
        }
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
        if (vehicle == null || vehicle.getVersion() == null) {
            return;
        }

        // *** BƯỚC KIỂM TRA ĐẦU TIÊN: Lấy versionId và log ra ngay lập tức ***
        final String versionId = vehicle.getVersion().getId();
        Log.d("ADAPTER_DEBUG", "Binding item vị trí " + position + ". Lấy được versionId: " + versionId);

        // Nếu versionId bị null ở đây, mọi thứ phía sau sẽ không hoạt động.
        if (versionId == null) {
            Log.e("ADAPTER_DEBUG", "LỖI NGHIÊM TRỌNG: versionId tại vị trí " + position + " là NULL. Vui lòng kiểm tra Model Version.java và JSON API danh sách.");
            // Không làm gì thêm để tránh crash
            holder.itemView.setOnClickListener(null); // Vô hiệu hóa click cho item lỗi
            return;
        }

        final boolean isExpanded = expandedState.getOrDefault(versionId, false);
        final VersionDetails details = fetchedDetails.get(versionId);

        holder.bindBasicInfo(vehicle);
        holder.updateDetailsView(isExpanded, details);
        holder.setupAddToCartButton(vehicle);  // Setup Add to Cart button

        holder.itemView.setOnClickListener(v -> {
            Log.d("ADAPTER_DEBUG", "CLICKED vào item có versionId: " + versionId); // Log khi click
            boolean newExpandedState = !isExpanded;
            expandedState.put(versionId, newExpandedState);

            if (newExpandedState && details == null) {
                Log.d("ADAPTER_DEBUG", "==> Thỏa mãn điều kiện: Mở rộng, chưa có chi tiết. Sẽ gọi onFetchDetails.");
                if (listener != null) {
                    listener.onFetchDetails(position, versionId);
                }
            } else {
                Log.d("ADAPTER_DEBUG", "==> Không thỏa mãn điều kiện gọi API. IsExpanded=" + isExpanded + ", Details!=null?" + (details != null));
            }

            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgVehicle;
        final TextView txtModel, txtColor, txtPrice;
        final FrameLayout detailsContainer;
        final ProgressBar detailsProgressBar;
        final LinearLayout layoutDetailsContent;
        final TextView txtDescription, txtMotorPower, txtRange, txtTopSpeed, txtProductionYear;
        final Button btnAddToCart;
        private final Context context;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            imgVehicle = itemView.findViewById(R.id.imgVehicle);
            txtModel = itemView.findViewById(R.id.txtModel);
            txtColor = itemView.findViewById(R.id.txtColor);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            detailsContainer = itemView.findViewById(R.id.details_container);
            detailsProgressBar = itemView.findViewById(R.id.details_progress_bar);
            layoutDetailsContent = itemView.findViewById(R.id.layout_details_content);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtMotorPower = itemView.findViewById(R.id.txtMotorPower);
            txtRange = itemView.findViewById(R.id.txtRange);
            txtTopSpeed = itemView.findViewById(R.id.txtTopSpeed);
            txtProductionYear = itemView.findViewById(R.id.txtProductionYear);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }

        void bindBasicInfo(TemplateVehicle vehicle) {
            txtModel.setText(vehicle.getVersion().getVersionName());
            txtColor.setText(vehicle.getColor().getColorName());
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            txtPrice.setText(currencyFormatter.format(vehicle.getPrice()));
            List<String> images = vehicle.getImgUrl();
            if (images != null && !images.isEmpty()) {
                Glide.with(context).load(images.get(0)).placeholder(R.drawable.ic_placeholder).error(R.drawable.ic_placeholder).into(imgVehicle);
            } else {
                imgVehicle.setImageResource(R.drawable.ic_placeholder);
            }
        }

        void updateDetailsView(boolean isExpanded, VersionDetails details) {
            detailsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            if (!isExpanded) return;
            if (details == null) {
                detailsProgressBar.setVisibility(View.VISIBLE);
                layoutDetailsContent.setVisibility(View.INVISIBLE);
            } else {
                detailsProgressBar.setVisibility(View.GONE);
                layoutDetailsContent.setVisibility(View.VISIBLE);
                txtDescription.setText(details.getDescription());
                txtMotorPower.setText(String.format(Locale.US, "Công suất: %dW", details.getMotorPower()));
                txtRange.setText(String.format(Locale.US, "Quãng đường: %dkm", details.getRangePerCharge()));
                txtTopSpeed.setText(String.format(Locale.US, "Tốc độ: %dkm/h", details.getTopSpeed()));
                txtProductionYear.setText(String.format(Locale.US, "Năm SX: %d", details.getProductionYear()));
            }
        }

        void setupAddToCartButton(TemplateVehicle vehicle) {
            if (btnAddToCart != null) {
                btnAddToCart.setOnClickListener(v -> {
                    // Convert TemplateVehicle -> Product với ID thật từ API
                    Product product = ProductConverter.fromTemplateVehicle(vehicle);
                    
                    if (product != null && product.getId() != null) {
                        // Add vào cart với ID thật từ API
                        CartManager.getInstance().addToCart(product, 1);
                        
                        Log.d("CART_DEBUG", "Đã thêm vào cart - ID: " + product.getId() + ", Name: " + product.getName());
                        Toast.makeText(context, "Đã thêm " + product.getName() + " vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("CART_DEBUG", "Lỗi: Product hoặc ID bị null");
                        Toast.makeText(context, "Lỗi: Không thể thêm sản phẩm vào giỏ", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
