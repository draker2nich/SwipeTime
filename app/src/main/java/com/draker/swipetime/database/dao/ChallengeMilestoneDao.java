package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.ChallengeMilestoneEntity;

import java.util.List;

/**
 * DAO для операций с этапами тематических испытаний
 */
@Dao
public interface ChallengeMilestoneDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChallengeMilestoneEntity milestone);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ChallengeMilestoneEntity> milestones);

    @Update
    void update(ChallengeMilestoneEntity milestone);

    @Delete
    void delete(ChallengeMilestoneEntity milestone);

    @Query("DELETE FROM challenge_milestones WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM challenge_milestones WHERE id = :id")
    ChallengeMilestoneEntity getById(String id);

    @Query("SELECT * FROM challenge_milestones WHERE id = :id")
    LiveData<ChallengeMilestoneEntity> observeById(String id);

    @Query("SELECT * FROM challenge_milestones")
    List<ChallengeMilestoneEntity> getAll();

    @Query("SELECT * FROM challenge_milestones")
    LiveData<List<ChallengeMilestoneEntity>> observeAll();

    @Query("SELECT * FROM challenge_milestones WHERE challenge_id = :challengeId ORDER BY order_index")
    List<ChallengeMilestoneEntity> getByChallengeId(String challengeId);

    @Query("SELECT * FROM challenge_milestones WHERE challenge_id = :challengeId ORDER BY order_index")
    LiveData<List<ChallengeMilestoneEntity>> observeByChallengeId(String challengeId);

    @Query("SELECT * FROM challenge_milestones WHERE challenge_id = :challengeId AND order_index = :orderIndex")
    ChallengeMilestoneEntity getByOrderIndex(String challengeId, int orderIndex);

    @Query("SELECT * FROM challenge_milestones WHERE challenge_id = :challengeId AND order_index > :currentIndex ORDER BY order_index LIMIT 1")
    ChallengeMilestoneEntity getNextMilestone(String challengeId, int currentIndex);

    @Query("SELECT * FROM challenge_milestones WHERE action_type = :actionType")
    List<ChallengeMilestoneEntity> getByActionType(String actionType);

    @Query("SELECT * FROM challenge_milestones WHERE content_category = :category OR content_category IS NULL")
    List<ChallengeMilestoneEntity> getByContentCategory(String category);

    @Query("SELECT * FROM challenge_milestones WHERE content_genre = :genre OR content_genre IS NULL")
    List<ChallengeMilestoneEntity> getByContentGenre(String genre);

    @Query("SELECT COUNT(*) FROM challenge_milestones WHERE challenge_id = :challengeId")
    int getCountByChallengeId(String challengeId);

    @Query("DELETE FROM challenge_milestones WHERE challenge_id = :challengeId")
    void deleteByChallengeId(String challengeId);
}
