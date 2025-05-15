package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.ContentEntity;

import java.util.List;

/**
 * DAO для операций с общим контентом
 */
@Dao
public interface ContentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ContentEntity content);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ContentEntity> contents);

    @Update
    void update(ContentEntity content);

    @Delete
    void delete(ContentEntity content);

    @Query("SELECT * FROM content WHERE id = :id")
    ContentEntity getById(String id);

    @Query("SELECT * FROM content")
    List<ContentEntity> getAll();

    @Query("SELECT * FROM content")
    LiveData<List<ContentEntity>> observeAll();

    @Query("SELECT * FROM content WHERE liked = 1")
    List<ContentEntity> getLiked();

    @Query("SELECT * FROM content WHERE liked = 1 AND category = :category")
    List<ContentEntity> getLikedByCategory(String category);

    @Query("SELECT * FROM content WHERE liked = 1")
    LiveData<List<ContentEntity>> observeLiked();

    @Query("SELECT * FROM content WHERE watched = 1")
    List<ContentEntity> getWatched();

    @Query("SELECT * FROM content WHERE watched = 1")
    LiveData<List<ContentEntity>> observeWatched();

    @Query("UPDATE content SET liked = :liked WHERE id = :id")
    void updateLikedStatus(String id, boolean liked);

    @Query("UPDATE content SET watched = :watched WHERE id = :id")
    void updateWatchedStatus(String id, boolean watched);

    @Query("SELECT * FROM content WHERE category = :category")
    List<ContentEntity> getByCategory(String category);

    @Query("SELECT * FROM content WHERE category = :category")
    LiveData<List<ContentEntity>> observeByCategory(String category);

    @Query("SELECT * FROM content WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<ContentEntity> search(String query);

    @Query("SELECT COUNT(*) FROM content")
    int getCount();

    @Query("SELECT COUNT(*) FROM content WHERE liked = 1")
    int getLikedCount();

    @Query("SELECT COUNT(*) FROM content WHERE watched = 1")
    int getWatchedCount();
    
    @Query("DELETE FROM content")
    void deleteAll();
}