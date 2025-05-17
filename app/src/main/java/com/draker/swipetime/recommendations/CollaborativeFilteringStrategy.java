package com.draker.swipetime.recommendations;

import java.util.List;

/**
 * Интерфейс для алгоритмов коллаборативной фильтрации
 */
public interface CollaborativeFilteringStrategy {

    /**
     * Вычисляет коэффициент схожести между двумя пользователями на основе их лайков
     *
     * @param userIdA ID первого пользователя
     * @param userIdB ID второго пользователя
     * @return коэффициент схожести (от 0 до 1)
     */
    double calculateUserSimilarity(String userIdA, String userIdB);

    /**
     * Предсказывает вероятность того, что пользователь лайкнет данный элемент
     *
     * @param userId ID пользователя
     * @param contentId ID элемента контента
     * @return вероятность (от 0 до 1)
     */
    double predictLikelihood(String userId, String contentId);

    /**
     * Возвращает список ID элементов, рекомендуемых для пользователя на основе коллаборативной фильтрации
     *
     * @param userId ID пользователя
     * @param limit максимальное количество рекомендаций
     * @return список ID рекомендуемых элементов
     */
    List<String> getRecommendations(String userId, int limit);
}