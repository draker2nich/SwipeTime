package com.draker.swipetime.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.UserDao;
import com.draker.swipetime.database.entities.UserEntity;

import java.util.List;

/**
 * Репозиторий для работы с пользователями
 */
public class UserRepository {

    private UserDao userDao;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
    }

    /**
     * Добавить нового пользователя
     * @param user пользователь для добавления
     */
    public void insert(UserEntity user) {
        userDao.insert(user);
    }

    /**
     * Добавить несколько пользователей
     * @param users список пользователей
     */
    public void insertAll(List<UserEntity> users) {
        userDao.insertAll(users);
    }

    /**
     * Обновить существующего пользователя
     * @param user пользователь для обновления
     */
    public void update(UserEntity user) {
        userDao.update(user);
    }

    /**
     * Удалить пользователя
     * @param user пользователь для удаления
     */
    public void delete(UserEntity user) {
        userDao.delete(user);
    }

    /**
     * Удалить пользователя по ID
     * @param id ID пользователя
     */
    public void deleteById(String id) {
        userDao.deleteById(id);
    }

    /**
     * Получить пользователя по ID
     * @param id ID пользователя
     * @return пользователь с указанным ID
     */
    public UserEntity getUserById(String id) {
        return userDao.getById(id);
    }

    /**
     * Наблюдать за пользователем по ID (LiveData)
     * @param id ID пользователя
     * @return LiveData с пользователем
     */
    public LiveData<UserEntity> observeById(String id) {
        return userDao.observeById(id);
    }

    /**
     * Получить пользователя по email
     * @param email email пользователя
     * @return пользователь с указанным email
     */
    public UserEntity getUserByEmail(String email) {
        return userDao.getByEmail(email);
    }

    /**
     * Получить пользователя по имени пользователя
     * @param username имя пользователя
     * @return пользователь с указанным именем
     */
    public UserEntity getUserByUsername(String username) {
        return userDao.getByUsername(username);
    }

    /**
     * Получить всех пользователей
     * @return список всех пользователей
     */
    public List<UserEntity> getAll() {
        return userDao.getAll();
    }
    
    /**
     * Получить всех пользователей (альтернативное название метода)
     * @return список всех пользователей
     */
    public List<UserEntity> getAllUsers() {
        return userDao.getAll();
    }

    /**
     * Наблюдать за всеми пользователями (LiveData)
     * @return LiveData со списком всех пользователей
     */
    public LiveData<List<UserEntity>> observeAll() {
        return userDao.observeAll();
    }

    /**
     * Получить пользователей с уровнем не ниже указанного
     * @param level минимальный уровень
     * @return список пользователей с уровнем не ниже указанного
     */
    public List<UserEntity> getUsersByMinLevel(int level) {
        return userDao.getByMinLevel(level);
    }

    /**
     * Получить пользователей с опытом не ниже указанного
     * @param experience минимальный опыт
     * @return список пользователей с опытом не ниже указанного
     */
    public List<UserEntity> getUsersByMinExperience(int experience) {
        return userDao.getByMinExperience(experience);
    }

    /**
     * Получить пользователей, отсортированных по уровню (высокие в начале)
     * @return отсортированный список пользователей
     */
    public List<UserEntity> getAllOrderedByLevelDesc() {
        return userDao.getAllOrderedByLevelDesc();
    }

    /**
     * Получить пользователей, отсортированных по опыту (высокие в начале)
     * @return отсортированный список пользователей
     */
    public List<UserEntity> getAllOrderedByExperienceDesc() {
        return userDao.getAllOrderedByExperienceDesc();
    }

    /**
     * Получить количество пользователей
     * @return количество пользователей
     */
    public int getCount() {
        return userDao.getCount();
    }

    /**
     * Обновить опыт пользователя
     * @param userId ID пользователя
     * @param experience новое значение опыта
     */
    public void updateExperience(String userId, int experience) {
        userDao.updateExperience(userId, experience);
    }

    /**
     * Обновить уровень пользователя
     * @param userId ID пользователя
     * @param level новое значение уровня
     */
    public void updateLevel(String userId, int level) {
        userDao.updateLevel(userId, level);
    }

    /**
     * Добавить опыт пользователю
     * @param userId ID пользователя
     * @param amount количество опыта для добавления
     * @return true, если уровень был повышен
     */
    public boolean addExperience(String userId, int amount) {
        UserEntity user = getUserById(userId);
        if (user != null) {
            boolean levelUp = user.addExperience(amount);
            update(user);
            return levelUp;
        }
        return false;
    }

    /**
     * Обновить предпочитаемые категории пользователя
     * @param userId ID пользователя
     * @param categories строка с предпочитаемыми категориями
     */
    public void updatePreferredCategories(String userId, String categories) {
        userDao.updatePreferredCategories(userId, categories);
    }
}
