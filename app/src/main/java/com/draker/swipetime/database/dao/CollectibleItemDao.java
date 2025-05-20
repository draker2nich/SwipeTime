package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.CollectibleItemEntity;

import java.util.List;

/**
 * DAO для операций с коллекционными предметами
 */
@Dao
public interface CollectibleItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CollectibleItemEntity item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CollectibleItemEntity> items);

    @Update
    void update(CollectibleItemEntity item);

    @Delete
    void delete(CollectibleItemEntity item);

    @Query("DELETE FROM collectible_items WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM collectible_items WHERE id = :id")
    CollectibleItemEntity getById(String id);

    @Query("SELECT * FROM collectible_items WHERE id = :id")
    LiveData<CollectibleItemEntity> observeById(String id);

    @Query("SELECT * FROM collectible_items")
    List<CollectibleItemEntity> getAll();

    @Query("SELECT * FROM collectible_items")
    LiveData<List<CollectibleItemEntity>> observeAll();

    @Query("SELECT * FROM collectible_items WHERE rarity = :rarity")
    List<CollectibleItemEntity> getByRarity(int rarity);

    @Query("SELECT * FROM collectible_items WHERE associated_category = :category")
    List<CollectibleItemEntity> getByCategory(String category);

    @Query("SELECT * FROM collectible_items WHERE associated_event_id = :eventId")
    List<CollectibleItemEntity> getByEventId(String eventId);

    @Query("SELECT * FROM collectible_items WHERE is_limited = 1")
    List<CollectibleItemEntity> getLimited();

    @Query("SELECT * FROM collectible_items WHERE is_limited = 0")
    List<CollectibleItemEntity> getPermanent();

    @Query("SELECT * FROM collectible_items WHERE availability_end_date >= :currentTime OR availability_end_date = 0")
    List<CollectibleItemEntity> getCurrentlyAvailable(long currentTime);

    @Query("SELECT * FROM collectible_items WHERE obtained_from = :source")
    List<CollectibleItemEntity> getBySource(String source);

    @Query("SELECT * FROM collectible_items WHERE usage_effect = :effect")
    List<CollectibleItemEntity> getByEffect(String effect);

    @Query("SELECT COUNT(*) FROM collectible_items")
    int getCount();

    @Query("SELECT COUNT(*) FROM collectible_items WHERE rarity = :rarity")
    int getCountByRarity(int rarity);

    @Query("SELECT COUNT(*) FROM collectible_items WHERE associated_event_id = :eventId")
    int getCountByEventId(String eventId);
}
