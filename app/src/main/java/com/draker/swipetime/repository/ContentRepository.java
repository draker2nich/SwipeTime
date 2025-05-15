package com.draker.swipetime.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.ContentDao;
import com.draker.swipetime.database.entities.ContentEntity;

import java.util.List;

/**
 * Репозиторий для работы с общим контентом
 */
public class ContentRepository {

    private ContentDao contentDao;

    public ContentRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        contentDao = db.contentDao();
    }

    /**
     * Добавить новый контент
     * @param content контент
     */
    public void insert(ContentEntity content) {
        contentDao.insert(content);
    }

    /**
     * Добавить несколько контентов
     * @param contents список контента
     */
    public void insertAll(List<ContentEntity> contents) {
        contentDao.insertAll(contents);
    }

    /**
     * Обновить контент
     * @param content контент
     */
    public void update(ContentEntity content) {
        contentDao.update(content);
    }

    /**
     * Удалить контент
     * @param content контент
     */
    public void delete(ContentEntity content) {
        contentDao.delete(content);
    }

    /**
     * Получить контент по ID
     * @param id ID контента
     * @return контент или null, если не найден
     */
    public ContentEntity getById(String id) {
        return contentDao.getById(id);
    }

    /**
     * Получить весь контент
     * @return список всего контента
     */
    public List<ContentEntity> getAll() {
        return contentDao.getAll();
    }

    /**
     * Получить список понравившегося контента
     * @return список контента с отметкой liked=true
     */
    public List<ContentEntity> getLiked() {
        return contentDao.getLiked();
    }
    
    /**
     * Получить список понравившегося контента по категории
     * @param category категория контента
     * @return список контента с отметкой liked=true в указанной категории
     */
    public List<ContentEntity> getLikedByCategory(String category) {
        return contentDao.getLikedByCategory(category);
    }

    /**
     * Обновить статус "понравилось" для контента
     * @param id ID контента
     * @param liked новый статус
     */
    public void updateLikedStatus(String id, boolean liked) {
        contentDao.updateLikedStatus(id, liked);
    }

    /**
     * Обновить статус "просмотрено" для контента
     * @param id ID контента
     * @param watched новый статус
     */
    public void updateWatchedStatus(String id, boolean watched) {
        contentDao.updateWatchedStatus(id, watched);
    }

    /**
     * Получить список просмотренного контента
     * @return список контента с отметкой watched=true
     */
    public List<ContentEntity> getWatched() {
        return contentDao.getWatched();
    }

    /**
     * Получить контент по категории
     * @param category категория
     * @return список контента указанной категории
     */
    public List<ContentEntity> getByCategory(String category) {
        return contentDao.getByCategory(category);
    }

    /**
     * Наблюдать за всем контентом (LiveData)
     * @return LiveData со списком всего контента
     */
    public LiveData<List<ContentEntity>> observeAll() {
        return contentDao.observeAll();
    }
    
    /**
     * Наблюдать за понравившимся контентом (LiveData)
     * @return LiveData со списком понравившегося контента
     */
    public LiveData<List<ContentEntity>> observeLiked() {
        return contentDao.observeLiked();
    }

    /**
     * Наблюдать за просмотренным контентом (LiveData)
     * @return LiveData со списком просмотренного контента
     */
    public LiveData<List<ContentEntity>> observeWatched() {
        return contentDao.observeWatched();
    }
    
    /**
     * Наблюдать за контентом по категории (LiveData)
     * @param category категория
     * @return LiveData со списком контента указанной категории
     */
    public LiveData<List<ContentEntity>> observeByCategory(String category) {
        return contentDao.observeByCategory(category);
    }
    
    /**
     * Поиск контента по запросу
     * @param query поисковый запрос
     * @return список контента, соответствующего запросу
     */
    public List<ContentEntity> search(String query) {
        return contentDao.search(query);
    }
    
    /**
     * Получить количество всего контента
     * @return количество контента
     */
    public int getCount() {
        return contentDao.getCount();
    }
    
    /**
     * Получить количество понравившегося контента
     * @return количество понравившегося контента
     */
    public int getLikedCount() {
        return contentDao.getLikedCount();
    }
    
    /**
     * Получить количество просмотренного контента
     * @return количество просмотренного контента
     */
    public int getWatchedCount() {
        return contentDao.getWatchedCount();
    }
}