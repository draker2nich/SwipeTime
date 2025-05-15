package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.GameEntity;

import java.util.List;

/**
 * DAO для операций с играми
 */
@Dao
public interface GameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GameEntity game);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GameEntity> games);

    @Update
    void update(GameEntity game);

    @Delete
    void delete(GameEntity game);
    
    @Query("SELECT * FROM games WHERE id = :id")
    GameEntity getById(String id);

    @Query("SELECT * FROM games")
    List<GameEntity> getAll();

    @Query("SELECT * FROM games")
    LiveData<List<GameEntity>> observeAll();

    @Query("SELECT * FROM games WHERE liked = 1")
    List<GameEntity> getLiked();

    @Query("SELECT * FROM games WHERE liked = 1")
    LiveData<List<GameEntity>> observeLiked();

    @Query("SELECT * FROM games WHERE is_completed = 1")
    List<GameEntity> getCompleted();

    @Query("SELECT * FROM games WHERE is_completed = 1")
    LiveData<List<GameEntity>> observeCompleted();

    @Query("UPDATE games SET liked = :liked WHERE id = :id")
    void updateLikedStatus(String id, boolean liked);

    @Query("UPDATE games SET is_completed = :completed WHERE id = :id")
    void updateCompletedStatus(String id, boolean completed);

    @Query("SELECT * FROM games WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<GameEntity> search(String query);

    @Query("SELECT COUNT(*) FROM games")
    int getCount();

    @Query("SELECT COUNT(*) FROM games WHERE liked = 1")
    int getLikedCount();

    @Query("SELECT COUNT(*) FROM games WHERE is_completed = 1")
    int getCompletedCount();
    
    @Query("DELETE FROM games")
    void deleteAll();
}