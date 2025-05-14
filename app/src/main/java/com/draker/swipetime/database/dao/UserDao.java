package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.UserEntity;

import java.util.List;

/**
 * DAO для операций с пользователями
 */
@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Delete
    void delete(UserEntity user);

    @Query("DELETE FROM users WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM users WHERE id = :id")
    UserEntity getById(String id);

    @Query("SELECT * FROM users WHERE id = :id")
    LiveData<UserEntity> observeById(String id);

    @Query("SELECT * FROM users")
    List<UserEntity> getAll();

    @Query("SELECT * FROM users")
    LiveData<List<UserEntity>> observeAll();

    @Query("SELECT * FROM users WHERE level >= :minLevel")
    List<UserEntity> getByMinLevel(int minLevel);

    @Query("SELECT * FROM users ORDER BY level DESC LIMIT :limit")
    List<UserEntity> getTopUsersByLevel(int limit);

    @Query("SELECT * FROM users ORDER BY experience DESC LIMIT :limit")
    List<UserEntity> getTopUsersByExperience(int limit);

    @Query("UPDATE users SET experience = :experience WHERE id = :id")
    void updateExperience(String id, int experience);

    @Query("UPDATE users SET level = :level WHERE id = :id")
    void updateLevel(String id, int level);

    @Query("UPDATE users SET experience = experience + :amount WHERE id = :id")
    void addExperience(String id, int amount);

    @Query("UPDATE users SET last_login = :timestamp WHERE id = :id")
    void updateLastLogin(String id, long timestamp);

    @Query("UPDATE users SET preferred_categories = :categories WHERE id = :id")
    void updatePreferredCategories(String id, String categories);

    @Query("SELECT * FROM users WHERE email = :email")
    UserEntity getByEmail(String email);

    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%'")
    List<UserEntity> searchByUsername(String query);

    @Query("DELETE FROM users")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM users")
    int getCount();
}
