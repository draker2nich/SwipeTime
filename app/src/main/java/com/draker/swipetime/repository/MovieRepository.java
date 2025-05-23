package com.draker.swipetime.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.MovieDao;
import com.draker.swipetime.database.entities.MovieEntity;

import java.util.List;

/**
 * Репозиторий для работы с фильмами
 */
public class MovieRepository {

    private MovieDao movieDao;

    public MovieRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        movieDao = db.movieDao();
    }

    /**
     * Добавить новый фильм
     * @param movie фильм
     */
    public void insert(MovieEntity movie) {
        movieDao.insert(movie);
    }

    /**
     * Добавить несколько фильмов
     * @param movies список фильмов
     */
    public void insertAll(List<MovieEntity> movies) {
        movieDao.insertAll(movies);
    }

    /**
     * Обновить фильм
     * @param movie фильм
     */
    public void update(MovieEntity movie) {
        movieDao.update(movie);
    }

    /**
     * Удалить фильм по ID
     * @param id ID фильма
     */
    public void deleteById(String id) {
        MovieEntity movie = movieDao.getById(id);
        if (movie != null) {
            movieDao.delete(movie);
        }
    }

    /**
     * Получить фильм по ID
     * @param id ID фильма
     * @return фильм или null, если не найден
     */
    public MovieEntity getById(String id) {
        return movieDao.getById(id);
    }

    /**
     * Получить все фильмы
     * @return список всех фильмов
     */
    public List<MovieEntity> getAll() {
        return movieDao.getAll();
    }

    /**
     * Получить список понравившихся фильмов
     * @return список фильмов с отметкой liked=true
     */
    public List<MovieEntity> getLiked() {
        return movieDao.getLiked();
    }

    /**
     * Обновить статус "понравилось" для фильма
     * @param id ID фильма
     * @param liked новый статус
     */
    public void updateLikedStatus(String id, boolean liked) {
        movieDao.updateLikedStatus(id, liked);
    }

    /**
     * Обновить статус "просмотрено" для фильма
     * @param id ID фильма
     * @param watched новый статус
     */
    public void updateWatchedStatus(String id, boolean watched) {
        movieDao.updateWatchedStatus(id, watched);
    }

    /**
     * Получить список просмотренных фильмов
     * @return список фильмов с отметкой watched=true
     */
    public List<MovieEntity> getWatched() {
        return movieDao.getWatched();
    }

    /**
     * Наблюдать за всеми фильмами (LiveData)
     * @return LiveData со списком всех фильмов
     */
    public LiveData<List<MovieEntity>> observeAll() {
        return movieDao.observeAll();
    }
    
    /**
     * Наблюдать за понравившимися фильмами (LiveData)
     * @return LiveData со списком понравившихся фильмов
     */
    public LiveData<List<MovieEntity>> observeLiked() {
        return movieDao.observeLiked();
    }

    /**
     * Наблюдать за просмотренными фильмами (LiveData)
     * @return LiveData со списком просмотренных фильмов
     */
    public LiveData<List<MovieEntity>> observeWatched() {
        return movieDao.observeWatched();
    }
    
    /**
     * Получить количество фильмов
     * @return общее количество фильмов в базе данных
     */
    public int getCount() {
        return movieDao.getCount();
    }
    
    /**
     * Получить количество понравившихся фильмов
     * @return количество фильмов с отметкой liked=true
     */
    public int getLikedCount() {
        return movieDao.getLikedCount();
    }
    
    /**
     * Получить количество просмотренных фильмов
     * @return количество фильмов с отметкой watched=true
     */
    public int getWatchedCount() {
        return movieDao.getWatchedCount();
    }
    
    /**
     * Поиск фильмов по запросу
     * @param query поисковый запрос
     * @return список фильмов, соответствующих запросу
     */
    public List<MovieEntity> search(String query) {
        return movieDao.search(query);
    }
    
    /**
     * Удалить все фильмы
     */
    public void deleteAll() {
        movieDao.deleteAll();
    }
}
