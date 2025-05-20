package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.UserItemEntity;

import java.util.List;

/**
 * DAO для операций с коллекционными предметами пользователя
 */
@Dao
public interface UserItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserItemEntity userItem);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UserItemEntity> userItems);

    @Update
    void update(UserItemEntity userItem);

    @Delete
    void delete(UserItemEntity userItem);

    @Query("DELETE FROM user_items WHERE user_id = :userId AND item_id = :itemId")
    void deleteByIds(String userId, String itemId);

    @Query("SELECT * FROM user_items WHERE user_id = :userId AND item_id = :itemId")
    UserItemEntity getByIds(String userId, String itemId);

    @Query("SELECT * FROM user_items WHERE user_id = :userId")
    List<UserItemEntity> getByUserId(String userId);

    @Query("SELECT * FROM user_items WHERE user_id = :userId")
    LiveData<List<UserItemEntity>> observeByUserId(String userId);

    @Query("SELECT * FROM user_items WHERE item_id = :itemId")
    List<UserItemEntity> getByItemId(String itemId);

    @Query("SELECT * FROM user_items WHERE user_id = :userId AND is_equipped = 1")
    List<UserItemEntity> getEquippedByUserId(String userId);

    @Query("SELECT * FROM user_items WHERE user_id = :userId AND equipped_slot = :slot")
    List<UserItemEntity> getEquippedInSlot(String userId, String slot);

    @Query("SELECT * FROM user_items WHERE user_id = :userId AND source = :source")
    List<UserItemEntity> getByUserIdAndSource(String userId, String source);

    @Query("SELECT COUNT(*) FROM user_items WHERE user_id = :userId")
    int getCountByUserId(String userId);

    @Query("SELECT COUNT(*) FROM user_items ui JOIN collectible_items ci ON ui.item_id = ci.id " +
           "WHERE ui.user_id = :userId AND ci.rarity = :rarity")
    int getCountByUserIdAndRarity(String userId, int rarity);

    @Query("SELECT COUNT(*) FROM user_items ui JOIN collectible_items ci ON ui.item_id = ci.id " +
           "WHERE ui.user_id = :userId AND ci.associated_event_id = :eventId")
    int getCountByUserIdAndEventId(String userId, String eventId);

    @Query("UPDATE user_items SET is_equipped = 0 WHERE user_id = :userId AND equipped_slot = :slot")
    void unequipAllInSlot(String userId, String slot);

    @Query("UPDATE user_items SET is_equipped = 1, equipped_slot = :slot " +
           "WHERE user_id = :userId AND item_id = :itemId")
    void equipItem(String userId, String itemId, String slot);

    @Query("UPDATE user_items SET is_equipped = 0, equipped_slot = NULL " +
           "WHERE user_id = :userId AND item_id = :itemId")
    void unequipItem(String userId, String itemId);
}
