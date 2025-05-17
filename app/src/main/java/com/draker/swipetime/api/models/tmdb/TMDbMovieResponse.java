package com.draker.swipetime.api.models.tmdb;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Модель ответа на запрос списка фильмов из TMDB API
 */
public class TMDbMovieResponse {
    @SerializedName("page")
    private int page;

    @SerializedName("results")
    private List<TMDbMovie> results;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("total_results")
    private int totalResults;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<TMDbMovie> getResults() {
        return results;
    }

    public void setResults(List<TMDbMovie> results) {
        this.results = results;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
}