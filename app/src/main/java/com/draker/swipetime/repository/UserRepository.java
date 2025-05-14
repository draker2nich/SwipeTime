package com.draker.swipetime.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.UserDao;
import com.draker.swipetime.database.entities.UserEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Репозиторий для работы с пользователями
 */
public class UserRepository {

    private final UserDao userDao;
    private final Executor executor;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        executor = Executors.newSingleThreadExecutor();
    }

    // Методы для работы с пользователями в фоновом потоке

    public void insert(UserEntity user) {
        executor.execute(() -> userDao.insert(user));
    }

    public void update(UserEntity user) {
        executor.execute(() -> userDao.update(user));
    }

    public void delete(UserEntity user) {
        executor.execute(() -> userDao.delete(user));
    }

    public void deleteById(String id) {
        executor.execute(() -> userDao.deleteById(id));
    }

    public void updateExperience(String id, int experience) {
        executor.execute(() -> userDao.updateExperience(id, experience));
    }

    public void updateLevel(String id, int level) {
        executor.execute(() -> userDao.updateLevel(id, level));
    }

    public void addExperience(String id, int amount) {
        executor.execute(() -> userDao.addExperience(id, amount));
    }

    public void updateLastLogin(String id, long timestamp) {
        executor.execute(() -> userDao.updateLastLogin(id, timestamp));
    }

    public void updatePreferredCategories(String id, String categories) {
        executor.execute(() -> userDao.updatePreferredCategories(id, categories));
    }

    public void deleteAll() {
        executor.execute(userDao::deleteAll);
    }

    // Синхронные методы для получения данных

    public UserEntity getById(String id) {
        return userDao.getById(id);
    }

    public List<UserEntity> getAll() {
        return userDao.getAll();
    }

    public List<UserEntity> getByMinLevel(int minLevel) {
        return userDao.getByMinLevel(minLevel);
    }

    public List<UserEntity> getTopUsersByLevel(int limit) {
        return userDao.getTopUsersByLevel(limit);
    }

    public List<UserEntity> getTopUsersByExperience(int limit) {
        return userDao.getTopUsersByExperience(limit);
    }

    public UserEntity getByEmail(String email) {
        return userDao.getByEmail(email);
    }

    public List<UserEntity> searchByUsername(String query) {
        return userDao.searchByUsername(query);
    }

    public int getCount() {
        return userDao.getCount();
    }

    // LiveData методы для наблюдения за данными

    public LiveData<UserEntity> observeById(String id) {
        return userDao.observeById(id);
    }

    public LiveData<List<UserEntity>> observeAll() {
        return userDao.observeAll();
    }
}
