package com.example.evshop.util;

import com.example.evshop.domain.models.Product;
import com.example.evshop.domain.models.TemplateVehicle;

import java.util.List;

/**
 * Helper class để convert giữa TemplateVehicle (từ API) và Product (cho Cart)
 */
public class ProductConverter {

    /**
     * Convert TemplateVehicle từ API thành Product để add vào cart
     * @param vehicle TemplateVehicle từ API
     * @return Product object với ID thật từ API
     */
    public static Product fromTemplateVehicle(TemplateVehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        // Lấy ID thật từ vehicle (hoặc từ version nếu vehicle.getId() null)
        String id = vehicle.getId();
        if (id == null && vehicle.getVersion() != null) {
            id = vehicle.getVersion().getId();
        }

        // Lấy tên từ version
        String name = vehicle.getVersion() != null 
                ? vehicle.getVersion().getVersionName() 
                : "Unknown Vehicle";

        // Brand mặc định (API không cung cấp brand trong template vehicle list)
        String brand = "Electric Vehicle";

        // Lấy image URL đầu tiên từ danh sách
        String imageUrl = null;
        List<String> images = vehicle.getImgUrl();
        if (images != null && !images.isEmpty()) {
            imageUrl = images.get(0);
        }

        // Convert price từ double sang long (VND)
        long priceVnd = (long) vehicle.getPrice();

        // Rating default (nếu API không có)
        float rating = 4.5f;

        // Category từ color hoặc default
        String category = vehicle.getColor() != null 
                ? vehicle.getColor().getColorName() 
                : "Electric Vehicle";

        // Tạo Product với constructor mới (dùng imageUrlString)
        return new Product(id, name, brand, imageUrl, priceVnd, rating, category);
    }
}

