package com.draker.swipetime;

import android.app.Application;
import android.util.Log;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.DataGenerator;
import com.draker.swipetime.database.DatabaseCleaner;
import com.draker.swipetime.utils.AchievementNotifier;
import com.draker.swipetime.utils.GamificationIntegrator;
import com.draker.swipetime.utils.GamificationManager;

/**
 * Класс приложения для выполнения инициализации при запуске
 */
public class SwipeTimeApplication extends Application {
    private static final String TAG = "SwipeTimeApplication";
    private static final String DATABASE_NAME = "swipetime-db";

    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            // Заполнить базу данных тестовыми данными
            DataGenerator.populateDatabase(this);
            
            // Инициализация системы геймификации
            GamificationIntegrator.ensureUserInitialized(this);
            
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("Migration didn't properly handle")) {
                Log.e(TAG, "Ошибка миграции базы данных: " + e.getMessage());
                
                // Очистка базы данных при ошибке миграции
                Log.d(TAG, "Пытаемся удалить и пересоздать базу данных");
                
                // Закрываем соединение с базой данных
                AppDatabase.destroyInstance();
                
                // Удаляем файл базы данных
                boolean deleted = DatabaseCleaner.deleteDatabase(this, DATABASE_NAME);
                Log.d(TAG, "База данных удалена: " + deleted);
                
                // Пересоздаем базу данных
                if (deleted) {
                    DataGenerator.populateDatabase(this);
                    GamificationIntegrator.ensureUserInitialized(this);
                    Log.d(TAG, "База данных успешно пересоздана");
                }
            } else {
                throw e;
            }
        }
    }
}
