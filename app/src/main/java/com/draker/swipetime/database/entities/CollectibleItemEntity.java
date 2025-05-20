package com.draker.swipetime.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Сущность для хранения коллекционных виртуальных предметов
 */
@Entity(tableName = "collectible_items")
public class CollectibleItemEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "icon_name")
    private String iconName;
    
    @ColumnInfo(name = "rarity")
    private int rarity; // 1 - обычный, 2 - редкий, 3 - эпический, 4 - легендарный, 5 - уникальный

    @ColumnInfo(name = "associated_category")
    private String associatedCategory; // может быть null, если предмет не связан с категорией

    @ColumnInfo(name = "associated_event_id")
    private String associatedEventId; // может быть null, если не связан с событием
    
    @ColumnInfo(name = "is_limited")
    private boolean isLimited; // ограниченный тираж или постоянно доступный
    
    @ColumnInfo(name = "availability_start_date")
    private long availabilityStartDate; // может быть 0, если всегда доступен
    
    @ColumnInfo(name = "availability_end_date")
    private long availabilityEndDate; // может быть 0, если всегда доступен
    
    @ColumnInfo(name = "obtained_from")
    private String obtainedFrom; // quest, achievement, event, purchase, etc.
    
    @ColumnInfo(name = "usage_effect")
    private String usageEffect; // decorative, profile_frame, badge, etc.

    public CollectibleItemEntity() {
    }

    @Ignore
    public CollectibleItemEntity(@NonNull String id, String name, String description, String iconName,
                               int rarity, String associatedCategory, String associatedEventId,
                               boolean isLimited, long availabilityStartDate, long availabilityEndDate,
                               String obtainedFrom, String usageEffect) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        this.rarity = rarity;
        this.associatedCategory = associatedCategory;
        this.associatedEventId = associatedEventId;
        this.isLimited = isLimited;
        this.availabilityStartDate = availabilityStartDate;
        this.availabilityEndDate = availabilityEndDate;
        this.obtainedFrom = obtainedFrom;
        this.usageEffect = usageEffect;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    
    public int getRarity() {
        return rarity;
    }
    
    public void setRarity(int rarity) {
        this.rarity = rarity;
    }

    public String getAssociatedCategory() {
        return associatedCategory;
    }

    public void setAssociatedCategory(String associatedCategory) {
        this.associatedCategory = associatedCategory;
    }

    public String getAssociatedEventId() {
        return associatedEventId;
    }

    public void setAssociatedEventId(String associatedEventId) {
        this.associatedEventId = associatedEventId;
    }
    
    public boolean isLimited() {
        return isLimited;
    }
    
    public void setLimited(boolean limited) {
        isLimited = limited;
    }
    
    public long getAvailabilityStartDate() {
        return availabilityStartDate;
    }
    
    public void setAvailabilityStartDate(long availabilityStartDate) {
        this.availabilityStartDate = availabilityStartDate;
    }
    
    public long getAvailabilityEndDate() {
        return availabilityEndDate;
    }
    
    public void setAvailabilityEndDate(long availabilityEndDate) {
        this.availabilityEndDate = availabilityEndDate;
    }
    
    public String getObtainedFrom() {
        return obtainedFrom;
    }
    
    public void setObtainedFrom(String obtainedFrom) {
        this.obtainedFrom = obtainedFrom;
    }
    
    public String getUsageEffect() {
        return usageEffect;
    }
    
    public void setUsageEffect(String usageEffect) {
        this.usageEffect = usageEffect;
    }
    
    /**
     * Проверяет, доступен ли предмет в настоящий момент
     * @return true если предмет доступен в настоящее время
     */
    public boolean isCurrentlyAvailable() {
        if (!isLimited) {
            return true;
        }
        
        long currentTime = System.currentTimeMillis();
        return currentTime >= availabilityStartDate && 
               (availabilityEndDate == 0 || currentTime <= availabilityEndDate);
    }
    
    /**
     * Возвращает текстовое представление редкости предмета
     * @return строка с названием редкости
     */
    public String getRarityText() {
        switch (rarity) {
            case 1:
                return "Обычный";
            case 2:
                return "Редкий";
            case 3:
                return "Эпический";
            case 4:
                return "Легендарный";
            case 5:
                return "Уникальный";
            default:
                return "Неизвестный";
        }
    }
}
