package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.ThematicChallengeEntity;

import java.util.List;

/**
 * DAO для операций с тематическими испытаниями
 */
@Dao
public interface ThematicChallengeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ThematicChallengeEntity challenge);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ThematicChallengeEntity> challenges);

    @Update
    void update(ThematicChallengeEntity challenge);

    @Delete
    void delete(ThematicChallengeEntity challenge);

    @Query("DELETE FROM thematic_challenges WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM thematic_challenges WHERE id = :id")
    ThematicChallengeEntity getById(String id);

    @Query("SELECT * FROM thematic_challenges WHERE id = :id")
    LiveData<ThematicChallengeEntity> observeById(String id);

    @Query("SELECT * FROM thematic_challenges")
    List<ThematicChallengeEntity> getAll();

    @Query("SELECT * FROM thematic_challenges")
    LiveData<List<ThematicChallengeEntity>> observeAll();

    @Query("SELECT * FROM thematic_challenges WHERE is_active = 1")
    List<ThematicChallengeEntity> getActive();

    @Query("SELECT * FROM thematic_challenges WHERE is_active = 1")
    LiveData<List<ThematicChallengeEntity>> observeActive();

    @Query("SELECT * FROM thematic_challenges WHERE start_date <= :currentTime AND end_date >= :currentTime AND is_active = 1")
    List<ThematicChallengeEntity> getCurrentlyActive(long currentTime);

    @Query("SELECT * FROM thematic_challenges WHERE start_date <= :currentTime AND end_date >= :currentTime AND is_active = 1")
    LiveData<List<ThematicChallengeEntity>> observeCurrentlyActive(long currentTime);

    @Query("SELECT * FROM thematic_challenges WHERE end_date < :currentTime")
    List<ThematicChallengeEntity> getPastChallenges(long currentTime);

    @Query("SELECT * FROM thematic_challenges WHERE start_date > :currentTime")
    List<ThematicChallengeEntity> getUpcomingChallenges(long currentTime);

    @Query("SELECT * FROM thematic_challenges WHERE difficulty = :difficulty")
    List<ThematicChallengeEntity> getByDifficulty(int difficulty);

    @Query("SELECT * FROM thematic_challenges WHERE category = :category")
    List<ThematicChallengeEntity> getByCategory(String category);

    @Query("SELECT * FROM thematic_challenges WHERE genre = :genre OR genre IS NULL")
    List<ThematicChallengeEntity> getByGenre(String genre);

    @Query("SELECT * FROM thematic_challenges WHERE associated_event_id = :eventId")
    List<ThematicChallengeEntity> getByEventId(String eventId);

    @Query("SELECT COUNT(*) FROM thematic_challenges WHERE is_active = 1")
    int getActiveCount();

    @Query("UPDATE thematic_challenges SET is_active = 0 WHERE end_date < :currentTime")
    void deactivateEndedChallenges(long currentTime);

    @Query("UPDATE thematic_challenges SET is_active = 1 WHERE start_date <= :currentTime AND end_date >= :currentTime")
    void activateCurrentChallenges(long currentTime);
    
    @Query("SELECT * FROM thematic_challenges WHERE start_date <= :currentTime AND end_date >= :currentTime AND is_active = 1")
    List<ThematicChallengeEntity> getActiveByTime(long currentTime);
    
    @Query("SELECT * FROM thematic_challenges WHERE end_date < :currentTime AND is_active = 1")
    List<ThematicChallengeEntity> getExpiredByTime(long currentTime);
}
