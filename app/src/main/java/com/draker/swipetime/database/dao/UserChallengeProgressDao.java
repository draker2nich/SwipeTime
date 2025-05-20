package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.UserChallengeProgressEntity;

import java.util.List;

/**
 * DAO для операций с прогрессом пользователя по тематическим испытаниям
 */
@Dao
public interface UserChallengeProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserChallengeProgressEntity progress);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UserChallengeProgressEntity> progressList);

    @Update
    void update(UserChallengeProgressEntity progress);

    @Delete
    void delete(UserChallengeProgressEntity progress);

    @Query("DELETE FROM user_challenge_progress WHERE user_id = :userId AND challenge_id = :challengeId")
    void deleteByIds(String userId, String challengeId);

    @Query("SELECT * FROM user_challenge_progress WHERE user_id = :userId AND challenge_id = :challengeId")
    UserChallengeProgressEntity getByIds(String userId, String challengeId);

    @Query("SELECT * FROM user_challenge_progress WHERE user_id = :userId AND challenge_id = :challengeId")
    LiveData<UserChallengeProgressEntity> observeByIds(String userId, String challengeId);

    @Query("SELECT * FROM user_challenge_progress WHERE user_id = :userId")
    List<UserChallengeProgressEntity> getByUserId(String userId);

    @Query("SELECT * FROM user_challenge_progress WHERE user_id = :userId")
    LiveData<List<UserChallengeProgressEntity>> observeByUserId(String userId);

    @Query("SELECT * FROM user_challenge_progress WHERE challenge_id = :challengeId")
    List<UserChallengeProgressEntity> getByChallengeId(String challengeId);

    @Query("SELECT * FROM user_challenge_progress WHERE user_id = :userId AND completed = 1")
    List<UserChallengeProgressEntity> getCompletedByUserId(String userId);

    @Query("SELECT * FROM user_challenge_progress WHERE user_id = :userId AND completed = 1 AND reward_claimed = 0")
    List<UserChallengeProgressEntity> getCompletedUnclaimedByUserId(String userId);

    @Query("SELECT * FROM user_challenge_progress ucp JOIN thematic_challenges tc ON ucp.challenge_id = tc.id " +
           "WHERE ucp.user_id = :userId AND tc.is_active = 1 AND tc.end_date >= :currentTime")
    List<UserChallengeProgressEntity> getActiveByUserId(String userId, long currentTime);

    @Query("SELECT * FROM user_challenge_progress ucp JOIN thematic_challenges tc ON ucp.challenge_id = tc.id " +
           "WHERE ucp.user_id = :userId AND tc.is_active = 1 AND tc.end_date >= :currentTime")
    LiveData<List<UserChallengeProgressEntity>> observeActiveByUserId(String userId, long currentTime);

    @Query("SELECT COUNT(*) FROM user_challenge_progress WHERE user_id = :userId AND completed = 1")
    int getCompletedCountByUserId(String userId);

    @Query("SELECT COUNT(*) FROM user_challenge_progress ucp JOIN thematic_challenges tc ON ucp.challenge_id = tc.id " +
           "WHERE ucp.user_id = :userId AND ucp.completed = 1 AND tc.category = :category")
    int getCompletedCountByCategory(String userId, String category);

    @Query("SELECT COUNT(*) FROM user_challenge_progress ucp JOIN thematic_challenges tc ON ucp.challenge_id = tc.id " +
           "WHERE ucp.user_id = :userId AND ucp.completed = 1 AND tc.difficulty = :difficulty")
    int getCompletedCountByDifficulty(String userId, int difficulty);

    @Query("UPDATE user_challenge_progress SET current_milestone_index = :milestoneIndex, current_progress = :progress " +
           "WHERE user_id = :userId AND challenge_id = :challengeId")
    void updateMilestoneProgress(String userId, String challengeId, int milestoneIndex, int progress);

    @Query("UPDATE user_challenge_progress SET reward_claimed = 1 WHERE user_id = :userId AND challenge_id = :challengeId")
    void markRewardClaimed(String userId, String challengeId);
}
