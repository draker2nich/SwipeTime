package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.UserPreferencesEntity;

@Dao
public interface UserPreferencesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserPreferencesEntity preferences);

    @Update
    void update(UserPreferencesEntity preferences);

    @Delete
    void delete(UserPreferencesEntity preferences);

    @Query("SELECT * FROM user_preferences WHERE user_id = :userId LIMIT 1")
    UserPreferencesEntity getByUserId(String userId);

    @Query("SELECT * FROM user_preferences WHERE user_id = :userId LIMIT 1")
    LiveData<UserPreferencesEntity> observeByUserId(String userId);

    @Query("UPDATE user_preferences SET preferred_genres = :genres WHERE user_id = :userId")
    void updateGenres(String userId, String genres);

    @Query("UPDATE user_preferences SET preferred_countries = :countries WHERE user_id = :userId")
    void updateCountries(String userId, String countries);

    @Query("UPDATE user_preferences SET preferred_languages = :languages WHERE user_id = :userId")
    void updateLanguages(String userId, String languages);

    @Query("UPDATE user_preferences SET interests_tags = :tags WHERE user_id = :userId")
    void updateInterestsTags(String userId, String tags);

    @Query("UPDATE user_preferences SET min_duration = :minDuration, max_duration = :maxDuration WHERE user_id = :userId")
    void updateDurationRange(String userId, int minDuration, int maxDuration);

    @Query("UPDATE user_preferences SET min_year = :minYear, max_year = :maxYear WHERE user_id = :userId")
    void updateYearRange(String userId, int minYear, int maxYear);

    @Query("UPDATE user_preferences SET adult_content_enabled = :enabled WHERE user_id = :userId")
    void updateAdultContentEnabled(String userId, boolean enabled);
}