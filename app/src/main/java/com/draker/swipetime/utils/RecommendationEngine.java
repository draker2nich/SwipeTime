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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Движок рекомендаций для предложения контента на основе предпочтений пользователя
 */
public class RecommendationEngine {

    private static final String TAG = "RecommendationEngine";

    /**
     * Составляет рекомендации на основе предпочтений пользователя
     * 
     * @param contentItems список элементов контента
     * @param preferences предпочтения пользователя
     * @return отсортированный список с самыми релевантными элементами в начале
     */
    public static List<ContentItem> generateRecommendations(
            List<ContentItem> contentItems,
            UserPreferencesEntity preferences,
            List<ContentEntity> contentEntities) {
        
        if (contentItems == null || contentItems.isEmpty() || preferences == null) {
            return contentItems;
        }
        
        // Создаем карту элементов для быстрого поиска
        Map<String, ContentEntity> entityMap = new HashMap<>();
        for (ContentEntity entity : contentEntities) {
            entityMap.put(entity.getId(), entity);
        }
        
        // Извлекаем предпочтения пользователя
        List<String> preferredGenres = parseJsonArray(preferences.getPreferredGenres());
        List<String> preferredCountries = parseJsonArray(preferences.getPreferredCountries());
        List<String> preferredLanguages = parseJsonArray(preferences.getPreferredLanguages());
        List<String> interestsTags = parseJsonArray(preferences.getInterestsTags());
        
        // Карта для хранения рейтинга релевантности
        Map<String, Double> relevanceScores = new HashMap<>();
        
        for (ContentItem item : contentItems) {
            // Базовая релевантность
            double relevance = 1.0;
            
            // Получаем соответствующий Entity для этого элемента
            ContentEntity entity = entityMap.get(item.getId());
            if (entity == null) {
                relevanceScores.put(item.getId(), relevance);
                continue;
            }
            
            // Увеличиваем релевантность в зависимости от совпадения с предпочтениями
            
            // Оценка по жанрам
            if (!preferredGenres.isEmpty()) {
                String genres = getGenresForEntity(entity);
                if (genres != null && !genres.isEmpty()) {
                    // Считаем количество совпадений жанров
                    int matches = countMatches(genres, preferredGenres);
                    // Увеличиваем релевантность за каждое совпадение
                    relevance += matches * 0.5;
                }
            }
            
            // Оценка по стране
            if (!preferredCountries.isEmpty() && entity instanceof MovieEntity) {
                // Для фильмов стран может не быть в базе, поэтому используем заглушку
                // В реальном приложении здесь будет проверка по странам производства
                relevance += 0.2;
            }
            
            // Оценка по языку
            if (!preferredLanguages.isEmpty()) {
                // В реальном приложении здесь будет проверка по языкам
                relevance += 0.2;
            }
            
            // Оценка по тегам интересов
            if (!interestsTags.isEmpty()) {
                // В реальном приложении здесь будет проверка по тегам
                relevance += 0.2;
            }
            
            // Дополнительные факторы релевантности
            
            // 1. Предпочтение более новым произведениям
            int year = getYearForEntity(entity);
            if (year > 0) {
                // Чем новее, тем выше релевантность
                int currentYear = 2025;
                double yearFactor = Math.min(1.0, Math.max(0.0, (year - 1900) / (double)(currentYear - 1900)));
                relevance += yearFactor * 0.3;
            }
            
            // 2. Учет рейтинга
            float rating = entity.getRating();
            if (rating > 0) {
                // Увеличиваем релевантность на основе рейтинга (от 0 до 10)
                relevance += (rating / 10.0) * 0.5;
            }
            
            // Сохраняем итоговую релевантность
            relevanceScores.put(item.getId(), relevance);
        }
        
        // Сортируем элементы по релевантности
        List<ContentItem> sortedItems = new ArrayList<>(contentItems);
        sortedItems.sort((item1, item2) -> {
            Double rel1 = relevanceScores.getOrDefault(item1.getId(), 0.0);
            Double rel2 = relevanceScores.getOrDefault(item2.getId(), 0.0);
            return Double.compare(rel2, rel1); // Для сортировки по убыванию
        });
        
        return sortedItems;
    }
    
    /**
     * Извлекает жанры из сущности в зависимости от ее типа
     * 
     * @param entity сущность контента
     * @return строка с жанрами или null
     */
    private static String getGenresForEntity(ContentEntity entity) {
        if (entity instanceof MovieEntity) {
            return ((MovieEntity) entity).getGenres();
        } else if (entity instanceof TVShowEntity) {
            return ((TVShowEntity) entity).getGenres();
        } else if (entity instanceof GameEntity) {
            return ((GameEntity) entity).getGenres();
        } else if (entity instanceof BookEntity) {
            return ((BookEntity) entity).getGenres();
        } else if (entity instanceof AnimeEntity) {
            return ((AnimeEntity) entity).getGenres();
        }
        
        return null;
    }
    
    /**
     * Извлекает год выпуска из сущности в зависимости от ее типа
     * 
     * @param entity сущность контента
     * @return год выпуска или 0, если не найден
     */
    private static int getYearForEntity(ContentEntity entity) {
        if (entity instanceof MovieEntity) {
            return ((MovieEntity) entity).getReleaseYear();
        } else if (entity instanceof TVShowEntity) {
            return ((TVShowEntity) entity).getStartYear();
        } else if (entity instanceof GameEntity) {
            return ((GameEntity) entity).getReleaseYear();
        } else if (entity instanceof BookEntity) {
            return ((BookEntity) entity).getPublishYear();
        } else if (entity instanceof AnimeEntity) {
            return ((AnimeEntity) entity).getReleaseYear();
        }
        
        return 0;
    }
    
    /**
     * Считает количество совпадений между списком жанров и предпочтениями
     * 
     * @param genresString строка с жанрами, разделенными запятыми
     * @param preferredGenres список предпочитаемых жанров
     * @return количество совпадений
     */
    private static int countMatches(String genresString, List<String> preferredGenres) {
        if (genresString == null || genresString.isEmpty() || preferredGenres.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        String[] genres = genresString.split(",");
        
        for (String genre : genres) {
            String trimmed = genre.trim();
            if (preferredGenres.contains(trimmed)) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Парсит JSON строку в список строк
     * 
     * @param jsonString JSON строка
     * @return список строк или пустой список в случае ошибки
     */
    private static List<String> parseJsonArray(String jsonString) {
        List<String> result = new ArrayList<>();
        
        if (jsonString == null || jsonString.isEmpty()) {
            return result;
        }
        
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                result.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON array: " + e.getMessage());
        }
        
        return result;
    }
}