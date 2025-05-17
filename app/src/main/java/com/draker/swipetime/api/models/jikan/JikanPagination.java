package com.draker.swipetime.api.models.jikan;

import com.google.gson.annotations.SerializedName;

/**
 * Модель пагинации из Jikan API
 */
public class JikanPagination {
    @SerializedName("last_visible_page")
    private int lastVisiblePage;

    @SerializedName("has_next_page")
    private boolean hasNextPage;

    @SerializedName("current_page")
    private int currentPage;

    @SerializedName("items")
    private JikanPaginationItems items;

    public int getLastVisiblePage() {
        return lastVisiblePage;
    }

    public void setLastVisiblePage(int lastVisiblePage) {
        this.lastVisiblePage = lastVisiblePage;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public JikanPaginationItems getItems() {
        return items;
    }

    public void setItems(JikanPaginationItems items) {
        this.items = items;
    }
}