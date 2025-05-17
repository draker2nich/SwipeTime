package com.draker.swipetime.recommendations;

import android.app.Application;
import android.util.Log;

import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Реализация коллаборативной фильтрации на основе пользователей
 * (User-based Collaborative Filtering)
 */
public class UserBasedCollaborativeFilter implements CollaborativeFilteringStrategy {

    private static final String TAG = "UserBasedFilter";

    // Настройки алгоритма
    private static final int MIN_COMMON_ITEMS = 3; // Минимальное количество общих лайков для учета схожести
    private static final double SIMILARITY_THRESHOLD = 0.2; // Минимальный порог схожести
    private static final int NEAREST_NEIGHBORS_COUNT = 10; // Количество "соседей" для рекомендаций

    private UserRepository userRepository;
    private ContentRepository contentRepository;

    // Кэш для схожести пользователей (userIdA:userIdB -> similarity)
    private Map<String, Double> similarityCache = new HashMap<>();

    public UserBasedCollaborativeFilter(Application application) {
        this.userRepository = new UserRepository(application);
        this.contentRepository = new ContentRepository(application);
    }

    @Override
    public double calculateUserSimilarity(String userIdA, String userIdB) {
        // Проверяем кэш
        String cacheKey = userIdA + ":" + userIdB;
        String reverseCacheKey = userIdB + ":" + userIdA;

        if (similarityCache.containsKey(cacheKey)) {
            return similarityCache.get(cacheKey);
        }

        if (similarityCache.containsKey(reverseCacheKey)) {
            return similarityCache.get(reverseCacheKey);
        }

        // Получаем лайкнутый контент обоих пользователей
        List<ContentEntity> userALikes = contentRepository.getLikedContentForUser(userIdA);
        List<ContentEntity> userBLikes = contentRepository.getLikedContentForUser(userIdB);

        // Создаем множества ID для быстрой проверки пересечений
        Set<String> userALikeIds = new HashSet<>();
        for (ContentEntity content : userALikes) {
            userALikeIds.add(content.getId());
        }

        Set<String> userBLikeIds = new HashSet<>();
        for (ContentEntity content : userBLikes) {
            userBLikeIds.add(content.getId());
        }

        // Находим пересечение (общие лайки)
        Set<String> intersection = new HashSet<>(userALikeIds);
        intersection.retainAll(userBLikeIds);

        // Если общих лайков слишком мало, схожесть низкая
        if (intersection.size() < MIN_COMMON_ITEMS) {
            double lowSimilarity = intersection.size() > 0 ? 0.1 : 0.0;
            similarityCache.put(cacheKey, lowSimilarity);
            return lowSimilarity;
        }

        // Используем коэффициент Жаккара для оценки схожести
        // |A ∩ B| / |A ∪ B|
        Set<String> union = new HashSet<>(userALikeIds);
        union.addAll(userBLikeIds);

        double similarity = (double) intersection.size() / union.size();

        // Сохраняем результат в кэш
        similarityCache.put(cacheKey, similarity);

        return similarity;
    }

    @Override
    public double predictLikelihood(String userId, String contentId) {
        // Получаем всех пользователей системы
        List<UserEntity> allUsers = userRepository.getAllUsers();

        // Рассчитываем лайкнул ли пользователь уже этот контент
        ContentEntity content = contentRepository.getById(contentId);
        if (content != null && content.isLiked()) {
            return 1.0; // Уже лайкнуто
        }

        // Список схожих пользователей
        List<UserSimilarity> similarUsers = new ArrayList<>();

        // Находим всех похожих пользователей
        for (UserEntity otherUser : allUsers) {
            String otherUserId = otherUser.getId();

            // Пропускаем самого пользователя
            if (otherUserId.equals(userId)) {
                continue;
            }

            // Вычисляем схожесть между пользователями
            double similarity = calculateUserSimilarity(userId, otherUserId);

            // Если схожесть выше порога, добавляем в список
            if (similarity >= SIMILARITY_THRESHOLD) {
                similarUsers.add(new UserSimilarity(otherUserId, similarity));
            }
        }

        // Если нет похожих пользователей, возвращаем нейтральное значение
        if (similarUsers.isEmpty()) {
            return 0.5;
        }

        // Сортируем по убыванию схожести
        Collections.sort(similarUsers, new Comparator<UserSimilarity>() {
            @Override
            public int compare(UserSimilarity u1, UserSimilarity u2) {
                return Double.compare(u2.similarity, u1.similarity);
            }
        });

        // Ограничиваем количество "соседей" для предсказания
        List<UserSimilarity> neighbors = similarUsers.subList(
                0, Math.min(NEAREST_NEIGHBORS_COUNT, similarUsers.size()));

        // Подсчитываем вес лайков и дизлайков
        double weightedLikes = 0.0;
        double totalWeight = 0.0;

        for (UserSimilarity neighbor : neighbors) {
            // Проверяем, лайкнул ли сосед данный контент
            boolean liked = hasUserLikedContent(neighbor.userId, contentId);

            // Добавляем к взвешенной сумме
            if (liked) {
                weightedLikes += neighbor.similarity;
            }

            totalWeight += neighbor.similarity;
        }

        // Если никто из соседей не взаимодействовал с контентом
        if (totalWeight == 0.0) {
            return 0.5;
        }

        // Нормализуем и возвращаем вероятность
        double likelihood = weightedLikes / totalWeight;

        return likelihood;
    }

    @Override
    public List<String> getRecommendations(String userId, int limit) {
        // Получаем всех пользователей системы
        List<UserEntity> allUsers = userRepository.getAllUsers();

        // Получаем все элементы, которые уже лайкнул пользователь
        List<ContentEntity> userLikes = contentRepository.getLikedContentForUser(userId);
        Set<String> userLikedIds = new HashSet<>();
        for (ContentEntity content : userLikes) {
            userLikedIds.add(content.getId());
        }

        // Находим схожих пользователей
        List<UserSimilarity> similarUsers = new ArrayList<>();

        for (UserEntity otherUser : allUsers) {
            String otherUserId = otherUser.getId();

            // Пропускаем самого пользователя
            if (otherUserId.equals(userId)) {
                continue;
            }

            // Вычисляем схожесть между пользователями
            double similarity = calculateUserSimilarity(userId, otherUserId);

            // Если схожесть выше порога, добавляем в список
            if (similarity >= SIMILARITY_THRESHOLD) {
                similarUsers.add(new UserSimilarity(otherUserId, similarity));
            }
        }

        // Если нет похожих пользователей, возвращаем пустой список
        if (similarUsers.isEmpty()) {
            Log.w(TAG, "Нет похожих пользователей для пользователя: " + userId);
            return new ArrayList<>();
        }

        // Сортируем по убыванию схожести
        Collections.sort(similarUsers, new Comparator<UserSimilarity>() {
            @Override
            public int compare(UserSimilarity u1, UserSimilarity u2) {
                return Double.compare(u2.similarity, u1.similarity);
            }
        });

        // Ограничиваем количество "соседей" для предсказания
        List<UserSimilarity> neighbors = similarUsers.subList(
                0, Math.min(NEAREST_NEIGHBORS_COUNT, similarUsers.size()));

        // Собираем все контенты, которые лайкнули соседи
        Map<String, Double> recommendationScores = new HashMap<>();

        for (UserSimilarity neighbor : neighbors) {
            // Получаем лайки соседа
            List<ContentEntity> neighborLikes = contentRepository.getLikedContentForUser(neighbor.userId);

            // Для каждого лайкнутого элемента
            for (ContentEntity content : neighborLikes) {
                String contentId = content.getId();

                // Пропускаем уже лайкнутые текущим пользователем
                if (userLikedIds.contains(contentId)) {
                    continue;
                }

                // Добавляем к рейтингу рекомендации с учетом схожести пользователей
                Double currentScore = recommendationScores.getOrDefault(contentId, 0.0);
                recommendationScores.put(contentId, currentScore + neighbor.similarity);
            }
        }

        // Сортируем контент по убыванию рекомендательного рейтинга
        List<ContentRecommendation> rankedRecommendations = new ArrayList<>();

        for (Map.Entry<String, Double> entry : recommendationScores.entrySet()) {
            rankedRecommendations.add(new ContentRecommendation(entry.getKey(), entry.getValue()));
        }

        Collections.sort(rankedRecommendations, new Comparator<ContentRecommendation>() {
            @Override
            public int compare(ContentRecommendation r1, ContentRecommendation r2) {
                return Double.compare(r2.score, r1.score);
            }
        });

        // Извлекаем только ID, ограничиваем результат
        List<String> result = new ArrayList<>();
        int resultCount = Math.min(limit, rankedRecommendations.size());

        for (int i = 0; i < resultCount; i++) {
            result.add(rankedRecommendations.get(i).contentId);
        }

        return result;
    }

    /**
     * Проверяет, лайкнул ли пользователь данный контент
     */
    private boolean hasUserLikedContent(String userId, String contentId) {
        List<ContentEntity> userLikes = contentRepository.getLikedContentForUser(userId);

        for (ContentEntity content : userLikes) {
            if (content.getId().equals(contentId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Вспомогательный класс для хранения схожести пользователей
     */
    private static class UserSimilarity {
        String userId;
        double similarity;

        UserSimilarity(String userId, double similarity) {
            this.userId = userId;
            this.similarity = similarity;
        }
    }

    /**
     * Вспомогательный класс для хранения рейтинга рекомендации
     */
    private static class ContentRecommendation {
        String contentId;
        double score;

        ContentRecommendation(String contentId, double score) {
            this.contentId = contentId;
            this.score = score;
        }
    }
}