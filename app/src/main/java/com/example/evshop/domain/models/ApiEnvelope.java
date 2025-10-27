package com.example.evshop.domain.models; // Đảm bảo package name đúng

import com.google.gson.annotations.SerializedName;

/**
 * Lớp này đại diện cho cấu trúc bao bọc chung của mọi response từ API.
 * @param <T> Kiểu dữ liệu của phần "result" (ví dụ: LoginResult, List<TemplateVehicle>)
 */
public class ApiEnvelope<T> {

    @SerializedName("isSuccess")
    public boolean isSuccess;

    @SerializedName("message")
    public String message;

    @SerializedName("statusCode")
    public int statusCode;

    // Rất quan trọng: Tên trường này phải khớp với key trong JSON API trả về
    @SerializedName("result")
    public T result;

}
