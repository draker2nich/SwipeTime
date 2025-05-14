package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.ReviewEntity;

import java.util.List;

/**
 * DAO для операций с отзывами
 */
@Dao
public interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ReviewEntity review);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ReviewEntity> reviews);

    @Update
    void update(ReviewEntity review);

    @Delete
    void delete(ReviewEntity review);

    @Query("DELETE FROM reviews WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM reviews WHERE id = :id")
    ReviewEntity getById(long id);

    @Query("SELECT * FROM reviews WHERE id = :id")
    LiveData<ReviewEntity> observeById(long id);

    @Query("SELECT * FROM reviews")
    List<ReviewEntity> getAll();

    @Query("SELECT * FROM reviews")
    LiveData<List<ReviewEntity>> observeAll();

    @Query("SELECT * FROM reviews WHERE user_id = :userId")
    List<ReviewEntity> getByUserId(String userId);

    @Query("SELECT * FROM reviews WHERE user_id = :userId")
    LiveData<List<ReviewEntity>> observeByUserId(String userId);

    @Query("SELECT * FROM reviews WHERE content_id = :contentId")
    List<ReviewEntity> getByContentId(String contentId);

    @Query("SELECT * FROM reviews WHERE content_id = :contentId")
    LiveData<List<ReviewEntity>> observeByContentId(String contentId);

    @Query("SELECT * FROM reviews WHERE content_id = :contentId AND user_id = :userId")
    ReviewEntity getByContentAndUserId(String contentId, String userId);

    @Query("SELECT * FROM reviews WHERE rating >= :minRating")
    List<ReviewEntity> getByMinRating(float minRating);

    @Query("SELECT * FROM reviews WHERE content_type = :contentType")
    List<ReviewEntity> getByContentType(String contentType);

    @Query("SELECT * FROM reviews WHERE content_type = :contentType")
    LiveData<List<ReviewEntity>> observeByContentType(String contentType);

    @Query("SELECT AVG(rating) FROM reviews WHERE content_id = :contentId")
    float getAverageRatingForContent(String contentId);

    @Query("SELECT COUNT(*) FROM reviews WHERE content_id = :contentId")
    int getReviewCountForContent(String contentId);

    @Query("SELECT * FROM reviews WHERE text LIKE '%' || :query || '%'")
    List<ReviewEntity> searchInText(String query);

    @Query("SELECT * FROM reviews ORDER BY created_at DESC")
    List<ReviewEntity> getAllOrderedByCreationDateDesc();

    @Query("SELECT * FROM reviews ORDER BY rating DESC")
    List<ReviewEntity> getAllOrderedByRatingDesc();

    @Query("SELECT * FROM reviews WHERE user_id = :userId ORDER BY created_at DESC")
    List<ReviewEntity> getByUserIdOrderedByCreationDateDesc(String userId);

    @Query("DELETE FROM reviews WHERE user_id = :userId")
    void deleteByUserId(String userId);

    @Query("DELETE FROM reviews WHERE content_id = :contentId")
    void deleteByContentId(String contentId);

    @Query("DELETE FROM reviews")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM reviews")
    int getCount();

    @Query("SELECT COUNT(*) FROM reviews WHERE user_id = :userId")
    int getCountByUserId(String userId);
}
