package com.draker.swipetime.utils;

import android.text.TextUtils;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Утилитарный класс для работы с изображениями
 */
public class ImageUtil {
    private static final String TAG = "ImageUtil";
    
    // Регулярное выражение для проверки URL изображений
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    
    // Список допустимых расширений файлов изображений
    private static final String[] IMAGE_EXTENSIONS = {
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    };
    
    /**
     * Проверить, является ли строка корректным URL изображения
     * @param url строка URL
     * @return true, если URL валидный и, возможно, указывает на изображение
     */
    public static boolean isValidImageUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        
        // Проверяем формат URL с помощью регулярного выражения
        if (!URL_PATTERN.matcher(url).matches()) {
            return false;
        }
        
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }
        
        // Проверяем наличие расширения файла (не всегда надежно, но может помочь)
        boolean hasImageExtension = false;
        String lowerCaseUrl = url.toLowerCase();
        for (String ext : IMAGE_EXTENSIONS) {
            if (lowerCaseUrl.contains(ext)) {
                hasImageExtension = true;
                break;
            }
        }
        
        return hasImageExtension || isApiImageUrl(url);
    }
    
    /**
     * Проверить, является ли URL адресом изображения из известного API
     * @param url строка URL
     * @return true, если URL относится к известному API изображений
     */
    private static boolean isApiImageUrl(String url) {
        // Проверяем, относится ли URL к известным API изображений
        return url.contains("image.tmdb.org") ||         // TMDb
               url.contains("media.rawg.io") ||          // RAWG
               url.contains("books.google.com") ||       // Google Books
               url.contains("cdn.animenewsnetwork.com") || // Anime News Network
               url.contains("cdn.myanimelist.net") ||    // MyAnimeList
               url.contains("media-amazon.com") ||       // Amazon
               url.contains("images-na.ssl-images-amazon.com") || // Amazon
               url.contains("i.pravatar.cc") ||          // Pravatar (аватары)
               url.contains("api.jikan.moe") ||          // Jikan API
               url.contains("cloudflare.steamstatic.com") || // Steam
               url.contains("steamuserimages") ||        // Steam
               url.contains("assets.nintendo.com");      // Nintendo
    }
    
    /**
     * Получить URL изображения заглушки, если оригинальный URL недействителен
     * @param originalUrl оригинальный URL или null
     * @param category категория контента
     * @return URL изображения заглушки
     */
    public static String getFallbackImageUrl(String originalUrl, String category) {
        // Если оригинальный URL валидный, возвращаем его
        if (isValidImageUrl(originalUrl)) {
            return originalUrl;
        }
        
        // URL изображений заглушек для разных категорий
        switch (category.toLowerCase()) {
            case "фильмы":
            case "movie":
                return "https://i.pravatar.cc/300?img=9"; // Заглушка для фильмов
            case "сериалы":
            case "tv_show":
                return "https://i.pravatar.cc/300?img=10"; // Заглушка для сериалов
            case "игры":
            case "game":
                return "https://i.pravatar.cc/300?img=11"; // Заглушка для игр
            case "книги":
            case "book":
                return "https://i.pravatar.cc/300?img=12"; // Заглушка для книг
            case "аниме":
            case "anime":
                return "https://i.pravatar.cc/300?img=13"; // Заглушка для аниме
            case "музыка":
            case "music":
                return "https://i.pravatar.cc/300?img=14"; // Заглушка для музыки
            default:
                return "https://i.pravatar.cc/300?img=15"; // Общая заглушка
        }
    }
}