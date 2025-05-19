package com.draker.swipetime.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.draker.swipetime.R;

/**
 * Утилитный класс для работы с Glide
 */
public class GlideUtils {

    /**
     * Загружает изображение в ImageView с округлёнными углами
     * @param context контекст приложения
     * @param imageUrl URL изображения
     * @param imageView целевое представление
     * @param cornerRadius радиус скругления углов (в пикселях)
     */
    public static void loadImageWithRoundedCorners(Context context, String imageUrl, ImageView imageView, int cornerRadius) {
        try {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(imageView);
        } catch (Exception e) {
            // Установка изображения заглушки в случае ошибки
            imageView.setImageResource(R.drawable.placeholder_image);
        }
    }

    /**
     * Загружает изображение для карточки в избранном с скруглёнными углами
     * @param context контекст приложения
     * @param imageUrl URL изображения
     * @param imageView целевое представление
     * @param category категория контента (для выбора подходящей заглушки)
     */
    public static void loadLikedContentImage(Context context, String imageUrl, ImageView imageView, String category) {
        // Проверяем наличие URL
        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.equals("url_to_image")) {
            // Выбираем заглушку в зависимости от категории
            int placeholderResId = getCategoryPlaceholderResource(category);
            imageView.setImageResource(placeholderResId);
            return;
        }

        try {
            // Загружаем изображение с простыми настройками для совместимости
            Glide.with(context)
                .load(imageUrl)
                .placeholder(getCategoryPlaceholderResource(category))
                .error(getCategoryPlaceholderResource(category))
                .centerCrop()
                .into(imageView);
        } catch (Exception e) {
            // Устанавливаем заглушку в случае ошибки
            imageView.setImageResource(getCategoryPlaceholderResource(category));
        }
    }
    
    /**
     * Загружает изображение для детального просмотра контента
     * @param context контекст приложения
     * @param imageUrl URL изображения
     * @param imageView целевое представление
     * @param category категория контента (для выбора подходящей заглушки)
     */
    public static void loadDetailContentImage(Context context, String imageUrl, ImageView imageView, String category) {
        // Проверяем наличие URL
        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.equals("url_to_image")) {
            // Выбираем заглушку в зависимости от категории
            int placeholderResId = getCategoryPlaceholderResource(category);
            imageView.setImageResource(placeholderResId);
            return;
        }

        try {
            // Загружаем изображение с детальными настройками для высокого качества
            Glide.with(context)
                .load(imageUrl)
                .placeholder(getCategoryPlaceholderResource(category))
                .error(getCategoryPlaceholderResource(category))
                .fitCenter() // Используем fitCenter вместо centerCrop для полного отображения
                .into(imageView);
        } catch (Exception e) {
            // Устанавливаем заглушку в случае ошибки
            imageView.setImageResource(getCategoryPlaceholderResource(category));
        }
    }

    /**
     * Возвращает ресурс заглушки в зависимости от категории контента
     * @param category категория контента
     * @return ID ресурса для заглушки
     */
    private static int getCategoryPlaceholderResource(String category) {
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
}
