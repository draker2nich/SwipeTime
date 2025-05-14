package com.draker.swipetime.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

/**
 * Сущность для аниме
 */
@Entity(tableName = "anime")
public class AnimeEntity extends ContentEntity {

    @ColumnInfo(name = "studio")
    private String studio;

    @ColumnInfo(name = "release_year")
    private int releaseYear;

    @ColumnInfo(name = "episodes")
    private int episodes;

    @ColumnInfo(name = "genres")
    private String genres;

    @ColumnInfo(name = "status")
    private String status; // ongoing, finished, cancelled

    @ColumnInfo(name = "type")
    private String type; // TV, Movie, OVA, etc.

    public AnimeEntity() {
        super();
        setContentType("anime");
    }

    @Ignore
    public AnimeEntity(String id, String title, String description, String imageUrl,
                       String studio, int releaseYear, int episodes, 
                       String genres, String status, String type) {
        super(id, title, description, imageUrl, "Аниме", "anime");
        this.studio = studio;
        this.releaseYear = releaseYear;
        this.episodes = episodes;
        this.genres = genres;
        this.status = status;
        this.type = type;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
        setUpdatedAt(System.currentTimeMillis());
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
        setUpdatedAt(System.currentTimeMillis());
    }

    public int getEpisodes() {
        return episodes;
    }

    public void setEpisodes(int episodes) {
        this.episodes = episodes;
        setUpdatedAt(System.currentTimeMillis());
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
        setUpdatedAt(System.currentTimeMillis());
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        setUpdatedAt(System.currentTimeMillis());
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        setUpdatedAt(System.currentTimeMillis());
    }
}
