package com.draker.swipetime.utils;

import android.util.Log;

import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.database.entities.UserPreferencesEntity;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.repository.AnimeRepository;
import com.draker.swipetime.repository.BookRepository;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.GameRepository;
import com.draker.swipetime.repository.MovieRepository;
import com.draker.swipetime.repository.TVShowRepository;
import com.draker.swipetime.repository.UserPreferencesRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс для интеграции фильтров с системой отображения карточек
 */
public class CardFilterIntegrator {

    private static final String TAG = "CardFilterIntegrator";

    /**
     * Получает отфильтрованный список элементов для отображения в CardStack
     * 
     * @param category категория контента
     * @param userId ID пользователя
     * @param movieRepository репозиторий фильмов
     * @param tvShowRepository репозиторий сериалов
     * @param gameRepository репозиторий игр
     * @param bookRepository репозиторий книг
     * @param animeRepository репозиторий аниме
     * @param contentRepository репозиторий общего контента
     * @param preferencesRepository репозиторий предпочтений пользователя
     * @return отфильтрованный список элементов
     */
    public static List<ContentItem> getFilteredContentItems(
            String category,
            String userId,
            MovieRepository movieRepository,
            TVShowRepository tvShowRepository,
            GameRepository gameRepository,
            BookRepository bookRepository,
            AnimeRepository animeRepository,
            ContentRepository contentRepository,
            UserPreferencesRepository preferencesRepository) {
        
        // Получаем предпочтения пользователя
        UserPreferencesEntity preferences = preferencesRepository.getByUserId(userId);
        
        // Получаем исходный список элементов в зависимости от категории
        List<ContentEntity> contentEntities = new ArrayList<>();
        
        switch (category) {
            case "Фильмы":
                List<MovieEntity> movies = movieRepository.getAll();
                contentEntities.addAll(movies);
                break;
                
            case "Сериалы":
                List<TVShowEntity> tvShows = tvShowRepository.getAll();
                contentEntities.addAll(tvShows);
                break;
                
            case "Игры":
                List<GameEntity> games = gameRepository.getAll();
                contentEntities.addAll(games);
                break;
                
            case "Книги":
                List<BookEntity> books = bookRepository.getAll();
                contentEntities.addAll(books);
                break;
                
            case "Аниме":
                List<AnimeEntity> animes = animeRepository.getAll();
                contentEntities.addAll(animes);
                break;
                
            default:
                // Для остальных категорий используем общую таблицу контента
                contentEntities = contentRepository.getByCategory(category);
                break;
        }
        
        Log.d(TAG, "Загружено элементов категории " + category + ": " + contentEntities.size());
        
        // Если есть предпочтения, применяем фильтрацию
        List<ContentEntity> filteredEntities;
        if (preferences != null && hasActiveFilters(preferences)) {
            filteredEntities = ContentFilterHelper.filterContent(contentEntities, preferences);
            Log.d(TAG, "После фильтрации осталось: " + filteredEntities.size());
        } else {
            // Если фильтров нет, используем весь список
            filteredEntities = contentEntities;
        }
        
        // Преобразуем отфильтрованные сущности в ContentItem
        List<ContentItem> resultItems = new ArrayList<>();
        for (ContentEntity entity : filteredEntities) {
            resultItems.add(new ContentItem(
                    entity.getId(),
                    entity.getTitle(),
                    entity.getDescription(),
                    entity.getImageUrl(),
                    entity.getCategory()
            ));
        }
        
        // Применяем сортировку по релевантности с помощью рекомендательного движка
        resultItems = RecommendationEngine.generateRecommendations(resultItems, preferences, filteredEntities);
        Log.d(TAG, "Сформированы рекомендации для пользователя " + userId);
        
        return resultItems;
    }
    
    /**
     * Проверяет, есть ли активные фильтры у пользователя
     * 
     * @param preferences предпочтения пользователя
     * @return true, если есть хотя бы один активный фильтр
     */
    private static boolean hasActiveFilters(UserPreferencesEntity preferences) {
        return (preferences.getPreferredGenres() != null && !preferences.getPreferredGenres().isEmpty()) ||
               (preferences.getPreferredCountries() != null && !preferences.getPreferredCountries().isEmpty()) ||
               (preferences.getPreferredLanguages() != null && !preferences.getPreferredLanguages().isEmpty()) ||
               (preferences.getInterestsTags() != null && !preferences.getInterestsTags().isEmpty()) ||
               preferences.getMinDuration() > 0 ||
               preferences.getMaxDuration() < Integer.MAX_VALUE ||
               preferences.getMinYear() > 1900 ||
               preferences.getMaxYear() < 2025 ||
               preferences.isAdultContentEnabled();
    }
    
    /**
     * Генерирует рекомендации на основе предпочтений пользователя
     * 
     * @param userId ID пользователя
     * @param categoryName категория контента
     * @param allItems все доступные элементы
     * @param preferences предпочтения пользователя
     * @return отсортированный по релевантности список элементов
     */
    public static List<ContentItem> generateRecommendations(
            String userId,
            String categoryName,
            List<ContentItem> allItems,
            UserPreferencesEntity preferences) {
        
        if (allItems == null || allItems.isEmpty() || preferences == null) {
            return allItems;
        }
        
        // Создаем карту для хранения рейтинга релевантности каждого элемента
        Map<String, Double> relevanceMap = new HashMap<>();
        
        // Используем простой алгоритм для расчета релевантности каждого элемента
        // в соответствии с предпочтениями пользователя
        
        // TODO: В будущем заменить на более продвинутый алгоритм (коллаборативная фильтрация, ML и т.д.)
        
        for (ContentItem item : allItems) {
            // Базовая релевантность для каждого элемента
            double relevance = 1.0;
            
            // Здесь будет логика расчета релевантности...
            // Например, увеличение релевантности при совпадении жанров, снижение при несоответствии и т.д.
            
            relevanceMap.put(item.getId(), relevance);
        }
        
        // Сортируем элементы по релевантности (от наиболее до наименее релевантных)
        allItems.sort((item1, item2) -> {
            Double rel1 = relevanceMap.getOrDefault(item1.getId(), 0.0);
            Double rel2 = relevanceMap.getOrDefault(item2.getId(), 0.0);
            return Double.compare(rel2, rel1); // Для сортировки по убыванию
        });
        
        return allItems;
    }
}