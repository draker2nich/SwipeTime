package com.draker.swipetime.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.repository.ContentRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Класс для управления избранным контентом с поддержкой сохранения между запусками
 */
public class PersistentFavoritesManager {
    private static final String TAG = "PersistentFavManager";
    private static final String PREFS_NAME = "favorites_prefs";
    private static final String KEY_FAVORITE_IDS = "favorite_content_ids";
    
    private final Context context;
    private final SharedPreferences preferences;
    private final ContentRepository contentRepository;
    
    public PersistentFavoritesManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.contentRepository = new ContentRepository(context.getApplicationContext());
    }
    
    /**
     * Добавить элемент в избранное и сохранить состояние
     * @param contentId ID контента
     * @return true, если операция успешна
     */
    public boolean addToFavorites(String contentId) {
        boolean result = contentRepository.updateAndPersistLikedStatus(contentId, true);
        if (result) {
            saveFavoriteIdToPrefs(contentId);
        }
        return result;
    }
    
    /**
     * Удалить элемент из избранного
     * @param contentId ID контента
     * @return true, если операция успешна
     */
    public boolean removeFromFavorites(String contentId) {
        boolean result = contentRepository.updateAndPersistLikedStatus(contentId, false);
        if (result) {
            removeFavoriteIdFromPrefs(contentId);
        }
        return result;
    }
    
    /**
     * Сохранить ID избранного элемента в SharedPreferences
     * @param contentId ID контента
     */
    private void saveFavoriteIdToPrefs(String contentId) {
        try {
            Set<String> favoriteIds = getFavoriteIdsFromPrefs();
            favoriteIds.add(contentId);
            
            SharedPreferences.Editor editor = preferences.edit();
            editor.putStringSet(KEY_FAVORITE_IDS, favoriteIds);
            editor.apply();
            
            Log.d(TAG, "ID избранного контента сохранен: " + contentId);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при сохранении ID избранного контента: " + e.getMessage());
        }
    }
    
    /**
     * Удалить ID избранного элемента из SharedPreferences
     * @param contentId ID контента
     */
    private void removeFavoriteIdFromPrefs(String contentId) {
        try {
            Set<String> favoriteIds = getFavoriteIdsFromPrefs();
            favoriteIds.remove(contentId);
            
            SharedPreferences.Editor editor = preferences.edit();
            editor.putStringSet(KEY_FAVORITE_IDS, favoriteIds);
            editor.apply();
            
            Log.d(TAG, "ID избранного контента удален: " + contentId);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при удалении ID избранного контента: " + e.getMessage());
        }
    }
    
    /**
     * Получить набор ID избранных элементов из SharedPreferences
     * @return Set с ID избранных элементов
     */
    private Set<String> getFavoriteIdsFromPrefs() {
        return new HashSet<>(preferences.getStringSet(KEY_FAVORITE_IDS, new HashSet<>()));
    }
    
    /**
     * Восстановить состояние всех избранных элементов из SharedPreferences
     * @return количество восстановленных элементов
     */
    public int restoreFavoritesState() {
        int restoredCount = 0;
        try {
            Set<String> favoriteIds = getFavoriteIdsFromPrefs();
            Log.d(TAG, "Восстановление избранного, найдено ID: " + favoriteIds.size());
            
            for (String contentId : favoriteIds) {
                ContentEntity content = contentRepository.getById(contentId);
                if (content != null) {
                    content.setLiked(true);
                    contentRepository.update(content);
                    restoredCount++;
                    Log.d(TAG, "Восстановлено избранное: " + content.getTitle());
                }
            }
            
            Log.d(TAG, "Восстановлено избранных элементов: " + restoredCount);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при восстановлении избранного: " + e.getMessage());
        }
        return restoredCount;
    }
    
    /**
     * Синхронизировать состояние избранного между БД и SharedPreferences
     */
    public void syncFavoritesState() {
        try {
            // Получаем текущие избранные из базы данных
            List<ContentEntity> likedContent = contentRepository.getLiked();
            Set<String> favoritesInDb = new HashSet<>();
            
            // Собираем ID всех избранных в базе
            for (ContentEntity content : likedContent) {
                favoritesInDb.add(content.getId());
            }
            
            // Получаем сохраненные ID из SharedPreferences
            Set<String> favoritesInPrefs = getFavoriteIdsFromPrefs();
            
            // Добавляем в SharedPreferences ID тех элементов, которые есть в базе, но нет в SharedPreferences
            Set<String> toAddToPrefs = new HashSet<>(favoritesInDb);
            toAddToPrefs.removeAll(favoritesInPrefs);
            
            // Удаляем из SharedPreferences ID тех элементов, которых нет в базе
            Set<String> toRemoveFromPrefs = new HashSet<>(favoritesInPrefs);
            toRemoveFromPrefs.removeAll(favoritesInDb);
            
            // Применяем изменения
            Set<String> updatedFavorites = new HashSet<>(favoritesInPrefs);
            updatedFavorites.addAll(toAddToPrefs);
            updatedFavorites.removeAll(toRemoveFromPrefs);
            
            // Сохраняем обновленный набор
            SharedPreferences.Editor editor = preferences.edit();
            editor.putStringSet(KEY_FAVORITE_IDS, updatedFavorites);
            editor.apply();
            
            Log.d(TAG, "Синхронизация избранного: добавлено " + toAddToPrefs.size() + 
                    ", удалено " + toRemoveFromPrefs.size() + 
                    ", всего в избранном " + updatedFavorites.size());
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при синхронизации избранного: " + e.getMessage());
        }
    }
    
    /**
     * Удалить все тестовые контенты из избранного
     * @return количество удаленных тестовых элементов
     */
    public int removeTestFavorites() {
        try {
            Set<String> favoriteIds = getFavoriteIdsFromPrefs();
            Set<String> toRemove = new HashSet<>();
            
            // Находим тестовые ID
            for (String id : favoriteIds) {
                if (id.startsWith("test_")) {
                    toRemove.add(id);
                    Log.d(TAG, "Тестовый элемент будет удален из избранного: " + id);
                }
            }
            
            // Удаляем тестовые ID
            favoriteIds.removeAll(toRemove);
            
            // Сохраняем обновленный набор
            SharedPreferences.Editor editor = preferences.edit();
            editor.putStringSet(KEY_FAVORITE_IDS, favoriteIds);
            editor.apply();
            
            // Также удаляем тестовые элементы из базы данных
            for (String id : toRemove) {
                contentRepository.updateLikedStatus(id, false);
                Log.d(TAG, "Установлен статус 'не избранное' для элемента: " + id);
            }
            
            Log.d(TAG, "Удалено тестовых элементов из избранного: " + toRemove.size());
            return toRemove.size();
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при удалении тестовых данных из избранного: " + e.getMessage());
            return 0;
        }
    }
}
