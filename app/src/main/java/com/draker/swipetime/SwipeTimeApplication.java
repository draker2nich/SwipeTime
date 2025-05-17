package com.draker.swipetime;

import android.app.Application;
import android.util.Log;

import com.draker.swipetime.api.ApiIntegrationManager;
import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.DataGenerator;
import com.draker.swipetime.database.DatabaseCleaner;
import com.draker.swipetime.database.DbCleanerUtil;
import com.draker.swipetime.utils.AchievementNotifier;
import com.draker.swipetime.utils.FirebaseAuthManager;
import com.draker.swipetime.utils.GamificationIntegrator;
import com.draker.swipetime.utils.GamificationManager;
import com.draker.swipetime.utils.ImageCacheManager;
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
        
        // Инициализация кеша изображений
        ImageCacheManager.initImageCache(this);
        
        try {
            // Заполнить базу данных базовыми данными (только пользователя)
            DataGenerator.populateDatabase(this);
            
            // Очистка тестовых данных (если они есть)
            DbCleanerUtil.clearTestData(this);
            
            // Инициализация системы геймификации
            GamificationIntegrator.ensureUserInitialized(this);
            
            // Проверка авторизации Firebase
            FirebaseAuthManager authManager = FirebaseAuthManager.getInstance(this);
            if (authManager.isUserSignedIn()) {
                Log.d(TAG, "Пользователь Firebase авторизован: " + authManager.getCurrentUser().getEmail());
            } else {
                Log.d(TAG, "Пользователь Firebase не авторизован");
            }
            
            // Инициализация интеграции внешних API
            initializeApiIntegration();
            
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
                    
                    // Также инициализируем интеграцию внешних API
                    initializeApiIntegration();
                }
            } else {
                throw e;
            }
        }
    }
    
    /**
     * Инициализировать интеграцию внешних API
     */
    private void initializeApiIntegration() {
        // Загружаем данные асинхронно, чтобы не блокировать запуск приложения
        new Thread(() -> {
            ApiIntegrationManager apiManager = ApiIntegrationManager.getInstance(this);
            apiManager.initializeApiIntegration(new ApiIntegrationManager.ApiInitCallback() {
                @Override
                public void onComplete(boolean success) {
                    Log.d(TAG, "Инициализация интеграции API завершена с результатом: " + success);
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Ошибка при инициализации интеграции API: " + errorMessage);
                }
            });
        }).start();
    }
}
