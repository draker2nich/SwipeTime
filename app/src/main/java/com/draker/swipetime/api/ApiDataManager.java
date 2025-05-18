package com.draker.swipetime.api;

import android.util.Log;

import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс для управления данными, загружаемыми из API и предотвращения дубликатов
 */
public class ApiDataManager {
    private static final String TAG = "ApiDataManager";
    
    // Синглтон
    private static volatile ApiDataManager instance;
    
    // Наборы для отслеживания уже загруженных элементов контента по категориям
    private final Map<String, Set<String>> loadedContentIds = new ConcurrentHashMap<>();
    
    // Карта для кеширования PageTokens для категорий
    private final Map<String, Integer> nextPageTokens = new ConcurrentHashMap<>();
    
    private ApiDataManager() {
        // Приватный конструктор для синглтона
        loadedContentIds.put("movie", new HashSet<>());
        loadedContentIds.put("tv_show", new HashSet<>());
        loadedContentIds.put("game", new HashSet<>());
        loadedContentIds.put("book", new HashSet<>());
        loadedContentIds.put("anime", new HashSet<>());
        
        nextPageTokens.put("movie", 1);
        nextPageTokens.put("tv_show", 1);
        nextPageTokens.put("game", 1);
        nextPageTokens.put("book", 1);
        nextPageTokens.put("anime", 1);
    }
    
    /**
     * Получить экземпляр ApiDataManager
     * @return экземпляр ApiDataManager
     */
    public static ApiDataManager getInstance() {
        if (instance == null) {
            synchronized (ApiDataManager.class) {
                if (instance == null) {
                    instance = new ApiDataManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Получить следующий номер страницы для категории
     * @param category категория контента
     * @return номер страницы
     */
    public int getNextPageToken(String category) {
        int token = nextPageTokens.getOrDefault(category, 1);
        
        // Инкрементируем токен для следующего запроса
        nextPageTokens.put(category, token + 1);
        
        return token;
    }
    
    /**
     * Установить следующий номер страницы для категории
     * @param category категория контента
     * @param token номер страницы
     */
    public void setNextPageToken(String category, int token) {
        nextPageTokens.put(category, token);
    }
    
    /**
     * Сбросить номер страницы для категории (начать с начала)
     * @param category категория контента
     */
    public void resetPageToken(String category) {
        nextPageTokens.put(category, 1);
    }
    
    /**
     * Сбросить все номера страниц
     */
    public void resetAllPageTokens() {
        for (String key : nextPageTokens.keySet()) {
            nextPageTokens.put(key, 1);
        }
    }
    
    /**
     * Отфильтровать уже загруженные элементы контента
     * @param category категория контента
     * @param entities список элементов
     * @param <T> тип элемента
     * @return отфильтрованный список элементов
     */
    public <T extends ContentEntity> List<T> filterAlreadyLoaded(String category, List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        
        Set<String> loadedIds = loadedContentIds.getOrDefault(category, new HashSet<>());
        List<T> uniqueEntities = new ArrayList<>();
        
        for (T entity : entities) {
            if (!loadedIds.contains(entity.getId())) {
                uniqueEntities.add(entity);
                loadedIds.add(entity.getId());
            }
        }
        
        // Обновляем набор загруженных идентификаторов
        loadedContentIds.put(category, loadedIds);
        
        Log.d(TAG, "Категория " + category + ": получено " + entities.size() + 
                " элементов, отфильтровано " + (entities.size() - uniqueEntities.size()) + 
                " дубликатов, добавлено " + uniqueEntities.size() + " уникальных элементов");
        
        return uniqueEntities;
    }
    
    /**
     * Отметить список элементов как загруженные
     * @param category категория контента
     * @param entities список элементов
     * @param <T> тип элемента
     */
    public <T extends ContentEntity> void markAsLoaded(String category, List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        
        Set<String> loadedIds = loadedContentIds.getOrDefault(category, new HashSet<>());
        
        for (T entity : entities) {
            loadedIds.add(entity.getId());
        }
        
        loadedContentIds.put(category, loadedIds);
        
        Log.d(TAG, "Добавлено " + entities.size() + " элементов в список загруженных для категории " + category);
    }
    
    /**
     * Отметить элемент как загруженный
     * @param category категория контента
     * @param entity элемент
     * @param <T> тип элемента
     */
    public <T extends ContentEntity> void markAsLoaded(String category, T entity) {
        if (entity == null) {
            return;
        }
        
        Set<String> loadedIds = loadedContentIds.getOrDefault(category, new HashSet<>());
        loadedIds.add(entity.getId());
        loadedContentIds.put(category, loadedIds);
    }
    
    /**
     * Проверить, загружен ли уже элемент
     * @param category категория контента
     * @param id идентификатор элемента
     * @return true, если элемент уже загружен
     */
    public boolean isAlreadyLoaded(String category, String id) {
        Set<String> loadedIds = loadedContentIds.getOrDefault(category, new HashSet<>());
        return loadedIds.contains(id);
    }
    
    /**
     * Сбросить отслеживание загруженных элементов для категории
     * @param category категория контента
     */
    public void resetLoadedItems(String category) {
        if (loadedContentIds.containsKey(category)) {
            loadedContentIds.get(category).clear();
            Log.d(TAG, "Сброшен кеш загруженных элементов для категории " + category);
        }
    }
    
    /**
     * Сбросить отслеживание всех загруженных элементов
     */
    public void resetAllLoadedItems() {
        for (Set<String> ids : loadedContentIds.values()) {
            ids.clear();
        }
        Log.d(TAG, "Сброшены все кеши загруженных элементов");
    }
    
    /**
     * Получить количество загруженных элементов по категории
     * @param category категория контента
     * @return количество загруженных элементов
     */
    public int getLoadedCount(String category) {
        return loadedContentIds.getOrDefault(category, new HashSet<>()).size();
    }
    
    /**
     * Получить общее количество загруженных элементов
     * @return общее количество загруженных элементов
     */
    public int getTotalLoadedCount() {
        int total = 0;
        for (Set<String> ids : loadedContentIds.values()) {
            total += ids.size();
        }
        return total;
    }
}
