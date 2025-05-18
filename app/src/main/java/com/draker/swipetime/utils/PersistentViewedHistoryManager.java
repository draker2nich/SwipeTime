package com.draker.swipetime.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.draker.swipetime.models.ContentItem;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Менеджер для сохранения истории просмотренного контента между запусками приложения
 */
public class PersistentViewedHistoryManager {
    private static final String TAG = "ViewedHistoryManager";
    private static final String PREFS_NAME = "viewed_history_prefs";
    private static final String PREF_KEY_PREFIX = "viewed_items_";
    private static final String PREF_KEY_DISLIKED_PREFIX = "disliked_items_";
    private static final String PREF_KEY_LIKED_PREFIX = "liked_items_";
    private static final int MAX_HISTORY_ITEMS = 1000; // Максимальное количество ID в истории для каждой категории
    
    private static PersistentViewedHistoryManager instance;
    
    private PersistentViewedHistoryManager() {
        // Приватный конструктор для Singleton
    }
    
    /**
     * Получить экземпляр менеджера истории просмотров
     */
    public static synchronized PersistentViewedHistoryManager getInstance() {
        if (instance == null) {
            instance = new PersistentViewedHistoryManager();
        }
        return instance;
    }
    
    /**
     * Добавляет элемент в историю просмотренных
     * @param context контекст приложения
     * @param category категория контента
     * @param itemId ID элемента
     * @param isLiked true если элемент понравился, false если нет
     */
    public void addToViewedHistory(Context context, String category, String itemId, boolean isLiked) {
        try {
            // Общая история просмотров
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String key = PREF_KEY_PREFIX + normalizeKey(category);
            Set<String> viewedItems = getStringSet(prefs, key);
            
            // Добавляем новый ID в историю
            viewedItems.add(itemId);
            
            // Ограничиваем размер истории
            if (viewedItems.size() > MAX_HISTORY_ITEMS) {
                Set<String> newSet = new HashSet<>();
                String[] itemsArray = viewedItems.toArray(new String[0]);
                for (int i = itemsArray.length - MAX_HISTORY_ITEMS; i < itemsArray.length; i++) {
                    newSet.add(itemsArray[i]);
                }
                viewedItems = newSet;
            }
            
            // Сохраняем обновленный набор
            prefs.edit().putStringSet(key, viewedItems).apply();
            
            // Сохраняем также в историю лайков/дизлайков
            if (isLiked) {
                String likedKey = PREF_KEY_LIKED_PREFIX + normalizeKey(category);
                Set<String> likedItems = getStringSet(prefs, likedKey);
                likedItems.add(itemId);
                prefs.edit().putStringSet(likedKey, likedItems).apply();
            } else {
                String dislikedKey = PREF_KEY_DISLIKED_PREFIX + normalizeKey(category);
                Set<String> dislikedItems = getStringSet(prefs, dislikedKey);
                dislikedItems.add(itemId);
                prefs.edit().putStringSet(dislikedKey, dislikedItems).apply();
            }
            
            Log.d(TAG, "Добавлен элемент " + itemId + " категории " + category + " в историю просмотров");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при сохранении истории просмотров: " + e.getMessage());
        }
    }
    
    /**
     * Проверяет, был ли элемент уже просмотрен
     * @param context контекст приложения
     * @param category категория контента
     * @param itemId ID элемента
     * @return true если элемент уже был просмотрен
     */
    public boolean hasBeenViewed(Context context, String category, String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return false;
        }
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String key = PREF_KEY_PREFIX + normalizeKey(category);
            Set<String> viewedItems = getStringSet(prefs, key);
            return viewedItems.contains(itemId);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при проверке истории просмотров: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Получает все просмотренные элементы для указанной категории
     * @param context контекст приложения
     * @param category категория контента
     * @return набор ID просмотренных элементов
     */
    public Set<String> getAllViewedItems(Context context, String category) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String key = PREF_KEY_PREFIX + normalizeKey(category);
            return getStringSet(prefs, key);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении истории просмотров: " + e.getMessage());
            return new HashSet<>();
        }
    }
    
    /**
     * Получает все понравившиеся элементы для указанной категории
     * @param context контекст приложения
     * @param category категория контента
     * @return набор ID понравившихся элементов
     */
    public Set<String> getLikedItems(Context context, String category) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String key = PREF_KEY_LIKED_PREFIX + normalizeKey(category);
            return getStringSet(prefs, key);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении истории лайков: " + e.getMessage());
            return new HashSet<>();
        }
    }
    
    /**
     * Получает все непонравившиеся элементы для указанной категории
     * @param context контекст приложения
     * @param category категория контента
     * @return набор ID непонравившихся элементов
     */
    public Set<String> getDislikedItems(Context context, String category) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String key = PREF_KEY_DISLIKED_PREFIX + normalizeKey(category);
            return getStringSet(prefs, key);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении истории дизлайков: " + e.getMessage());
            return new HashSet<>();
        }
    }
    
    /**
     * Сбрасывает историю просмотров для указанной категории
     * @param context контекст приложения
     * @param category категория контента
     */
    public void clearHistory(Context context, String category) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String viewedKey = PREF_KEY_PREFIX + normalizeKey(category);
            String likedKey = PREF_KEY_LIKED_PREFIX + normalizeKey(category);
            String dislikedKey = PREF_KEY_DISLIKED_PREFIX + normalizeKey(category);
            
            prefs.edit()
                .remove(viewedKey)
                .remove(likedKey)
                .remove(dislikedKey)
                .apply();
            
            Log.d(TAG, "История просмотров для категории " + category + " очищена");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при очистке истории просмотров: " + e.getMessage());
        }
    }
    
    /**
     * Сбрасывает всю историю просмотров
     * @param context контекст приложения
     */
    public void clearAllHistory(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
            Log.d(TAG, "Вся история просмотров очищена");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при очистке всей истории просмотров: " + e.getMessage());
        }
    }
    
    /**
     * Преобразует ключ категории для использования в SharedPreferences
     * @param category категория контента
     * @return нормализованный ключ
     */
    private String normalizeKey(String category) {
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
     * Получает Set<String> из SharedPreferences с обработкой по умолчанию
     * @param prefs SharedPreferences
     * @param key ключ
     * @return набор строк
     */
    private Set<String> getStringSet(SharedPreferences prefs, String key) {
        Set<String> defaultSet = new HashSet<>();
        Set<String> result = prefs.getStringSet(key, defaultSet);
        
        // Создаем новую копию для изменяемого набора
        if (result == defaultSet || result.isEmpty()) {
            return new HashSet<>();
        } else {
            return new HashSet<>(result);
        }
    }
}