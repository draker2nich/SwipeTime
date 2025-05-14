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
    long insert(AnimeEntity anime);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AnimeEntity> animes);

    @Update
    void update(AnimeEntity anime);

    @Delete
    void delete(AnimeEntity anime);

    @Query("DELETE FROM anime WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM anime WHERE id = :id")
    AnimeEntity getById(String id);

    @Query("SELECT * FROM anime WHERE id = :id")
    LiveData<AnimeEntity> observeById(String id);

    @Query("SELECT * FROM anime")
    List<AnimeEntity> getAll();

    @Query("SELECT * FROM anime")
    LiveData<List<AnimeEntity>> observeAll();

    @Query("SELECT * FROM anime WHERE liked = 1")
    List<AnimeEntity> getLiked();

    @Query("SELECT * FROM anime WHERE liked = 1")
    LiveData<List<AnimeEntity>> observeLiked();

    @Query("SELECT * FROM anime WHERE genres LIKE '%' || :genre || '%'")
    List<AnimeEntity> getByGenre(String genre);

    @Query("SELECT * FROM anime WHERE genres LIKE '%' || :genre || '%'")
    LiveData<List<AnimeEntity>> observeByGenre(String genre);

    @Query("SELECT * FROM anime WHERE studio LIKE '%' || :studio || '%'")
    List<AnimeEntity> getByStudio(String studio);

    @Query("SELECT * FROM anime WHERE studio LIKE '%' || :studio || '%'")
    LiveData<List<AnimeEntity>> observeByStudio(String studio);

    @Query("SELECT * FROM anime WHERE release_year = :year")
    List<AnimeEntity> getByReleaseYear(int year);

    @Query("SELECT * FROM anime WHERE status = :status")
    List<AnimeEntity> getByStatus(String status);

    @Query("SELECT * FROM anime WHERE status = :status")
    LiveData<List<AnimeEntity>> observeByStatus(String status);

    @Query("SELECT * FROM anime WHERE type = :type")
    List<AnimeEntity> getByType(String type);

    @Query("SELECT * FROM anime WHERE type = :type")
    LiveData<List<AnimeEntity>> observeByType(String type);

    @Query("SELECT * FROM anime WHERE episodes <= :episodeCount")
    List<AnimeEntity> getByMaxEpisodes(int episodeCount);

    @Query("SELECT * FROM anime WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<AnimeEntity> search(String query);

    @Query("DELETE FROM anime")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM anime")
    int getCount();

    @Query("SELECT COUNT(*) FROM anime WHERE liked = 1")
    int getLikedCount();

    @Query("SELECT COUNT(*) FROM anime WHERE genres LIKE '%' || :genre || '%'")
    int getCountByGenre(String genre);
}
