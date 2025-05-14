package com.draker.swipetime.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.ContentDao;
import com.draker.swipetime.database.entities.ContentEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Репозиторий для работы с базовым контентом
 */
public class ContentRepository {

    private final ContentDao contentDao;
    private final Executor executor;

    public ContentRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        contentDao = db.contentDao();
        executor = Executors.newSingleThreadExecutor();
    }

    // Методы для работы с контентом в фоновом потоке

    public void insert(ContentEntity content) {
        executor.execute(() -> contentDao.insert(content));
    }

    public void insertAll(List<ContentEntity> contents) {
        executor.execute(() -> contentDao.insertAll(contents));
    }

    public void update(ContentEntity content) {
        executor.execute(() -> contentDao.update(content));
    }

    public void delete(ContentEntity content) {
        executor.execute(() -> contentDao.delete(content));
    }

    public void deleteById(String id) {
        executor.execute(() -> contentDao.deleteById(id));
    }

    public void updateLikedStatus(String id, boolean liked) {
        executor.execute(() -> contentDao.updateLikedStatus(id, liked));
    }

    public void updateViewedStatus(String id, boolean viewed) {
        executor.execute(() -> contentDao.updateViewedStatus(id, viewed));
    }

    public void updateRating(String id, float rating) {
        executor.execute(() -> contentDao.updateRating(id, rating));
    }

    public void deleteAll() {
        executor.execute(contentDao::deleteAll);
    }

    // Синхронные методы для получения данных

    public ContentEntity getById(String id) {
        return contentDao.getById(id);
    }

    public List<ContentEntity> getAll() {
        return contentDao.getAll();
    }

    public List<ContentEntity> getByCategory(String category) {
        return contentDao.getByCategory(category);
    }

    public List<ContentEntity> getLiked() {
        return contentDao.getLiked();
    }

    public List<ContentEntity> getByContentType(String contentType) {
        return contentDao.getByContentType(contentType);
    }

    public List<ContentEntity> getLikedByContentType(String contentType) {
        return contentDao.getLikedByContentType(contentType);
    }

    public List<ContentEntity> search(String query) {
        return contentDao.search(query);
    }

    public int getCount() {
        return contentDao.getCount();
    }

    public int getCountByCategory(String category) {
        return contentDao.getCountByCategory(category);
    }

    public int getLikedCount() {
        return contentDao.getLikedCount();
    }

    // LiveData методы для наблюдения за данными

    public LiveData<ContentEntity> observeById(String id) {
        return contentDao.observeById(id);
    }

    public LiveData<List<ContentEntity>> observeAll() {
        return contentDao.observeAll();
    }

    public LiveData<List<ContentEntity>> observeByCategory(String category) {
        return contentDao.observeByCategory(category);
    }

    public LiveData<List<ContentEntity>> observeLiked() {
        return contentDao.observeLiked();
    }

    public LiveData<List<ContentEntity>> observeByContentType(String contentType) {
        return contentDao.observeByContentType(contentType);
    }

    public LiveData<List<ContentEntity>> observeLikedByContentType(String contentType) {
        return contentDao.observeLikedByContentType(contentType);
    }
}
