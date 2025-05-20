package com.draker.swipetime.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

/**
 * Сущность для хранения прогресса пользователя по ежедневным заданиям
 */
@Entity(tableName = "user_quest_progress",
        primaryKeys = {"user_id", "quest_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = DailyQuestEntity.class,
                        parentColumns = "id",
                        childColumns = "quest_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("user_id"),
                @Index("quest_id")
        })
public class UserQuestProgressEntity {

    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @NonNull
    @ColumnInfo(name = "quest_id")
    private String questId;

    @ColumnInfo(name = "current_progress")
    private int currentProgress;

    @ColumnInfo(name = "completed")
    private boolean completed;

    @ColumnInfo(name = "completion_date")
    private long completionDate;

    @ColumnInfo(name = "reward_claimed")
    private boolean rewardClaimed;

    public UserQuestProgressEntity() {
    }

    @Ignore
    public UserQuestProgressEntity(@NonNull String userId, @NonNull String questId) {
        this.userId = userId;
        this.questId = questId;
        this.currentProgress = 0;
        this.completed = false;
        this.completionDate = 0;
        this.rewardClaimed = false;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getQuestId() {
        return questId;
    }

    public void setQuestId(@NonNull String questId) {
        this.questId = questId;
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
        if (completed && completionDate == 0) {
            this.completionDate = System.currentTimeMillis();
        }
    }

    public long getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(long completionDate) {
        this.completionDate = completionDate;
    }

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    public void setRewardClaimed(boolean rewardClaimed) {
        this.rewardClaimed = rewardClaimed;
    }

    /**
     * Обновляет прогресс задания и проверяет его завершение
     * 
     * @param amount количество для добавления к прогрессу
     * @param requiredCount необходимое количество для завершения
     * @return true если задание было завершено этим обновлением
     */
    public boolean updateProgress(int amount, int requiredCount) {
        boolean wasCompletedBefore = this.completed;
        this.currentProgress += amount;

        if (!this.completed && this.currentProgress >= requiredCount) {
            this.completed = true;
            this.completionDate = System.currentTimeMillis();
            return !wasCompletedBefore; // Возвращаем true только если задание ранее не было завершено
        }

        return false;
    }
}
