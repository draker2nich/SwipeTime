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
    
    // Дополнительные поля для улучшенных карточек
    private String genre;
    private int year;
    private String director;
    private String author;
    private String publisher;
    private int pages;
    private String developer;
    private String platforms;
    private String studio;
    private int seasons;
    private int episodes;

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
        this.genre = "";
        this.year = 0;
        this.director = "";
        this.author = "";
        this.publisher = "";
        this.pages = 0;
        this.developer = "";
        this.platforms = "";
        this.studio = "";
        this.seasons = 0;
        this.episodes = 0;
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
    
    // Геттеры и сеттеры для дополнительных полей
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public String getDirector() {
        return director;
    }
    
    public void setDirector(String director) {
        this.director = director;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public int getPages() {
        return pages;
    }
    
    public void setPages(int pages) {
        this.pages = pages;
    }
    
    public String getDeveloper() {
        return developer;
    }
    
    public void setDeveloper(String developer) {
        this.developer = developer;
    }
    
    public String getPlatforms() {
        return platforms;
    }
    
    public void setPlatforms(String platforms) {
        this.platforms = platforms;
    }
    
    public String getStudio() {
        return studio;
    }
    
    public void setStudio(String studio) {
        this.studio = studio;
    }
    
    public int getSeasons() {
        return seasons;
    }
    
    public void setSeasons(int seasons) {
        this.seasons = seasons;
    }
    
    public int getEpisodes() {
        return episodes;
    }
    
    public void setEpisodes(int episodes) {
        this.episodes = episodes;
    }
}
