package com.draker.swipetime.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Сущность для хранения информации о рангах пользователей
 */
@Entity(tableName = "user_ranks")
public class UserRankEntity {

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

    @ColumnInfo(name = "badge_color")
    private String badgeColor;

    @ColumnInfo(name = "required_level")
    private int requiredLevel;
    
    @ColumnInfo(name = "required_achievements_count")
    private int requiredAchievementsCount;
    
    @ColumnInfo(name = "required_categories_count")
    private int requiredCategoriesCount;
    
    @ColumnInfo(name = "bonus_xp_multiplier")
    private float bonusXpMultiplier;
    
    @ColumnInfo(name = "category")
    private String category; // general, movie_buff, bookworm, gamer, etc.
    
    @ColumnInfo(name = "order_index")
    private int orderIndex;

    public UserRankEntity() {
    }

    @Ignore
    public UserRankEntity(@NonNull String id, String name, String description, String iconName,
                        String badgeColor, int requiredLevel, int requiredAchievementsCount,
                        int requiredCategoriesCount, float bonusXpMultiplier, String category,
                        int orderIndex) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        this.badgeColor = badgeColor;
        this.requiredLevel = requiredLevel;
        this.requiredAchievementsCount = requiredAchievementsCount;
        this.requiredCategoriesCount = requiredCategoriesCount;
        this.bonusXpMultiplier = bonusXpMultiplier;
        this.category = category;
        this.orderIndex = orderIndex;
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

    public String getBadgeColor() {
        return badgeColor;
    }

    public void setBadgeColor(String badgeColor) {
        this.badgeColor = badgeColor;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }
    
    public int getRequiredAchievementsCount() {
        return requiredAchievementsCount;
    }
    
    public void setRequiredAchievementsCount(int requiredAchievementsCount) {
        this.requiredAchievementsCount = requiredAchievementsCount;
    }
    
    public int getRequiredCategoriesCount() {
        return requiredCategoriesCount;
    }
    
    public void setRequiredCategoriesCount(int requiredCategoriesCount) {
        this.requiredCategoriesCount = requiredCategoriesCount;
    }
    
    public float getBonusXpMultiplier() {
        return bonusXpMultiplier;
    }
    
    public void setBonusXpMultiplier(float bonusXpMultiplier) {
        this.bonusXpMultiplier = bonusXpMultiplier;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public int getOrderIndex() {
        return orderIndex;
    }
    
    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
    
    /**
     * Проверяет, соответствует ли пользователь требованиям ранга
     * 
     * @param userLevel текущий уровень пользователя
     * @param achievementsCount количество полученных достижений
     * @param categoriesCount количество категорий, в которых активен пользователь
     * @return true если пользователь соответствует требованиям ранга
     */
    public boolean matchesRequirements(int userLevel, int achievementsCount, int categoriesCount) {
        // Проверяем все требования ранга
        return userLevel >= requiredLevel &&
               achievementsCount >= requiredAchievementsCount &&
               categoriesCount >= requiredCategoriesCount;
    }
}
