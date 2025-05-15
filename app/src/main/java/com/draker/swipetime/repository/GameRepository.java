package com.draker.swipetime.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.GameDao;
import com.draker.swipetime.database.entities.GameEntity;

import java.util.List;

/**
 * Репозиторий для работы с играми
 */
public class GameRepository {

    private GameDao gameDao;

    public GameRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        gameDao = db.gameDao();
    }

    /**
     * Добавить новую игру
     * @param game игра
     */
    public void insert(GameEntity game) {
        gameDao.insert(game);
    }

    /**
     * Добавить несколько игр
     * @param games список игр
     */
    public void insertAll(List<GameEntity> games) {
        gameDao.insertAll(games);
    }

    /**
     * Обновить игру
     * @param game игра
     */
    public void update(GameEntity game) {
        gameDao.update(game);
    }

    /**
     * Удалить игру
     * @param game игра
     */
    public void delete(GameEntity game) {
        gameDao.delete(game);
    }

    /**
     * Получить игру по ID
     * @param id ID игры
     * @return игра или null, если не найдена
     */
    public GameEntity getById(String id) {
        return gameDao.getById(id);
    }

    /**
     * Получить все игры
     * @return список всех игр
     */
    public List<GameEntity> getAll() {
        return gameDao.getAll();
    }

    /**
     * Получить список понравившихся игр
     * @return список игр с отметкой liked=true
     */
    public List<GameEntity> getLiked() {
        return gameDao.getLiked();
    }

    /**
     * Обновить статус "понравилось" для игры
     * @param id ID игры
     * @param liked новый статус
     */
    public void updateLikedStatus(String id, boolean liked) {
        gameDao.updateLikedStatus(id, liked);
    }

    /**
     * Обновить статус "пройдено" для игры
     * @param id ID игры
     * @param completed новый статус
     */
    public void updateCompletedStatus(String id, boolean completed) {
        gameDao.updateCompletedStatus(id, completed);
    }

    /**
     * Получить список пройденных игр
     * @return список игр с отметкой is_completed=true
     */
    public List<GameEntity> getCompleted() {
        return gameDao.getCompleted();
    }

    /**
     * Наблюдать за всеми играми (LiveData)
     * @return LiveData со списком всех игр
     */
    public LiveData<List<GameEntity>> observeAll() {
        return gameDao.observeAll();
    }
    
    /**
     * Наблюдать за понравившимися играми (LiveData)
     * @return LiveData со списком понравившихся игр
     */
    public LiveData<List<GameEntity>> observeLiked() {
        return gameDao.observeLiked();
    }

    /**
     * Наблюдать за пройденными играми (LiveData)
     * @return LiveData со списком пройденных игр
     */
    public LiveData<List<GameEntity>> observeCompleted() {
        return gameDao.observeCompleted();
    }
    
    /**
     * Поиск игр по запросу
     * @param query поисковый запрос
     * @return список игр, соответствующих запросу
     */
    public List<GameEntity> search(String query) {
        return gameDao.search(query);
    }
    
    /**
     * Получить количество всех игр
     * @return количество игр
     */
    public int getCount() {
        return gameDao.getCount();
    }
    
    /**
     * Получить количество понравившихся игр
     * @return количество понравившихся игр
     */
    public int getLikedCount() {
        return gameDao.getLikedCount();
    }
    
    /**
     * Получить количество пройденных игр
     * @return количество пройденных игр
     */
    public int getCompletedCount() {
        return gameDao.getCompletedCount();
    }
}