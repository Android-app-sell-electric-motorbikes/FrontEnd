package com.example.evshop.data.network.requests;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CreateTemplateVehicleRequest {

    @SerializedName("versionId")
    private final String versionId;

    @SerializedName("colorId")
    private final String colorId;

    @SerializedName("description")
    private final String description;

    @SerializedName("price")
    private final double price;

    @SerializedName("attachmentKeys")
    private final List<String> attachmentKeys;

    @SerializedName("isActive")
    private final boolean isActive;

    /**
     * Constructor đầy đủ với 6 tham số mà backend yêu cầu.
     */
    public CreateTemplateVehicleRequest(String versionId, String colorId, String description, double price, List<String> attachmentKeys, boolean isActive) {
        this.versionId = versionId ;
        this.colorId = colorId;
        this.description = description;
        this.price = price;
        this.attachmentKeys = attachmentKeys;
        this.isActive = isActive;
    }
}
