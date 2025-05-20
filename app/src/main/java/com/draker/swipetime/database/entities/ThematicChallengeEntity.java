package com.draker.swipetime.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Сущность для хранения тематических испытаний
 */
@Entity(tableName = "thematic_challenges")
public class ThematicChallengeEntity {

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

    @ColumnInfo(name = "theme_color")
    private String themeColor;

    @ColumnInfo(name = "start_date")
    private long startDate;

    @ColumnInfo(name = "end_date")
    private long endDate;

    @ColumnInfo(name = "is_active")
    private boolean isActive;
    
    @ColumnInfo(name = "difficulty")
    private int difficulty; // 1 - легкое, 2 - среднее, 3 - сложное
    
    @ColumnInfo(name = "category")
    private String category; // фильмы, сериалы, книги, etc.
    
    @ColumnInfo(name = "genre")
    private String genre; // может быть null, если испытание не привязано к конкретному жанру
    
    @ColumnInfo(name = "xp_reward")
    private int xpReward;
    
    @ColumnInfo(name = "item_reward_id")
    private String itemRewardId; // может быть null, если нет предметной награды
    
    @ColumnInfo(name = "associated_event_id")
    private String associatedEventId; // может быть null, если не привязано к событию
    
    @ColumnInfo(name = "required_steps_count")
    private int requiredStepsCount; // количество этапов для выполнения испытания

    public ThematicChallengeEntity() {
    }

    @Ignore
    public ThematicChallengeEntity(@NonNull String id, String title, String description, String iconName,
                                  String themeColor, long startDate, long endDate, boolean isActive,
                                  int difficulty, String category, String genre, int xpReward,
                                  String itemRewardId, String associatedEventId, int requiredStepsCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconName = iconName;
        this.themeColor = themeColor;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.difficulty = difficulty;
        this.category = category;
        this.genre = genre;
        this.xpReward = xpReward;
        this.itemRewardId = itemRewardId;
        this.associatedEventId = associatedEventId;
        this.requiredStepsCount = requiredStepsCount;
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

    public String getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
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
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public int getXpReward() {
        return xpReward;
    }
    
    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }
    
    public String getItemRewardId() {
        return itemRewardId;
    }
    
    public void setItemRewardId(String itemRewardId) {
        this.itemRewardId = itemRewardId;
    }
    
    public String getAssociatedEventId() {
        return associatedEventId;
    }
    
    public void setAssociatedEventId(String associatedEventId) {
        this.associatedEventId = associatedEventId;
    }
    
    public int getRequiredStepsCount() {
        return requiredStepsCount;
    }
    
    public void setRequiredStepsCount(int requiredStepsCount) {
        this.requiredStepsCount = requiredStepsCount;
    }
    
    /**
     * Проверяет, активно ли испытание в настоящий момент
     * @return true если испытание активно
     */
    public boolean isCurrentlyActive() {
        long currentTime = System.currentTimeMillis();
        return isActive && currentTime >= startDate && currentTime <= endDate;
    }
    
    /**
     * Проверяет, закончилось ли испытание
     * @return true если испытание закончилось
     */
    public boolean isEnded() {
        return System.currentTimeMillis() > endDate;
    }
    
    /**
     * Вычисляет процент оставшегося времени испытания (от 0 до 100)
     * @return процент оставшегося времени
     */
    public int getRemainingTimePercentage() {
        long currentTime = System.currentTimeMillis();
        if (currentTime < startDate) {
            return 100;
        } else if (currentTime > endDate) {
            return 0;
        } else {
            long totalDuration = endDate - startDate;
            long remaining = endDate - currentTime;
            return (int) ((remaining * 100) / totalDuration);
        }
    }
    
    /**
     * Возвращает текстовое представление уровня сложности
     * @return строка с названием сложности
     */
    public String getDifficultyText() {
        switch (difficulty) {
            case 1:
                return "Легкое";
            case 2:
                return "Среднее";
            case 3:
                return "Сложное";
            default:
                return "Неизвестно";
        }
    }
}
