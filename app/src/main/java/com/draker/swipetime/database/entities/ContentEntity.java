package com.draker.swipetime.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Базовая сущность для всех типов контента в базе данных
 */
@Entity(tableName = "content")
public class ContentEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "image_url")
    private String imageUrl;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "content_type")
    private String contentType;

    @ColumnInfo(name = "liked")
    private boolean liked;

    @ColumnInfo(name = "watched")
    private boolean watched;

    @ColumnInfo(name = "rating")
    private float rating;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    public ContentEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    @Ignore
    public ContentEntity(@NonNull String id, String title, String description, String imageUrl, 
                          String category, String contentType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.contentType = contentType;
        this.liked = false;
        this.watched = false;
        this.rating = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
        this.updatedAt = System.currentTimeMillis();
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Проверить, просмотрен/прослушан/прочитан ли контент
     * @return true, если контент отмечен как просмотренный/прослушанный/прочитанный
     */
    public boolean isCompleted() {
        return watched;
    }
    
    /**
     * Установить статус контента как просмотренный/прослушанный/прочитанный
     * @param completed true, если контент отмечен как просмотренный/прослушанный/прочитанный
     */
    public void setCompleted(boolean completed) {
        this.watched = completed;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * Получить временную метку создания/обновления
     * @return временная метка в миллисекундах
     */
    public long getTimestamp() {
        return updatedAt;
    }
    
    /**
     * Установить временную метку
     * @param timestamp временная метка в миллисекундах
     */
    public void setTimestamp(long timestamp) {
        this.updatedAt = timestamp;
    }
}
