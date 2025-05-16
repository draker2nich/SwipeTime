package com.draker.swipetime.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.ReviewDao;
import com.draker.swipetime.database.entities.ReviewEntity;

import java.util.List;

/**
 * Репозиторий для работы с отзывами пользователей
 */
public class ReviewRepository {

    private ReviewDao reviewDao;

    public ReviewRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        reviewDao = db.reviewDao();
    }

    /**
     * Добавить новый отзыв
     * @param review отзыв для добавления
     * @return ID добавленного отзыва
     */
    public long insert(ReviewEntity review) {
        return reviewDao.insert(review);
    }

    /**
     * Добавить несколько отзывов
     * @param reviews список отзывов
     */
    public void insertAll(List<ReviewEntity> reviews) {
        reviewDao.insertAll(reviews);
    }

    /**
     * Обновить существующий отзыв
     * @param review отзыв для обновления
     */
    public void update(ReviewEntity review) {
        reviewDao.update(review);
    }

    /**
     * Удалить отзыв
     * @param review отзыв для удаления
     */
    public void delete(ReviewEntity review) {
        reviewDao.delete(review);
    }

    /**
     * Удалить отзыв по ID
     * @param id ID отзыва
     */
    public void deleteById(long id) {
        reviewDao.deleteById(id);
    }

    /**
     * Получить отзыв по ID
     * @param id ID отзыва
     * @return отзыв с указанным ID
     */
    public ReviewEntity getById(long id) {
        return reviewDao.getById(id);
    }

    /**
     * Наблюдать за отзывом по ID (LiveData)
     * @param id ID отзыва
     * @return LiveData с отзывом
     */
    public LiveData<ReviewEntity> observeById(long id) {
        return reviewDao.observeById(id);
    }

    /**
     * Получить все отзывы
     * @return список всех отзывов
     */
    public List<ReviewEntity> getAll() {
        return reviewDao.getAll();
    }

    /**
     * Наблюдать за всеми отзывами (LiveData)
     * @return LiveData со списком всех отзывов
     */
    public LiveData<List<ReviewEntity>> observeAll() {
        return reviewDao.observeAll();
    }

    /**
     * Получить отзывы пользователя
     * @param userId ID пользователя
     * @return список отзывов пользователя
     */
    public List<ReviewEntity> getByUserId(String userId) {
        return reviewDao.getByUserId(userId);
    }

    /**
     * Наблюдать за отзывами пользователя (LiveData)
     * @param userId ID пользователя
     * @return LiveData со списком отзывов пользователя
     */
    public LiveData<List<ReviewEntity>> observeByUserId(String userId) {
        return reviewDao.observeByUserId(userId);
    }

    /**
     * Получить отзывы по контенту
     * @param contentId ID контента
     * @return список отзывов о контенте
     */
    public List<ReviewEntity> getByContentId(String contentId) {
        return reviewDao.getByContentId(contentId);
    }

    /**
     * Наблюдать за отзывами по контенту (LiveData)
     * @param contentId ID контента
     * @return LiveData со списком отзывов о контенте
     */
    public LiveData<List<ReviewEntity>> observeByContentId(String contentId) {
        return reviewDao.observeByContentId(contentId);
    }

    /**
     * Получить отзыв по контенту и пользователю
     * @param contentId ID контента
     * @param userId ID пользователя
     * @return отзыв пользователя о контенте или null, если отзыва нет
     */
    public ReviewEntity getByContentAndUserId(String contentId, String userId) {
        return reviewDao.getByContentAndUserId(contentId, userId);
    }

    /**
     * Получить отзывы с минимальным рейтингом
     * @param minRating минимальный рейтинг
     * @return список отзывов с рейтингом не ниже указанного
     */
    public List<ReviewEntity> getByMinRating(float minRating) {
        return reviewDao.getByMinRating(minRating);
    }

    /**
     * Получить отзывы по типу контента
     * @param contentType тип контента (фильмы, сериалы, игры и т.д.)
     * @return список отзывов о контенте указанного типа
     */
    public List<ReviewEntity> getByContentType(String contentType) {
        return reviewDao.getByContentType(contentType);
    }

    /**
     * Наблюдать за отзывами по типу контента (LiveData)
     * @param contentType тип контента (фильмы, сериалы, игры и т.д.)
     * @return LiveData со списком отзывов о контенте указанного типа
     */
    public LiveData<List<ReviewEntity>> observeByContentType(String contentType) {
        return reviewDao.observeByContentType(contentType);
    }

    /**
     * Получить средний рейтинг контента
     * @param contentId ID контента
     * @return средний рейтинг контента
     */
    public float getAverageRatingForContent(String contentId) {
        return reviewDao.getAverageRatingForContent(contentId);
    }

    /**
     * Получить количество отзывов о контенте
     * @param contentId ID контента
     * @return количество отзывов о контенте
     */
    public int getReviewCountForContent(String contentId) {
        return reviewDao.getReviewCountForContent(contentId);
    }

    /**
     * Поиск по тексту отзывов
     * @param query поисковый запрос
     * @return список отзывов, содержащих искомый текст
     */
    public List<ReviewEntity> searchInText(String query) {
        return reviewDao.searchInText(query);
    }

    /**
     * Получить отзывы, отсортированные по дате создания (новые в начале)
     * @return отсортированный список отзывов
     */
    public List<ReviewEntity> getAllOrderedByCreationDateDesc() {
        return reviewDao.getAllOrderedByCreationDateDesc();
    }

    /**
     * Получить отзывы, отсортированные по рейтингу (высокие в начале)
     * @return отсортированный список отзывов
     */
    public List<ReviewEntity> getAllOrderedByRatingDesc() {
        return reviewDao.getAllOrderedByRatingDesc();
    }

    /**
     * Получить отзывы пользователя, отсортированные по дате (новые в начале)
     * @param userId ID пользователя
     * @return отсортированный список отзывов пользователя
     */
    public List<ReviewEntity> getByUserIdOrderedByCreationDateDesc(String userId) {
        return reviewDao.getByUserIdOrderedByCreationDateDesc(userId);
    }

    /**
     * Удалить все отзывы пользователя
     * @param userId ID пользователя
     */
    public void deleteByUserId(String userId) {
        reviewDao.deleteByUserId(userId);
    }

    /**
     * Удалить все отзывы о контенте
     * @param contentId ID контента
     */
    public void deleteByContentId(String contentId) {
        reviewDao.deleteByContentId(contentId);
    }

    /**
     * Удалить все отзывы
     */
    public void deleteAll() {
        reviewDao.deleteAll();
    }

    /**
     * Получить общее количество отзывов
     * @return количество отзывов
     */
    public int getCount() {
        return reviewDao.getCount();
    }

    /**
     * Получить количество отзывов пользователя
     * @param userId ID пользователя
     * @return количество отзывов пользователя
     */
    public int getCountByUserId(String userId) {
        return reviewDao.getCountByUserId(userId);
    }
    
    /**
     * Получить отзывы пользователя по ID
     * @param userId ID пользователя
     * @return список отзывов пользователя
     */
    public List<ReviewEntity> getReviewsByUserId(String userId) {
        return getByUserId(userId);
    }
    
    /**
     * Добавить или обновить отзыв
     * @param review отзыв для добавления или обновления
     */
    public void insertOrUpdate(ReviewEntity review) {
        ReviewEntity existingReview = getByContentAndUserId(review.getContentId(), review.getUserId());
        if (existingReview != null) {
            review.setId(existingReview.getId());
            update(review);
        } else {
            insert(review);
        }
    }
}
