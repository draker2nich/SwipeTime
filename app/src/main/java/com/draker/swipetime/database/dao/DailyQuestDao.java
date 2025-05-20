package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.DailyQuestEntity;

import java.util.List;

/**
 * DAO для операций с ежедневными заданиями
 */
@Dao
public interface DailyQuestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DailyQuestEntity quest);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DailyQuestEntity> quests);

    @Update
    void update(DailyQuestEntity quest);

    @Delete
    void delete(DailyQuestEntity quest);

    @Query("DELETE FROM daily_quests WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM daily_quests WHERE id = :id")
    DailyQuestEntity getById(String id);

    @Query("SELECT * FROM daily_quests WHERE id = :id")
    LiveData<DailyQuestEntity> observeById(String id);

    @Query("SELECT * FROM daily_quests")
    List<DailyQuestEntity> getAll();

    @Query("SELECT * FROM daily_quests")
    LiveData<List<DailyQuestEntity>> observeAll();

    @Query("SELECT * FROM daily_quests WHERE is_active = 1")
    List<DailyQuestEntity> getActive();

    @Query("SELECT * FROM daily_quests WHERE is_active = 1")
    LiveData<List<DailyQuestEntity>> observeActive();

    @Query("SELECT * FROM daily_quests WHERE expiration_date > :currentTime AND is_active = 1")
    List<DailyQuestEntity> getNonExpired(long currentTime);

    @Query("SELECT * FROM daily_quests WHERE required_action = :action")
    List<DailyQuestEntity> getByRequiredAction(String action);

    @Query("SELECT * FROM daily_quests WHERE required_category = :category OR required_category IS NULL")
    List<DailyQuestEntity> getByCategory(String category);

    @Query("SELECT * FROM daily_quests WHERE difficulty = :difficulty")
    List<DailyQuestEntity> getByDifficulty(int difficulty);

    @Query("DELETE FROM daily_quests WHERE expiration_date < :currentTime")
    void deleteExpired(long currentTime);

    @Query("UPDATE daily_quests SET is_active = 0 WHERE expiration_date < :currentTime")
    void deactivateExpired(long currentTime);

    @Query("SELECT COUNT(*) FROM daily_quests WHERE is_active = 1")
    int getActiveCount();

    @Query("SELECT COUNT(*) FROM daily_quests")
    int getCount();
}
