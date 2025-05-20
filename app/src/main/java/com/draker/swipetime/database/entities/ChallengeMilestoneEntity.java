package com.draker.swipetime.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Сущность для хранения этапов (вех) тематических испытаний
 */
@Entity(tableName = "challenge_milestones",
        foreignKeys = {
                @ForeignKey(
                        entity = ThematicChallengeEntity.class,
                        parentColumns = "id",
                        childColumns = "challenge_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("challenge_id")
        })
public class ChallengeMilestoneEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @NonNull
    @ColumnInfo(name = "challenge_id")
    private String challengeId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "order_index")
    private int orderIndex;
    
    @ColumnInfo(name = "action_type")
    private String actionType; // swipe, rate, review, complete

    @ColumnInfo(name = "required_count")
    private int requiredCount;
    
    @ColumnInfo(name = "content_category")
    private String contentCategory; // может быть null, если не требуется конкретная категория
    
    @ColumnInfo(name = "content_genre")
    private String contentGenre; // может быть null, если не требуется конкретный жанр
    
    @ColumnInfo(name = "content_filter")
    private String contentFilter; // JSON строка с дополнительными фильтрами
    
    @ColumnInfo(name = "xp_reward")
    private int xpReward;

    public ChallengeMilestoneEntity() {
    }

    @Ignore
    public ChallengeMilestoneEntity(@NonNull String id, @NonNull String challengeId, int stepNumber,
                                   String title, String description, int experienceReward) {
        this.id = id;
        this.challengeId = challengeId;
        this.title = title;
        this.description = description;
        this.orderIndex = stepNumber;
        this.actionType = "general";
        this.requiredCount = 1;
        this.contentCategory = null;
        this.contentGenre = null;
        this.contentFilter = null;
        this.xpReward = experienceReward;
    }

    @Ignore
    public ChallengeMilestoneEntity(@NonNull String id, @NonNull String challengeId, String title,
                                   String description, int orderIndex, String actionType,
                                   int requiredCount, String contentCategory, String contentGenre,
                                   String contentFilter, int xpReward) {
        this.id = id;
        this.challengeId = challengeId;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
        this.actionType = actionType;
        this.requiredCount = requiredCount;
        this.contentCategory = contentCategory;
        this.contentGenre = contentGenre;
        this.contentFilter = contentFilter;
        this.xpReward = xpReward;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(@NonNull String challengeId) {
        this.challengeId = challengeId;
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

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
    
    public String getActionType() {
        return actionType;
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public int getRequiredCount() {
        return requiredCount;
    }

    public void setRequiredCount(int requiredCount) {
        this.requiredCount = requiredCount;
    }
    
    public String getContentCategory() {
        return contentCategory;
    }
    
    public void setContentCategory(String contentCategory) {
        this.contentCategory = contentCategory;
    }
    
    public String getContentGenre() {
        return contentGenre;
    }
    
    public void setContentGenre(String contentGenre) {
        this.contentGenre = contentGenre;
    }
    
    public String getContentFilter() {
        return contentFilter;
    }
    
    public void setContentFilter(String contentFilter) {
        this.contentFilter = contentFilter;
    }
    
    public int getXpReward() {
        return xpReward;
    }
    
    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }
    
    /**
     * Возвращает номер этапа (синоним для orderIndex)
     * @return номер этапа
     */
    public int getStepNumber() {
        return orderIndex;
    }
    
    /**
     * Устанавливает номер этапа (синоним для orderIndex)
     * @param stepNumber номер этапа
     */
    public void setStepNumber(int stepNumber) {
        this.orderIndex = stepNumber;
    }
    
    /**
     * Возвращает награду за опыт (синоним для xpReward)
     * @return награда за опыт
     */
    public int getExperienceReward() {
        return xpReward;
    }
    
    /**
     * Возвращает требуемый прогресс для завершения этапа (alias для requiredCount)
     * @return требуемый прогресс
     */
    public int getRequiredProgress() {
        return requiredCount;
    }
    
    /**
     * Возвращает ID предметной награды (пока не реализовано)
     * @return ID предметной награды
     */
    public String getItemRewardId() {
        // TODO: Добавить поле item_reward_id в будущих версиях
        return null;
    }
}
