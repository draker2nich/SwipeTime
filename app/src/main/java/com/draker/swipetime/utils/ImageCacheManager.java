package com.draker.swipetime.utils;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

/**
 * Менеджер кеширования изображений для улучшения производительности и экономии трафика
 */
public class ImageCacheManager {
    private static final String TAG = "ImageCacheManager";
    
    // Размер дискового кеша для Glide
    private static final int DISK_CACHE_SIZE_BYTES = 250 * 1024 * 1024; // 250 МБ
    
    // Размер оперативного кеша для Glide (% от доступной памяти)
    private static final float MEMORY_CACHE_PERCENT = 0.2f; // 20% доступной памяти
    
    /**
     * Инициализировать кеш изображений
     * @param context контекст приложения
     */
    public static void initImageCache(Context context) {
        try {
            // Настраиваем кеширование для Glide
            RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false);

            // Применяем настройки
            Glide.with(context)
                .setDefaultRequestOptions(options);
            
            // Префетчинг для категорий
            prefetchCategoryImages(context);
            
            Log.d(TAG, "Image cache initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing image cache: " + e.getMessage());
        }
    }
    
    /**
     * Префетчинг изображений для категорий (заглушки и т.д.)
     * @param context контекст приложения
     */
    private static void prefetchCategoryImages(Context context) {
        try {
            // Загружаем заглушки для разных категорий
            String[] placeholders = {
                "https://i.pravatar.cc/300?img=9",  // Фильмы
                "https://i.pravatar.cc/300?img=10", // Сериалы
                "https://i.pravatar.cc/300?img=11", // Игры
                "https://i.pravatar.cc/300?img=12", // Книги
                "https://i.pravatar.cc/300?img=13", // Аниме
                "https://i.pravatar.cc/300?img=14", // Музыка
                "https://i.pravatar.cc/300?img=15"  // Общая заглушка
            };
            
            for (String url : placeholders) {
                Glide.with(context)
                    .load(url)
                    .preload();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during prefetching images: " + e.getMessage());
        }
    }
    
    /**
     * Очистить кеш изображений (используется при нехватке памяти или в настройках)
     * @param context контекст приложения
     */
    public static void clearImageCache(Context context) {
        try {
            // Очищаем память и диск
            new Thread(() -> {
                try {
                    Glide.get(context).clearDiskCache();
                } catch (Exception e) {
                    Log.e(TAG, "Error clearing disk cache: " + e.getMessage());
                }
            }).start();
            
            // Очищаем оперативную память (должно выполняться в главном потоке)
            Glide.get(context).clearMemory();
            
            Log.d(TAG, "Image cache cleared successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing image cache: " + e.getMessage());
        }
    }
}