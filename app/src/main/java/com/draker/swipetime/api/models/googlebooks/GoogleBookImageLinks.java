package com.draker.swipetime.api.models.googlebooks;

import com.google.gson.annotations.SerializedName;

/**
 * Модель ссылок на изображения книги из Google Books API
 */
public class GoogleBookImageLinks {
    @SerializedName("smallThumbnail")
    private String smallThumbnail;

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("small")
    private String small;

    @SerializedName("medium")
    private String medium;

    @SerializedName("large")
    private String large;

    @SerializedName("extraLarge")
    private String extraLarge;

    public String getSmallThumbnail() {
        return smallThumbnail;
    }

    public void setSmallThumbnail(String smallThumbnail) {
        this.smallThumbnail = smallThumbnail;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getSmall() {
        return small;
    }

    public void setSmall(String small) {
        this.small = small;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getLarge() {
        return large;
    }

    public void setLarge(String large) {
        this.large = large;
    }

    public String getExtraLarge() {
        return extraLarge;
    }

    public void setExtraLarge(String extraLarge) {
        this.extraLarge = extraLarge;
    }

    /**
     * Получить лучшее доступное изображение
     * @return URL изображения или null
     */
    public String getBestAvailableImage() {
        if (extraLarge != null && !extraLarge.isEmpty()) {
            return extraLarge;
        } else if (large != null && !large.isEmpty()) {
            return large;
        } else if (medium != null && !medium.isEmpty()) {
            return medium;
        } else if (small != null && !small.isEmpty()) {
            return small;
        } else if (thumbnail != null && !thumbnail.isEmpty()) {
            return thumbnail;
        } else if (smallThumbnail != null && !smallThumbnail.isEmpty()) {
            return smallThumbnail;
        }
        
        return null;
    }
}