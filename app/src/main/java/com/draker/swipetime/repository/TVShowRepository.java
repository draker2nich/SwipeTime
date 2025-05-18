package com.draker.swipetime.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.TVShowDao;
import com.draker.swipetime.database.entities.TVShowEntity;

import java.util.List;

/**
 * Репозиторий для работы с сериалами
 */
public class TVShowRepository {

    private TVShowDao tvShowDao;

    public TVShowRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        tvShowDao = db.tvShowDao();
    }

    /**
     * Добавить новый сериал
     * @param tvShow сериал
     */
    public void insert(TVShowEntity tvShow) {
        tvShowDao.insert(tvShow);
    }

    /**
     * Добавить несколько сериалов
     * @param tvShows список сериалов
     */
    public void insertAll(List<TVShowEntity> tvShows) {
        tvShowDao.insertAll(tvShows);
    }

    /**
     * Обновить сериал
     * @param tvShow сериал
     */
    public void update(TVShowEntity tvShow) {
        tvShowDao.update(tvShow);
    }

    /**
     * Удалить сериал по ID
     * @param id ID сериала
     */
    public void deleteById(String id) {
        TVShowEntity tvShow = tvShowDao.getById(id);
        if (tvShow != null) {
            tvShowDao.delete(tvShow);
        }
    }

    /**
     * Получить сериал по ID
     * @param id ID сериала
     * @return сериал или null, если не найден
     */
    public TVShowEntity getById(String id) {
        return tvShowDao.getById(id);
    }

    /**
     * Получить все сериалы
     * @return список всех сериалов
     */
    public List<TVShowEntity> getAll() {
        return tvShowDao.getAll();
    }

    /**
     * Получить список понравившихся сериалов
     * @return список сериалов с отметкой liked=true
     */
    public List<TVShowEntity> getLiked() {
        return tvShowDao.getLiked();
    }

    /**
     * Обновить статус "понравилось" для сериала
     * @param id ID сериала
     * @param liked новый статус
     */
    public void updateLikedStatus(String id, boolean liked) {
        tvShowDao.updateLikedStatus(id, liked);
    }

    /**
     * Обновить статус "просмотрено" для сериала
     * @param id ID сериала
     * @param watched новый статус
     */
    public void updateWatchedStatus(String id, boolean watched) {
        tvShowDao.updateWatchedStatus(id, watched);
    }

    /**
     * Получить список просмотренных сериалов
     * @return список сериалов с отметкой watched=true
     */
    public List<TVShowEntity> getWatched() {
        return tvShowDao.getWatched();
    }

    /**
     * Наблюдать за всеми сериалами (LiveData)
     * @return LiveData со списком всех сериалов
     */
    public LiveData<List<TVShowEntity>> observeAll() {
        return tvShowDao.observeAll();
    }
    
    /**
     * Наблюдать за понравившимися сериалами (LiveData)
     * @return LiveData со списком понравившихся сериалов
     */
    public LiveData<List<TVShowEntity>> observeLiked() {
        return tvShowDao.observeLiked();
    }

    /**
     * Наблюдать за просмотренными сериалами (LiveData)
     * @return LiveData со списком просмотренных сериалов
     */
    public LiveData<List<TVShowEntity>> observeWatched() {
        return tvShowDao.observeWatched();
    }
    
    /**
     * Получить количество сериалов
     * @return общее количество сериалов в базе данных
     */
    public int getCount() {
        return tvShowDao.getCount();
    }
    
    /**
     * Получить количество понравившихся сериалов
     * @return количество сериалов с отметкой liked=true
     */
    public int getLikedCount() {
        return tvShowDao.getLikedCount();
    }
    
    /**
     * Получить количество просмотренных сериалов
     * @return количество сериалов с отметкой watched=true
     */
    public int getWatchedCount() {
        return tvShowDao.getWatchedCount();
    }
    
    /**
     * Поиск сериалов по запросу
     * @param query поисковый запрос
     * @return список сериалов, соответствующих запросу
     */
    public List<TVShowEntity> search(String query) {
        return tvShowDao.search(query);
    }
    
    /**
     * Удалить все сериалы
     */
    public void deleteAll() {
        tvShowDao.deleteAll();
    }
}
