package com.example.evshop.data.network.requests;

import com.google.gson.annotations.SerializedName;

public class GetUploadUrlRequest {

    @SerializedName("fileName")
    private String fileName;

    @SerializedName("contentType")
    private String contentType;

    public GetUploadUrlRequest(String fileName, String contentType) {
        this.fileName = fileName;
        this.contentType = contentType;
    }
}
