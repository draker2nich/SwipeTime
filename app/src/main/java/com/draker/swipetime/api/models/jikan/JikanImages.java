package com.draker.swipetime.api.models.jikan;

import com.google.gson.annotations.SerializedName;

/**
 * Модель изображений аниме из Jikan API
 */
public class JikanImages {
    @SerializedName("jpg")
    private JikanImage jpg;

    @SerializedName("webp")
    private JikanImage webp;

    public JikanImage getJpg() {
        return jpg;
    }

    public void setJpg(JikanImage jpg) {
        this.jpg = jpg;
    }

    public JikanImage getWebp() {
        return webp;
    }

    public void setWebp(JikanImage webp) {
        this.webp = webp;
    }
}