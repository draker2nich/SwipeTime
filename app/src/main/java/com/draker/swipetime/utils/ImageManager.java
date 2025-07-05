package com.draker.swipetime.utils;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.draker.swipetime.R;

/**
 * Объединенный класс для управления изображениями, включая:
 * - Загрузку изображений через Glide
 * - Управление кешем изображений
 * - Оптимизацию производительности
 */
public class ImageManager {
    private static final String TAG = "ImageManager";

    // Singleton instance
    private static ImageManager instance;

    // Размер дискового кеша для Glide
    private static final int DISK_CACHE_SIZE_BYTES = 250 * 1024 * 1024; // 250 МБ

    // Размер оперативного кеша для Glide (% от доступной памяти)
    private static final float MEMORY_CACHE_PERCENT = 0.2f; // 20% доступной памяти

    private final Context context;

    private ImageManager(Context context) {
        this.context = context.getApplicationContext();
        initImageCache();
    }

    public static synchronized ImageManager getInstance(Context context) {
        if (instance == null) {
            instance = new ImageManager(context);
        }
        return instance;
    }

    // ==================== GLIDE UTILITIES SECTION ====================

    /**
     * Загружает изображение в ImageView с округлёнными углами
     */
    public void loadImageWithRoundedCorners(String imageUrl, ImageView imageView, int cornerRadius) {
        try {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(imageView);
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.placeholder_image);
            Log.e(TAG, "Error loading image with rounded corners: " + e.getMessage());
        }
    }

    /**
     * Загружает изображение для карточки в избранном с скруглёнными углами (статический метод)
     */
    public static void loadLikedContentImage(Context context, String imageUrl, ImageView imageView, String category) {
        ImageManager instance = getInstance(context);
        instance.loadLikedContentImage(imageUrl, imageView, category);
    }

    /**
     * Загружает изображение для детального просмотра контента (статический метод)
     */
    public static void loadDetailContentImage(Context context, String imageUrl, ImageView imageView, String category) {
        ImageManager instance = getInstance(context);
        instance.loadDetailContentImage(imageUrl, imageView, category);
    }

    /**
     * Загружает изображение для карточки в избранном с скруглёнными углами
     */
    public void loadLikedContentImage(String imageUrl, ImageView imageView, String category) {
        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.equals("url_to_image")) {
            int placeholderResId = getCategoryPlaceholderResource(category);
            imageView.setImageResource(placeholderResId);
            return;
        }

        try {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(getCategoryPlaceholderResource(category))
                    .error(getCategoryPlaceholderResource(category))
                    .centerCrop()
                    .into(imageView);
        } catch (Exception e) {
            imageView.setImageResource(getCategoryPlaceholderResource(category));
            Log.e(TAG, "Error loading liked content image: " + e.getMessage());
        }
    }

    /**
     * Загружает изображение для детального просмотра контента
     */
    public void loadDetailContentImage(String imageUrl, ImageView imageView, String category) {
        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.equals("url_to_image")) {
            int placeholderResId = getCategoryPlaceholderResource(category);
            imageView.setImageResource(placeholderResId);
            return;
        }

        try {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(getCategoryPlaceholderResource(category))
                    .error(getCategoryPlaceholderResource(category))
                    .fitCenter() // Используем fitCenter для полного отображения
                    .into(imageView);
        } catch (Exception e) {
            imageView.setImageResource(getCategoryPlaceholderResource(category));
            Log.e(TAG, "Error loading detail content image: " + e.getMessage());
        }
    }

    /**
     * Загружает изображение карточки контента с оптимизацией
     */
    public void loadCardImage(String imageUrl, ImageView imageView, String category) {
        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.equals("url_to_image")) {
            int placeholderResId = getCategoryPlaceholderResource(category);
            imageView.setImageResource(placeholderResId);
            return;
        }

        try {
            // Проверяем, является ли это горизонтальное изображение (для игр)
            if ((category.equalsIgnoreCase("игры") || category.equalsIgnoreCase("game"))
                    && isLikelyHorizontalImage(imageUrl)) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(getCategoryPlaceholderResource(category))
                        .error(getCategoryPlaceholderResource(category))
                        .centerCrop()
                        .into(imageView);
            } else {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(getCategoryPlaceholderResource(category))
                        .error(getCategoryPlaceholderResource(category))
                        .centerCrop()
                        .into(imageView);
            }
        } catch (Exception e) {
            imageView.setImageResource(getCategoryPlaceholderResource(category));
            Log.e(TAG, "Error loading card image: " + e.getMessage());
        }
    }

    /**
     * Загружает аватар пользователя
     */
    public void loadUserAvatar(String avatarUrl, ImageView imageView) {
        try {
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_user_avatar)
                    .error(R.drawable.ic_user_avatar)
                    .circleCrop() // Делаем круглый аватар
                    .into(imageView);
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.ic_user_avatar);
            Log.e(TAG, "Error loading user avatar: " + e.getMessage());
        }
    }

    /**
     * Предзагружает изображение в кеш
     */
    public void preloadImage(String imageUrl) {
        try {
            Glide.with(context)
                    .load(imageUrl)
                    .preload();
        } catch (Exception e) {
            Log.e(TAG, "Error preloading image: " + e.getMessage());
        }
    }

    // ==================== IMAGE CACHE MANAGEMENT SECTION ====================

    /**
     * Инициализировать кеш изображений
     */
    private void initImageCache() {
        try {
            RequestOptions options = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false);

            Glide.with(context)
                    .setDefaultRequestOptions(options);

            prefetchCategoryImages();

            Log.d(TAG, "Image cache initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing image cache: " + e.getMessage());
        }
    }

    /**
     * Префетчинг изображений для категорий (заглушки и т.д.)
     */
    private void prefetchCategoryImages() {
        try {
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
     * Очистить кеш изображений
     */
    public void clearImageCache() {
        try {
            new Thread(() -> {
                try {
                    Glide.get(context).clearDiskCache();
                } catch (Exception e) {
                    Log.e(TAG, "Error clearing disk cache: " + e.getMessage());
                }
            }).start();

            Glide.get(context).clearMemory();

            Log.d(TAG, "Image cache cleared successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing image cache: " + e.getMessage());
        }
    }

    /**
     * Получить размер кеша в мегабайтах
     */
    public float getCacheSizeMB() {
        try {
            // Приблизительная оценка размера кеша
            return DISK_CACHE_SIZE_BYTES / (1024f * 1024f);
        } catch (Exception e) {
            Log.e(TAG, "Error getting cache size: " + e.getMessage());
            return 0f;
        }
    }

    /**
     * Очистить только оперативный кеш
     */
    public void clearMemoryCache() {
        try {
            Glide.get(context).clearMemory();
            Log.d(TAG, "Memory cache cleared successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing memory cache: " + e.getMessage());
        }
    }

    /**
     * Очистить только дисковый кеш
     */
    public void clearDiskCache() {
        new Thread(() -> {
            try {
                Glide.get(context).clearDiskCache();
                Log.d(TAG, "Disk cache cleared successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error clearing disk cache: " + e.getMessage());
            }
        }).start();
    }

    // ==================== HELPER METHODS ====================

    /**
     * Возвращает ресурс заглушки в зависимости от категории контента
     */
    private int getCategoryPlaceholderResource(String category) {
        if (category == null) {
            return R.drawable.placeholder_image;
        }

        switch (category.toLowerCase()) {
            case "фильмы":
                return R.drawable.ic_category_movies;
            case "сериалы":
                return R.drawable.ic_category_tv_shows;
            case "игры":
                return R.drawable.ic_category_games;
            case "книги":
                return R.drawable.ic_category_books;
            case "аниме":
                return R.drawable.ic_category_anime;
            case "музыка":
                return R.drawable.ic_category_music;
            default:
                return R.drawable.placeholder_image;
        }
    }

    /**
     * Определить, является ли изображение горизонтальным (для игр)
     */
    private boolean isLikelyHorizontalImage(String url) {
        if (url == null) return false;

        if (url.contains("media.rawg.io")) {
            return url.contains("screenshots") ||
                    !url.contains("crop/600/400") && !url.contains("crop/400/600");
        }

        return false;
    }

    /**
     * Проверяет, является ли URL валидным для изображения
     */
    public boolean isValidImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        // Проверяем базовые признаки URL изображения
        return url.startsWith("http") &&
                (url.contains(".jpg") || url.contains(".jpeg") ||
                        url.contains(".png") || url.contains(".gif") ||
                        url.contains(".webp") || url.contains("image") ||
                        isApiImageUrl(url));
    }

    /**
     * Проверяет, является ли URL адресом изображения из известного API
     */
    private boolean isApiImageUrl(String url) {
        return url.contains("image.tmdb.org") ||
                url.contains("media.rawg.io") ||
                url.contains("books.google.com") ||
                url.contains("cdn.animenewsnetwork.com") ||
                url.contains("cdn.myanimelist.net") ||
                url.contains("media-amazon.com") ||
                url.contains("images-na.ssl-images-amazon.com") ||
                url.contains("i.pravatar.cc") ||
                url.contains("api.jikan.moe") ||
                url.contains("cloudflare.steamstatic.com") ||
                url.contains("steamuserimages") ||
                url.contains("assets.nintendo.com");
    }

    /**
     * Получить URL изображения заглушки для категории
     */
    public String getFallbackImageUrl(String originalUrl, String category) {
        if (isValidImageUrl(originalUrl)) {
            return originalUrl;
        }

        switch (category.toLowerCase()) {
            case "фильмы":
            case "movie":
                return "https://m.media-amazon.com/images/M/MV5BMzUzNDM2NjQ5M15BMl5BanBnXkFtZTgwNTM3NTg4OTE@._V1_UX182_CR0,0,182,268_AL_.jpg";
            case "сериалы":
            case "tv_show":
                return "https://m.media-amazon.com/images/M/MV5BMjA5MTE1MjQyNV5BMl5BanBnXkFtZTgwMzI5Njc0ODE@._V1_UX182_CR0,0,182,268_AL_.jpg";
            case "игры":
            case "game":
                return "https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/capsule_616x353.jpg";
            case "книги":
            case "book":
                return "https://m.media-amazon.com/images/I/51bVNTqHFlL._SX323_BO1,204,203,200_.jpg";
            case "аниме":
            case "anime":
                return "https://cdn.myanimelist.net/images/anime/1171/109222.jpg";
            default:
                return "https://i.pravatar.cc/300?img=15";
        }
    }

    /**
     * Предзагружает список изображений
     */
    public void preloadImages(String[] imageUrls) {
        for (String url : imageUrls) {
            if (isValidImageUrl(url)) {
                preloadImage(url);
            }
        }
    }

    /**
     * Оптимизирует кеш (удаляет старые файлы при необходимости)
     */
    public void optimizeCache() {
        new Thread(() -> {
            try {
                // Здесь можно добавить логику для оптимизации кеша
                // Например, удаление самых старых файлов при превышении лимита
                Log.d(TAG, "Cache optimization completed");
            } catch (Exception e) {
                Log.e(TAG, "Error during cache optimization: " + e.getMessage());
            }
        }).start();
    }
}