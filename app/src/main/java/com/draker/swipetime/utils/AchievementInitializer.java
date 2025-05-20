package com.draker.swipetime.utils;

import android.content.Context;
import android.util.Log;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.AchievementDao;
import com.draker.swipetime.database.entities.AchievementEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Класс для принудительной инициализации достижений
 */
public class AchievementInitializer {
    private static final String TAG = "AchievementInitializer";
    private static final Executor executor = Executors.newSingleThreadExecutor();

    /**
     * Принудительно инициализировать достижения
     * @param context контекст приложения
     * @param callback обратный вызов для уведомления о завершении
     */
    public static void forceInitializeAchievements(Context context, InitializationCallback callback) {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                AchievementDao achievementDao = db.achievementDao();
                
                // Удаляем старые достижения
                achievementDao.deleteAll();
                Log.d(TAG, "Старые достижения удалены");
                
                // Создаем новые достижения
                List<AchievementEntity> achievements = createDefaultAchievements();
                achievementDao.insertAll(achievements);
                
                Log.d(TAG, "Создано " + achievements.size() + " достижений");
                
                // Проверяем результат
                int count = achievementDao.getCount();
                Log.d(TAG, "Общее количество достижений в базе: " + count);
                
                if (callback != null) {
                    callback.onInitialized(count > 0, count);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при инициализации достижений: " + e.getMessage());
                if (callback != null) {
                    callback.onInitialized(false, 0);
                }
            }
        });
    }
    
    /**
     * Создать набор достижений по умолчанию
     * @return список достижений
     */
    private static List<AchievementEntity> createDefaultAchievements() {
        List<AchievementEntity> achievements = new ArrayList<>();
        
        // Достижения для свайпов
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Первый шаг",
                "Сделайте первый свайп",
                "ic_achievement_first_swipe",
                10, // опыт за достижение
                GamificationManager.ACTION_SWIPE,
                1, // требуемое количество
                GamificationManager.CATEGORY_BEGINNER
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Исследователь",
                "Сделайте 50 свайпов",
                "ic_achievement_first_swipe",
                50,
                GamificationManager.ACTION_SWIPE,
                50,
                GamificationManager.CATEGORY_BEGINNER
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Мастер свайпа",
                "Сделайте 500 свайпов",
                "ic_achievement_first_swipe",
                200,
                GamificationManager.ACTION_SWIPE,
                500,
                GamificationManager.CATEGORY_INTERMEDIATE
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Свайп-гуру",
                "Сделайте 2000 свайпов",
                "ic_achievement_first_swipe",
                500,
                GamificationManager.ACTION_SWIPE,
                2000,
                GamificationManager.CATEGORY_ADVANCED
        ));
        
        // Достижения для оценок
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Первая оценка",
                "Поставьте первую оценку",
                "ic_achievement_first_rating",
                20,
                GamificationManager.ACTION_RATE,
                1,
                GamificationManager.CATEGORY_BEGINNER
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Критик",
                "Поставьте 25 оценок",
                "ic_achievement_first_rating",
                100,
                GamificationManager.ACTION_RATE,
                25,
                GamificationManager.CATEGORY_INTERMEDIATE
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Опытный критик",
                "Поставьте 100 оценок",
                "ic_achievement_first_rating",
                300,
                GamificationManager.ACTION_RATE,
                100,
                GamificationManager.CATEGORY_ADVANCED
        ));
        
        // Достижения для рецензий
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Рецензент",
                "Напишите первую рецензию",
                "ic_achievement_reviewer",
                50,
                GamificationManager.ACTION_REVIEW,
                1,
                GamificationManager.CATEGORY_BEGINNER
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Обозреватель",
                "Напишите 10 рецензий",
                "ic_achievement_reviewer",
                200,
                GamificationManager.ACTION_REVIEW,
                10,
                GamificationManager.CATEGORY_INTERMEDIATE
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Профессиональный критик",
                "Напишите 50 рецензий",
                "ic_achievement_reviewer",
                500,
                GamificationManager.ACTION_REVIEW,
                50,
                GamificationManager.CATEGORY_ADVANCED
        ));
        
        // Достижения для просмотренного/прочитанного контента
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Первый опыт",
                "Отметьте первый элемент как просмотренный/прочитанный",
                "ic_achievement_first_complete",
                30,
                GamificationManager.ACTION_COMPLETE,
                1,
                GamificationManager.CATEGORY_BEGINNER
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Коллекционер",
                "Отметьте 20 элементов как просмотренные/прочитанные",
                "ic_achievement_first_complete",
                150,
                GamificationManager.ACTION_COMPLETE,
                20,
                GamificationManager.CATEGORY_INTERMEDIATE
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Энциклопедист",
                "Отметьте 100 элементов как просмотренные/прочитанные",
                "ic_achievement_first_complete",
                400,
                GamificationManager.ACTION_COMPLETE,
                100,
                GamificationManager.CATEGORY_ADVANCED
        ));
        
        // Достижения для общего количества действий
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Путь начинается",
                "Совершите 10 действий",
                "ic_achievement",
                20,
                "total",
                10,
                GamificationManager.CATEGORY_BEGINNER
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Активный пользователь",
                "Совершите 100 действий",
                "ic_achievement",
                100,
                "total",
                100,
                GamificationManager.CATEGORY_INTERMEDIATE
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Эксперт",
                "Совершите 1000 действий",
                "ic_achievement",
                500,
                "total",
                1000,
                GamificationManager.CATEGORY_ADVANCED
        ));
        
        // Достижения для дней активности
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Еженедельник",
                "Будьте активны 7 дней подряд",
                "ic_achievement",
                100,
                "streak",
                7,
                GamificationManager.CATEGORY_STREAK
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Месяц с нами",
                "Будьте активны 30 дней подряд",
                "ic_achievement",
                300,
                "streak",
                30,
                GamificationManager.CATEGORY_STREAK
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Преданный фанат",
                "Будьте активны 100 дней подряд",
                "ic_achievement",
                1000,
                "streak",
                100,
                GamificationManager.CATEGORY_STREAK
        ));
        
        return achievements;
    }
    
    /**
     * Интерфейс для обратного вызова инициализации
     */
    public interface InitializationCallback {
        void onInitialized(boolean success, int achievementsCount);
    }
}
