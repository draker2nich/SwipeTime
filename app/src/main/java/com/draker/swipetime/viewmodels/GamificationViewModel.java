package com.draker.swipetime.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.entities.AchievementEntity;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.database.entities.UserStatsEntity;
import com.draker.swipetime.utils.ActionLogger;
import com.draker.swipetime.utils.FirebaseManager;
import com.draker.swipetime.utils.GamificationManager;
import com.draker.swipetime.utils.XpLevelCalculator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * ViewModel для управления геймификацией в приложении
 */
public class GamificationViewModel extends AndroidViewModel {

    private static final String TAG = "GamificationViewModel";
    private static final String DEFAULT_USER_ID = "user_1";

    private final AppDatabase database;
    private final GamificationManager gamificationManager;
    private final FirebaseManager firebaseManager;
    
    private String currentUserId = DEFAULT_USER_ID;
    
    private final MutableLiveData<UserEntity> currentUser = new MutableLiveData<>();
    private final MutableLiveData<UserStatsEntity> userStats = new MutableLiveData<>();
    private final MutableLiveData<List<GamificationManager.UserAchievementInfo>> userAchievements = new MutableLiveData<>();
    private final MutableLiveData<Integer> completedAchievementsCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalAchievementsCount = new MutableLiveData<>(0);
    private final MutableLiveData<String> currentRank = new MutableLiveData<>("");
    private final MutableLiveData<Integer> levelProgress = new MutableLiveData<>(0);

    public GamificationViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(application);
        gamificationManager = GamificationManager.getInstance(application);
        firebaseManager = FirebaseManager.getInstance(application);
        
        // Проверяем, вошел ли пользователь
        if (firebaseManager.isUserSignedIn()) {
            FirebaseUser user = firebaseManager.getCurrentUser();
            if (user != null) {
                currentUserId = user.getUid();
                Log.d(TAG, "Использую ID авторизованного пользователя: " + currentUserId);
            }
        } else {
            Log.d(TAG, "Использую ID пользователя по умолчанию: " + DEFAULT_USER_ID);
        }
        
        // Загружаем данные пользователя
        loadUserData();
    }

    /**
     * Загрузка данных пользователя из базы данных
     */
    public void loadUserData() {
        loadUserData(currentUserId);
    }
    
    /**
     * Загрузка данных пользователя из базы данных по указанному ID
     * @param userId ID пользователя
     */
    public void loadUserData(String userId) {
        try {
            // Обновляем currentUserId, если он изменился
            currentUserId = userId;
            
            // Загружаем данные пользователя
            UserEntity user = database.userDao().getById(userId);
            if (user == null) {
                // Если пользователь не существует, создаем нового
                user = new UserEntity(userId, "Demo User", "demo@example.com", "");
                user.setExperience(0);
                user.setLevel(0);
                database.userDao().insert(user);
                Log.d(TAG, "Создан новый пользователь с ID: " + userId);
            }
            currentUser.postValue(user);
            
            // Загружаем статистику пользователя
            UserStatsEntity stats = gamificationManager.getUserStats(userId);
            userStats.postValue(stats);
            
            // Загружаем достижения пользователя
            List<GamificationManager.UserAchievementInfo> achievements = gamificationManager.getUserAchievements(userId);
            userAchievements.postValue(achievements);
            
            // Получаем количество завершенных достижений
            int completed = gamificationManager.getCompletedAchievementsCount(userId);
            completedAchievementsCount.postValue(completed);
            
            // Получаем общее количество достижений
            int total = gamificationManager.getTotalAchievementsCount();
            totalAchievementsCount.postValue(total);
            
            // Получаем текущий ранг пользователя
            String rank = XpLevelCalculator.getLevelRank(user.getLevel());
            currentRank.postValue(rank);
            
            // Получаем прогресс к следующему уровню
            int progress = XpLevelCalculator.calculateLevelProgress(user.getExperience(), user.getLevel());
            levelProgress.postValue(progress);
            
            Log.d(TAG, "Загружены данные пользователя: уровень=" + user.getLevel() + 
                      ", опыт=" + user.getExperience() + ", достижения=" + completed + "/" + total);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при загрузке данных пользователя: " + e.getMessage());
        }
    }
    
    /**
     * Регистрирует действие пользователя и начисляет опыт
     * 
     * @param action тип действия (swipe, rate, review, complete)
     * @param data дополнительные данные (зависит от действия)
     * @return true если произошло повышение уровня
     */
    public boolean processUserAction(String action, String data) {
        boolean levelUp = gamificationManager.processUserAction(currentUserId, action, data);
        
        // Обновляем LiveData
        loadUserData();
        
        return levelUp;
    }
    
    /**
     * Регистрирует свайп пользователя
     * 
     * @param direction направление свайпа (true - вправо, false - влево)
     * @param contentId ID контента
     * @param contentTitle название контента
     * @return true если произошло повышение уровня
     */
    public boolean registerSwipe(boolean direction, String contentId, String contentTitle) {
        // Логируем действие
        ActionLogger.logSwipe(direction, contentId, contentTitle);
        
        // Обрабатываем действие
        return processUserAction(GamificationManager.ACTION_SWIPE, String.valueOf(direction));
    }
    
    /**
     * Регистрирует оценку контента
     * 
     * @param contentId ID контента
     * @param contentTitle название контента
     * @param rating оценка (от 1 до 5)
     * @return true если произошло повышение уровня
     */
    public boolean registerRating(String contentId, String contentTitle, float rating) {
        // Логируем действие
        ActionLogger.logRating(contentId, contentTitle, rating);
        
        // Обрабатываем действие
        return processUserAction(GamificationManager.ACTION_RATE, String.valueOf(rating));
    }
    
    /**
     * Регистрирует рецензию на контент
     * 
     * @param contentId ID контента
     * @param contentTitle название контента
     * @return true если произошло повышение уровня
     */
    public boolean registerReview(String contentId, String contentTitle) {
        // Логируем действие
        ActionLogger.logReview(contentId, contentTitle);
        
        // Обрабатываем действие
        return processUserAction(GamificationManager.ACTION_REVIEW, contentId);
    }
    
    /**
     * Регистрирует просмотр/прочтение контента
     * 
     * @param contentId ID контента
     * @param contentTitle название контента
     * @param contentType тип контента
     * @return true если произошло повышение уровня
     */
    public boolean registerCompletion(String contentId, String contentTitle, String contentType) {
        // Логируем действие
        ActionLogger.logCompleted(contentId, contentTitle, contentType);
        
        // Обрабатываем действие
        return processUserAction(GamificationManager.ACTION_COMPLETE, contentId);
    }
    
    /**
     * Метод для обновления текущего ID пользователя после входа
     * @param userId ID пользователя из Firebase
     */
    public void updateCurrentUserId(String userId) {
        if (userId != null && !userId.isEmpty()) {
            this.currentUserId = userId;
            Log.d(TAG, "ID пользователя обновлен: " + userId);
            loadUserData();
        }
    }
    
    /**
     * Получить текущий ID пользователя
     * @return ID пользователя
     */
    public String getCurrentUserId() {
        return currentUserId;
    }
    
    // Геттеры для LiveData
    public LiveData<UserEntity> getCurrentUser() {
        return currentUser;
    }
    
    public LiveData<UserStatsEntity> getUserStats() {
        return userStats;
    }
    
    public LiveData<List<GamificationManager.UserAchievementInfo>> getUserAchievements() {
        return userAchievements;
    }
    
    public LiveData<Integer> getCompletedAchievementsCount() {
        return completedAchievementsCount;
    }
    
    public LiveData<Integer> getTotalAchievementsCount() {
        return totalAchievementsCount;
    }
    
    public LiveData<String> getCurrentRank() {
        return currentRank;
    }
    
    public LiveData<Integer> getLevelProgress() {
        return levelProgress;
    }
}