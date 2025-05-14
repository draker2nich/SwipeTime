package com.draker.swipetime.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.AnimeDao;
import com.draker.swipetime.database.entities.AnimeEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Репозиторий для работы с аниме
 */
public class AnimeRepository {

    private final AnimeDao animeDao;
    private final Executor executor;

    public AnimeRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        animeDao = db.animeDao();
        executor = Executors.newSingleThreadExecutor();
    }

    // Методы для работы с аниме в фоновом потоке

    public void insert(AnimeEntity anime) {
        executor.execute(() -> animeDao.insert(anime));
    }

    public void insertAll(List<AnimeEntity> animes) {
        executor.execute(() -> animeDao.insertAll(animes));
    }

    public void update(AnimeEntity anime) {
        executor.execute(() -> animeDao.update(anime));
    }

    public void delete(AnimeEntity anime) {
        executor.execute(() -> animeDao.delete(anime));
    }

    public void deleteById(String id) {
        executor.execute(() -> animeDao.deleteById(id));
    }

    public void deleteAll() {
        executor.execute(animeDao::deleteAll);
    }

    // Синхронные методы для получения данных

    public AnimeEntity getById(String id) {
        return animeDao.getById(id);
    }

    public List<AnimeEntity> getAll() {
        return animeDao.getAll();
    }

    public List<AnimeEntity> getLiked() {
        return animeDao.getLiked();
    }

    public List<AnimeEntity> getByGenre(String genre) {
        return animeDao.getByGenre(genre);
    }

    public List<AnimeEntity> getByStudio(String studio) {
        return animeDao.getByStudio(studio);
    }

    public List<AnimeEntity> getByReleaseYear(int year) {
        return animeDao.getByReleaseYear(year);
    }

    public List<AnimeEntity> getByStatus(String status) {
        return animeDao.getByStatus(status);
    }

    public List<AnimeEntity> getByType(String type) {
        return animeDao.getByType(type);
    }

    public List<AnimeEntity> getByMaxEpisodes(int episodeCount) {
        return animeDao.getByMaxEpisodes(episodeCount);
    }

    public List<AnimeEntity> search(String query) {
        return animeDao.search(query);
    }

    public int getCount() {
        return animeDao.getCount();
    }

    public int getLikedCount() {
        return animeDao.getLikedCount();
    }

    public int getCountByGenre(String genre) {
        return animeDao.getCountByGenre(genre);
    }

    // LiveData методы для наблюдения за данными

    public LiveData<AnimeEntity> observeById(String id) {
        return animeDao.observeById(id);
    }

    public LiveData<List<AnimeEntity>> observeAll() {
        return animeDao.observeAll();
    }

    public LiveData<List<AnimeEntity>> observeLiked() {
        return animeDao.observeLiked();
    }

    public LiveData<List<AnimeEntity>> observeByGenre(String genre) {
        return animeDao.observeByGenre(genre);
    }

    public LiveData<List<AnimeEntity>> observeByStudio(String studio) {
        return animeDao.observeByStudio(studio);
    }

    public LiveData<List<AnimeEntity>> observeByStatus(String status) {
        return animeDao.observeByStatus(status);
    }

    public LiveData<List<AnimeEntity>> observeByType(String type) {
        return animeDao.observeByType(type);
    }
}
