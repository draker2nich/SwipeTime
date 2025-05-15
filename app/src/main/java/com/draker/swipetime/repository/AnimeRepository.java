package com.draker.swipetime.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.AnimeDao;
import com.draker.swipetime.database.entities.AnimeEntity;

import java.util.List;

/**
 * Репозиторий для работы с аниме
 */
public class AnimeRepository {

    private AnimeDao animeDao;

    public AnimeRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        animeDao = db.animeDao();
    }

    /**
     * Добавить новое аниме
     * @param anime аниме
     */
    public void insert(AnimeEntity anime) {
        animeDao.insert(anime);
    }

    /**
     * Добавить несколько аниме
     * @param animes список аниме
     */
    public void insertAll(List<AnimeEntity> animes) {
        animeDao.insertAll(animes);
    }

    /**
     * Обновить аниме
     * @param anime аниме
     */
    public void update(AnimeEntity anime) {
        animeDao.update(anime);
    }

    /**
     * Удалить аниме
     * @param anime аниме
     */
    public void delete(AnimeEntity anime) {
        animeDao.delete(anime);
    }

    /**
     * Получить аниме по ID
     * @param id ID аниме
     * @return аниме или null, если не найдено
     */
    public AnimeEntity getById(String id) {
        return animeDao.getById(id);
    }

    /**
     * Получить все аниме
     * @return список всех аниме
     */
    public List<AnimeEntity> getAll() {
        return animeDao.getAll();
    }

    /**
     * Получить список понравившихся аниме
     * @return список аниме с отметкой liked=true
     */
    public List<AnimeEntity> getLiked() {
        return animeDao.getLiked();
    }

    /**
     * Обновить статус "понравилось" для аниме
     * @param id ID аниме
     * @param liked новый статус
     */
    public void updateLikedStatus(String id, boolean liked) {
        animeDao.updateLikedStatus(id, liked);
    }

    /**
     * Обновить статус "просмотрено" для аниме
     * @param id ID аниме
     * @param watched новый статус
     */
    public void updateWatchedStatus(String id, boolean watched) {
        animeDao.updateWatchedStatus(id, watched);
    }

    /**
     * Получить список просмотренных аниме
     * @return список аниме с отметкой watched=true
     */
    public List<AnimeEntity> getWatched() {
        return animeDao.getWatched();
    }

    /**
     * Наблюдать за всеми аниме (LiveData)
     * @return LiveData со списком всех аниме
     */
    public LiveData<List<AnimeEntity>> observeAll() {
        return animeDao.observeAll();
    }
    
    /**
     * Наблюдать за понравившимися аниме (LiveData)
     * @return LiveData со списком понравившихся аниме
     */
    public LiveData<List<AnimeEntity>> observeLiked() {
        return animeDao.observeLiked();
    }

    /**
     * Наблюдать за просмотренными аниме (LiveData)
     * @return LiveData со списком просмотренных аниме
     */
    public LiveData<List<AnimeEntity>> observeWatched() {
        return animeDao.observeWatched();
    }
    
    /**
     * Поиск аниме по запросу
     * @param query поисковый запрос
     * @return список аниме, соответствующих запросу
     */
    public List<AnimeEntity> search(String query) {
        return animeDao.search(query);
    }
    
    /**
     * Получить количество всех аниме
     * @return количество аниме
     */
    public int getCount() {
        return animeDao.getCount();
    }
    
    /**
     * Получить количество понравившихся аниме
     * @return количество понравившихся аниме
     */
    public int getLikedCount() {
        return animeDao.getLikedCount();
    }
    
    /**
     * Получить количество просмотренных аниме
     * @return количество просмотренных аниме
     */
    public int getWatchedCount() {
        return animeDao.getWatchedCount();
    }
}