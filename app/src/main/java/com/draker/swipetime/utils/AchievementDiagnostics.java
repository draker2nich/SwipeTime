package com.draker.swipetime.utils;

import android.content.Context;
import android.util.Log;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.entities.AchievementEntity;
import com.draker.swipetime.database.entities.UserAchievementCrossRef;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.database.entities.UserStatsEntity;

import java.util.List;

/**
 * Утилита для диагностики системы достижений
 */
public class AchievementDiagnostics {
    private static final String TAG = "AchievementDiagnostics";

    /**
     * Проводит полную диагностику системы достижений
     * @param context контекст приложения
     * @return строка с результатами диагностики
     */
    public static String runFullDiagnostics(Context context) {
        StringBuilder report = new StringBuilder();
        
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            String userId = GamificationIntegrator.getCurrentUserId(context);
            
            report.append("=== ДИАГНОСТИКА СИСТЕМЫ ДОСТИЖЕНИЙ ===\n\n");
            
            // 1. Проверка пользователя
            UserEntity user = db.userDao().getById(userId);
            report.append("1. ПОЛЬЗОВАТЕЛЬ:\n");
            if (user != null) {
                report.append("   ✓ Пользователь найден: ").append(user.getUsername()).append("\n");
                report.append("   ✓ ID: ").append(user.getId()).append("\n");
                report.append("   ✓ Уровень: ").append(user.getLevel()).append("\n");
                report.append("   ✓ Опыт: ").append(user.getExperience()).append("\n");
            } else {
                report.append("   ✗ Пользователь не найден!\n");
                return report.toString();
            }
            
            // 2. Проверка статистики
            UserStatsEntity stats = db.userStatsDao().getByUserId(userId);
            report.append("\n2. СТАТИСТИКА ПОЛЬЗОВАТЕЛЯ:\n");
            if (stats != null) {
                report.append("   ✓ Статистика найдена\n");
                report.append("   ✓ Свайпы: ").append(stats.getSwipesCount()).append("\n");
                report.append("   ✓ Оценки: ").append(stats.getRatingsCount()).append("\n");
                report.append("   ✓ Отзывы: ").append(stats.getReviewsCount()).append("\n");
                report.append("   ✓ Просмотры: ").append(stats.getConsumedCount()).append("\n");
                report.append("   ✓ Общие действия: ").append(stats.getTotalActions()).append("\n");
            } else {
                report.append("   ✗ Статистика не найдена!\n");
            }
            
            // 3. Проверка достижений в базе
            List<AchievementEntity> allAchievements = db.achievementDao().getAll();
            report.append("\n3. ДОСТИЖЕНИЯ В БАЗЕ:\n");
            report.append("   ✓ Всего достижений: ").append(allAchievements.size()).append("\n");
            
            if (allAchievements.isEmpty()) {
                report.append("   ✗ Достижения отсутствуют в базе данных!\n");
                return report.toString();
            }
            
            for (int i = 0; i < Math.min(5, allAchievements.size()); i++) {
                AchievementEntity achievement = allAchievements.get(i);
                report.append("   - ").append(achievement.getTitle())
                      .append(" (").append(achievement.getRequiredAction())
                      .append(", требуется: ").append(achievement.getRequiredCount()).append(")\n");
            }
            
            if (allAchievements.size() > 5) {
                report.append("   ... и еще ").append(allAchievements.size() - 5).append(" достижений\n");
            }
            
            // 4. Проверка пользовательских достижений
            List<UserAchievementCrossRef> userAchievements = db.userAchievementDao().getByUserId(userId);
            report.append("\n4. ДОСТИЖЕНИЯ ПОЛЬЗОВАТЕЛЯ:\n");
            report.append("   ✓ Записей о достижениях: ").append(userAchievements.size()).append("\n");
            
            int completedCount = 0;
            for (UserAchievementCrossRef userAch : userAchievements) {
                if (userAch.isCompleted()) {
                    completedCount++;
                    AchievementEntity achievement = db.achievementDao().getById(userAch.getAchievementId());
                    if (achievement != null) {
                        report.append("   ✓ Выполнено: ").append(achievement.getTitle()).append("\n");
                    }
                }
            }
            
            report.append("   ✓ Выполненных достижений: ").append(completedCount).append("\n");
            
            // 5. Тестирование GamificationManager
            report.append("\n5. ТЕСТИРОВАНИЕ GAMIFICATION MANAGER:\n");
            GamificationManager gamificationManager = GamificationManager.getInstance(context);
            
            int managerCompletedCount = gamificationManager.getCompletedAchievementsCount(userId);
            int managerTotalCount = gamificationManager.getTotalAchievementsCount();
            
            report.append("   ✓ Менеджер - выполнено: ").append(managerCompletedCount).append("\n");
            report.append("   ✓ Менеджер - всего: ").append(managerTotalCount).append("\n");
            
            List<GamificationManager.UserAchievementInfo> achievementInfos = gamificationManager.getUserAchievements(userId);
            report.append("   ✓ Менеджер - информация о достижениях: ").append(achievementInfos.size()).append("\n");
            
            // Детали первых 3 достижений
            for (int i = 0; i < Math.min(3, achievementInfos.size()); i++) {
                GamificationManager.UserAchievementInfo info = achievementInfos.get(i);
                report.append("   - ").append(info.getAchievement().getTitle())
                      .append(" (выполнено: ").append(info.isCompleted())
                      .append(", прогресс: ").append(info.getProgress()).append("%)\n");
            }
            
            // 6. Проверка соответствия статистики и достижений
            report.append("\n6. АНАЛИЗ СООТВЕТСТВИЯ:\n");
            
            if (stats != null) {
                // Проверяем достижения за свайпы
                List<AchievementEntity> swipeAchievements = db.achievementDao().getByRequiredAction(GamificationManager.ACTION_SWIPE);
                report.append("   Свайпы (").append(stats.getSwipesCount()).append("): ");
                for (AchievementEntity ach : swipeAchievements) {
                    if (stats.getSwipesCount() >= ach.getRequiredCount()) {
                        report.append(ach.getTitle()).append("(✓) ");
                    } else {
                        report.append(ach.getTitle()).append("(✗) ");
                    }
                }
                report.append("\n");
                
                // Проверяем достижения за оценки
                List<AchievementEntity> ratingAchievements = db.achievementDao().getByRequiredAction(GamificationManager.ACTION_RATE);
                report.append("   Оценки (").append(stats.getRatingsCount()).append("): ");
                for (AchievementEntity ach : ratingAchievements) {
                    if (stats.getRatingsCount() >= ach.getRequiredCount()) {
                        report.append(ach.getTitle()).append("(✓) ");
                    } else {
                        report.append(ach.getTitle()).append("(✗) ");
                    }
                }
                report.append("\n");
            }
            
            report.append("\n=== ДИАГНОСТИКА ЗАВЕРШЕНА ===\n");
            
        } catch (Exception e) {
            report.append("\n✗ ОШИБКА ДИАГНОСТИКИ: ").append(e.getMessage()).append("\n");
            Log.e(TAG, "Ошибка диагностики", e);
        }
        
        return report.toString();
    }
    
    /**
     * Принудительно синхронизирует достижения пользователя с его статистикой
     * @param context контекст приложения
     * @return количество разблокированных достижений
     */
    public static int forceSyncAchievements(Context context) {
        try {
            String userId = GamificationIntegrator.getCurrentUserId(context);
            AppDatabase db = AppDatabase.getInstance(context);
            UserStatsEntity stats = db.userStatsDao().getByUserId(userId);
            
            if (stats == null) {
                Log.e(TAG, "Статистика пользователя не найдена");
                return 0;
            }
            
            GamificationManager manager = GamificationManager.getInstance(context);
            int unlockedCount = 0;
            
            // Принудительно обрабатываем все действия из статистики
            for (int i = 0; i < stats.getSwipesCount(); i++) {
                boolean levelUp = manager.processUserAction(userId, GamificationManager.ACTION_SWIPE, "true");
                if (levelUp) unlockedCount++;
            }
            
            for (int i = 0; i < stats.getRatingsCount(); i++) {
                boolean levelUp = manager.processUserAction(userId, GamificationManager.ACTION_RATE, "5.0");
                if (levelUp) unlockedCount++;
            }
            
            for (int i = 0; i < stats.getReviewsCount(); i++) {
                boolean levelUp = manager.processUserAction(userId, GamificationManager.ACTION_REVIEW, "test_review");
                if (levelUp) unlockedCount++;
            }
            
            for (int i = 0; i < stats.getConsumedCount(); i++) {
                boolean levelUp = manager.processUserAction(userId, GamificationManager.ACTION_COMPLETE, "test_content");
                if (levelUp) unlockedCount++;
            }
            
            Log.d(TAG, "Синхронизация завершена, разблокировано достижений: " + unlockedCount);
            return unlockedCount;
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка синхронизации достижений", e);
            return 0;
        }
    }
}
