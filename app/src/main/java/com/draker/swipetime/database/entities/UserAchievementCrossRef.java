package com.draker.swipetime.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

/**
 * Кросс-таблица для связи пользователей и их достижений
 */
@Entity(tableName = "user_achievements", primaryKeys = {"user_id", "achievement_id"})
public class UserAchievementCrossRef {

    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @NonNull
    @ColumnInfo(name = "achievement_id")
    private String achievementId;

    @ColumnInfo(name = "current_progress")
    private int currentProgress;

    @ColumnInfo(name = "completed")
    private boolean completed;

    @ColumnInfo(name = "completion_date")
    private long completionDate;

    public UserAchievementCrossRef() {
    }

    @Ignore
    public UserAchievementCrossRef(@NonNull String userId, @NonNull String achievementId, int currentProgress) {
        this.userId = userId;
        this.achievementId = achievementId;
        this.currentProgress = currentProgress;
        this.completed = false;
        this.completionDate = 0;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getAchievementId() {
        return achievementId;
    }

    public void setAchievementId(@NonNull String achievementId) {
        this.achievementId = achievementId;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed) {
            this.completionDate = System.currentTimeMillis();
        }
    }

    public long getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(long completionDate) {
        this.completionDate = completionDate;
    }

    /**
     * Увеличить прогресс достижения и проверить его завершение
     * @param amount количество прогресса для добавления
     * @param requiredCount необходимое количество для завершения
     * @return true если достижение было завершено этим обновлением
     */
    public boolean updateProgress(int amount, int requiredCount) {
        this.currentProgress += amount;
        
        if (!this.completed && this.currentProgress >= requiredCount) {
            this.completed = true;
            this.completionDate = System.currentTimeMillis();
            return true;
        }
        
        return false;
    }
}
