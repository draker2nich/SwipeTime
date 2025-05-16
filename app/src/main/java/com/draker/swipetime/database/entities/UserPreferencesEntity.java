package com.draker.swipetime.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Сущность для хранения пользовательских настроек и предпочтений
 */
@Entity(
    tableName = "user_preferences",
    foreignKeys = @ForeignKey(
        entity = UserEntity.class,
        parentColumns = "id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("user_id")}
)
public class UserPreferencesEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "preferred_genres")
    private String preferredGenres; // Сохраняем как JSON строку

    @ColumnInfo(name = "min_duration")
    private int minDuration; // Минимальная длительность в минутах (для фильмов/сериалов)

    @ColumnInfo(name = "max_duration")
    private int maxDuration; // Максимальная длительность в минутах

    @ColumnInfo(name = "preferred_countries")
    private String preferredCountries; // Сохраняем как JSON строку

    @ColumnInfo(name = "preferred_languages")
    private String preferredLanguages; // Сохраняем как JSON строку

    @ColumnInfo(name = "min_year")
    private int minYear; // Минимальный год выпуска

    @ColumnInfo(name = "max_year") 
    private int maxYear; // Максимальный год выпуска

    @ColumnInfo(name = "interests_tags")
    private String interestsTags; // Сохраняем как JSON строку

    @ColumnInfo(name = "adult_content_enabled")
    private boolean adultContentEnabled; // Показывать контент 18+

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    public UserPreferencesEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        // Значения по умолчанию
        this.minDuration = 0;
        this.maxDuration = Integer.MAX_VALUE;
        this.minYear = 1900;
        this.maxYear = 2100;
        this.adultContentEnabled = false;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getPreferredGenres() {
        return preferredGenres;
    }

    public void setPreferredGenres(String preferredGenres) {
        this.preferredGenres = preferredGenres;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(int minDuration) {
        this.minDuration = minDuration;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getPreferredCountries() {
        return preferredCountries;
    }

    public void setPreferredCountries(String preferredCountries) {
        this.preferredCountries = preferredCountries;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getPreferredLanguages() {
        return preferredLanguages;
    }

    public void setPreferredLanguages(String preferredLanguages) {
        this.preferredLanguages = preferredLanguages;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getMinYear() {
        return minYear;
    }

    public void setMinYear(int minYear) {
        this.minYear = minYear;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getMaxYear() {
        return maxYear;
    }

    public void setMaxYear(int maxYear) {
        this.maxYear = maxYear;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getInterestsTags() {
        return interestsTags;
    }

    public void setInterestsTags(String interestsTags) {
        this.interestsTags = interestsTags;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isAdultContentEnabled() {
        return adultContentEnabled;
    }

    public void setAdultContentEnabled(boolean adultContentEnabled) {
        this.adultContentEnabled = adultContentEnabled;
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
}