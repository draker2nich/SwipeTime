package com.draker.swipetime.models;

import java.io.Serializable;

/**
 * Модель данных для отображения элементов контента в карточках
 */
public class ContentItem implements Serializable {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String category;
    private boolean liked;
    private boolean watched;
    private float rating;
    private String review;

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
        this.watched = false;
        this.rating = 0;
        this.review = "";
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
    
    public boolean isWatched() {
        return watched;
    }
    
    public void setWatched(boolean watched) {
        this.watched = watched;
    }
    
    public float getRating() {
        return rating;
    }
    
    public void setRating(float rating) {
        this.rating = rating;
    }
    
    public String getReview() {
        return review;
    }
    
    public void setReview(String review) {
        this.review = review;
    }
}
