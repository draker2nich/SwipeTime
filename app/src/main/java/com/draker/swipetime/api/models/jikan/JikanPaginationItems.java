package com.draker.swipetime.api.models.jikan;

import com.google.gson.annotations.SerializedName;

/**
 * Модель информации о количестве элементов из Jikan API
 */
public class JikanPaginationItems {
    @SerializedName("count")
    private int count;

    @SerializedName("total")
    private int total;

    @SerializedName("per_page")
    private int perPage;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPerPage() {
        return perPage;
    }

    public void setPerPage(int perPage) {
        this.perPage = perPage;
    }
}