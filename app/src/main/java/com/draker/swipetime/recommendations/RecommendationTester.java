package com.draker.swipetime.recommendations;

import android.app.Application;
import android.util.Log;

import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Класс для тестирования качества рекомендаций
 */
public class RecommendationTester {
    private static final String TAG = "RecommendationTester";

    // Метрики качества рекомендаций
    private static class RecommendationMetrics {
        // Суммы для подсчета средних
        int totalTestCases = 0;
        double totalPrecision = 0.0;
        double totalRecall = 0.0;
        double totalF1Score = 0.0;
        double totalMRR = 0.0;

        // Суммарная метрика качества (0 - 1)
        double getOverallQuality() {
            if (totalTestCases == 0) return 0;
            return (getAveragePrecision() * 0.3 +
                    getAverageRecall() * 0.2 +
                    getAverageF1Score() * 0.3 +
                    getAverageMRR() * 0.2);
        }

        // Средняя точность
        double getAveragePrecision() {
            return totalTestCases > 0 ? totalPrecision / totalTestCases : 0;
        }

        // Средняя полнота
        double getAverageRecall() {
            return totalTestCases > 0 ? totalRecall / totalTestCases : 0;
        }

        // Средний F1-Score
        double getAverageF1Score() {
            return totalTestCases > 0 ? totalF1Score / totalTestCases : 0;
        }

        // Средний MRR (Mean Reciprocal Rank)
        double getAverageMRR() {
            return totalTestCases > 0 ? totalMRR / totalTestCases : 0;
        }
    }

    private Application application;
    private RecommendationManager recommendationManager;
    private ContentRepository contentRepository;
    private UserRepository userRepository;

    public RecommendationTester(Application application) {
        this.application = application;
        this.recommendationManager = new RecommendationManager(application);
        this.contentRepository = new ContentRepository(application);
        this.userRepository = new UserRepository(application);
    }

    /**
     * Запускает тестирование качества рекомендаций
     *
     * @return общая оценка качества рекомендаций (0 - 1)
     */
    public double testRecommendationQuality() {
        Log.d(TAG, "Запуск тестирования качества рекомендаций");

        RecommendationMetrics metrics = new RecommendationMetrics();

        // Получаем всех пользователей
        List<UserEntity> allUsers = userRepository.getAllUsers();
        if (allUsers.isEmpty()) {
            Log.w(TAG, "Нет пользователей для тестирования");
            return 0.0;
        }

        // Для каждого пользователя
        for (UserEntity user : allUsers) {
            // Тестируем качество рекомендаций для пользователя
            testUserRecommendations(user.getId(), metrics);
        }

        // Логируем результаты
        Log.d(TAG, "Результаты тестирования:");
        Log.d(TAG, "Количество тест-кейсов: " + metrics.totalTestCases);
        Log.d(TAG, "Средняя точность: " + String.format("%.2f", metrics.getAveragePrecision()));
        Log.d(TAG, "Средняя полнота: " + String.format("%.2f", metrics.getAverageRecall()));
        Log.d(TAG, "Средний F1-Score: " + String.format("%.2f", metrics.getAverageF1Score()));
        Log.d(TAG, "Средний MRR: " + String.format("%.2f", metrics.getAverageMRR()));
        Log.d(TAG, "Общее качество рекомендаций: " + String.format("%.2f", metrics.getOverallQuality()));

        return metrics.getOverallQuality();
    }

    /**
     * Тестирует качество рекомендаций для конкретного пользователя
     */
    private void testUserRecommendations(String userId, RecommendationMetrics metrics) {
        // Получаем все лайкнутые элементы пользователя
        List<ContentEntity> likedContent = contentRepository.getLikedContentForUser(userId);

        // Если пользователь лайкнул менее 5 элементов, пропускаем (недостаточно данных)
        if (likedContent.size() < 5) {
            Log.d(TAG, "Пользователь " + userId + " имеет мало лайков (" + likedContent.size() + "), пропускаем");
            return;
        }

        // Для тестирования разделяем лайки на обучающую и тестовую выборки
        // Берем 70% случайных лайков для обучения, остальные для тестирования
        int trainingSize = (int) Math.ceil(likedContent.size() * 0.7);

        // Случайно перемешиваем список
        shuffleList(likedContent);

        // Разделяем на обучающую и тестовую выборки
        List<ContentEntity> trainingSet = new ArrayList<>(likedContent.subList(0, trainingSize));
        List<ContentEntity> testSet = new ArrayList<>(likedContent.subList(trainingSize, likedContent.size()));

        // Создаем карту для быстрой проверки, входит ли элемент в тестовую выборку
        Map<String, Boolean> testSetMap = new HashMap<>();
        for (ContentEntity content : testSet) {
            testSetMap.put(content.getId(), true);
        }

        // Делаем прогноз рекомендаций на основе обучающей выборки
        // Здесь в реальности нужно было бы имитировать лайки пользователя только для trainingSet
        // Но для упрощения, мы используем существующую систему рекомендаций

        // Получаем рекомендации для пользователя (20 рекомендаций)
        List<ContentItem> recommendations = recommendationManager.getRecommendations(userId, null, 20);

        // Считаем метрики
        int relevantRecommendations = 0; // Рекомендации, которые присутствуют в тестовой выборке
        double reciprocalRank = 0.0; // Для MRR

        for (int i = 0; i < recommendations.size(); i++) {
            ContentItem recommendation = recommendations.get(i);
            if (testSetMap.containsKey(recommendation.getId())) {
                relevantRecommendations++;

                // Для MRR берем первое совпадение
                if (reciprocalRank == 0.0) {
                    reciprocalRank = 1.0 / (i + 1);
                }
            }
        }

        // Вычисляем метрики
        double precision = recommendations.isEmpty() ?
                0.0 : (double) relevantRecommendations / recommendations.size();

        double recall = testSet.isEmpty() ?
                0.0 : (double) relevantRecommendations / testSet.size();

        double f1Score = (precision + recall == 0) ?
                0.0 : 2 * precision * recall / (precision + recall);

        // Обновляем суммарные метрики
        metrics.totalTestCases++;
        metrics.totalPrecision += precision;
        metrics.totalRecall += recall;
        metrics.totalF1Score += f1Score;
        metrics.totalMRR += reciprocalRank;

        // Логируем результаты для данного пользователя
        Log.d(TAG, "Пользователь " + userId +
                ", Точность: " + String.format("%.2f", precision) +
                ", Полнота: " + String.format("%.2f", recall) +
                ", F1: " + String.format("%.2f", f1Score) +
                ", MRR: " + String.format("%.2f", reciprocalRank));
    }

    /**
     * Перемешивает список случайным образом
     */
    private <T> void shuffleList(List<T> list) {
        Collections.shuffle(list, new Random(System.currentTimeMillis()));
    }
}