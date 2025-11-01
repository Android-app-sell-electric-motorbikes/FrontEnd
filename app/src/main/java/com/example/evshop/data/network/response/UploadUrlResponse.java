package com.example.evshop.data.network.responses;

import com.google.gson.annotations.SerializedName;

// Class cha, tương ứng với toàn bộ JSON trả về
public class UploadUrlResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("result")
    private Result result;

    // Getters
    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Result getResult() {
        return result;
    }

    // --- Lớp con (nested class) để chứa object "result" ---
    public static class Result {

        @SerializedName("uploadUrl")
        private String uploadUrl;

        @SerializedName("objectKey")
        private String objectKey;

        // Getters
        public String getUploadUrl() {
            return uploadUrl;
        }

        public String getObjectKey() {
            return objectKey;
        }
    }
}
