package com.draker.swipetime;

import android.app.Application;
import android.util.Log;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.DataGenerator;
import com.draker.swipetime.database.DatabaseCleaner;
import com.draker.swipetime.utils.AchievementNotifier;
import com.draker.swipetime.utils.FirebaseAuthManager;
import com.draker.swipetime.utils.GamificationIntegrator;
import com.draker.swipetime.utils.GamificationManager;
import com.google.firebase.FirebaseApp;

/**
 * Класс приложения для выполнения инициализации при запуске
 */
public class SwipeTimeApplication extends Application {
    private static final String TAG = "SwipeTimeApplication";
    private static final String DATABASE_NAME = "swipetime-db";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Инициализация Firebase
        FirebaseApp.initializeApp(this);
        
        try {
            // Заполнить базу данных тестовыми данными
            DataGenerator.populateDatabase(this);
            
            // Инициализация системы геймификации
            GamificationIntegrator.ensureUserInitialized(this);
            
            // Проверка авторизации Firebase
            FirebaseAuthManager authManager = FirebaseAuthManager.getInstance(this);
            if (authManager.isUserSignedIn()) {
                Log.d(TAG, "Пользователь Firebase авторизован: " + authManager.getCurrentUser().getEmail());
            } else {
                Log.d(TAG, "Пользователь Firebase не авторизован");
            }
            
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
