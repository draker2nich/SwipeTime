package com.draker.swipetime.recommendations;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Движок для генерации рекомендаций контента пользователю
 */
public class ContentRecommendationEngine {
    private static final String TAG = "ContentRecommendEngine";

    // Веса различных факторов для расчета релевантности
    private static final double GENRE_MATCH_WEIGHT = 3.0;
    private static final double YEAR_MATCH_WEIGHT = 1.5;
    private static final double DURATION_MATCH_WEIGHT = 1.0;
    private static final double TAG_MATCH_WEIGHT = 2.0;
    private static final double RATING_WEIGHT = 2.0;
    private static final double RECENCY_WEIGHT = 1.0;

    // Минимальный порог релевантности для рекомендаций
    private static final double RELEVANCE_THRESHOLD = 0.5;

    /**
     * Генерирует рекомендации на основе предпочтений пользователя
     *
     * @param allContent весь доступный контент
     * @param likedContent лайкнутый пользователем контент
     * @param preferences предпочтения пользователя
     * @return отсортированный по релевантности список контента
     */
    public static List<ContentEntity> generateRecommendations(
            List<ContentEntity> allContent,
            List<ContentEntity> likedContent,
            UserPreferencesEntity preferences) {

        if (allContent == null || allContent.isEmpty()) {
            Log.w(TAG, "Нет доступного контента для рекомендаций");
            return new ArrayList<>();
        }

        Log.d(TAG, "Генерация рекомендаций для " + allContent.size() + " элементов контента");

        // Извлекаем предпочтения для использования в расчетах
        List<String> preferredGenres = parseJsonPreference(preferences.getPreferredGenres());
        List<String> interestTags = parseJsonPreference(preferences.getInterestsTags());
        int minYear = preferences.getMinYear();
        int maxYear = preferences.getMaxYear();
        int minDuration = preferences.getMinDuration();
        int maxDuration = preferences.getMaxDuration();

        // Создаем множество ID уже лайкнутого контента для быстрой проверки
        Map<String, Boolean> likedContentIds = new HashMap<>();
        if (likedContent != null) {
            for (ContentEntity liked : likedContent) {
                likedContentIds.put(liked.getId(), true);
            }
        }

        // Карта для хранения релевантности каждого элемента
        Map<String, Double> relevanceScores = new HashMap<>();
        Map<String, Double> categoryScores = new HashMap<>();

        // Вычисляем релевантность каждого элемента
        for (ContentEntity content : allContent) {
            // Пропускаем уже лайкнутый контент
            if (likedContentIds.containsKey(content.getId())) {
                continue;
            }

            // Базовая релевантность
            double relevance = calculateBaseRelevance(content);

            // Релевантность по жанрам
            relevance += calculateGenreRelevance(content, preferredGenres) * GENRE_MATCH_WEIGHT;

            // Релевантность по году выпуска
            relevance += calculateYearRelevance(content, minYear, maxYear) * YEAR_MATCH_WEIGHT;

            // Релевантность по длительности
            relevance += calculateDurationRelevance(content, minDuration, maxDuration) * DURATION_MATCH_WEIGHT;

            // Релевантность по тегам
            relevance += calculateTagRelevance(content, interestTags) * TAG_MATCH_WEIGHT;

            // Релевантность по рейтингу
            relevance += calculateRatingRelevance(content) * RATING_WEIGHT;

            // Релевантность по новизне
            relevance += calculateRecencyRelevance(content) * RECENCY_WEIGHT;

            // Сохраняем итоговый балл релевантности
            relevanceScores.put(content.getId(), relevance);

            // Также сохраняем категорию для разнообразия рекомендаций
            String category = content.getCategory();
            if (!categoryScores.containsKey(category)) {
                categoryScores.put(category, 0.0);
            }
            categoryScores.put(category, categoryScores.get(category) + relevance);
        }

        // Применяем разнообразие (diversity) к результатам
        applyDiversityFactor(allContent, relevanceScores, categoryScores);

        // Отфильтровываем контент с низкой релевантностью
        List<ContentEntity> filteredContent = new ArrayList<>();
        for (ContentEntity content : allContent) {
            Double relevance = relevanceScores.get(content.getId());
            if (relevance != null && relevance >= RELEVANCE_THRESHOLD && !likedContentIds.containsKey(content.getId())) {
                filteredContent.add(content);
            }
        }

        // Сортируем по релевантности
        Collections.sort(filteredContent, new Comparator<ContentEntity>() {
            @Override
            public int compare(ContentEntity c1, ContentEntity c2) {
                Double r1 = relevanceScores.getOrDefault(c1.getId(), 0.0);
                Double r2 = relevanceScores.getOrDefault(c2.getId(), 0.0);
                return Double.compare(r2, r1); // Сортировка по убыванию
            }
        });

        Log.d(TAG, "Сгенерировано " + filteredContent.size() + " рекомендаций");
        return filteredContent;
    }

    /**
     * Преобразует список ContentEntity в ContentItem для отображения в UI
     */
    public static List<ContentItem> convertToContentItems(List<ContentEntity> entities) {
        List<ContentItem> items = new ArrayList<>();
        for (ContentEntity entity : entities) {
            ContentItem item = new ContentItem(
                    entity.getId(),
                    entity.getTitle(),
                    entity.getDescription(),
                    entity.getImageUrl(),
                    entity.getCategory()
            );
            // Копируем дополнительные атрибуты
            item.setLiked(entity.isLiked());
            item.setWatched(entity.isWatched());
            item.setRating(entity.getRating());

            items.add(item);
        }
        return items;
    }

    /**
     * Применяет фактор разнообразия к рекомендациям,
     * чтобы избежать однообразных результатов из одной категории
     */
    private static void applyDiversityFactor(List<ContentEntity> content,
                                             Map<String, Double> relevanceScores,
                                             Map<String, Double> categoryScores) {
        // Для категорий с слишком высоким общим баллом, немного снижаем релевантность
        double maxCategoryScore = 0.0;
        for (double score : categoryScores.values()) {
            maxCategoryScore = Math.max(maxCategoryScore, score);
        }

        if (maxCategoryScore > 0) {
            for (ContentEntity item : content) {
                String id = item.getId();
                String category = item.getCategory();
                Double currentRelevance = relevanceScores.get(id);
                Double categoryScore = categoryScores.get(category);

                if (currentRelevance != null && categoryScore != null && categoryScore > 0) {
                    // Уменьшаем релевантность для доминирующих категорий
                    double diversityFactor = 1.0 - 0.2 * (categoryScore / maxCategoryScore);
                    relevanceScores.put(id, currentRelevance * diversityFactor);
                }
            }
        }

        // Добавляем небольшой элемент случайности для разнообразия (exploration)
        Random random = new Random();
        for (String id : relevanceScores.keySet()) {
            double currentScore = relevanceScores.get(id);
            // Добавляем до ±10% случайного шума
            double noise = (random.nextDouble() * 0.2) - 0.1;
            relevanceScores.put(id, currentScore * (1 + noise));
        }
    }

    /**
     * Вычисляет базовую релевантность для элемента
     */
    private static double calculateBaseRelevance(ContentEntity content) {
        // Базовая релевантность одинакова для всех элементов
        return 1.0;
    }

    /**
     * Вычисляет релевантность на основе соответствия жанров
     */
    private static double calculateGenreRelevance(ContentEntity content, List<String> preferredGenres) {
        if (preferredGenres.isEmpty()) {
            return 0.5; // Нейтральное значение, если нет предпочтений
        }

        String contentGenres = getContentGenres(content);
        if (contentGenres == null || contentGenres.isEmpty()) {
            return 0.0;
        }

        String[] genres = contentGenres.toLowerCase().split(",");
        int matchCount = 0;

        for (String genre : genres) {
            String trimmedGenre = genre.trim();
            if (preferredGenres.contains(trimmedGenre)) {
                matchCount++;
            }
        }

        // Нормализуем результат от 0 до 1
        return matchCount > 0 ? Math.min(1.0, matchCount / 3.0) : 0.0;
    }

    /**
     * Вычисляет релевантность на основе соответствия года выпуска
     */
    private static double calculateYearRelevance(ContentEntity content, int minYear, int maxYear) {
        int year = getContentYear(content);
        if (year <= 0) {
            return 0.5; // Нейтральное значение, если год неизвестен
        }

        // Если год в предпочитаемом диапазоне, максимальная релевантность
        if (year >= minYear && year <= maxYear) {
            return 1.0;
        }

        // Иначе, релевантность понижается чем дальше от диапазона
        int distance = Math.min(Math.abs(year - minYear), Math.abs(year - maxYear));
        return Math.max(0.0, 1.0 - (distance / 10.0)); // За каждые 10 лет от диапазона теряем 100% релевантности
    }

    /**
     * Вычисляет релевантность на основе соответствия длительности
     */
    private static double calculateDurationRelevance(ContentEntity content, int minDuration, int maxDuration) {
        int duration = getContentDuration(content);
        if (duration <= 0) {
            return 0.5; // Нейтральное значение, если длительность неизвестна
        }

        // Если длительность в предпочитаемом диапазоне, максимальная релевантность
        if (duration >= minDuration && duration <= maxDuration) {
            return 1.0;
        }

        // Иначе, релевантность понижается чем дальше от диапазона
        int distance = Math.min(Math.abs(duration - minDuration), Math.abs(duration - maxDuration));
        return Math.max(0.0, 1.0 - (distance / 60.0)); // За каждые 60 минут от диапазона теряем 100% релевантности
    }

    /**
     * Вычисляет релевантность на основе соответствия тегов
     */
    private static double calculateTagRelevance(ContentEntity content, List<String> interestTags) {
        if (interestTags.isEmpty()) {
            return 0.5; // Нейтральное значение, если нет предпочтений
        }

        // В реальном приложении здесь был бы более сложный анализ
        // контента на соответствие тегам интересов

        // Для примера, просто проверяем наличие тегов в названии и описании
        String title = content.getTitle().toLowerCase();
        String description = content.getDescription() != null ? content.getDescription().toLowerCase() : "";

        int matchCount = 0;
        for (String tag : interestTags) {
            tag = tag.toLowerCase();
            if (title.contains(tag) || description.contains(tag)) {
                matchCount++;
            }
        }

        // Нормализуем результат от 0 до 1
        return matchCount > 0 ? Math.min(1.0, matchCount / 2.0) : 0.0;
    }

    /**
     * Вычисляет релевантность на основе рейтинга
     */
    private static double calculateRatingRelevance(ContentEntity content) {
        float rating = content.getRating();
        if (rating <= 0) {
            return 0.5; // Нейтральное значение, если рейтинг неизвестен
        }

        // Нормализуем от 0 до 1 (рейтинг от 0 до 10)
        return Math.min(1.0, rating / 10.0);
    }

    /**
     * Вычисляет релевантность на основе новизны контента
     */
    private static double calculateRecencyRelevance(ContentEntity content) {
        int year = getContentYear(content);
        if (year <= 0) {
            return 0.5; // Нейтральное значение, если год неизвестен
        }

        // Более новый контент получает более высокую релевантность
        int currentYear = 2025; // Текущий год
        int yearsAgo = currentYear - year;

        // Контент последних 3 лет получает максимальную релевантность
        if (yearsAgo <= 3) {
            return 1.0;
        }

        // За каждые 5 лет старше релевантность снижается на 0.2
        return Math.max(0.0, 1.0 - ((yearsAgo - 3) / 5.0 * 0.2));
    }

    /**
     * Извлекает год выпуска из контента в зависимости от его типа
     */
    private static int getContentYear(ContentEntity content) {
        // Проверяем тип контента и извлекаем год
        if (content instanceof MovieEntity) {
            return ((MovieEntity) content).getReleaseYear();
        } else if (content instanceof TVShowEntity) {
            return ((TVShowEntity) content).getStartYear();
        } else if (content instanceof GameEntity) {
            return ((GameEntity) content).getReleaseYear();
        } else if (content instanceof BookEntity) {
            return ((BookEntity) content).getPublishYear();
        } else if (content instanceof AnimeEntity) {
            return ((AnimeEntity) content).getReleaseYear();
        }

        // Для остальных типов возвращаем 0 (неизвестно)
        return 0;
    }

    /**
     * Извлекает длительность из контента в зависимости от его типа
     */
    private static int getContentDuration(ContentEntity content) {
        // Проверяем тип контента и извлекаем длительность
        if (content instanceof MovieEntity) {
            return ((MovieEntity) content).getDuration();
        } else if (content instanceof TVShowEntity) {
            // Для сериалов используем среднюю длительность эпизода или 0 если неизвестно
            return ((TVShowEntity) content).getEpisodeDuration();
        }

        // Для остальных типов возвращаем 0 (неприменимо)
        return 0;
    }

    /**
     * Извлекает жанры из контента в зависимости от его типа
     */
    private static String getContentGenres(ContentEntity content) {
        // Проверяем тип контента и извлекаем жанры
        if (content instanceof MovieEntity) {
            return ((MovieEntity) content).getGenres();
        } else if (content instanceof TVShowEntity) {
            return ((TVShowEntity) content).getGenres();
        } else if (content instanceof GameEntity) {
            return ((GameEntity) content).getGenres();
        } else if (content instanceof BookEntity) {
            return ((BookEntity) content).getGenres();
        } else if (content instanceof AnimeEntity) {
            return ((AnimeEntity) content).getGenres();
        }

        // Для остальных типов возвращаем null (неизвестно)
        return null;
    }

    /**
     * Парсит JSON-строку предпочтений в список строк
     */
    private static List<String> parseJsonPreference(String jsonString) {
        List<String> result = new ArrayList<>();

        if (jsonString == null || jsonString.isEmpty()) {
            return result;
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                result.add(jsonArray.getString(i).toLowerCase());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Ошибка при парсинге JSON: " + e.getMessage());
        }

        return result;
    }
}