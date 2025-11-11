package com.example.evshop.domain.models; // Đảm bảo package name đúng

import com.google.gson.annotations.SerializedName;

/**
 * Lớp này đại diện cho cấu trúc bao bọc chung của mọi response từ API.
 * @param <T> Kiểu dữ liệu của phần "result" (ví dụ: LoginResult, List<TemplateVehicle>)
 */
public class ApiEnvelope<T> {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("statusCode")
    private int statusCode;

    // SỬA: Đổi tên trường từ 'result' thành 'data' để khớp với thói quen gọi .getData()
    // Hoặc giữ 'result' và tạo getter tên là getData(). Cả hai đều được.
    // Ở đây, tôi sẽ đổi tên trường để nhất quán.
    @SerializedName("result") // Giữ nguyên SerializedName để khớp với JSON từ API
    private T data;

    // --- CÁC PHƯƠNG THỨC GETTER ĐƯỢC THÊM VÀO ---

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    // ✅ PHƯƠGN THỨC getData() MÀ BẠN YÊU CẦU
    public T getData() {
        return data;
    }

    // (Tùy chọn) Thêm các phương thức Setter nếu bạn cần tạo đối tượng này thủ công
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
        this.data = data;
    }
}
