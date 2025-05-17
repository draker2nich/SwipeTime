package com.draker.swipetime.recommendations;

import android.app.Application;
import android.util.Log;

import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.UserPreferencesEntity;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.UserPreferencesRepository;
import com.draker.swipetime.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Основной менеджер рекомендаций, объединяющий различные алгоритмы
 */
public class RecommendationManager {
    private static final String TAG = "RecommendationManager";

    // Веса для различных рекомендательных алгоритмов
    private static final double CONTENT_BASED_WEIGHT = 0.6;
    private static final double COLLABORATIVE_WEIGHT = 0.4;

    // Максимальное количество рекомендаций от каждого алгоритма
    private static final int MAX_CONTENT_BASED_RECOMMENDATIONS = 30;
    private static final int MAX_COLLABORATIVE_RECOMMENDATIONS = 20;

    private final Application application;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;

    // Реализации алгоритмов
    private final CollaborativeFilteringStrategy collaborativeFilter;

    /**
     * Создает менеджер рекомендаций
     * @param application контекст приложения
     */
    public RecommendationManager(Application application) {
        this.application = application;
        this.contentRepository = new ContentRepository(application);
        this.userRepository = new UserRepository(application);
        this.preferencesRepository = new UserPreferencesRepository(application);

        // Инициализация алгоритмов
        this.collaborativeFilter = new UserBasedCollaborativeFilter(application);
    }

    /**
     * Генерирует рекомендации для пользователя, используя комбинацию алгоритмов
     *
     * @param userId ID пользователя
     * @param category категория контента (может быть null для всех категорий)
     * @param limit максимальное количество рекомендаций
     * @return список рекомендуемых элементов контента
     */
    public List<ContentItem> getRecommendations(String userId, String category, int limit) {
        Log.d(TAG, "Получение рекомендаций для пользователя: " + userId +
                ", категория: " + (category != null ? category : "все") +
                ", лимит: " + limit);

        // Получаем предпочтения пользователя
        UserPreferencesEntity preferences = preferencesRepository.getByUserId(userId);

        // Получаем весь доступный контент
        List<ContentEntity> allContent;
        if (category != null) {
            allContent = contentRepository.getByCategory(category);
        } else {
            allContent = contentRepository.getAll();
        }

        // Получаем лайкнутый контент пользователя
        List<ContentEntity> likedContent = contentRepository.getLikedContentForUser(userId);

        // Шаг 1: Получаем рекомендации на основе контента (Content-based Filtering)
        List<ContentEntity> contentBasedRecommendations =
                ContentRecommendationEngine.generateRecommendations(
                        allContent, likedContent, preferences);

        // Ограничиваем количество
        if (contentBasedRecommendations.size() > MAX_CONTENT_BASED_RECOMMENDATIONS) {
            contentBasedRecommendations = contentBasedRecommendations.subList(
                    0, MAX_CONTENT_BASED_RECOMMENDATIONS);
        }

        // Шаг 2: Получаем рекомендации на основе коллаборативной фильтрации
        List<String> collaborativeRecommendationsIds =
                collaborativeFilter.getRecommendations(userId, MAX_COLLABORATIVE_RECOMMENDATIONS);

        // Преобразуем ID в реальные объекты контента
        List<ContentEntity> collaborativeRecommendations = new ArrayList<>();
        for (String contentId : collaborativeRecommendationsIds) {
            ContentEntity content = contentRepository.getById(contentId);
            if (content != null) {
                // Фильтруем по категории, если указана
                if (category == null || content.getCategory().equals(category)) {
                    collaborativeRecommendations.add(content);
                }
            }
        }

        // Шаг 3: Объединяем результаты с учетом весов
        List<ContentEntity> combinedRecommendations = combineRecommendations(
                contentBasedRecommendations,
                collaborativeRecommendations,
                limit);

        // Шаг 4: Преобразуем в ContentItem для отображения в UI
        List<ContentItem> recommendationItems =
                ContentRecommendationEngine.convertToContentItems(combinedRecommendations);

        Log.d(TAG, "Сгенерировано рекомендаций: " + recommendationItems.size());

        return recommendationItems;
    }

    /**
     * Объединяет рекомендации от разных алгоритмов с учетом весов
     */
    private List<ContentEntity> combineRecommendations(
            List<ContentEntity> contentBasedRecs,
            List<ContentEntity> collaborativeRecs,
            int limit) {

        List<ContentEntity> result = new ArrayList<>();

        // Рассчитываем, сколько элементов брать из каждого источника
        int contentBasedCount = (int) Math.ceil(limit * CONTENT_BASED_WEIGHT);
        int collaborativeCount = (int) Math.ceil(limit * COLLABORATIVE_WEIGHT);

        // Убедимся, что не выходим за пределы
        contentBasedCount = Math.min(contentBasedCount, contentBasedRecs.size());
        collaborativeCount = Math.min(collaborativeCount, collaborativeRecs.size());

        // Добавляем рекомендации на основе контента
        for (int i = 0; i < contentBasedCount; i++) {
            ContentEntity entity = contentBasedRecs.get(i);
            // Проверяем, не добавлен ли уже этот элемент (избегаем дубликатов)
            if (!containsContent(result, entity.getId())) {
                result.add(entity);
            }
        }

        // Добавляем рекомендации на основе коллаборативной фильтрации
        for (int i = 0; i < collaborativeCount; i++) {
            ContentEntity entity = collaborativeRecs.get(i);
            // Проверяем, не добавлен ли уже этот элемент (избегаем дубликатов)
            if (!containsContent(result, entity.getId())) {
                result.add(entity);
            }

            // Если достигли лимита, выходим
            if (result.size() >= limit) {
                break;
            }
        }

        // Если не набрали нужное количество, добавляем еще из контент-рекомендаций
        if (result.size() < limit) {
            // Начинаем с позиции после уже добавленных
            for (int i = contentBasedCount; i < contentBasedRecs.size(); i++) {
                ContentEntity entity = contentBasedRecs.get(i);
                // Проверяем, не добавлен ли уже этот элемент (избегаем дубликатов)
                if (!containsContent(result, entity.getId())) {
                    result.add(entity);
                }

                // Если достигли лимита, выходим
                if (result.size() >= limit) {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Проверяет, содержится ли уже контент с данным ID в списке
     */
    private boolean containsContent(List<ContentEntity> contentList, String contentId) {
        for (ContentEntity entity : contentList) {
            if (entity.getId().equals(contentId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Анализирует предпочтения пользователя и обновляет их в базе данных
     * @param userId ID пользователя
     */
    public void analyzeAndUpdateUserPreferences(String userId) {
        // Получаем текущие предпочтения
        UserPreferencesEntity preferences = preferencesRepository.getByUserId(userId);

        // Получаем лайкнутый контент
        List<ContentEntity> likedContent = contentRepository.getLikedContentForUser(userId);

        // Анализируем и обновляем предпочтения
        UserPreferencesEntity updatedPreferences =
                UserPreferenceAnalyzer.analyzeUserPreferences(userId, likedContent, preferences);

        // Сохраняем обновленные предпочтения
        preferencesRepository.update(updatedPreferences);

        Log.d(TAG, "Предпочтения пользователя " + userId + " были обновлены");
    }

    /**
     * Очищает внутренние кэши рекомендаций
     */
    public void clearCaches() {
        // В будущем здесь можно добавить очистку внутренних кэшей
        // collaborativeFilter.clearCache();
    }
}