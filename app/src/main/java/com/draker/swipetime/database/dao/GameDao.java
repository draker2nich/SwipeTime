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
    long insert(GameEntity game);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GameEntity> games);

    @Update
    void update(GameEntity game);

    @Delete
    void delete(GameEntity game);

    @Query("DELETE FROM games WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM games WHERE id = :id")
    GameEntity getById(String id);

    @Query("SELECT * FROM games WHERE id = :id")
    LiveData<GameEntity> observeById(String id);

    @Query("SELECT * FROM games")
    List<GameEntity> getAll();

    @Query("SELECT * FROM games")
    LiveData<List<GameEntity>> observeAll();

    @Query("SELECT * FROM games WHERE liked = 1")
    List<GameEntity> getLiked();

    @Query("SELECT * FROM games WHERE liked = 1")
    LiveData<List<GameEntity>> observeLiked();

    @Query("SELECT * FROM games WHERE genres LIKE '%' || :genre || '%'")
    List<GameEntity> getByGenre(String genre);

    @Query("SELECT * FROM games WHERE genres LIKE '%' || :genre || '%'")
    LiveData<List<GameEntity>> observeByGenre(String genre);

    @Query("SELECT * FROM games WHERE developer LIKE '%' || :developer || '%'")
    List<GameEntity> getByDeveloper(String developer);

    @Query("SELECT * FROM games WHERE publisher LIKE '%' || :publisher || '%'")
    List<GameEntity> getByPublisher(String publisher);

    @Query("SELECT * FROM games WHERE release_year = :year")
    List<GameEntity> getByReleaseYear(int year);

    @Query("SELECT * FROM games WHERE platforms LIKE '%' || :platform || '%'")
    List<GameEntity> getByPlatform(String platform);

    @Query("SELECT * FROM games WHERE platforms LIKE '%' || :platform || '%'")
    LiveData<List<GameEntity>> observeByPlatform(String platform);

    @Query("SELECT * FROM games WHERE esrb_rating = :rating")
    List<GameEntity> getByEsrbRating(String rating);

    @Query("SELECT * FROM games WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<GameEntity> search(String query);

    @Query("DELETE FROM games")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM games")
    int getCount();

    @Query("SELECT COUNT(*) FROM games WHERE liked = 1")
    int getLikedCount();

    @Query("SELECT COUNT(*) FROM games WHERE genres LIKE '%' || :genre || '%'")
    int getCountByGenre(String genre);
}
