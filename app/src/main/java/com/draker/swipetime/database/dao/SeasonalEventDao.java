package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.SeasonalEventEntity;

import java.util.List;

/**
 * DAO для операций с сезонными событиями
 */
@Dao
public interface SeasonalEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SeasonalEventEntity event);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SeasonalEventEntity> events);

    @Update
    void update(SeasonalEventEntity event);

    @Delete
    void delete(SeasonalEventEntity event);

    @Query("DELETE FROM seasonal_events WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM seasonal_events WHERE id = :id")
    SeasonalEventEntity getById(String id);

    @Query("SELECT * FROM seasonal_events WHERE id = :id")
    LiveData<SeasonalEventEntity> observeById(String id);

    @Query("SELECT * FROM seasonal_events")
    List<SeasonalEventEntity> getAll();

    @Query("SELECT * FROM seasonal_events")
    LiveData<List<SeasonalEventEntity>> observeAll();

    @Query("SELECT * FROM seasonal_events WHERE is_active = 1")
    List<SeasonalEventEntity> getActive();

    @Query("SELECT * FROM seasonal_events WHERE is_active = 1")
    LiveData<List<SeasonalEventEntity>> observeActive();

    @Query("SELECT * FROM seasonal_events WHERE start_date <= :currentTime AND end_date >= :currentTime AND is_active = 1")
    List<SeasonalEventEntity> getCurrentlyActive(long currentTime);

    @Query("SELECT * FROM seasonal_events WHERE start_date <= :currentTime AND end_date >= :currentTime AND is_active = 1")
    LiveData<List<SeasonalEventEntity>> observeCurrentlyActive(long currentTime);

    @Query("SELECT * FROM seasonal_events WHERE end_date < :currentTime")
    List<SeasonalEventEntity> getPastEvents(long currentTime);

    @Query("SELECT * FROM seasonal_events WHERE start_date > :currentTime")
    List<SeasonalEventEntity> getUpcomingEvents(long currentTime);

    @Query("SELECT * FROM seasonal_events WHERE event_type = :eventType")
    List<SeasonalEventEntity> getByEventType(String eventType);

    @Query("SELECT COUNT(*) FROM seasonal_events WHERE is_active = 1")
    int getActiveCount();

    @Query("UPDATE seasonal_events SET is_active = 0 WHERE end_date < :currentTime")
    void deactivateEndedEvents(long currentTime);

    @Query("UPDATE seasonal_events SET is_active = 1 WHERE start_date <= :currentTime AND end_date >= :currentTime")
    void activateCurrentEvents(long currentTime);
}
