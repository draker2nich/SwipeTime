package com.draker.swipetime;

import android.app.Application;
import android.util.Log;

import com.draker.swipetime.api.ApiDataManager;
import com.draker.swipetime.api.ApiIntegrationManager;
import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.DataGenerator;
import com.draker.swipetime.database.DatabaseCleaner;
import com.draker.swipetime.database.DbCleanerUtil;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.utils.AchievementNotifier;
import com.draker.swipetime.utils.ContentShuffler;
import com.draker.swipetime.utils.FirebaseAuthManager;
import com.draker.swipetime.utils.GamificationIntegrator;
import com.draker.swipetime.utils.GamificationManager;
import com.draker.swipetime.utils.ImageCacheManager;
import com.draker.swipetime.utils.PersistentFavoritesManager;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс приложения для выполнения инициализации при запуске
 */
public class SwipeTimeApplication extends Application {
    private static final String TAG = "SwipeTimeApplication";
    private static final String DATABASE_NAME = "swipetime-db";
    
    // Флаг для отслеживания первого запуска приложения
    private static boolean isFirstLaunch = true;

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Инициализация Firebase
        FirebaseApp.initializeApp(this);
        
        // Инициализация кеша изображений
        ImageCacheManager.initImageCache(this);
        
        try {
            // Очистка тестовых данных при первом запуске
            if (isFirstLaunch) {
                // Очищаем тестовые данные
                cleanupTestData();
                
                // Сбрасываем кеши API и истории показа
                resetCaches();
                
                isFirstLaunch = false;
            }
            
            // Заполнить базу данных базовыми данными (только пользователя)
            DataGenerator.populateDatabase(this);
            
            // Инициализация системы геймификации
            GamificationIntegrator.ensureUserInitialized(this);
            
            // Восстанавливаем состояние избранного
            PersistentFavoritesManager favManager = new PersistentFavoritesManager(this);
            favManager.restoreFavoritesState();
            favManager.removeTestFavorites(); // Удаляем тестовые данные из избранного
            
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
     * Очистка тестовых данных
     */
    private void cleanupTestData() {
        Log.d(TAG, "Запуск полной очистки данных...");
        
        try {
            // Используем ContentRepository напрямую для удаления тестовых данных
            ContentRepository contentRepo = new ContentRepository(this);
            
            // Сначала получаем все тестовые элементы
            List<ContentEntity> allContent = contentRepo.getAll();
            List<String> testIds = new ArrayList<>();
            
            // Собираем все ID тестовых элементов
            for (ContentEntity content : allContent) {
                if (content.getId() != null && content.getId().startsWith("test_")) {
                    testIds.add(content.getId());
                    Log.d(TAG, "Найден тестовый элемент для удаления: " + content.getTitle());
                }
            }
            
            // Удаляем каждый тестовый элемент
            for (String id : testIds) {
                Log.d(TAG, "Удаление тестового элемента: " + id);
                contentRepo.deleteById(id);
            }
            
            // Также удаляем конкретные известные тестовые элементы
            String[] knownTestIds = {
                "test_movie_1", "test_tvshow_1", "test_game_1", 
                "test_book_1", "test_anime_1", "test_music_1"
            };
            
            for (String id : knownTestIds) {
                contentRepo.deleteById(id);
                Log.d(TAG, "Удаление известного тестового элемента: " + id);
            }
            
            // Очищаем сохраненные ID в SharedPreferences
            PersistentFavoritesManager favManager = new PersistentFavoritesManager(this);
            int removed = favManager.removeTestFavorites();
            Log.d(TAG, "Удалено " + removed + " тестовых элементов из SharedPreferences");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при удалении тестовых данных: " + e.getMessage());
        }
        
        // После удаления тестовых данных также используем общую очистку
        DbCleanerUtil.clearTestData(this);
        
        // Ждем немного, чтобы операция очистки успела выполниться
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Log.e(TAG, "Прерывание во время ожидания очистки данных: " + e.getMessage());
        }
        
        Log.d(TAG, "Очистка данных завершена");
    }
    
    /**
     * Сбросить все кеши состояния
     */
    private void resetCaches() {
        // Сбрасываем API кеши
        ApiDataManager.getInstance().resetAllLoadedItems();
        ApiDataManager.getInstance().resetAllPageTokens();
        
        // Сбрасываем историю показа контента
        ContentShuffler.resetAllHistory();
        
        Log.d(TAG, "Все кеши успешно сброшены");
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