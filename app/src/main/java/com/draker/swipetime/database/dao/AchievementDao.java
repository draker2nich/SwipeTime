package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.AchievementEntity;

import java.util.List;

/**
 * DAO для операций с достижениями
 */
@Dao
public interface AchievementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AchievementEntity achievement);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AchievementEntity> achievements);

    @Update
    void update(AchievementEntity achievement);

    @Delete
    void delete(AchievementEntity achievement);

    @Query("DELETE FROM achievements WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM achievements WHERE id = :id")
    AchievementEntity getById(String id);

    @Query("SELECT * FROM achievements WHERE id = :id")
    LiveData<AchievementEntity> observeById(String id);

    @Query("SELECT * FROM achievements")
    List<AchievementEntity> getAll();

    @Query("SELECT * FROM achievements")
    LiveData<List<AchievementEntity>> observeAll();

    @Query("SELECT * FROM achievements WHERE category = :category")
    List<AchievementEntity> getByCategory(String category);

    @Query("SELECT * FROM achievements WHERE category = :category")
    LiveData<List<AchievementEntity>> observeByCategory(String category);

    @Query("SELECT * FROM achievements WHERE required_action = :action")
    List<AchievementEntity> getByRequiredAction(String action);

    @Query("SELECT * FROM achievements WHERE required_count <= :count AND required_action = :action")
    List<AchievementEntity> getAchievableByActionAndCount(String action, int count);

    @Query("SELECT * FROM achievements ORDER BY experience_reward DESC")
    List<AchievementEntity> getAllOrderedByExperienceReward();

    @Query("SELECT * FROM achievements WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<AchievementEntity> search(String query);

    @Query("DELETE FROM achievements")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM achievements")
    int getCount();

    @Query("SELECT COUNT(*) FROM achievements WHERE category = :category")
    int getCountByCategory(String category);
}
