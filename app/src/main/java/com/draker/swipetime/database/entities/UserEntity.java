package com.draker.swipetime.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Сущность пользователя для хранения данных профиля и прогресса
 */
@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "username")
    private String username;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "avatar_url")
    private String avatarUrl;

    @ColumnInfo(name = "experience")
    private int experience;

    @ColumnInfo(name = "level")
    private int level;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    @ColumnInfo(name = "preferred_categories")
    private String preferredCategories;

    @ColumnInfo(name = "last_login")
    private long lastLogin;

    public UserEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.lastLogin = System.currentTimeMillis();
        this.level = 1;
        this.experience = 0;
    }

    @Ignore
    public UserEntity(@NonNull String id, String username, String email, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.experience = 0;
        this.level = 1;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.lastLogin = System.currentTimeMillis();
    }
    
    @Ignore
    public UserEntity(@NonNull String id, String username, String email, String avatarUrl, int experience, int level) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.experience = experience;
        this.level = level;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.lastLogin = System.currentTimeMillis();
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPreferredCategories() {
        return preferredCategories;
    }

    public void setPreferredCategories(String preferredCategories) {
        this.preferredCategories = preferredCategories;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Добавить опыт пользователю
     * @param amount количество опыта
     * @return true, если уровень был повышен
     */
    public boolean addExperience(int amount) {
        this.experience += amount;
        this.updatedAt = System.currentTimeMillis();
        
        // Проверка повышения уровня (формула: требуемый опыт = 100 * уровень^2)
        int experienceRequired = 100 * (level * level);
        if (this.experience >= experienceRequired) {
            this.level++;
            return true;
        }
        return false;
    }
}
