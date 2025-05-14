package com.draker.swipetime.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.TVShowDao;
import com.draker.swipetime.database.entities.TVShowEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Репозиторий для работы с сериалами
 */
public class TVShowRepository {

    private final TVShowDao tvShowDao;
    private final Executor executor;

    public TVShowRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        tvShowDao = db.tvShowDao();
        executor = Executors.newSingleThreadExecutor();
    }

    // Методы для работы с сериалами в фоновом потоке

    public void insert(TVShowEntity tvShow) {
        executor.execute(() -> tvShowDao.insert(tvShow));
    }

    public void insertAll(List<TVShowEntity> tvShows) {
        executor.execute(() -> tvShowDao.insertAll(tvShows));
    }

    public void update(TVShowEntity tvShow) {
        executor.execute(() -> tvShowDao.update(tvShow));
    }

    public void delete(TVShowEntity tvShow) {
        executor.execute(() -> tvShowDao.delete(tvShow));
    }

    public void deleteById(String id) {
        executor.execute(() -> tvShowDao.deleteById(id));
    }

    public void deleteAll() {
        executor.execute(tvShowDao::deleteAll);
    }

    // Синхронные методы для получения данных

    public TVShowEntity getById(String id) {
        return tvShowDao.getById(id);
    }

    public List<TVShowEntity> getAll() {
        return tvShowDao.getAll();
    }

    public List<TVShowEntity> getLiked() {
        return tvShowDao.getLiked();
    }

    public List<TVShowEntity> getByGenre(String genre) {
        return tvShowDao.getByGenre(genre);
    }

    public List<TVShowEntity> getByCreator(String creator) {
        return tvShowDao.getByCreator(creator);
    }

    public List<TVShowEntity> getByStartYear(int year) {
        return tvShowDao.getByStartYear(year);
    }

    public List<TVShowEntity> getByStatus(String status) {
        return tvShowDao.getByStatus(status);
    }

    public List<TVShowEntity> search(String query) {
        return tvShowDao.search(query);
    }

    public int getCount() {
        return tvShowDao.getCount();
    }

    public int getLikedCount() {
        return tvShowDao.getLikedCount();
    }

    public int getCountByGenre(String genre) {
        return tvShowDao.getCountByGenre(genre);
    }

    // LiveData методы для наблюдения за данными

    public LiveData<TVShowEntity> observeById(String id) {
        return tvShowDao.observeById(id);
    }

    public LiveData<List<TVShowEntity>> observeAll() {
        return tvShowDao.observeAll();
    }

    public LiveData<List<TVShowEntity>> observeLiked() {
        return tvShowDao.observeLiked();
    }

    public LiveData<List<TVShowEntity>> observeByGenre(String genre) {
        return tvShowDao.observeByGenre(genre);
    }

    public LiveData<List<TVShowEntity>> observeByStatus(String status) {
        return tvShowDao.observeByStatus(status);
    }
}
