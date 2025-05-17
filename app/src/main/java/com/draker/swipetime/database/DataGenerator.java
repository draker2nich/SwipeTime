package com.draker.swipetime.database;

import android.content.Context;
import android.util.Log;

import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.database.entities.UserStatsEntity;
import com.draker.swipetime.utils.GamificationManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Класс для создания базового профиля пользователя
 */
public class DataGenerator {
    private static final String TAG = "DataGenerator";
    private static final Executor executor = Executors.newSingleThreadExecutor();

    /**
     * Инициализировать базу данных с профилем пользователя
     * @param context контекст приложения
     */
    public static void populateDatabase(Context context) {
        Log.d(TAG, "Начало инициализации базы данных");

        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);

                // Проверяем, есть ли пользователи в базе
                int userCount = db.userDao().getCount();

                Log.d(TAG, "Проверка пользователей в базе: " + userCount);

                // Если нет пользователей, создаем профиль
                if (userCount == 0) {
                    Log.d(TAG, "Создаем пользователя");
                    
                    // Создаем пользователя
                    UserEntity defaultUser = getDefaultUser();
                    db.userDao().insert(defaultUser);
                    
                    // Создаем статистику пользователя
                    UserStatsEntity stats = new UserStatsEntity(defaultUser.getId());
                    stats.setSwipesCount(0);
                    stats.setRightSwipesCount(0);
                    stats.setLeftSwipesCount(0);
                    stats.setRatingsCount(0);
                    stats.setReviewsCount(0);
                    stats.setConsumedCount(0);
                    stats.setStreakDays(0);
                    stats.setLastActivityDate(System.currentTimeMillis());
                    db.userStatsDao().insert(stats);

                    // Инициализация базового набора достижений
                    GamificationManager gamificationManager = GamificationManager.getInstance(context);
                    
                    Log.d(TAG, "Пользователь создан");
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при инициализации базы данных: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Создать пользователя по умолчанию
     * @return пользователь по умолчанию
     */
    private static UserEntity getDefaultUser() {
        return new UserEntity(
                "user_1",
                "Пользователь",
                "user@swipetime.com",
                "https://i.pravatar.cc/150?img=1"
        );
    }
}