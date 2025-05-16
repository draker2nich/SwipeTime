package com.draker.swipetime.utils;

import android.util.Log;

/**
 * Утилитный класс для логирования действий пользователя
 */
public class ActionLogger {
    
    private static final String TAG = "SwipeTimeActionLog";
    
    /**
     * Логировать действие свайпа
     * @param direction направление свайпа (true - вправо, false - влево)
     * @param contentId ID контента
     * @param contentTitle название контента
     */
    public static void logSwipe(boolean direction, String contentId, String contentTitle) {
        String message = "Пользователь свайпнул " + (direction ? "ВПРАВО" : "ВЛЕВО") + 
                       " контент: [" + contentId + "] " + contentTitle;
        Log.i(TAG, message);
    }
    
    /**
     * Логировать действие оценки контента
     * @param contentId ID контента
     * @param contentTitle название контента
     * @param rating оценка
     */
    public static void logRating(String contentId, String contentTitle, float rating) {
        String message = "Пользователь оценил контент [" + contentId + "] " + 
                       contentTitle + " на " + rating + " звезд";
        Log.i(TAG, message);
    }
    
    /**
     * Логировать действие написания рецензии
     * @param contentId ID контента
     * @param contentTitle название контента
     */
    public static void logReview(String contentId, String contentTitle) {
        String message = "Пользователь написал рецензию на контент [" + contentId + "] " + contentTitle;
        Log.i(TAG, message);
    }
    
    /**
     * Логировать действие отметки о завершении (просмотр/прочтение и т.д.)
     * @param contentId ID контента
     * @param contentTitle название контента
     * @param contentType тип контента (фильм, книга и т.д.)
     */
    public static void logCompleted(String contentId, String contentTitle, String contentType) {
        String action = getCompletionVerbByType(contentType);
        String message = "Пользователь отметил как " + action + " контент [" + 
                       contentId + "] " + contentTitle;
        Log.i(TAG, message);
    }
    
    /**
     * Логировать получение достижения
     * @param achievementId ID достижения
     * @param achievementTitle название достижения
     * @param experienceGained полученный опыт
     */
    public static void logAchievement(String achievementId, String achievementTitle, int experienceGained) {
        String message = "Пользователь получил достижение [" + achievementId + "] " + 
                       achievementTitle + " (+" + experienceGained + " XP)";
        Log.i(TAG, message);
    }
    
    /**
     * Логировать повышение уровня
     * @param oldLevel предыдущий уровень
     * @param newLevel новый уровень
     * @param rank новое звание
     */
    public static void logLevelUp(int oldLevel, int newLevel, String rank) {
        String message = "Пользователь повысил уровень с " + oldLevel + " до " + 
                       newLevel + " (звание: " + rank + ")";
        Log.i(TAG, message);
    }
    
    /**
     * Определяет глагол для типа контента
     * @param contentType тип контента
     * @return глагол (просмотренный, прочитанный и т.д.)
     */
    private static String getCompletionVerbByType(String contentType) {
        switch (contentType.toLowerCase()) {
            case "фильмы":
            case "movies":
            case "сериалы":
            case "tvshows":
            case "аниме":
            case "anime":
                return "просмотренный";
            case "книги":
            case "books":
                return "прочитанный";
            case "игры":
            case "games":
                return "пройденный";
            case "музыка":
            case "music":
                return "прослушанный";
            default:
                return "завершенный";
        }
    }
}