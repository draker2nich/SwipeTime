package com.draker.swipetime.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.GameDao;
import com.draker.swipetime.database.entities.GameEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Репозиторий для работы с играми
 */
public class GameRepository {

    private final GameDao gameDao;
    private final Executor executor;

    public GameRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        gameDao = db.gameDao();
        executor = Executors.newSingleThreadExecutor();
    }

    // Методы для работы с играми в фоновом потоке

    public void insert(GameEntity game) {
        executor.execute(() -> gameDao.insert(game));
    }

    public void insertAll(List<GameEntity> games) {
        executor.execute(() -> gameDao.insertAll(games));
    }

    public void update(GameEntity game) {
        executor.execute(() -> gameDao.update(game));
    }

    public void delete(GameEntity game) {
        executor.execute(() -> gameDao.delete(game));
    }

    public void deleteById(String id) {
        executor.execute(() -> gameDao.deleteById(id));
    }

    public void deleteAll() {
        executor.execute(gameDao::deleteAll);
    }

    // Синхронные методы для получения данных

    public GameEntity getById(String id) {
        return gameDao.getById(id);
    }

    public List<GameEntity> getAll() {
        return gameDao.getAll();
    }

    public List<GameEntity> getLiked() {
        return gameDao.getLiked();
    }

    public List<GameEntity> getByGenre(String genre) {
        return gameDao.getByGenre(genre);
    }

    public List<GameEntity> getByDeveloper(String developer) {
        return gameDao.getByDeveloper(developer);
    }

    public List<GameEntity> getByPublisher(String publisher) {
        return gameDao.getByPublisher(publisher);
    }

    public List<GameEntity> getByReleaseYear(int year) {
        return gameDao.getByReleaseYear(year);
    }

    public List<GameEntity> getByPlatform(String platform) {
        return gameDao.getByPlatform(platform);
    }

    public List<GameEntity> getByEsrbRating(String rating) {
        return gameDao.getByEsrbRating(rating);
    }

    public List<GameEntity> search(String query) {
        return gameDao.search(query);
    }

    public int getCount() {
        return gameDao.getCount();
    }

    public int getLikedCount() {
        return gameDao.getLikedCount();
    }

    public int getCountByGenre(String genre) {
        return gameDao.getCountByGenre(genre);
    }

    // LiveData методы для наблюдения за данными

    public LiveData<GameEntity> observeById(String id) {
        return gameDao.observeById(id);
    }

    public LiveData<List<GameEntity>> observeAll() {
        return gameDao.observeAll();
    }

    public LiveData<List<GameEntity>> observeLiked() {
        return gameDao.observeLiked();
    }

    public LiveData<List<GameEntity>> observeByGenre(String genre) {
        return gameDao.observeByGenre(genre);
    }

    public LiveData<List<GameEntity>> observeByPlatform(String platform) {
        return gameDao.observeByPlatform(platform);
    }
}
