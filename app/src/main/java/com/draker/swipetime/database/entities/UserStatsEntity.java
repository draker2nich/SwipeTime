package com.draker.swipetime.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Сущность для хранения статистики действий пользователя
 */
@Entity(tableName = "user_stats", 
        foreignKeys = {
            @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
            )
        },
        indices = {
            @Index("user_id")
        })
public class UserStatsEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;
    
    // Количество свайпов
    @ColumnInfo(name = "swipes_count")
    private int swipesCount;
    
    // Количество свайпов "вправо" (понравилось)
    @ColumnInfo(name = "right_swipes_count")
    private int rightSwipesCount;
    
    // Количество свайпов "влево" (не понравилось)
    @ColumnInfo(name = "left_swipes_count")
    private int leftSwipesCount;
    
    // Количество поставленных оценок
    @ColumnInfo(name = "ratings_count")
    private int ratingsCount;
    
    // Количество написанных рецензий
    @ColumnInfo(name = "reviews_count")
    private int reviewsCount;
    
    // Количество просмотренного/прочитанного контента
    @ColumnInfo(name = "consumed_count")
    private int consumedCount;
    
    // Общее количество действий пользователя
    @ColumnInfo(name = "total_actions")
    private int totalActions;
    
    // Количество дней подряд с активностью
    @ColumnInfo(name = "streak_days")
    private int streakDays;
    
    // Дата последней активности
    @ColumnInfo(name = "last_activity_date")
    private long lastActivityDate;
    
    // Количество полученных достижений
    @ColumnInfo(name = "achievements_count")
    private int achievementsCount;
    
    public UserStatsEntity() {
        this.swipesCount = 0;
        this.rightSwipesCount = 0;
        this.leftSwipesCount = 0;
        this.ratingsCount = 0;
        this.reviewsCount = 0;
        this.consumedCount = 0;
        this.totalActions = 0;
        this.streakDays = 0;
        this.lastActivityDate = 0;
        this.achievementsCount = 0;
    }
    
    @Ignore
    public UserStatsEntity(@NonNull String userId) {
        this.userId = userId;
        this.swipesCount = 0;
        this.rightSwipesCount = 0;
        this.leftSwipesCount = 0;
        this.ratingsCount = 0;
        this.reviewsCount = 0;
        this.consumedCount = 0;
        this.totalActions = 0;
        this.streakDays = 0;
        this.lastActivityDate = System.currentTimeMillis();
        this.achievementsCount = 0;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    @NonNull
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }
    
    public int getSwipesCount() {
        return swipesCount;
    }
    
    public void setSwipesCount(int swipesCount) {
        this.swipesCount = swipesCount;
        this.totalActions = calculateTotalActions();
    }
    
    public int getRightSwipesCount() {
        return rightSwipesCount;
    }
    
    public void setRightSwipesCount(int rightSwipesCount) {
        this.rightSwipesCount = rightSwipesCount;
    }
    
    public int getLeftSwipesCount() {
        return leftSwipesCount;
    }
    
    public void setLeftSwipesCount(int leftSwipesCount) {
        this.leftSwipesCount = leftSwipesCount;
    }
    
    public int getRatingsCount() {
        return ratingsCount;
    }
    
    public void setRatingsCount(int ratingsCount) {
        this.ratingsCount = ratingsCount;
        this.totalActions = calculateTotalActions();
    }
    
    public int getReviewsCount() {
        return reviewsCount;
    }
    
    public void setReviewsCount(int reviewsCount) {
        this.reviewsCount = reviewsCount;
        this.totalActions = calculateTotalActions();
    }
    
    public int getConsumedCount() {
        return consumedCount;
    }
    
    public void setConsumedCount(int consumedCount) {
        this.consumedCount = consumedCount;
    }
    
    public int getTotalActions() {
        return totalActions;
    }
    
    public void setTotalActions(int totalActions) {
        this.totalActions = totalActions;
    }
    
    public int getStreakDays() {
        return streakDays;
    }
    
    public void setStreakDays(int streakDays) {
        this.streakDays = streakDays;
    }
    
    public long getLastActivityDate() {
        return lastActivityDate;
    }
    
    public void setLastActivityDate(long lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }
    
    public int getAchievementsCount() {
        return achievementsCount;
    }
    
    public void setAchievementsCount(int achievementsCount) {
        this.achievementsCount = achievementsCount;
    }
    
    /**
     * Увеличивает счетчик свайпов и обновляет статистику
     * @param direction направление свайпа (true - вправо, false - влево)
     */
    public void incrementSwipes(boolean direction) {
        this.swipesCount++;
        if (direction) {
            this.rightSwipesCount++;
        } else {
            this.leftSwipesCount++;
        }
        this.totalActions = calculateTotalActions();
        this.lastActivityDate = System.currentTimeMillis();
    }
    
    /**
     * Увеличивает счетчик оценок и обновляет статистику
     */
    public void incrementRatings() {
        this.ratingsCount++;
        this.totalActions = calculateTotalActions();
        this.lastActivityDate = System.currentTimeMillis();
    }
    
    /**
     * Увеличивает счетчик рецензий и обновляет статистику
     */
    public void incrementReviews() {
        this.reviewsCount++;
        this.totalActions = calculateTotalActions();
        this.lastActivityDate = System.currentTimeMillis();
    }
    
    /**
     * Увеличивает счетчик просмотренного/прочитанного контента
     */
    public void incrementConsumed() {
        this.consumedCount++;
        this.lastActivityDate = System.currentTimeMillis();
    }
    
    /**
     * Увеличивает счетчик достижений
     */
    public void incrementAchievements() {
        this.achievementsCount++;
        this.lastActivityDate = System.currentTimeMillis();
    }
    
    /**
     * Обновляет дни активности на основе последней даты
     * @param currentDate текущая дата в миллисекундах
     */
    public void updateStreak(long currentDate) {
        // Один день в миллисекундах
        final long ONE_DAY_MS = 24 * 60 * 60 * 1000;
        
        if (this.lastActivityDate == 0) {
            // Первая активность
            this.streakDays = 1;
        } else {
            // Проверяем, была ли активность вчера или сегодня
            long daysDiff = (currentDate - this.lastActivityDate) / ONE_DAY_MS;
            
            if (daysDiff == 0) {
                // Активность уже была сегодня, ничего не делаем
            } else if (daysDiff == 1) {
                // Активность была вчера, увеличиваем счетчик
                this.streakDays++;
            } else {
                // Пропущены дни, сбрасываем счетчик
                this.streakDays = 1;
            }
        }
        
        this.lastActivityDate = currentDate;
    }
    
    /**
     * Рассчитывает общее количество действий
     * @return общее количество действий
     */
    private int calculateTotalActions() {
        return this.swipesCount + this.ratingsCount + this.reviewsCount;
    }
}
