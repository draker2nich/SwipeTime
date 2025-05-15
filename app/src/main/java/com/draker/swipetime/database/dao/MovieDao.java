package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.MovieEntity;

import java.util.List;

/**
 * DAO для операций с фильмами
 */
@Dao
public interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MovieEntity movie);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MovieEntity> movies);

    @Update
    void update(MovieEntity movie);

    @Delete
    void delete(MovieEntity movie);

    @Query("SELECT * FROM movies WHERE id = :id")
    MovieEntity getById(String id);

    @Query("SELECT * FROM movies")
    List<MovieEntity> getAll();

    @Query("SELECT * FROM movies")
    LiveData<List<MovieEntity>> observeAll();

    @Query("SELECT * FROM movies WHERE liked = 1")
    List<MovieEntity> getLiked();

    @Query("SELECT * FROM movies WHERE liked = 1")
    LiveData<List<MovieEntity>> observeLiked();

    @Query("SELECT * FROM movies WHERE watched = 1")
    List<MovieEntity> getWatched();

    @Query("SELECT * FROM movies WHERE watched = 1")
    LiveData<List<MovieEntity>> observeWatched();

    @Query("UPDATE movies SET liked = :liked WHERE id = :id")
    void updateLikedStatus(String id, boolean liked);

    @Query("UPDATE movies SET watched = :watched WHERE id = :id")
    void updateWatchedStatus(String id, boolean watched);

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<MovieEntity> search(String query);

    @Query("SELECT COUNT(*) FROM movies")
    int getCount();

    @Query("SELECT COUNT(*) FROM movies WHERE liked = 1")
    int getLikedCount();

    @Query("SELECT COUNT(*) FROM movies WHERE watched = 1")
    int getWatchedCount();
    
    @Query("DELETE FROM movies")
    void deleteAll();
}
