package com.draker.swipetime.api.models.googlebooks;

import com.google.gson.annotations.SerializedName;

/**
 * Модель информации о продаже книги из Google Books API
 */
public class GoogleBookSaleInfo {
    @SerializedName("country")
    private String country;

    @SerializedName("saleability")
    private String saleability;

    @SerializedName("isEbook")
    private boolean isEbook;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSaleability() {
        return saleability;
    }

    public void setSaleability(String saleability) {
        this.saleability = saleability;
    }

    public boolean isEbook() {
        return isEbook;
    }

    public void setEbook(boolean ebook) {
        isEbook = ebook;
    }
}