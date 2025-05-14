package com.draker.swipetime.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

/**
 * Сущность для фильмов
 */
@Entity(tableName = "movies")
public class MovieEntity extends ContentEntity {

    @ColumnInfo(name = "director")
    private String director;

    @ColumnInfo(name = "release_year")
    private int releaseYear;

    @ColumnInfo(name = "duration")
    private int duration;

    @ColumnInfo(name = "genres")
    private String genres;

    public MovieEntity() {
        super();
        setContentType("movie");
    }

    @Ignore
    public MovieEntity(String id, String title, String description, String imageUrl, 
                       String director, int releaseYear, int duration, String genres) {
        super(id, title, description, imageUrl, "Фильмы", "movie");
        this.director = director;
        this.releaseYear = releaseYear;
        this.duration = duration;
        this.genres = genres;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
        setUpdatedAt(System.currentTimeMillis());
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
        setUpdatedAt(System.currentTimeMillis());
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
        setUpdatedAt(System.currentTimeMillis());
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
        setUpdatedAt(System.currentTimeMillis());
    }
}
