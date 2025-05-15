package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.TVShowEntity;

import java.util.List;

/**
 * DAO для операций с сериалами
 */
@Dao
public interface TVShowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TVShowEntity tvShow);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TVShowEntity> tvShows);

    @Update
    void update(TVShowEntity tvShow);

    @Delete
    void delete(TVShowEntity tvShow);

    @Query("SELECT * FROM tv_shows WHERE id = :id")
    TVShowEntity getById(String id);

    @Query("SELECT * FROM tv_shows")
    List<TVShowEntity> getAll();

    @Query("SELECT * FROM tv_shows")
    LiveData<List<TVShowEntity>> observeAll();

    @Query("SELECT * FROM tv_shows WHERE liked = 1")
    List<TVShowEntity> getLiked();

    @Query("SELECT * FROM tv_shows WHERE liked = 1")
    LiveData<List<TVShowEntity>> observeLiked();

    @Query("SELECT * FROM tv_shows WHERE watched = 1")
    List<TVShowEntity> getWatched();

    @Query("SELECT * FROM tv_shows WHERE watched = 1")
    LiveData<List<TVShowEntity>> observeWatched();

    @Query("UPDATE tv_shows SET liked = :liked WHERE id = :id")
    void updateLikedStatus(String id, boolean liked);

    @Query("UPDATE tv_shows SET watched = :watched WHERE id = :id")
    void updateWatchedStatus(String id, boolean watched);

    @Query("SELECT * FROM tv_shows WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<TVShowEntity> search(String query);

    @Query("SELECT COUNT(*) FROM tv_shows")
    int getCount();

    @Query("SELECT COUNT(*) FROM tv_shows WHERE liked = 1")
    int getLikedCount();

    @Query("SELECT COUNT(*) FROM tv_shows WHERE watched = 1")
    int getWatchedCount();
    
    @Query("DELETE FROM tv_shows")
    void deleteAll();
}
