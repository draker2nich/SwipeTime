package com.draker.swipetime.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс для отслеживания аналитики использования приложения
 */
public class AnalyticsTracker {
    private static final String TAG = "AnalyticsTracker";
    private static final String PREFS_NAME = "analytics_prefs";
    
    // Ключи для метрик
    private static final String KEY_SWIPE_COUNT = "swipe_count";
    private static final String KEY_CARD_VIEW_COUNT = "card_view_count";
    private static final String KEY_CONTENT_LOAD_COUNT = "content_load_count";
    private static final String KEY_EMPTY_STATE_COUNT = "empty_state_count";
    private static final String KEY_FRAGMENT_SWITCH_COUNT = "fragment_switch_count";
    
    // Префиксы для категорий
    private static final String PREFIX_MOVIES = "movies_";
    private static final String PREFIX_TV = "tv_";
    private static final String PREFIX_GAMES = "games_";
    private static final String PREFIX_BOOKS = "books_";
    private static final String PREFIX_ANIME = "anime_";
    
    /**
     * Увеличивает счетчик свайпов
     * @param context контекст приложения
     * @param category категория контента
     * @param isLiked true, если лайк, false если дизлайк
     */
    public static void trackSwipe(Context context, String category, boolean isLiked) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String directionKey = isLiked ? "like" : "dislike";
        String key = KEY_SWIPE_COUNT + "_" + getCategoryPrefix(category) + directionKey;
        
        int count = prefs.getInt(key, 0) + 1;
        prefs.edit().putInt(key, count).apply();
        
        // Также обновляем общий счетчик
        String totalKey = KEY_SWIPE_COUNT + "_total";
        int totalCount = prefs.getInt(totalKey, 0) + 1;
        prefs.edit().putInt(totalKey, totalCount).apply();
        
        Log.d(TAG, "Отслежен свайп: " + key + " = " + count + ", всего: " + totalCount);
    }
    
    /**
     * Увеличивает счетчик просмотров карточек
     * @param context контекст приложения
     * @param category категория контента
     */
    public static void trackCardView(Context context, String category) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = KEY_CARD_VIEW_COUNT + "_" + getCategoryPrefix(category);
        
        int count = prefs.getInt(key, 0) + 1;
        prefs.edit().putInt(key, count).apply();
        
        // Также обновляем общий счетчик
        String totalKey = KEY_CARD_VIEW_COUNT + "_total";
        int totalCount = prefs.getInt(totalKey, 0) + 1;
        prefs.edit().putInt(totalKey, totalCount).apply();
    }
    
    /**
     * Увеличивает счетчик загрузок контента
     * @param context контекст приложения
     * @param category категория контента
     * @param itemsCount количество загруженных элементов
     * @param isSuccess true, если загрузка успешна
     */
    public static void trackContentLoad(Context context, String category, int itemsCount, boolean isSuccess) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String statusKey = isSuccess ? "success" : "failure";
        String key = KEY_CONTENT_LOAD_COUNT + "_" + getCategoryPrefix(category) + statusKey;
        
        int count = prefs.getInt(key, 0) + 1;
        prefs.edit().putInt(key, count).apply();
        
        // Также сохраняем среднее количество элементов при успешной загрузке
        if (isSuccess) {
            String itemsKey = KEY_CONTENT_LOAD_COUNT + "_" + getCategoryPrefix(category) + "items_total";
            int itemsTotal = prefs.getInt(itemsKey, 0) + itemsCount;
            prefs.edit().putInt(itemsKey, itemsTotal).apply();
        }
    }
    
    /**
     * Увеличивает счетчик показов пустого состояния
     * @param context контекст приложения
     * @param category категория контента
     */
    public static void trackEmptyState(Context context, String category) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = KEY_EMPTY_STATE_COUNT + "_" + getCategoryPrefix(category);
        
        int count = prefs.getInt(key, 0) + 1;
        prefs.edit().putInt(key, count).apply();
        
        // Также обновляем общий счетчик
        String totalKey = KEY_EMPTY_STATE_COUNT + "_total";
        int totalCount = prefs.getInt(totalKey, 0) + 1;
        prefs.edit().putInt(totalKey, totalCount).apply();
    }
    
    /**
     * Увеличивает счетчик переключений фрагментов
     * @param context контекст приложения
     * @param fragmentName название фрагмента
     */
    public static void trackFragmentSwitch(Context context, String fragmentName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = KEY_FRAGMENT_SWITCH_COUNT + "_" + fragmentName;
        
        int count = prefs.getInt(key, 0) + 1;
        prefs.edit().putInt(key, count).apply();
        
        // Также обновляем общий счетчик
        String totalKey = KEY_FRAGMENT_SWITCH_COUNT + "_total";
        int totalCount = prefs.getInt(totalKey, 0) + 1;
        prefs.edit().putInt(totalKey, totalCount).apply();
    }
    
    /**
     * Возвращает префикс для категории
     * @param category категория контента
     * @return префикс для категории
     */
    private static String getCategoryPrefix(String category) {
        if (category == null) return "";
        
        String lowerCategory = category.toLowerCase();
        
        if (lowerCategory.contains("фильм")) {
            return PREFIX_MOVIES;
        } else if (lowerCategory.contains("сериал")) {
            return PREFIX_TV;
        } else if (lowerCategory.contains("игр")) {
            return PREFIX_GAMES;
        } else if (lowerCategory.contains("книг")) {
            return PREFIX_BOOKS;
        } else if (lowerCategory.contains("аниме")) {
            return PREFIX_ANIME;
        }
        
        return "";
    }
    
    /**
     * Получает все метрики аналитики
     * @param context контекст приложения
     * @return карта с метриками
     */
    public static Map<String, Integer> getAllMetrics(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Map<String, Integer> metrics = new HashMap<>();
        
        // Получаем все метрики из SharedPreferences
        Map<String, ?> allPrefs = prefs.getAll();
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                metrics.put(entry.getKey(), (Integer) entry.getValue());
            }
        }
        
        return metrics;
    }
    
    /**
     * Очищает все метрики аналитики
     * @param context контекст приложения
     */
    public static void clearAllMetrics(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        
        Log.d(TAG, "Все метрики аналитики очищены");
    }
}