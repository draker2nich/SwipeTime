package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.UserStatsEntity;

import java.util.List;

/**
 * DAO для операций со статистикой пользователей
 */
@Dao
public interface UserStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserStatsEntity userStats);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UserStatsEntity> userStatsList);

    @Update
    void update(UserStatsEntity userStats);

    @Delete
    void delete(UserStatsEntity userStats);

    @Query("DELETE FROM user_stats WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM user_stats WHERE id = :id")
    UserStatsEntity getById(long id);

    @Query("SELECT * FROM user_stats WHERE id = :id")
    LiveData<UserStatsEntity> observeById(long id);

    @Query("SELECT * FROM user_stats WHERE user_id = :userId")
    UserStatsEntity getByUserId(String userId);

    @Query("SELECT * FROM user_stats WHERE user_id = :userId")
    LiveData<UserStatsEntity> observeByUserId(String userId);

    @Query("SELECT * FROM user_stats")
    List<UserStatsEntity> getAll();

    @Query("SELECT * FROM user_stats")
    LiveData<List<UserStatsEntity>> observeAll();

    @Query("SELECT * FROM user_stats ORDER BY streak_days DESC")
    List<UserStatsEntity> getAllOrderedByStreakDesc();

    @Query("SELECT * FROM user_stats ORDER BY total_actions DESC")
    List<UserStatsEntity> getAllOrderedByActionsDesc();

    @Query("SELECT * FROM user_stats ORDER BY swipes_count DESC")
    List<UserStatsEntity> getAllOrderedBySwipesDesc();

    @Query("SELECT * FROM user_stats ORDER BY ratings_count DESC")
    List<UserStatsEntity> getAllOrderedByRatingsDesc();

    @Query("SELECT * FROM user_stats ORDER BY reviews_count DESC")
    List<UserStatsEntity> getAllOrderedByReviewsDesc();

    @Query("SELECT * FROM user_stats ORDER BY consumed_count DESC")
    List<UserStatsEntity> getAllOrderedByConsumedDesc();

    @Query("SELECT * FROM user_stats ORDER BY achievements_count DESC")
    List<UserStatsEntity> getAllOrderedByAchievementsDesc();

    @Query("UPDATE user_stats SET swipes_count = swipes_count + 1, " +
            "right_swipes_count = CASE WHEN :direction = 1 THEN right_swipes_count + 1 ELSE right_swipes_count END, " +
            "left_swipes_count = CASE WHEN :direction = 0 THEN left_swipes_count + 1 ELSE left_swipes_count END, " +
            "total_actions = total_actions + 1, " +
            "last_activity_date = :activityDate " +
            "WHERE user_id = :userId")
    void incrementSwipes(String userId, int direction, long activityDate);

    @Query("UPDATE user_stats SET ratings_count = ratings_count + 1, " +
            "total_actions = total_actions + 1, " +
            "last_activity_date = :activityDate " +
            "WHERE user_id = :userId")
    void incrementRatings(String userId, long activityDate);

    @Query("UPDATE user_stats SET reviews_count = reviews_count + 1, " +
            "total_actions = total_actions + 1, " +
            "last_activity_date = :activityDate " +
            "WHERE user_id = :userId")
    void incrementReviews(String userId, long activityDate);

    @Query("UPDATE user_stats SET consumed_count = consumed_count + 1, " +
            "last_activity_date = :activityDate " +
            "WHERE user_id = :userId")
    void incrementConsumed(String userId, long activityDate);
    
    @Query("UPDATE user_stats SET achievements_count = achievements_count + 1, " +
            "last_activity_date = :activityDate " +
            "WHERE user_id = :userId")
    void incrementAchievements(String userId, long activityDate);

    @Query("UPDATE user_stats SET streak_days = :streakDays " +
            "WHERE user_id = :userId")
    void updateStreakDays(String userId, int streakDays);

    @Query("SELECT COUNT(*) FROM user_stats")
    int getCount();

    @Query("DELETE FROM user_stats")
    void deleteAll();

    @Query("DELETE FROM user_stats WHERE user_id = :userId")
    void deleteByUserId(String userId);
}
