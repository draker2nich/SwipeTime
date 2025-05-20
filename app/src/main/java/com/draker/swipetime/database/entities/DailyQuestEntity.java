package com.draker.swipetime.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Сущность для хранения ежедневных заданий
 */
@Entity(tableName = "daily_quests")
public class DailyQuestEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "icon_name")
    private String iconName;

    @ColumnInfo(name = "experience_reward")
    private int experienceReward;
    
    @ColumnInfo(name = "item_reward_id")
    private String itemRewardId;

    @ColumnInfo(name = "required_action")
    private String requiredAction; // swipe, rate, review, etc.

    @ColumnInfo(name = "required_count")
    private int requiredCount;
    
    @ColumnInfo(name = "required_category")
    private String requiredCategory; // может быть null, если задание не привязано к категории
    
    @ColumnInfo(name = "creation_date")
    private long creationDate;
    
    @ColumnInfo(name = "expiration_date")
    private long expirationDate;
    
    @ColumnInfo(name = "is_active")
    private boolean isActive;
    
    @ColumnInfo(name = "difficulty")
    private int difficulty; // 1 - легкое, 2 - среднее, 3 - сложное

    public DailyQuestEntity() {
    }

    @Ignore
    public DailyQuestEntity(@NonNull String id, String title, String description, String iconName,
                          int experienceReward, String itemRewardId, String requiredAction, 
                          int requiredCount, String requiredCategory, long creationDate, 
                          long expirationDate, boolean isActive, int difficulty) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconName = iconName;
        this.experienceReward = experienceReward;
        this.itemRewardId = itemRewardId;
        this.requiredAction = requiredAction;
        this.requiredCount = requiredCount;
        this.requiredCategory = requiredCategory;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.isActive = isActive;
        this.difficulty = difficulty;
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

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public int getExperienceReward() {
        return experienceReward;
    }

    public void setExperienceReward(int experienceReward) {
        this.experienceReward = experienceReward;
    }
    
    public String getItemRewardId() {
        return itemRewardId;
    }
    
    public void setItemRewardId(String itemRewardId) {
        this.itemRewardId = itemRewardId;
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
    
    public String getRequiredCategory() {
        return requiredCategory;
    }
    
    public void setRequiredCategory(String requiredCategory) {
        this.requiredCategory = requiredCategory;
    }
    
    public long getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }
    
    public long getExpirationDate() {
        return expirationDate;
    }
    
    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public int getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
    
    /**
     * Проверяет, истек ли срок действия задания
     * @return true если задание истекло
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationDate;
    }
}
