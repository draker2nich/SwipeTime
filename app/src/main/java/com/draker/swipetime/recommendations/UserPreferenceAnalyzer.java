package com.draker.swipetime.recommendations;

import android.util.Log;

import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.database.entities.UserPreferencesEntity;
import com.draker.swipetime.database.entities.UserStatsEntity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Класс для анализа предпочтений пользователя на основе его взаимодействий с контентом
 */
public class UserPreferenceAnalyzer {
    private static final String TAG = "UserPreferenceAnalyzer";

    /**
     * Анализирует лайкнутый контент и обновляет предпочтения пользователя
     *
     * @param userId ID пользователя
     * @param likedContent список лайкнутого контента
     * @param userPreferences текущие предпочтения пользователя
     * @return обновленные предпочтения пользователя
     */
    public static UserPreferencesEntity analyzeUserPreferences(
            String userId,
            List<ContentEntity> likedContent,
            UserPreferencesEntity userPreferences) {
        
        if (likedContent == null || likedContent.isEmpty() || userPreferences == null) {
            Log.d(TAG, "Недостаточно данных для анализа предпочтений пользователя");
            return userPreferences;
        }

        Log.d(TAG, "Анализ предпочтений для пользователя: " + userId);
        Log.d(TAG, "Количество лайкнутых элементов: " + likedContent.size());

        // Анализ жанров
        Map<String, Integer> genreCount = new HashMap<>();
        
        // Анализ годов выпуска
        int minYear = Integer.MAX_VALUE;
        int maxYear = Integer.MIN_VALUE;
        int totalYearsCount = 0;
        int yearSum = 0;
        
        // Анализ длительности (для фильмов, сериалов)
        int minDuration = Integer.MAX_VALUE;
        int maxDuration = Integer.MIN_VALUE;
        int totalDurationsCount = 0;
        int durationSum = 0;

        // Анализ тегов и другой информации
        Set<String> interestTags = new HashSet<>();

        // Перебираем лайкнутый контент и собираем статистику
        for (ContentEntity content : likedContent) {
            // Анализ жанров
            analyzeGenres(content, genreCount);
            
            // Анализ годов
            int contentYear = getContentYear(content);
            if (contentYear > 0) {
                if (contentYear < minYear) minYear = contentYear;
                if (contentYear > maxYear) maxYear = contentYear;
                yearSum += contentYear;
                totalYearsCount++;
            }
            
            // Анализ длительности
            int contentDuration = getContentDuration(content);
            if (contentDuration > 0) {
                if (contentDuration < minDuration) minDuration = contentDuration;
                if (contentDuration > maxDuration) maxDuration = contentDuration;
                durationSum += contentDuration;
                totalDurationsCount++;
            }
            
            // Анализ тегов
            extractInterestTags(content, interestTags);
        }

        // Формируем обновленные предпочтения
        
        // Жанры - берем топ-5 самых частых
        List<String> topGenres = getTopGenres(genreCount, 5);
        try {
            if (!topGenres.isEmpty()) {
                JSONArray genresJson = new JSONArray();
                for (String genre : topGenres) {
                    genresJson.put(genre);
                }
                userPreferences.setPreferredGenres(genresJson.toString());
                Log.d(TAG, "Обновлены предпочитаемые жанры: " + genresJson.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при сохранении жанров: " + e.getMessage());
        }
        
        // Устанавливаем диапазон годов
        if (totalYearsCount > 0) {
            // Расширяем диапазон на 5 лет в обе стороны от мин/макс значений
            userPreferences.setMinYear(Math.max(1900, minYear - 5));
            userPreferences.setMaxYear(Math.min(2025, maxYear + 5));
            Log.d(TAG, "Установлен диапазон годов: " + userPreferences.getMinYear() + " - " + userPreferences.getMaxYear());
        }
        
        // Устанавливаем диапазон длительности
        if (totalDurationsCount > 0) {
            // Расширяем диапазон на 30 минут в обе стороны от мин/макс значений
            userPreferences.setMinDuration(Math.max(0, minDuration - 30));
            userPreferences.setMaxDuration(maxDuration + 30);
            Log.d(TAG, "Установлен диапазон длительности: " + userPreferences.getMinDuration() + " - " + userPreferences.getMaxDuration() + " минут");
        }
        
        // Сохраняем теги интересов
        try {
            if (!interestTags.isEmpty()) {
                JSONArray tagsJson = new JSONArray();
                for (String tag : interestTags) {
                    tagsJson.put(tag);
                }
                userPreferences.setInterestsTags(tagsJson.toString());
                Log.d(TAG, "Обновлены теги интересов: " + tagsJson.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при сохранении тегов: " + e.getMessage());
        }

        return userPreferences;
    }

    /**
     * Анализирует жанры контента и обновляет счетчик жанров
     *
     * @param content элемент контента
     * @param genreCount карта для подсчета частоты жанров
     */
    private static void analyzeGenres(ContentEntity content, Map<String, Integer> genreCount) {
        String genresString = getContentGenres(content);
        if (genresString != null && !genresString.isEmpty()) {
            String[] genres = genresString.split(",");
            for (String genre : genres) {
                String trimmedGenre = genre.trim();
                if (!trimmedGenre.isEmpty()) {
                    genreCount.put(trimmedGenre, genreCount.getOrDefault(trimmedGenre, 0) + 1);
                }
            }
        }
    }

    /**
     * Извлекает теги интересов из контента
     *
     * @param content элемент контента
     * @param interestTags множество для хранения уникальных тегов
     */
    private static void extractInterestTags(ContentEntity content, Set<String> interestTags) {
        // В реальном приложении здесь был бы более сложный алгоритм
        // для извлечения и анализа тегов из различных полей контента
        
        // Пока просто собираем некоторые базовые теги из описания и названия
        String title = content.getTitle();
        String description = content.getDescription();
        
        if (title != null) {
            // Пример: добавление ключевых слов из названия
            if (title.toLowerCase().contains("война")) interestTags.add("война");
            if (title.toLowerCase().contains("любовь")) interestTags.add("романтика");
            if (title.toLowerCase().contains("детектив")) interestTags.add("криминал");
            // и т.д.
        }
        
        if (description != null) {
            // Более сложный анализ описания может быть здесь
            // Например, поиск ключевых слов или использование NLP
        }
    }

    /**
     * Получает топ-N самых частых жанров
     *
     * @param genreCount карта с подсчетом жанров
     * @param n количество жанров для возврата
     * @return список топ-N жанров
     */
    private static List<String> getTopGenres(Map<String, Integer> genreCount, int n) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(genreCount.entrySet());
        entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        List<String> topGenres = new ArrayList<>();
        for (int i = 0; i < Math.min(n, entries.size()); i++) {
            topGenres.add(entries.get(i).getKey());
        }
        
        return topGenres;
    }

    /**
     * Получает год выпуска контента в зависимости от его типа
     *
     * @param content элемент контента
     * @return год выпуска или 0, если не удалось определить
     */
    private static int getContentYear(ContentEntity content) {
        // Это метод-заглушка, в реальном приложении требуется проверка типа объекта
        // и извлечение года из соответствующего поля
        
        // Пример реализации:
        return 2020; // Возвращаем фиксированное значение для тестирования
    }

    /**
     * Получает длительность контента в зависимости от его типа
     *
     * @param content элемент контента
     * @return длительность в минутах или 0, если не применимо
     */
    private static int getContentDuration(ContentEntity content) {
        // Это метод-заглушка, в реальном приложении требуется проверка типа объекта
        // и извлечение длительности из соответствующего поля
        
        // Пример реализации:
        return 120; // Возвращаем фиксированное значение для тестирования
    }

    /**
     * Получает строку с жанрами контента в зависимости от его типа
     *
     * @param content элемент контента
     * @return строка с жанрами или null, если не удалось определить
     */
    private static String getContentGenres(ContentEntity content) {
        // Это метод-заглушка, в реальном приложении требуется проверка типа объекта
        // и извлечение жанров из соответствующего поля
        
        // Пример реализации:
        return "Боевик, Фантастика, Триллер"; // Возвращаем фиксированное значение для тестирования
    }
}
