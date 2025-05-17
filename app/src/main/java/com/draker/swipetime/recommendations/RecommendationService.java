package com.draker.swipetime.recommendations;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.UserPreferencesRepository;
import com.draker.swipetime.repository.UserRepository;
import com.draker.swipetime.utils.GamificationIntegrator;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для легкой интеграции системы рекомендаций в приложение
 */
public class RecommendationService {
    private static final String TAG = "RecommendationService";

    private static RecommendationService instance;

    private Application application;
    private RecommendationManager recommendationManager;
    private RecommendationTester recommendationTester;

    /**
     * Получить экземпляр сервиса рекомендаций
     * @param application контекст приложения
     * @return экземпляр сервиса
     */
    public static synchronized RecommendationService getInstance(Application application) {
        if (instance == null) {
            instance = new RecommendationService(application);
        }
        return instance;
    }

    private RecommendationService(Application application) {
        this.application = application;
        this.recommendationManager = new RecommendationManager(application);
        this.recommendationTester = new RecommendationTester(application);
    }

    /**
     * Получить рекомендации контента для текущего пользователя
     *
     * @param context контекст
     * @param category категория контента (null для всех категорий)
     * @param limit максимальное количество рекомендаций
     * @return список рекомендуемых элементов
     */
    public List<ContentItem> getRecommendationsForCurrentUser(Context context, String category, int limit) {
        String userId = GamificationIntegrator.getCurrentUserId(context);
        return recommendationManager.getRecommendations(userId, category, limit);
    }

    /**
     * Обновляет предпочтения пользователя на основе его лайков
     *
     * @param context контекст
     */
    public void updateCurrentUserPreferences(Context context) {
        String userId = GamificationIntegrator.getCurrentUserId(context);
        recommendationManager.analyzeAndUpdateUserPreferences(userId);
    }

    /**
     * Запускает тестирование качества рекомендаций
     *
     * @return общая оценка качества рекомендаций (0-1)
     */
    public double testRecommendationQuality() {
        return recommendationTester.testRecommendationQuality();
    }

    /**
     * Обрабатывает событие свайпа (лайка/дизлайка) для обновления рекомендаций
     *
     * @param context контекст
     * @param contentId ID контента
     * @param liked true, если контент понравился (свайп вправо)
     */
    public void handleSwipeEvent(Context context, String contentId, boolean liked) {
        // Получаем ID текущего пользователя
        String userId = GamificationIntegrator.getCurrentUserId(context);

        // Если это лайк, обновляем предпочтения пользователя
        if (liked) {
            // Выполняем асинхронное обновление предпочтений
            new Thread(() -> {
                ContentRepository contentRepository = new ContentRepository(application);

                // Обновляем статус "нравится" для элемента
                contentRepository.updateLikedStatus(contentId, true);

                // Анализируем и обновляем предпочтения пользователя
                try {
                    Thread.sleep(500); // Небольшая задержка для уверенности, что лайк сохранен
                    recommendationManager.analyzeAndUpdateUserPreferences(userId);

                    Log.d(TAG, "Предпочтения пользователя " + userId + " обновлены после лайка контента " + contentId);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Ошибка при обновлении предпочтений: " + e.getMessage());
                }
            }).start();
        }
    }

    /**
     * Очищает кэши рекомендаций
     */
    public void clearCaches() {
        recommendationManager.clearCaches();
    }

    /**
     * Сортирует список контента по релевантности для текущего пользователя
     *
     * @param context контекст
     * @param contentItems список элементов для сортировки
     * @return отсортированный список
     */
    public List<ContentItem> sortByRelevance(Context context, List<ContentItem> contentItems) {
        String userId = GamificationIntegrator.getCurrentUserId(context);
        UserPreferencesRepository preferencesRepository = new UserPreferencesRepository(application);
        ContentRepository contentRepository = new ContentRepository(application);

        // Преобразуем контент в ContentEntity
        List<ContentEntity> contentEntities = new ArrayList<>();
        for (ContentItem item : contentItems) {
            ContentEntity entity = contentRepository.getById(item.getId());
            if (entity != null) {
                contentEntities.add(entity);
            }
        }

        // Получаем лайкнутый контент пользователя
        List<ContentEntity> likedContent = contentRepository.getLikedContentForUser(userId);

        // Генерируем рекомендации (сортируем по релевантности)
        List<ContentEntity> sortedEntities = ContentRecommendationEngine.generateRecommendations(
                contentEntities,
                likedContent,
                preferencesRepository.getByUserId(userId));

        // Преобразуем обратно в ContentItem
        return ContentRecommendationEngine.convertToContentItems(sortedEntities);
    }
}