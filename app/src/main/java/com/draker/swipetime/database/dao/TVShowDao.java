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
    long insert(TVShowEntity tvShow);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TVShowEntity> tvShows);

    @Update
    void update(TVShowEntity tvShow);

    @Delete
    void delete(TVShowEntity tvShow);

    @Query("DELETE FROM tv_shows WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM tv_shows WHERE id = :id")
    TVShowEntity getById(String id);

    @Query("SELECT * FROM tv_shows WHERE id = :id")
    LiveData<TVShowEntity> observeById(String id);

    @Query("SELECT * FROM tv_shows")
    List<TVShowEntity> getAll();

    @Query("SELECT * FROM tv_shows")
    LiveData<List<TVShowEntity>> observeAll();

    @Query("SELECT * FROM tv_shows WHERE liked = 1")
    List<TVShowEntity> getLiked();

    @Query("SELECT * FROM tv_shows WHERE liked = 1")
    LiveData<List<TVShowEntity>> observeLiked();

    @Query("SELECT * FROM tv_shows WHERE genres LIKE '%' || :genre || '%'")
    List<TVShowEntity> getByGenre(String genre);

    @Query("SELECT * FROM tv_shows WHERE genres LIKE '%' || :genre || '%'")
    LiveData<List<TVShowEntity>> observeByGenre(String genre);

    @Query("SELECT * FROM tv_shows WHERE creator LIKE '%' || :creator || '%'")
    List<TVShowEntity> getByCreator(String creator);

    @Query("SELECT * FROM tv_shows WHERE start_year = :year")
    List<TVShowEntity> getByStartYear(int year);

    @Query("SELECT * FROM tv_shows WHERE status = :status")
    List<TVShowEntity> getByStatus(String status);

    @Query("SELECT * FROM tv_shows WHERE status = :status")
    LiveData<List<TVShowEntity>> observeByStatus(String status);

    @Query("SELECT * FROM tv_shows WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<TVShowEntity> search(String query);

    @Query("DELETE FROM tv_shows")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM tv_shows")
    int getCount();

    @Query("SELECT COUNT(*) FROM tv_shows WHERE liked = 1")
    int getLikedCount();

    @Query("SELECT * FROM tv_shows WHERE seasons <= :seasonCount")
    List<TVShowEntity> getByMaxSeasons(int seasonCount);

    @Query("SELECT COUNT(*) FROM tv_shows WHERE genres LIKE '%' || :genre || '%'")
    int getCountByGenre(String genre);
}
