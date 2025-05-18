package com.draker.swipetime.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс для отслеживания аналитики в приложении
 */
public class AnalyticsTracker {
    private static final String TAG = "AnalyticsTracker";
    private static final String PREFS_NAME = "analytics_prefs";
    
    // Ключи для событий
    private static final String KEY_SWIPES_RIGHT = "swipes_right_";
    private static final String KEY_SWIPES_LEFT = "swipes_left_";
    private static final String KEY_CONTENT_LOADED = "content_loaded_";
    private static final String KEY_CONTENT_VIEWS = "content_views_";
    private static final String KEY_CONTENT_LOAD_ERRORS = "content_load_errors_";
    private static final String KEY_EMPTY_STATES = "empty_states_";
    
    // Общая сессионная статистика
    private static final Map<String, Integer> sessionStats = new HashMap<>();
    
    /**
     * Отслеживает событие свайпа
     * @param context контекст приложения
     * @param category категория контента
     * @param isRightSwipe true, если свайп вправо (лайк)
     */
    public static void trackSwipe(Context context, String category, boolean isRightSwipe) {
        try {
            String normalizedCategory = normalizeKey(category);
            String key = isRightSwipe ? KEY_SWIPES_RIGHT + normalizedCategory : KEY_SWIPES_LEFT + normalizedCategory;
            
            // Увеличиваем счетчик в SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            int count = prefs.getInt(key, 0) + 1;
            prefs.edit().putInt(key, count).apply();
            
            // Увеличиваем сессионный счетчик
            incrementSessionStat(key, 1);
            
            Log.d(TAG, "Отслеживание свайпа: " + (isRightSwipe ? "вправо" : "влево") + 
                    " для категории " + category + ", всего: " + count);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при отслеживании свайпа: " + e.getMessage());
        }
    }
    
    /**
     * Отслеживает загрузку контента
     * @param context контекст приложения
     * @param category категория контента
     * @param count количество загруженных элементов
     * @param success успешна ли загрузка
     */
    public static void trackContentLoad(Context context, String category, int count, boolean success) {
        try {
            String normalizedCategory = normalizeKey(category);
            
            if (success) {
                // Увеличиваем счетчик успешных загрузок
                String key = KEY_CONTENT_LOADED + normalizedCategory;
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                int totalCount = prefs.getInt(key, 0) + count;
                prefs.edit().putInt(key, totalCount).apply();
                
                // Увеличиваем сессионный счетчик
                incrementSessionStat(key, count);
                
                Log.d(TAG, "Отслеживание загрузки контента для категории " + category + 
                        ", загружено: " + count + ", всего: " + totalCount);
            } else {
                // Увеличиваем счетчик ошибок загрузки
                String key = KEY_CONTENT_LOAD_ERRORS + normalizedCategory;
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                int errorCount = prefs.getInt(key, 0) + 1;
                prefs.edit().putInt(key, errorCount).apply();
                
                // Увеличиваем сессионный счетчик
                incrementSessionStat(key, 1);
                
                Log.d(TAG, "Отслеживание ошибки загрузки контента для категории " + category + 
                        ", всего ошибок: " + errorCount);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при отслеживании загрузки контента: " + e.getMessage());
        }
    }
    
    /**
     * Отслеживает просмотр карточки
     * @param context контекст приложения
     * @param category категория контента
     */
    public static void trackCardView(Context context, String category) {
        try {
            String normalizedCategory = normalizeKey(category);
            String key = KEY_CONTENT_VIEWS + normalizedCategory;
            
            // Увеличиваем счетчик просмотров
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            int viewCount = prefs.getInt(key, 0) + 1;
            prefs.edit().putInt(key, viewCount).apply();
            
            // Увеличиваем сессионный счетчик
            incrementSessionStat(key, 1);
            
            Log.d(TAG, "Отслеживание просмотра карточки для категории " + category + 
                    ", всего просмотров: " + viewCount);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при отслеживании просмотра карточки: " + e.getMessage());
        }
    }
    
    /**
     * Отслеживает пустое состояние (когда нет карточек для показа)
     * @param context контекст приложения
     * @param category категория контента
     */
    public static void trackEmptyState(Context context, String category) {
        try {
            String normalizedCategory = normalizeKey(category);
            String key = KEY_EMPTY_STATES + normalizedCategory;
            
            // Увеличиваем счетчик пустых состояний
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            int emptyCount = prefs.getInt(key, 0) + 1;
            prefs.edit().putInt(key, emptyCount).apply();
            
            // Увеличиваем сессионный счетчик
            incrementSessionStat(key, 1);
            
            Log.d(TAG, "Отслеживание пустого состояния для категории " + category + 
                    ", всего пустых состояний: " + emptyCount);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при отслеживании пустого состояния: " + e.getMessage());
        }
    }
    
    /**
     * Получает статистику сессии
     * @return отображение статистики
     */
    public static Map<String, Integer> getSessionStats() {
        synchronized (sessionStats) {
            return new HashMap<>(sessionStats);
        }
    }
    
    /**
     * Сбрасывает статистику сессии
     */
    public static void resetSessionStats() {
        synchronized (sessionStats) {
            sessionStats.clear();
        }
    }
    
    /**
     * Получает полную статистику из SharedPreferences
     * @param context контекст приложения
     * @return отображение статистики
     */
    public static Map<String, Integer> getAllStats(Context context) {
        Map<String, Integer> stats = new HashMap<>();
        
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Map<String, ?> allPrefs = prefs.getAll();
            
            for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
                if (entry.getValue() instanceof Integer) {
                    stats.put(entry.getKey(), (Integer) entry.getValue());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении всей статистики: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Преобразует ключ категории для использования в SharedPreferences
     * @param category категория контента
     * @return нормализованный ключ
     */
    private static String normalizeKey(String category) {
        if (category == null) {
            return "unknown";
        }
        
        return category.toLowerCase()
                .replace(" ", "_")
                .replace(".", "_")
                .replace(",", "_")
                .replace("-", "_");
    }
    
    /**
     * Увеличивает значение счетчика в сессионной статистике
     * @param key ключ
     * @param increment значение для увеличения
     */
    private static void incrementSessionStat(String key, int increment) {
        synchronized (sessionStats) {
            int current = sessionStats.getOrDefault(key, 0);
            sessionStats.put(key, current + increment);
        }
    }
}
