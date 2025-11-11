package com.example.evshop.domain.models; // Đảm bảo package name đúng

import com.google.gson.annotations.SerializedName;

/**
 * Lớp này đại diện cho cấu trúc bao bọc chung của mọi response từ API.
 * @param <T> Kiểu dữ liệu của phần "result" (ví dụ: LoginResult, List<TemplateVehicle>)
 */
public class ApiEnvelope<T> {

    // SỬA 1: Đổi từ private thành public để dễ dàng truy cập từ các lớp khác
    @SerializedName("isSuccess")
    public boolean isSuccess;

    @SerializedName("message")
    public String message;

    @SerializedName("statusCode")
    public int statusCode;

    // SỬA 2: Đổi tên trường lại thành 'result' để khớp với JSON
    // Đồng thời vẫn giữ 'public' để dễ truy cập nếu cần
    @SerializedName("result")
    public T result;

    // --- CÁC PHƯƠNG THỨC GETTER VẪN GIỮ NGUYÊN ĐỂ ĐẢM BẢO TƯƠNG THÍCH ---

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    // SỬA 3: Phương thức getData() bây giờ sẽ trả về giá trị của trường 'result'
    // Điều này giúp code đang gọi .getData() không bị lỗi.
    public T getData() {
        return result;
    }

    // (Tùy chọn) Các phương thức Setter vẫn giữ nguyên
    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setData(T data) {
        // Setter này sẽ gán dữ liệu vào trường 'result'
        this.result = data;
    }
}
