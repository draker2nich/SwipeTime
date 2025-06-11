package com.draker.swipetime.api.models.googlebooks;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Упрощенная модель ответа на запрос списка книг из Google Books API
 * Содержит только необходимые поля для работы приложения
 */
public class GoogleBooksResponse {
    @SerializedName("kind")
    private String kind;

    @SerializedName("totalItems")
    private int totalItems;

    @SerializedName("items")
    private List<GoogleBook> items;

    /**
     * Конструктор по умолчанию
     */
    public GoogleBooksResponse() {
    }

    /**
     * Конструктор с параметрами
     */
    public GoogleBooksResponse(String kind, int totalItems, List<GoogleBook> items) {
        this.kind = kind;
        this.totalItems = totalItems;
        this.items = items;
    }

    // Геттеры и сеттеры
    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public List<GoogleBook> getItems() {
        return items;
    }

    public void setItems(List<GoogleBook> items) {
        this.items = items;
    }

    /**
     * Проверяет, есть ли результаты в ответе
     */
    public boolean hasResults() {
        return items != null && !items.isEmpty();
    }

    /**
     * Получить количество возвращенных элементов
     */
    public int getResultCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * Проверяет, является ли ответ валидным
     */
    public boolean isValidResponse() {
        return kind != null && (kind.contains("books#volumes") || kind.contains("books#volume"));
    }

    @Override
    public String toString() {
        return "GoogleBooksResponse{" +
                "kind='" + kind + '\'' +
                ", totalItems=" + totalItems +
                ", resultCount=" + getResultCount() +
                '}';
    }
}