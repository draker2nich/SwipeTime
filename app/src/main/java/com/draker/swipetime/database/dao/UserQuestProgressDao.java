package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.UserQuestProgressEntity;

import java.util.List;

/**
 * DAO для операций с прогрессом ежедневных заданий пользователя
 */
@Dao
public interface UserQuestProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserQuestProgressEntity progress);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UserQuestProgressEntity> progressList);

    @Update
    void update(UserQuestProgressEntity progress);

    @Delete
    void delete(UserQuestProgressEntity progress);

    @Query("DELETE FROM user_quest_progress WHERE user_id = :userId AND quest_id = :questId")
    void deleteByIds(String userId, String questId);

    @Query("SELECT * FROM user_quest_progress WHERE user_id = :userId AND quest_id = :questId")
    UserQuestProgressEntity getByIds(String userId, String questId);

    @Query("SELECT * FROM user_quest_progress WHERE user_id = :userId")
    List<UserQuestProgressEntity> getByUserId(String userId);

    @Query("SELECT * FROM user_quest_progress WHERE user_id = :userId")
    LiveData<List<UserQuestProgressEntity>> observeByUserId(String userId);

    @Query("SELECT * FROM user_quest_progress WHERE quest_id = :questId")
    List<UserQuestProgressEntity> getByQuestId(String questId);

    @Query("SELECT * FROM user_quest_progress WHERE user_id = :userId AND completed = 1")
    List<UserQuestProgressEntity> getCompletedByUserId(String userId);

    @Query("SELECT * FROM user_quest_progress WHERE user_id = :userId AND completed = 1 AND reward_claimed = 0")
    List<UserQuestProgressEntity> getCompletedUnclaimedByUserId(String userId);

    @Query("SELECT COUNT(*) FROM user_quest_progress WHERE user_id = :userId AND completed = 1")
    int getCompletedCountByUserId(String userId);

    @Query("SELECT COUNT(*) FROM user_quest_progress WHERE user_id = :userId AND completed = 1 AND completion_date >= :startDate AND completion_date <= :endDate")
    int getCompletedCountInDateRange(String userId, long startDate, long endDate);

    @Query("UPDATE user_quest_progress SET reward_claimed = 1 WHERE user_id = :userId AND quest_id = :questId")
    void markRewardClaimed(String userId, String questId);

    @Query("UPDATE user_quest_progress SET current_progress = :progress WHERE user_id = :userId AND quest_id = :questId")
    void updateProgress(String userId, String questId, int progress);

    @Query("SELECT * FROM user_quest_progress upp JOIN daily_quests dq ON upp.quest_id = dq.id " +
           "WHERE upp.user_id = :userId AND dq.is_active = 1 AND dq.expiration_date > :currentTime")
    List<UserQuestProgressEntity> getActiveQuestProgress(String userId, long currentTime);

    @Query("DELETE FROM user_quest_progress WHERE quest_id IN " +
           "(SELECT id FROM daily_quests WHERE expiration_date < :currentTime)")
    void deleteExpiredQuestProgress(long currentTime);
}
