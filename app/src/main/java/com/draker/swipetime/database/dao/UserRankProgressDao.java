package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.UserRankProgressEntity;

import java.util.List;

/**
 * DAO для операций с прогрессом рангов пользователей
 */
@Dao
public interface UserRankProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserRankProgressEntity progress);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UserRankProgressEntity> progressList);

    @Update
    void update(UserRankProgressEntity progress);

    @Delete
    void delete(UserRankProgressEntity progress);

    @Query("DELETE FROM user_rank_progress WHERE user_id = :userId AND rank_id = :rankId")
    void deleteByIds(String userId, String rankId);

    @Query("SELECT * FROM user_rank_progress WHERE user_id = :userId AND rank_id = :rankId")
    UserRankProgressEntity getByIds(String userId, String rankId);

    @Query("SELECT * FROM user_rank_progress WHERE user_id = :userId AND rank_id = :rankId")
    LiveData<UserRankProgressEntity> observeByIds(String userId, String rankId);

    @Query("SELECT * FROM user_rank_progress WHERE user_id = :userId")
    List<UserRankProgressEntity> getByUserId(String userId);

    @Query("SELECT * FROM user_rank_progress WHERE user_id = :userId")
    LiveData<List<UserRankProgressEntity>> observeByUserId(String userId);

    @Query("SELECT * FROM user_rank_progress WHERE rank_id = :rankId")
    List<UserRankProgressEntity> getByRankId(String rankId);

    @Query("SELECT * FROM user_rank_progress WHERE user_id = :userId AND unlocked = 1")
    List<UserRankProgressEntity> getUnlockedByUserId(String userId);

    @Query("SELECT * FROM user_rank_progress WHERE user_id = :userId AND is_active = 1")
    List<UserRankProgressEntity> getActiveByUserId(String userId);

    @Query("SELECT * FROM user_rank_progress urp JOIN user_ranks ur ON urp.rank_id = ur.id " +
           "WHERE urp.user_id = :userId AND urp.unlocked = 1 AND ur.category = :category")
    List<UserRankProgressEntity> getUnlockedByCategory(String userId, String category);

    @Query("SELECT * FROM user_rank_progress urp JOIN user_ranks ur ON urp.rank_id = ur.id " +
           "WHERE urp.user_id = :userId AND urp.is_active = 1 AND ur.category = :category")
    UserRankProgressEntity getActiveByCategory(String userId, String category);

    @Query("SELECT COUNT(*) FROM user_rank_progress WHERE user_id = :userId AND unlocked = 1")
    int getUnlockedCountByUserId(String userId);

    @Query("SELECT COUNT(*) FROM user_rank_progress urp JOIN user_ranks ur ON urp.rank_id = ur.id " +
           "WHERE urp.user_id = :userId AND urp.unlocked = 1 AND ur.category = :category")
    int getUnlockedCountByCategory(String userId, String category);

    @Query("UPDATE user_rank_progress SET is_active = 0 WHERE user_id = :userId")
    void deactivateAllForUser(String userId);

    @Query("UPDATE user_rank_progress SET is_active = 0 WHERE user_id = :userId AND rank_id IN " +
           "(SELECT id FROM user_ranks WHERE category = :category)")
    void deactivateAllInCategory(String userId, String category);

    @Query("UPDATE user_rank_progress SET is_active = 1 WHERE user_id = :userId AND rank_id = :rankId")
    void activateRank(String userId, String rankId);
}
