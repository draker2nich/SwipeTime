package com.draker.swipetime.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

/**
 * Сущность для сериалов
 */
@Entity(tableName = "tv_shows")
public class TVShowEntity extends ContentEntity {

    @ColumnInfo(name = "creator")
    private String creator;

    @ColumnInfo(name = "start_year")
    private int startYear;

    @ColumnInfo(name = "end_year")
    private int endYear;

    @ColumnInfo(name = "seasons")
    private int seasons;

    @ColumnInfo(name = "episodes")
    private int episodes;

    @ColumnInfo(name = "genres")
    private String genres;

    @ColumnInfo(name = "status")
    private String status; // ongoing, finished, cancelled
    
    @ColumnInfo(name = "watched")
    private boolean watched;

    public TVShowEntity() {
        super();
        setContentType("tv_show");
    }

    @Ignore
    public TVShowEntity(String id, String title, String description, String imageUrl,
                        String creator, int startYear, int endYear, int seasons, 
                        int episodes, String genres, String status) {
        super(id, title, description, imageUrl, "Сериалы", "tv_show");
        this.creator = creator;
        this.startYear = startYear;
        this.endYear = endYear;
        this.seasons = seasons;
        this.episodes = episodes;
        this.genres = genres;
        this.status = status;
        this.watched = false;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
        setUpdatedAt(System.currentTimeMillis());
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
        setUpdatedAt(System.currentTimeMillis());
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
        setUpdatedAt(System.currentTimeMillis());
    }

    public int getSeasons() {
        return seasons;
    }

    public void setSeasons(int seasons) {
        this.seasons = seasons;
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
    
    public boolean isWatched() {
        return watched;
    }
    
    public void setWatched(boolean watched) {
        this.watched = watched;
        setUpdatedAt(System.currentTimeMillis());
    }
    
    /**
     * Получает среднюю длительность эпизода (в минутах)
     * Метод-заглушка для системы рекомендаций
     * @return примерная длительность эпизода в минутах (по умолчанию 40)
     */
    public int getEpisodeDuration() {
        // В реальном приложении здесь должно быть реальное значение
        return 40; // Стандартное значение для большинства сериалов
    }
}