package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserData {
    // Giữ lại các trường cũ của bạn
    @SerializedName("email") // Thêm @SerializedName để đảm bảo ánh xạ đúng
    public String email;

    @SerializedName("fullName")
    public String fullName;

    @SerializedName("address")
    public String address;

    @SerializedName("sex")
    public String sex;

    @SerializedName("dateOfBirth")
    public String dateOfBirth;

    // *** 1. THÊM TRƯỜNG "ROLES" ĐỂ NHẬN DỮ LIỆU TỪ API ***
    @SerializedName("roles")
    public List<String> roles;

    // *** 2. THÊM HÀM "ISADMIN()" ĐỂ KIỂM TRA QUYỀN ***
    public boolean isAdmin() {
        // Nếu danh sách vai trò không tồn tại hoặc rỗng, thì chắc chắn không phải admin
        if (roles == null || roles.isEmpty()) {
            return false;
        }

        // Duyệt qua từng vai trò trong danh sách
        for (String role : roles) {
            // So sánh không phân biệt hoa thường với "ROLE_ADMIN"
            if ("ROLE_ADMIN".equalsIgnoreCase(role)) {
                return true; // Tìm thấy vai trò admin, trả về true ngay lập tức
            }
        }

        // Nếu duyệt hết mà không tìm thấy, trả về false
        return false;
    }
}
