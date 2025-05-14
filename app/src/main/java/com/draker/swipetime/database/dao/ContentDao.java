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
 * DAO для базовых операций с контентом
 */
@Dao
public interface ContentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ContentEntity content);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ContentEntity> contents);

    @Update
    void update(ContentEntity content);

    @Delete
    void delete(ContentEntity content);

    @Query("DELETE FROM content WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM content WHERE id = :id")
    ContentEntity getById(String id);

    @Query("SELECT * FROM content WHERE id = :id")
    LiveData<ContentEntity> observeById(String id);

    @Query("SELECT * FROM content")
    List<ContentEntity> getAll();

    @Query("SELECT * FROM content")
    LiveData<List<ContentEntity>> observeAll();

    @Query("SELECT * FROM content WHERE category = :category")
    List<ContentEntity> getByCategory(String category);

    @Query("SELECT * FROM content WHERE category = :category")
    LiveData<List<ContentEntity>> observeByCategory(String category);

    @Query("SELECT * FROM content WHERE liked = 1")
    List<ContentEntity> getLiked();

    @Query("SELECT * FROM content WHERE liked = 1")
    LiveData<List<ContentEntity>> observeLiked();

    @Query("SELECT * FROM content WHERE content_type = :contentType")
    List<ContentEntity> getByContentType(String contentType);

    @Query("SELECT * FROM content WHERE content_type = :contentType")
    LiveData<List<ContentEntity>> observeByContentType(String contentType);

    @Query("SELECT * FROM content WHERE liked = 1 AND content_type = :contentType")
    List<ContentEntity> getLikedByContentType(String contentType);

    @Query("SELECT * FROM content WHERE liked = 1 AND content_type = :contentType")
    LiveData<List<ContentEntity>> observeLikedByContentType(String contentType);

    @Query("UPDATE content SET liked = :liked WHERE id = :id")
    void updateLikedStatus(String id, boolean liked);

    @Query("UPDATE content SET viewed = :viewed WHERE id = :id")
    void updateViewedStatus(String id, boolean viewed);

    @Query("UPDATE content SET rating = :rating WHERE id = :id")
    void updateRating(String id, float rating);

    @Query("SELECT * FROM content WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<ContentEntity> search(String query);

    @Query("DELETE FROM content")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM content")
    int getCount();

    @Query("SELECT COUNT(*) FROM content WHERE category = :category")
    int getCountByCategory(String category);

    @Query("SELECT COUNT(*) FROM content WHERE liked = 1")
    int getLikedCount();
}
