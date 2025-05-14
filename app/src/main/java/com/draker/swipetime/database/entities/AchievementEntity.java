package com.draker.swipetime.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Сущность для хранения достижений пользователей
 */
@Entity(tableName = "achievements")
public class AchievementEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "icon_url")
    private String iconUrl;

    @ColumnInfo(name = "experience_reward")
    private int experienceReward;

    @ColumnInfo(name = "required_action")
    private String requiredAction; // swipe, rate, review, etc.

    @ColumnInfo(name = "required_count")
    private int requiredCount;

    @ColumnInfo(name = "category")
    private String category; // beginner, advanced, expert, collector, etc.

    public AchievementEntity() {
    }

    @Ignore
    public AchievementEntity(@NonNull String id, String title, String description, String iconUrl,
                             int experienceReward, String requiredAction, int requiredCount, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconUrl = iconUrl;
        this.experienceReward = experienceReward;
        this.requiredAction = requiredAction;
        this.requiredCount = requiredCount;
        this.category = category;
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
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getExperienceReward() {
        return experienceReward;
    }

    public void setExperienceReward(int experienceReward) {
        this.experienceReward = experienceReward;
    }

    public String getRequiredAction() {
        return requiredAction;
    }

    public void setRequiredAction(String requiredAction) {
        this.requiredAction = requiredAction;
    }

    public int getRequiredCount() {
        return requiredCount;
    }

    public void setRequiredCount(int requiredCount) {
        this.requiredCount = requiredCount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
