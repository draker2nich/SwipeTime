package com.draker.swipetime.api.models.tmdb;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Универсальная модель ответа TMDb API
 * Заменяет TMDbMovieResponse.java и TMDbTVShowResponse.java
 */
public class TMDbResponse {
    @SerializedName("page")
    private int page;

    @SerializedName("results")
    private List<TMDbContent> results;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("total_results")
    private int totalResults;

    public TMDbResponse() {}

    // Геттеры и сеттеры
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public List<TMDbContent> getResults() { return results; }
    public void setResults(List<TMDbContent> results) { this.results = results; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getTotalResults() { return totalResults; }
    public void setTotalResults(int totalResults) { this.totalResults = totalResults; }

    // Утилитарные методы

    /**
     * Проверка наличия результатов
     */
    public boolean hasResults() {
        return results != null && !results.isEmpty();
    }

    /**
     * Получить количество результатов на текущей странице
     */
    public int getCurrentPageResultsCount() {
        return results != null ? results.size() : 0;
    }

    /**
     * Проверка является ли текущая страница последней
     */
    public boolean isLastPage() {
        return page >= totalPages;
    }

    /**
     * Проверка является ли текущая страница первой
     */
    public boolean isFirstPage() {
        return page <= 1;
    }

    /**
     * Получить номер следующей страницы
     */
    public int getNextPage() {
        return isLastPage() ? page : page + 1;
    }

    /**
     * Получить номер предыдущей страницы
     */
    public int getPreviousPage() {
        return isFirstPage() ? page : page - 1;
    }

    /**
     * Проверка валидности ответа
     */
    public boolean isValid() {
        return page > 0 && totalPages >= 0 && totalResults >= 0;
    }

    @Override
    public String toString() {
        return "TMDbResponse{" +
                "page=" + page +
                ", resultsCount=" + getCurrentPageResultsCount() +
                ", totalPages=" + totalPages +
                ", totalResults=" + totalResults +
                '}';
    }
}