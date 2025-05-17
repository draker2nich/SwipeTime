package com.draker.swipetime.utils;

import android.app.Application;
import android.util.Log;

import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.UserPreferencesEntity;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.recommendations.RecommendationService;
import com.draker.swipetime.repository.AnimeRepository;
import com.draker.swipetime.repository.BookRepository;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.GameRepository;
import com.draker.swipetime.repository.MovieRepository;
import com.draker.swipetime.repository.TVShowRepository;
import com.draker.swipetime.repository.UserPreferencesRepository;

import java.util.List;

/**
 * Улучшенная версия CardFilterIntegrator, использующая систему ИИ-рекомендаций
 */
public class CardFilterIntegratorV2 {
    private static final String TAG = "CardFilterIntegratorV2";

    /**
     * Получает отфильтрованный и отсортированный по релевантности список элементов для отображения в CardStack
     *
     * @param application контекст приложения
     * @param category категория контента
     * @param userId ID пользователя
     * @param movieRepository репозиторий фильмов
     * @param tvShowRepository репозиторий сериалов
     * @param gameRepository репозиторий игр
     * @param bookRepository репозиторий книг
     * @param animeRepository репозиторий аниме
     * @param contentRepository репозиторий общего контента
     * @param preferencesRepository репозиторий предпочтений пользователя
     * @return отфильтрованный и отсортированный список элементов
     */
    public static List<ContentItem> getFilteredAndRecommendedContentItems(
            Application application,
            String category,
            String userId,
            MovieRepository movieRepository,
            TVShowRepository tvShowRepository,
            GameRepository gameRepository,
            BookRepository bookRepository,
            AnimeRepository animeRepository,
            ContentRepository contentRepository,
            UserPreferencesRepository preferencesRepository) {

        // Сначала получаем отфильтрованный список элементов
        List<ContentItem> filteredItems = CardFilterIntegrator.getFilteredContentItems(
                category,
                userId,
                movieRepository,
                tvShowRepository,
                gameRepository,
                bookRepository,
                animeRepository,
                contentRepository,
                preferencesRepository
        );

        Log.d(TAG, "После фильтрации получено элементов: " + filteredItems.size());

        // Если список пуст, возвращаем его как есть
        if (filteredItems.isEmpty()) {
            return filteredItems;
        }

        // Получаем сервис рекомендаций
        RecommendationService recommendationService = RecommendationService.getInstance(application);

        // Сортируем по релевантности с помощью рекомендательной системы
        List<ContentItem> recommendedItems = recommendationService.sortByRelevance(application, filteredItems);

        Log.d(TAG, "После применения рекомендательной системы получено элементов: " + recommendedItems.size());

        return recommendedItems;
    }

    /**
     * Получает отфильтрованный и отсортированный по релевантности список элементов для отображения в CardStack
     *
     * @param application контекст приложения
     * @param category категория контента
     * @param userId ID пользователя
     * @param limit максимальное количество элементов
     * @return отфильтрованный и отсортированный список элементов
     */
    public static List<ContentItem> getRecommendedContentItems(
            Application application,
            String category,
            String userId,
            int limit) {

        // Используем сервис рекомендаций для получения рекомендаций
        RecommendationService recommendationService = RecommendationService.getInstance(application);
        List<ContentItem> recommendedItems = recommendationService.getRecommendationsForCurrentUser(application, category, limit);

        Log.d(TAG, "Получено " + recommendedItems.size() + " рекомендаций для пользователя " + userId);

        return recommendedItems;
    }

    /**
     * Обрабатывает событие свайпа для обновления рекомендательной системы
     *
     * @param application контекст приложения
     * @param contentId ID контента
     * @param liked true, если свайп был вправо (лайк)
     */
    public static void handleSwipeEvent(Application application, String contentId, boolean liked) {
        RecommendationService recommendationService = RecommendationService.getInstance(application);
        recommendationService.handleSwipeEvent(application, contentId, liked);
    }
}
