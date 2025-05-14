package com.draker.swipetime.models;

/**
 * Модель данных для отображения элементов контента в карточках
 */
public class ContentItem {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String category;
    private boolean liked;

    public ContentItem() {
        // Пустой конструктор для Firebase
    }

    public ContentItem(String id, String title, String description, String imageUrl, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.liked = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
