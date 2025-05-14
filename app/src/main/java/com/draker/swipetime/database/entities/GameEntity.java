package com.draker.swipetime.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

/**
 * Сущность для игр
 */
@Entity(tableName = "games")
public class GameEntity extends ContentEntity {

    @ColumnInfo(name = "developer")
    private String developer;

    @ColumnInfo(name = "publisher")
    private String publisher;

    @ColumnInfo(name = "release_year")
    private int releaseYear;

    @ColumnInfo(name = "platforms")
    private String platforms;

    @ColumnInfo(name = "genres")
    private String genres;

    @ColumnInfo(name = "esrb_rating")
    private String esrbRating;

    public GameEntity() {
        super();
        setContentType("game");
    }

    @Ignore
    public GameEntity(String id, String title, String description, String imageUrl,
                      String developer, String publisher, int releaseYear, 
                      String platforms, String genres, String esrbRating) {
        super(id, title, description, imageUrl, "Игры", "game");
        this.developer = developer;
        this.publisher = publisher;
        this.releaseYear = releaseYear;
        this.platforms = platforms;
        this.genres = genres;
        this.esrbRating = esrbRating;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
        setUpdatedAt(System.currentTimeMillis());
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
        setUpdatedAt(System.currentTimeMillis());
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
        setUpdatedAt(System.currentTimeMillis());
    }

    public String getPlatforms() {
        return platforms;
    }

    public void setPlatforms(String platforms) {
        this.platforms = platforms;
        setUpdatedAt(System.currentTimeMillis());
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
        setUpdatedAt(System.currentTimeMillis());
    }

    public String getEsrbRating() {
        return esrbRating;
    }

    public void setEsrbRating(String esrbRating) {
        this.esrbRating = esrbRating;
        setUpdatedAt(System.currentTimeMillis());
    }
}
