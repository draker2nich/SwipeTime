package com.draker.swipetime.api.models.googlebooks;

import com.google.gson.annotations.SerializedName;

/**
 * Модель книги из Google Books API
 */
public class GoogleBook {
    @SerializedName("id")
    private String id;

    @SerializedName("volumeInfo")
    private GoogleBookVolumeInfo volumeInfo;

    @SerializedName("saleInfo")
    private GoogleBookSaleInfo saleInfo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GoogleBookVolumeInfo getVolumeInfo() {
        return volumeInfo;
    }

    public void setVolumeInfo(GoogleBookVolumeInfo volumeInfo) {
        this.volumeInfo = volumeInfo;
    }

    public GoogleBookSaleInfo getSaleInfo() {
        return saleInfo;
    }

    public void setSaleInfo(GoogleBookSaleInfo saleInfo) {
        this.saleInfo = saleInfo;
    }
}