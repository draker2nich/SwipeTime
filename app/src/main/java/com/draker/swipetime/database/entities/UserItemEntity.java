package com.draker.swipetime.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

/**
 * Сущность для хранения коллекционных предметов пользователя
 */
@Entity(tableName = "user_items",
        primaryKeys = {"user_id", "item_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = CollectibleItemEntity.class,
                        parentColumns = "id",
                        childColumns = "item_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("user_id"),
                @Index("item_id")
        })
public class UserItemEntity {

    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @NonNull
    @ColumnInfo(name = "item_id")
    private String itemId;

    @ColumnInfo(name = "obtained_date")
    private long obtainedDate;

    @ColumnInfo(name = "source")
    private String source; // quest, achievement, event, purchase

    @ColumnInfo(name = "is_equipped")
    private boolean isEquipped;

    @ColumnInfo(name = "equipped_slot")
    private String equippedSlot; // profile_frame, avatar_badge, etc.

    public UserItemEntity() {
    }

    @Ignore
    public UserItemEntity(@NonNull String userId, @NonNull String itemId, long obtainedDate, String source) {
        this.userId = userId;
        this.itemId = itemId;
        this.obtainedDate = obtainedDate;
        this.source = source;
        this.isEquipped = false;
        this.equippedSlot = null;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getItemId() {
        return itemId;
    }

    public void setItemId(@NonNull String itemId) {
        this.itemId = itemId;
    }

    public long getObtainedDate() {
        return obtainedDate;
    }

    public void setObtainedDate(long obtainedDate) {
        this.obtainedDate = obtainedDate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isEquipped() {
        return isEquipped;
    }

    public void setEquipped(boolean equipped) {
        isEquipped = equipped;
    }

    public String getEquippedSlot() {
        return equippedSlot;
    }

    public void setEquippedSlot(String equippedSlot) {
        this.equippedSlot = equippedSlot;
    }
}
