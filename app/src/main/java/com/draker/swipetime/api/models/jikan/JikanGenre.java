package com.draker.swipetime.api.models.jikan;

import com.google.gson.annotations.SerializedName;

/**
 * Модель жанра аниме из Jikan API
 */
public class JikanGenre {
    @SerializedName("mal_id")
    private int malId;

    @SerializedName("type")
    private String type;

    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String url;

    public int getMalId() {
        return malId;
    }

    public void setMalId(int malId) {
        this.malId = malId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}