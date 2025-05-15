package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.AnimeEntity;

import java.util.List;

/**
 * DAO для операций с аниме
 */
@Dao
public interface AnimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AnimeEntity anime);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AnimeEntity> animes);

    @Update
    void update(AnimeEntity anime);

    @Delete
    void delete(AnimeEntity anime);

    @Query("SELECT * FROM anime WHERE id = :id")
    AnimeEntity getById(String id);

    @Query("SELECT * FROM anime")
    List<AnimeEntity> getAll();

    @Query("SELECT * FROM anime")
    LiveData<List<AnimeEntity>> observeAll();

    @Query("SELECT * FROM anime WHERE liked = 1")
    List<AnimeEntity> getLiked();

    @Query("SELECT * FROM anime WHERE liked = 1")
    LiveData<List<AnimeEntity>> observeLiked();

    @Query("SELECT * FROM anime WHERE watched = 1")
    List<AnimeEntity> getWatched();

    @Query("SELECT * FROM anime WHERE watched = 1")
    LiveData<List<AnimeEntity>> observeWatched();

    @Query("UPDATE anime SET liked = :liked WHERE id = :id")
    void updateLikedStatus(String id, boolean liked);

    @Query("UPDATE anime SET watched = :watched WHERE id = :id")
    void updateWatchedStatus(String id, boolean watched);

    @Query("SELECT * FROM anime WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<AnimeEntity> search(String query);

    @Query("SELECT COUNT(*) FROM anime")
    int getCount();

    @Query("SELECT COUNT(*) FROM anime WHERE liked = 1")
    int getLikedCount();

    @Query("SELECT COUNT(*) FROM anime WHERE watched = 1")
    int getWatchedCount();
    
    @Query("DELETE FROM anime")
    void deleteAll();
}