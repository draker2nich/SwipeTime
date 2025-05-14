package com.draker.swipetime.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.MovieDao;
import com.draker.swipetime.database.entities.MovieEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Репозиторий для работы с фильмами
 */
public class MovieRepository {

    private final MovieDao movieDao;
    private final Executor executor;

    public MovieRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        movieDao = db.movieDao();
        executor = Executors.newSingleThreadExecutor();
    }

    // Методы для работы с фильмами в фоновом потоке

    public void insert(MovieEntity movie) {
        executor.execute(() -> movieDao.insert(movie));
    }

    public void insertAll(List<MovieEntity> movies) {
        executor.execute(() -> movieDao.insertAll(movies));
    }

    public void update(MovieEntity movie) {
        executor.execute(() -> movieDao.update(movie));
    }

    public void delete(MovieEntity movie) {
        executor.execute(() -> movieDao.delete(movie));
    }

    public void deleteById(String id) {
        executor.execute(() -> movieDao.deleteById(id));
    }

    public void deleteAll() {
        executor.execute(movieDao::deleteAll);
    }

    // Синхронные методы для получения данных

    public MovieEntity getById(String id) {
        return movieDao.getById(id);
    }

    public List<MovieEntity> getAll() {
        return movieDao.getAll();
    }

    public List<MovieEntity> getLiked() {
        return movieDao.getLiked();
    }

    public List<MovieEntity> getByGenre(String genre) {
        return movieDao.getByGenre(genre);
    }

    public List<MovieEntity> getByDirector(String director) {
        return movieDao.getByDirector(director);
    }

    public List<MovieEntity> getByReleaseYear(int year) {
        return movieDao.getByReleaseYear(year);
    }

    public List<MovieEntity> getByReleaseYearRange(int startYear, int endYear) {
        return movieDao.getByReleaseYearRange(startYear, endYear);
    }

    public List<MovieEntity> search(String query) {
        return movieDao.search(query);
    }

    public int getCount() {
        return movieDao.getCount();
    }

    public int getLikedCount() {
        return movieDao.getLikedCount();
    }

    public int getCountByGenre(String genre) {
        return movieDao.getCountByGenre(genre);
    }

    // LiveData методы для наблюдения за данными

    public LiveData<MovieEntity> observeById(String id) {
        return movieDao.observeById(id);
    }

    public LiveData<List<MovieEntity>> observeAll() {
        return movieDao.observeAll();
    }

    public LiveData<List<MovieEntity>> observeLiked() {
        return movieDao.observeLiked();
    }

    public LiveData<List<MovieEntity>> observeByGenre(String genre) {
        return movieDao.observeByGenre(genre);
    }
}
