package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.draker.swipetime.database.entities.UserAchievementCrossRef;

import java.util.List;

/**
 * DAO для операций с прогрессом достижений пользователей
 */
@Dao
public interface UserAchievementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserAchievementCrossRef userAchievement);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UserAchievementCrossRef> userAchievements);

    @Update
    void update(UserAchievementCrossRef userAchievement);

    @Delete
    void delete(UserAchievementCrossRef userAchievement);

    @Query("DELETE FROM user_achievements WHERE user_id = :userId AND achievement_id = :achievementId")
    void deleteByIds(String userId, String achievementId);

    @Query("SELECT * FROM user_achievements WHERE user_id = :userId AND achievement_id = :achievementId")
    UserAchievementCrossRef getByIds(String userId, String achievementId);

    @Query("SELECT * FROM user_achievements WHERE user_id = :userId")
    List<UserAchievementCrossRef> getByUserId(String userId);

    @Query("SELECT * FROM user_achievements WHERE user_id = :userId")
    LiveData<List<UserAchievementCrossRef>> observeByUserId(String userId);

    @Query("SELECT * FROM user_achievements WHERE achievement_id = :achievementId")
    List<UserAchievementCrossRef> getByAchievementId(String achievementId);

    @Query("SELECT * FROM user_achievements WHERE user_id = :userId AND completed = 1")
    List<UserAchievementCrossRef> getCompletedByUserId(String userId);

    @Query("SELECT * FROM user_achievements WHERE user_id = :userId AND completed = 1")
    LiveData<List<UserAchievementCrossRef>> observeCompletedByUserId(String userId);

    @Query("SELECT * FROM user_achievements WHERE user_id = :userId AND completed = 0")
    List<UserAchievementCrossRef> getInProgressByUserId(String userId);

    @Query("SELECT * FROM user_achievements WHERE user_id = :userId AND completed = 0")
    LiveData<List<UserAchievementCrossRef>> observeInProgressByUserId(String userId);

    @Query("UPDATE user_achievements SET current_progress = :progress WHERE user_id = :userId AND achievement_id = :achievementId")
    void updateProgress(String userId, String achievementId, int progress);

    @Query("UPDATE user_achievements SET current_progress = current_progress + :amount WHERE user_id = :userId AND achievement_id = :achievementId")
    void addProgress(String userId, String achievementId, int amount);

    @Query("UPDATE user_achievements SET completed = 1, completion_date = :timestamp WHERE user_id = :userId AND achievement_id = :achievementId")
    void setCompleted(String userId, String achievementId, long timestamp);

    @Transaction
    @Query("SELECT COUNT(*) FROM user_achievements WHERE user_id = :userId AND completed = 1")
    int getCompletedCountByUserId(String userId);

    @Query("DELETE FROM user_achievements WHERE user_id = :userId")
    void deleteByUserId(String userId);

    @Query("DELETE FROM user_achievements")
    void deleteAll();
}
