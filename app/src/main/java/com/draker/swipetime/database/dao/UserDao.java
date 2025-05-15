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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UserEntity> users);

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

    @Query("SELECT * FROM users WHERE email = :email")
    UserEntity getByEmail(String email);

    @Query("SELECT * FROM users WHERE username = :username")
    UserEntity getByUsername(String username);

    @Query("SELECT * FROM users")
    List<UserEntity> getAll();

    @Query("SELECT * FROM users")
    LiveData<List<UserEntity>> observeAll();

    @Query("SELECT * FROM users WHERE level >= :level")
    List<UserEntity> getByMinLevel(int level);

    @Query("SELECT * FROM users WHERE experience >= :experience")
    List<UserEntity> getByMinExperience(int experience);

    @Query("SELECT * FROM users ORDER BY level DESC")
    List<UserEntity> getAllOrderedByLevelDesc();

    @Query("SELECT * FROM users ORDER BY experience DESC")
    List<UserEntity> getAllOrderedByExperienceDesc();

    @Query("SELECT COUNT(*) FROM users")
    int getCount();

    @Query("UPDATE users SET experience = :experience WHERE id = :userId")
    void updateExperience(String userId, int experience);

    @Query("UPDATE users SET level = :level WHERE id = :userId")
    void updateLevel(String userId, int level);

    @Query("UPDATE users SET preferred_categories = :categories WHERE id = :userId")
    void updatePreferredCategories(String userId, String categories);
    
    @Query("DELETE FROM users")
    void deleteAll();
}
