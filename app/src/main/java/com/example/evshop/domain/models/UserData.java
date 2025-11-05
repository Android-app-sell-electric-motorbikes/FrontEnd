package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class UserData implements Serializable {

    // Các trường này đến từ JSON body của API response
    @SerializedName("email")
    public String email;

    @SerializedName("fullName")
    public String fullName;

    @SerializedName("address")
    public String address;

    @SerializedName("sex")
    public String sex;

    @SerializedName("dateOfBirth")
    public String dateOfBirth;

    // Trường role sẽ được điền vào theo cách thủ công sau khi giải mã token,
    // không phải từ Gson.
    public String role;

    // Hàm này sẽ được gọi sau khi trường 'role' được điền vào.
    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role);
    }
}
