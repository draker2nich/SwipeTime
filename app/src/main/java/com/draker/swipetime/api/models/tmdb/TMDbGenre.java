package com.draker.swipetime.api.models.tmdb;

import com.google.gson.annotations.SerializedName;

/**
 * Модель жанра из TMDB API
 */
public class TMDbGenre {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

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
}
