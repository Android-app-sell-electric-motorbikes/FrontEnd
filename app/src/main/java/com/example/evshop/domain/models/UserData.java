package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class UserData implements Serializable {

    // ** THÊM TRƯỜNG USERNAME **
    public String username;

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

    public String role;

    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role);
    }
}
