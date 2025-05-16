package com.draker.swipetime.utils;

import android.content.Context;
import android.widget.Toast;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.entities.AchievementEntity;
import com.draker.swipetime.database.entities.UserEntity;

/**
 * Класс для интеграции системы геймификации в различные компоненты приложения
 */
public class GamificationIntegrator {

    // ID текущего пользователя (в реальном приложении должен быть получен из аутентификации)
    private static final String CURRENT_USER_ID = "user_1";

    // Настройка слушателя для уведомлений о достижениях
    public static void setupAchievementListener(Context context) {
        GamificationManager gamificationManager = GamificationManager.getInstance(context);
        AchievementNotifier notifier = new AchievementNotifier(context);
        gamificationManager.setAchievementListener(notifier);
    }

    /**
     * Регистрация свайпа в системе геймификации
     * @param context контекст приложения
     * @param direction направление свайпа (true для вправо, false для влево)
     * @return true если произошло повышение уровня
     */
    public static boolean registerSwipe(Context context, boolean direction) {
        GamificationManager gamificationManager = GamificationManager.getInstance(context);
        return gamificationManager.processUserAction(CURRENT_USER_ID, GamificationManager.ACTION_SWIPE, String.valueOf(direction));
    }

    /**
     * Регистрация оценки контента в системе геймификации
     * @param context контекст приложения
     * @param contentId ID контента
     * @param contentTitle название контента
     * @param rating оценка (от 0 до 5)
     * @return true если произошло повышение уровня
     */
    public static boolean registerRating(Context context, String contentId, String contentTitle, float rating) {
        GamificationManager gamificationManager = GamificationManager.getInstance(context);
        return gamificationManager.processUserAction(CURRENT_USER_ID, GamificationManager.ACTION_RATE, String.valueOf(rating));
    }
    
    /**
     * Регистрация оценки контента в системе геймификации
     * @param context контекст приложения
     * @param rating оценка (от 0 до 5)
     * @return true если произошло повышение уровня
     */
    public static boolean registerRating(Context context, float rating) {
        GamificationManager gamificationManager = GamificationManager.getInstance(context);
        return gamificationManager.processUserAction(CURRENT_USER_ID, GamificationManager.ACTION_RATE, String.valueOf(rating));
    }

    /**
     * Регистрация написания рецензии в системе геймификации
     * @param context контекст приложения
     * @param contentId ID контента
     * @param contentTitle название контента
     * @return true если произошло повышение уровня
     */
    public static boolean registerReview(Context context, String contentId, String contentTitle) {
        GamificationManager gamificationManager = GamificationManager.getInstance(context);
        return gamificationManager.processUserAction(CURRENT_USER_ID, GamificationManager.ACTION_REVIEW, contentId);
    }
    
    /**
     * Регистрация написания рецензии в системе геймификации
     * @param context контекст приложения
     * @return true если произошло повышение уровня
     */
    public static boolean registerReview(Context context) {
        GamificationManager gamificationManager = GamificationManager.getInstance(context);
        return gamificationManager.processUserAction(CURRENT_USER_ID, GamificationManager.ACTION_REVIEW, "");
    }

    /**
     * Регистрация просмотра/прочтения контента в системе геймификации
     * @param context контекст приложения
     * @param contentId ID контента
     * @param contentTitle название контента
     * @param contentType тип контента
     * @return true если произошло повышение уровня
     */
    public static boolean registerCompletion(Context context, String contentId, String contentTitle, String contentType) {
        GamificationManager gamificationManager = GamificationManager.getInstance(context);
        return gamificationManager.processUserAction(CURRENT_USER_ID, GamificationManager.ACTION_COMPLETE, contentId);
    }
    
    /**
     * Регистрация просмотра/прочтения контента в системе геймификации
     * @param context контекст приложения
     * @param contentId ID контента
     * @return true если произошло повышение уровня
     */
    public static boolean registerCompletion(Context context, String contentId) {
        GamificationManager gamificationManager = GamificationManager.getInstance(context);
        return gamificationManager.processUserAction(CURRENT_USER_ID, GamificationManager.ACTION_COMPLETE, contentId);
    }

    /**
     * Проверка системы геймификации и инициализация профиля если нужно
     * @param context контекст приложения
     */
    public static void ensureUserInitialized(Context context) {
        // Проверяем, существует ли пользователь, если нет - создаем
        AppDatabase database = AppDatabase.getInstance(context);
        UserEntity user = database.userDao().getById(CURRENT_USER_ID);
        
        if (user == null) {
            // Создаем базового пользователя если он не существует
            user = new UserEntity(CURRENT_USER_ID, "Demo User", "demo@example.com", "");
            user.setExperience(0);
            user.setLevel(1);
            database.userDao().insert(user);
        }
        
        // Инициализируем менеджер геймификации и настраиваем уведомления
        setupAchievementListener(context);
    }
    
    /**
     * Показывает информацию о достижении
     * @param context контекст приложения
     * @param achievement достижение
     * @param experienceGained полученный опыт
     */
    public static void showAchievementToast(Context context, AchievementEntity achievement, int experienceGained) {
        String message = "Достижение разблокировано: " + achievement.getTitle() + 
                       "\n+" + experienceGained + " опыта";
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Показывает информацию о повышении уровня
     * @param context контекст приложения
     * @param newLevel новый уровень
     */
    public static void showLevelUpToast(Context context, int newLevel) {
        String rank = XpLevelCalculator.getLevelRank(newLevel);
        String message = "Уровень повышен до " + newLevel + "!\nНовое звание: " + rank;
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
