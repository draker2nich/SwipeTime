package com.draker.swipetime.api.models.jikan;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Модель ответа на запрос списка аниме из Jikan API
 */
public class JikanResponse {
    @SerializedName("pagination")
    private JikanPagination pagination;

    @SerializedName("data")
    private List<JikanAnime> data;

    public JikanPagination getPagination() {
        return pagination;
    }

    public void setPagination(JikanPagination pagination) {
        this.pagination = pagination;
    }

    public List<JikanAnime> getData() {
        return data;
    }

    public void setData(List<JikanAnime> data) {
        this.data = data;
    }
}