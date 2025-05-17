package com.draker.swipetime.api.models.jikan;

import com.google.gson.annotations.SerializedName;

/**
 * Модель изображения аниме из Jikan API
 */
public class JikanImage {
    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("small_image_url")
    private String smallImageUrl;

    @SerializedName("large_image_url")
    private String largeImageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSmallImageUrl() {
        return smallImageUrl;
    }

    public void setSmallImageUrl(String smallImageUrl) {
        this.smallImageUrl = smallImageUrl;
    }

    public String getLargeImageUrl() {
        return largeImageUrl;
    }

    public void setLargeImageUrl(String largeImageUrl) {
        this.largeImageUrl = largeImageUrl;
    }

    /**
     * Получить лучшее доступное изображение
     * @return URL изображения или null
     */
    public String getBestAvailableImage() {
        if (largeImageUrl != null && !largeImageUrl.isEmpty()) {
            return largeImageUrl;
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            return imageUrl;
        } else if (smallImageUrl != null && !smallImageUrl.isEmpty()) {
            return smallImageUrl;
        }
        
        return null;
    }
}