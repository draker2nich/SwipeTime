package com.draker.swipetime.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Сущность для хранения информации о сезонных событиях
 */
@Entity(tableName = "seasonal_events")
public class SeasonalEventEntity {

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
    
    @ColumnInfo(name = "has_special_items")
    private boolean hasSpecialItems;
    
    @ColumnInfo(name = "has_special_quests")
    private boolean hasSpecialQuests;
    
    @ColumnInfo(name = "bonus_xp_multiplier")
    private float bonusXpMultiplier;
    
    @ColumnInfo(name = "event_type")
    private String eventType; // holiday, special, collab, etc.

    public SeasonalEventEntity() {
    }

    @Ignore
    public SeasonalEventEntity(@NonNull String id, String title, String description, String iconName,
                             String themeColor, long startDate, long endDate, boolean isActive,
                             boolean hasSpecialItems, boolean hasSpecialQuests, 
                             float bonusXpMultiplier, String eventType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconName = iconName;
        this.themeColor = themeColor;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.hasSpecialItems = hasSpecialItems;
        this.hasSpecialQuests = hasSpecialQuests;
        this.bonusXpMultiplier = bonusXpMultiplier;
        this.eventType = eventType;
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
    
    public boolean hasSpecialItems() {
        return hasSpecialItems;
    }
    
    public void setHasSpecialItems(boolean hasSpecialItems) {
        this.hasSpecialItems = hasSpecialItems;
    }
    
    public boolean hasSpecialQuests() {
        return hasSpecialQuests;
    }
    
    public void setHasSpecialQuests(boolean hasSpecialQuests) {
        this.hasSpecialQuests = hasSpecialQuests;
    }
    
    public float getBonusXpMultiplier() {
        return bonusXpMultiplier;
    }
    
    public void setBonusXpMultiplier(float bonusXpMultiplier) {
        this.bonusXpMultiplier = bonusXpMultiplier;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    /**
     * Проверяет, активно ли событие в настоящий момент
     * @return true если текущая дата находится между startDate и endDate
     */
    public boolean isCurrentlyActive() {
        long currentTime = System.currentTimeMillis();
        return isActive && currentTime >= startDate && currentTime <= endDate;
    }
    
    /**
     * Проверяет, закончилось ли событие
     * @return true если текущая дата после endDate
     */
    public boolean isEnded() {
        return System.currentTimeMillis() > endDate;
    }
    
    /**
     * Проверяет, началось ли событие
     * @return true если текущая дата после startDate
     */
    public boolean hasStarted() {
        return System.currentTimeMillis() >= startDate;
    }
    
    /**
     * Вычисляет процент прогресса события (от 0 до 100)
     * @return процент прогресса или 0, если событие еще не началось
     */
    public int getProgressPercentage() {
        long currentTime = System.currentTimeMillis();
        if (currentTime < startDate) {
            return 0;
        } else if (currentTime > endDate) {
            return 100;
        } else {
            long totalDuration = endDate - startDate;
            long elapsed = currentTime - startDate;
            return (int) ((elapsed * 100) / totalDuration);
        }
    }
}
