package com.draker.swipetime.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.draker.swipetime.R;

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
     * Определить, является ли изображение горизонтальным (для игр)
     * @param url строка URL
     * @return true, если это скорее всего горизонтальное изображение
     */
    public static boolean isLikelyHorizontalImage(String url) {
        if (url == null) return false;
        
        // Для RAWG API - часто содержат скриншоты, которые горизонтальные
        // или обложки, которые могут иметь специфический формат
        if (url.contains("media.rawg.io")) {
            return url.contains("screenshots") || 
                  !url.contains("crop/600/400") && !url.contains("crop/400/600");
        }
        
        return false;
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
        
        // URL изображений заглушек для разных категорий (вертикальные постеры)
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
                return "https://i.pravatar.cc/300?img=15"; // Общая заглушка
        }
    }
    
    /**
     * Загрузить изображение в ImageView с учетом особенностей категории (карточки)
     * @param context контекст
     * @param imageUrl URL изображения
     * @param imageView целевой ImageView
     * @param category категория контента
     */
    public static void loadCardImage(Context context, String imageUrl, ImageView imageView, String category) {
        // Проверяем валидность URL изображения и используем заглушки при необходимости
        String finalImageUrl = getFallbackImageUrl(imageUrl, category);
        
        try {
            // Для игр проверяем формат изображения и применяем специальную обработку
            if ((category.equalsIgnoreCase("игры") || category.equalsIgnoreCase("game")) 
                    && isLikelyHorizontalImage(finalImageUrl)) {
                // Для горизонтальных изображений (скриншоты) - обрезаем до квадрата по центру
                Glide.with(context)
                    .load(finalImageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .transform(new CenterCrop())
                    .into(imageView);
            } else {
                // Для остальных категорий - стандартная загрузка
                Glide.with(context)
                    .load(finalImageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(imageView);
            }
        } catch (Exception e) {
            // В случае ошибки показываем заглушку
            imageView.setImageResource(R.drawable.placeholder_image);
            Log.e(TAG, "Error loading image: " + e.getMessage());
        }
    }
}