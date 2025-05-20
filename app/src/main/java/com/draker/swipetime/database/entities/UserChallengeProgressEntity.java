package com.draker.swipetime.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

/**
 * Сущность для хранения прогресса пользователя по тематическим испытаниям
 */
@Entity(tableName = "user_challenge_progress",
        primaryKeys = {"user_id", "challenge_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = ThematicChallengeEntity.class,
                        parentColumns = "id",
                        childColumns = "challenge_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("user_id"),
                @Index("challenge_id")
        })
public class UserChallengeProgressEntity {

    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @NonNull
    @ColumnInfo(name = "challenge_id")
    private String challengeId;

    @ColumnInfo(name = "current_milestone_index")
    private int currentMilestoneIndex;

    @ColumnInfo(name = "current_progress")
    private int currentProgress;

    @ColumnInfo(name = "total_milestones_completed")
    private int totalMilestonesCompleted;

    @ColumnInfo(name = "completed")
    private boolean completed;

    @ColumnInfo(name = "completion_date")
    private long completionDate;

    @ColumnInfo(name = "reward_claimed")
    private boolean rewardClaimed;

    public UserChallengeProgressEntity() {
    }

    @Ignore
    public UserChallengeProgressEntity(@NonNull String userId, @NonNull String challengeId) {
        this.userId = userId;
        this.challengeId = challengeId;
        this.currentMilestoneIndex = 0;
        this.currentProgress = 0;
        this.totalMilestonesCompleted = 0;
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
    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(@NonNull String challengeId) {
        this.challengeId = challengeId;
    }

    public int getCurrentMilestoneIndex() {
        return currentMilestoneIndex;
    }

    public void setCurrentMilestoneIndex(int currentMilestoneIndex) {
        this.currentMilestoneIndex = currentMilestoneIndex;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public int getTotalMilestonesCompleted() {
        return totalMilestonesCompleted;
    }

    public void setTotalMilestonesCompleted(int totalMilestonesCompleted) {
        this.totalMilestonesCompleted = totalMilestonesCompleted;
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
     * Получить дату присоединения к испытанию
     * @return дата присоединения
     */
    public long getJoinDate() {
        // Если дата присоединения не хранится отдельно, используем дату завершения первого этапа
        // В реальной реализации лучше добавить отдельное поле join_date
        return 0; // TODO: Добавить поле join_date в будущих версиях
    }
    
    /**
     * Установить дату присоединения к испытанию
     * @param joinDate дата присоединения
     */
    public void setJoinDate(long joinDate) {
        // TODO: Добавить поле join_date в будущих версиях
    }
    
    /**
     * Получить дату последнего обновления прогресса
     * @return дата последнего обновления
     */
    public long getLastUpdateDate() {
        // TODO: Добавить поле last_update_date в будущих версиях
        return System.currentTimeMillis();
    }
    
    /**
     * Установить дату последнего обновления прогресса
     * @param lastUpdateDate дата последнего обновления
     */
    public void setLastUpdateDate(long lastUpdateDate) {
        // TODO: Добавить поле last_update_date в будущих версиях
    }
    
    /**
     * Проверить, активно ли участие в испытании
     * @return true если участие активно
     */
    public boolean isActive() {
        return !completed; // Участие активно, пока испытание не завершено
    }
    
    /**
     * Установить статус активности участия
     * @param active статус активности
     */
    public void setActive(boolean active) {
        // TODO: Добавить поле is_active в будущих версиях
    }
    
    /**
     * Получить количество завершенных этапов (alias для totalMilestonesCompleted)
     * @return количество завершенных этапов
     */
    public int getCompletedStepsCount() {
        return totalMilestonesCompleted;
    }
    
    /**
     * Установить количество завершенных этапов
     * @param completedStepsCount количество завершенных этапов
     */
    public void setCompletedStepsCount(int completedStepsCount) {
        this.totalMilestonesCompleted = completedStepsCount;
    }

    /**
     * Получить общий прогресс по испытанию
     * @return общий прогресс
     */
    public int getTotalProgress() {
        return currentProgress; // Используем текущий прогресс как общий прогресс
    }
    
    /**
     * Установить общий прогресс по испытанию
     * @param totalProgress общий прогресс
     */
    public void setTotalProgress(int totalProgress) {
        this.currentProgress = totalProgress;
    }
    
    /**
     * Проверить, завершен ли определенный этап
     * @param milestoneIndex индекс этапа
     * @return true если этап завершен
     */
    public boolean isMilestoneCompleted(int milestoneIndex) {
        return milestoneIndex <= totalMilestonesCompleted;
    }
    
    /**
     * Отметить этап как завершенный
     * @param milestoneIndex индекс этапа
     */
    public void markMilestoneCompleted(int milestoneIndex) {
        if (milestoneIndex > totalMilestonesCompleted) {
            totalMilestonesCompleted = milestoneIndex;
            if (milestoneIndex > currentMilestoneIndex) {
                currentMilestoneIndex = milestoneIndex;
            }
        }
    }
    
    /**
     * Установить дату завершения
     * @param completedAt дата завершения
     */
    public void setCompletedAt(long completedAt) {
        this.completionDate = completedAt;
    }
}
