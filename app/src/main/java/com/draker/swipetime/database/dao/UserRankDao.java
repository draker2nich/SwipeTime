package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.UserRankEntity;

import java.util.List;

/**
 * DAO для операций с рангами пользователей
 */
@Dao
public interface UserRankDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserRankEntity rank);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UserRankEntity> ranks);

    @Update
    void update(UserRankEntity rank);

    @Delete
    void delete(UserRankEntity rank);

    @Query("DELETE FROM user_ranks WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM user_ranks WHERE id = :id")
    UserRankEntity getById(String id);

    @Query("SELECT * FROM user_ranks WHERE id = :id")
    LiveData<UserRankEntity> observeById(String id);

    @Query("SELECT * FROM user_ranks")
    List<UserRankEntity> getAll();

    @Query("SELECT * FROM user_ranks")
    LiveData<List<UserRankEntity>> observeAll();

    @Query("SELECT * FROM user_ranks ORDER BY order_index")
    List<UserRankEntity> getAllOrdered();

    @Query("SELECT * FROM user_ranks ORDER BY order_index")
    LiveData<List<UserRankEntity>> observeAllOrdered();

    @Query("SELECT * FROM user_ranks WHERE category = :category ORDER BY order_index")
    List<UserRankEntity> getByCategory(String category);

    @Query("SELECT * FROM user_ranks WHERE category = :category ORDER BY order_index")
    LiveData<List<UserRankEntity>> observeByCategory(String category);

    @Query("SELECT * FROM user_ranks WHERE required_level <= :level ORDER BY required_level DESC LIMIT 1")
    UserRankEntity getRankForLevel(int level);

    @Query("SELECT * FROM user_ranks WHERE required_level > :currentLevel ORDER BY required_level LIMIT 1")
    UserRankEntity getNextRankForLevel(int currentLevel);

    @Query("SELECT COUNT(*) FROM user_ranks")
    int getCount();

    @Query("SELECT COUNT(*) FROM user_ranks WHERE category = :category")
    int getCountByCategory(String category);
    
    @Query("SELECT * FROM user_ranks ORDER BY order_index")
    List<UserRankEntity> getAllOrderedByIndex();
}
