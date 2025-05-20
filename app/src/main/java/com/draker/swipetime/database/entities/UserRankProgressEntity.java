package com.draker.swipetime.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

/**
 * Сущность для хранения прогресса пользователя по рангам
 */
@Entity(tableName = "user_rank_progress",
        primaryKeys = {"user_id", "rank_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = UserRankEntity.class,
                        parentColumns = "id",
                        childColumns = "rank_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("user_id"),
                @Index("rank_id")
        })
public class UserRankProgressEntity {

    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @NonNull
    @ColumnInfo(name = "rank_id")
    private String rankId;

    @ColumnInfo(name = "unlocked")
    private boolean unlocked;

    @ColumnInfo(name = "unlock_date")
    private long unlockDate;

    @ColumnInfo(name = "is_active")
    private boolean isActive;

    @ColumnInfo(name = "level_progress")
    private int levelProgress; // прогресс по требованию к уровню (процент)

    @ColumnInfo(name = "achievements_progress")
    private int achievementsProgress; // прогресс по требованию к достижениям (процент)

    @ColumnInfo(name = "categories_progress")
    private int categoriesProgress; // прогресс по требованию к категориям (процент)

    public UserRankProgressEntity() {
    }

    @Ignore
    public UserRankProgressEntity(@NonNull String userId, @NonNull String rankId) {
        this.userId = userId;
        this.rankId = rankId;
        this.unlocked = false;
        this.unlockDate = 0;
        this.isActive = false;
        this.levelProgress = 0;
        this.achievementsProgress = 0;
        this.categoriesProgress = 0;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getRankId() {
        return rankId;
    }

    public void setRankId(@NonNull String rankId) {
        this.rankId = rankId;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
        if (unlocked && unlockDate == 0) {
            this.unlockDate = System.currentTimeMillis();
        }
    }

    public long getUnlockDate() {
        return unlockDate;
    }

    public void setUnlockDate(long unlockDate) {
        this.unlockDate = unlockDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getLevelProgress() {
        return levelProgress;
    }

    public void setLevelProgress(int levelProgress) {
        this.levelProgress = levelProgress;
    }

    public int getAchievementsProgress() {
        return achievementsProgress;
    }

    public void setAchievementsProgress(int achievementsProgress) {
        this.achievementsProgress = achievementsProgress;
    }

    public int getCategoriesProgress() {
        return categoriesProgress;
    }

    public void setCategoriesProgress(int categoriesProgress) {
        this.categoriesProgress = categoriesProgress;
    }

    /**
     * Обновляет прогресс по всем требованиям ранга
     *
     * @param userLevel текущий уровень пользователя
     * @param achievementsCount количество полученных достижений
     * @param categoriesCount количество категорий, в которых активен пользователь
     * @param requiredLevel требуемый уровень для ранга
     * @param requiredAchievements требуемое количество достижений для ранга
     * @param requiredCategories требуемое количество категорий для ранга
     * @return true если ранг был разблокирован этим обновлением
     */
    public boolean updateProgress(int userLevel, int achievementsCount, int categoriesCount,
                                 int requiredLevel, int requiredAchievements, int requiredCategories) {
        boolean wasUnlockedBefore = this.unlocked;
        
        // Обновляем прогресс по уровню
        if (requiredLevel > 0) {
            this.levelProgress = Math.min(100, (userLevel * 100) / requiredLevel);
        } else {
            this.levelProgress = 100;
        }
        
        // Обновляем прогресс по достижениям
        if (requiredAchievements > 0) {
            this.achievementsProgress = Math.min(100, (achievementsCount * 100) / requiredAchievements);
        } else {
            this.achievementsProgress = 100;
        }
        
        // Обновляем прогресс по категориям
        if (requiredCategories > 0) {
            this.categoriesProgress = Math.min(100, (categoriesCount * 100) / requiredCategories);
        } else {
            this.categoriesProgress = 100;
        }
        
        // Проверяем, разблокирован ли ранг
        if (this.levelProgress >= 100 && this.achievementsProgress >= 100 && this.categoriesProgress >= 100) {
            this.unlocked = true;
            if (this.unlockDate == 0) {
                this.unlockDate = System.currentTimeMillis();
            }
        }
        
        return this.unlocked && !wasUnlockedBefore;
    }
    
    /**
     * Рассчитывает общий прогресс для разблокировки ранга
     * @return процент общего прогресса (0-100)
     */
    public int calculateOverallProgress() {
        // Средний прогресс по всем трем требованиям
        return (levelProgress + achievementsProgress + categoriesProgress) / 3;
    }
}
