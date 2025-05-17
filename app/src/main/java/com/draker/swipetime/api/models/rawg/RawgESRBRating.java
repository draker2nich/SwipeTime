package com.draker.swipetime.api.models.rawg;

import com.google.gson.annotations.SerializedName;

/**
 * Модель возрастного рейтинга ESRB из RAWG API
 */
public class RawgESRBRating {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("slug")
    private String slug;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}