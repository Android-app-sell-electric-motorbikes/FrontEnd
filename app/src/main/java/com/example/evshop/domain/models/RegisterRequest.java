package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("userName")
    private final String userName;

    @SerializedName("password")
    private final String password;

    @SerializedName("email")
    private final String email;

    @SerializedName("phoneNumber")
    private final String phoneNumber;

    @SerializedName("address")
    private final String address;

    public RegisterRequest(String userName, String password, String email, String phoneNumber, String address) {
        this.userName = userName;
        this.password = password;

        // ** XỬ LÝ CHUỖI RỖNG THÀNH NULL **
        this.email = (email != null && email.isEmpty()) ? null : email;
        this.phoneNumber = (phoneNumber != null && phoneNumber.isEmpty()) ? null : phoneNumber;
        this.address = (address != null && address.isEmpty()) ? null : address;
    }
}
