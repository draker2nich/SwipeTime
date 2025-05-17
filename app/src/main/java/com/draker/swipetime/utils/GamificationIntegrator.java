package com.draker.swipetime.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.entities.AchievementEntity;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.utils.FirebaseAuthManager;
import com.draker.swipetime.utils.FirestoreDataManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Класс для интеграции системы геймификации в различные компоненты приложения
 */
public class GamificationIntegrator {

    private static final String TAG = "GamificationIntegrator";
    private static final String DEFAULT_USER_ID = "user_1";

    /**
     * Получение ID текущего пользователя
     * @param context контекст приложения
     * @return ID пользователя
     */
    public static String getCurrentUserId(Context context) {
        FirebaseAuthManager authManager = FirebaseAuthManager.getInstance(context);
        if (authManager != null && authManager.isUserSignedIn()) {
            FirebaseUser user = authManager.getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                Log.d(TAG, "Используется ID авторизованного пользователя: " + userId);
                return userId;
            }
        }
        
        Log.d(TAG, "Используется ID пользователя по умолчанию: " + DEFAULT_USER_ID);
        return DEFAULT_USER_ID;
    }

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
        String userId = getCurrentUserId(context);
        GamificationManager gamificationManager = GamificationManager.getInstance(context);
        return gamificationManager.processUserAction(userId, GamificationManager.ACTION_SWIPE, String.valueOf(direction));
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
        String userId = getCurrentUserId(context);
        GamificationManager gamificationManager = GamificationManager.getInstance(context);
        return gamificationManager.processUserAction(userId, GamificationManager.ACTION_RATE, String.valueOf(rating));
    }
    
    /**
     * Регистрация оценки контента в системе геймификации
     * @param context контекст приложения
     * @param rating оценка (от 0 до 5)
     * @return true если произошло повышение уровня
     */
    public static boolean registerRating(Context context, float rating) {
        String userId = getCurrentUserId(context);
        GamificationManager gamificationManager = GamificationManager.getInstance(context);
        return gamificationManager.processUserAction(userId, GamificationManager.ACTION_RATE, String.valueOf(rating));
    }

    /**
     * Регистрация написания рецензии в системе геймификации
     * @param context контекст приложения
     * @param contentId ID контента
     * @param contentTitle название контента
     * @return true если произошло повышение уровня
     */
    public static boolean registerReview(Context context, String contentId, String contentTitle) {
        String userId = getCurrentUserId(context);
        GamificationManager gamificationManager = GamificationManager.getInstance(context);
        return gamificationManager.processUserAction(userId, GamificationManager.ACTION_REVIEW, contentId);
    }
    
    /**
     * Регистрация написания рецензии в системе геймификации
     * @param context контекст приложения
     * @return true если произошло повышение уровня
     */
    public static boolean registerReview(Context context) {
        String userId = getCurrentUserId(context);
        GamificationManager gamificationManager = GamificationManager.getInstance(context);
        return gamificationManager.processUserAction(userId, GamificationManager.ACTION_REVIEW, "");
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
        String userId = getCurrentUserId(context);
        GamificationManager gamificationManager = GamificationManager.getInstance(context);
        boolean levelUp = gamificationManager.processUserAction(userId, GamificationManager.ACTION_COMPLETE, contentId);
        
        // Если пользователь авторизован, синхронизируем данные с Firebase
        if (!userId.equals(DEFAULT_USER_ID) && isUserAuthenticated(context)) {
            syncUserWithFirebase(context, userId);
            // Также синхронизируем информацию о завершенном контенте
            syncContentWithFirebase(context, userId, contentId);
        }
        
        return levelUp;
    }
    
    /**
     * Регистрация просмотра/прочтения контента в системе геймификации
     * @param context контекст приложения
     * @param contentId ID контента
     * @return true если произошло повышение уровня
     */
    public static boolean registerCompletion(Context context, String contentId) {
        String userId = getCurrentUserId(context);
        GamificationManager gamificationManager = GamificationManager.getInstance(context);
        boolean levelUp = gamificationManager.processUserAction(userId, GamificationManager.ACTION_COMPLETE, contentId);
        
        // Если пользователь авторизован, синхронизируем данные с Firebase
        if (!userId.equals(DEFAULT_USER_ID) && isUserAuthenticated(context)) {
            syncUserWithFirebase(context, userId);
            // Также синхронизируем информацию о завершенном контенте
            syncContentWithFirebase(context, userId, contentId);
        }
        
        return levelUp;
    }
    
    /**
     * Проверяет авторизован ли пользователь через Firebase
     * @param context контекст приложения
     * @return true если пользователь авторизован
     */
    private static boolean isUserAuthenticated(Context context) {
        FirebaseAuthManager authManager = FirebaseAuthManager.getInstance(context);
        return authManager != null && authManager.isUserSignedIn();
    }
    
    /**
     * Синхронизирует данные пользователя с Firebase
     * @param context контекст приложения
     * @param userId ID пользователя
     */
    private static void syncUserWithFirebase(Context context, String userId) {
        try {
            FirestoreDataManager firestoreManager = FirestoreDataManager.getInstance(context);
            firestoreManager.syncUserData(userId, new FirestoreDataManager.SyncCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Синхронизация пользователя с Firebase успешно выполнена");
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "Ошибка синхронизации пользователя с Firebase: " + errorMessage);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при выполнении синхронизации с Firebase: " + e.getMessage(), e);
        }
    }
    
    /**
     * Синхронизирует отзыв с Firebase
     * @param context контекст приложения
     * @param userId ID пользователя
     * @param contentId ID контента
     */
    private static void syncReviewWithFirebase(Context context, String userId, String contentId) {
        try {
            FirestoreDataManager firestoreManager = FirestoreDataManager.getInstance(context);
            firestoreManager.syncUserData(userId, new FirestoreDataManager.SyncCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Синхронизация отзыва с Firebase успешно выполнена");
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "Ошибка синхронизации отзыва с Firebase: " + errorMessage);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при выполнении синхронизации отзыва с Firebase: " + e.getMessage(), e);
        }
    }
    
    /**
     * Синхронизирует информацию о контенте с Firebase
     * @param context контекст приложения
     * @param userId ID пользователя
     * @param contentId ID контента
     */
    private static void syncContentWithFirebase(Context context, String userId, String contentId) {
        try {
            FirestoreDataManager firestoreManager = FirestoreDataManager.getInstance(context);
            firestoreManager.syncUserData(userId, new FirestoreDataManager.SyncCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Синхронизация контента с Firebase успешно выполнена");
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "Ошибка синхронизации контента с Firebase: " + errorMessage);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при выполнении синхронизации контента с Firebase: " + e.getMessage(), e);
        }
    }

    /**
     * Проверка системы геймификации и инициализация профиля если нужно
     * @param context контекст приложения
     */
    public static void ensureUserInitialized(Context context) {
        String userId = getCurrentUserId(context);
        
        // Проверяем, существует ли пользователь, если нет - создаем
        AppDatabase database = AppDatabase.getInstance(context);
        UserEntity user = database.userDao().getById(userId);
        
        if (user == null) {
            // Создаем базового пользователя если он не существует
            FirebaseAuthManager authManager = FirebaseAuthManager.getInstance(context);
            String username = "Demo User";
            String email = "demo@example.com";
            
            if (authManager != null && authManager.isUserSignedIn()) {
                FirebaseUser firebaseUser = authManager.getCurrentUser();
                if (firebaseUser != null) {
                    username = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User";
                    email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "user@example.com";
                }
            }
            
            user = new UserEntity(userId, username, email, "");
            user.setExperience(0);
            user.setLevel(0);
            database.userDao().insert(user);
            
            Log.d(TAG, "Создан новый пользователь: " + username + " (ID: " + userId + ")");
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