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
    long insert(MovieEntity movie);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MovieEntity> movies);

    @Update
    void update(MovieEntity movie);

    @Delete
    void delete(MovieEntity movie);

    @Query("DELETE FROM movies WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM movies WHERE id = :id")
    MovieEntity getById(String id);

    @Query("SELECT * FROM movies WHERE id = :id")
    LiveData<MovieEntity> observeById(String id);

    @Query("SELECT * FROM movies")
    List<MovieEntity> getAll();

    @Query("SELECT * FROM movies")
    LiveData<List<MovieEntity>> observeAll();

    @Query("SELECT * FROM movies WHERE liked = 1")
    List<MovieEntity> getLiked();

    @Query("SELECT * FROM movies WHERE liked = 1")
    LiveData<List<MovieEntity>> observeLiked();

    @Query("SELECT * FROM movies WHERE genres LIKE '%' || :genre || '%'")
    List<MovieEntity> getByGenre(String genre);

    @Query("SELECT * FROM movies WHERE genres LIKE '%' || :genre || '%'")
    LiveData<List<MovieEntity>> observeByGenre(String genre);

    @Query("SELECT * FROM movies WHERE director LIKE '%' || :director || '%'")
    List<MovieEntity> getByDirector(String director);

    @Query("SELECT * FROM movies WHERE release_year = :year")
    List<MovieEntity> getByReleaseYear(int year);

    @Query("SELECT * FROM movies WHERE release_year BETWEEN :startYear AND :endYear")
    List<MovieEntity> getByReleaseYearRange(int startYear, int endYear);

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<MovieEntity> search(String query);

    @Query("DELETE FROM movies")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM movies")
    int getCount();

    @Query("SELECT COUNT(*) FROM movies WHERE liked = 1")
    int getLikedCount();

    @Query("SELECT COUNT(*) FROM movies WHERE genres LIKE '%' || :genre || '%'")
    int getCountByGenre(String genre);
}
