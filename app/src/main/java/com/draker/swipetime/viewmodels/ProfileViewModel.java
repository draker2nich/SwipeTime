package com.draker.swipetime.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.UserDao;
import com.draker.swipetime.database.dao.UserStatsDao;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.database.entities.UserStatsEntity;
import com.draker.swipetime.utils.GamificationManager;
import com.draker.swipetime.utils.XpLevelCalculator;

/**
 * ViewModel для работы с профилем пользователя и его достижениями
 */
public class ProfileViewModel extends AndroidViewModel {

    private final UserDao userDao;
    private final UserStatsDao userStatsDao;
    private final GamificationManager gamificationManager;
    
    private final MutableLiveData<UserEntity> currentUser = new MutableLiveData<>();
    private final MutableLiveData<UserStatsEntity> currentUserStats = new MutableLiveData<>();
    private final MutableLiveData<Integer> levelProgress = new MutableLiveData<>();
    private final MutableLiveData<String> levelRank = new MutableLiveData<>();
    private final MutableLiveData<Integer> completedAchievements = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalAchievements = new MutableLiveData<>();
    
    public ProfileViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        userDao = database.userDao();
        userStatsDao = database.userStatsDao();
        gamificationManager = GamificationManager.getInstance(application);
    }
    
    /**
     * Загрузить данные пользователя
     * 
     * @param userId ID пользователя
     */
    public void loadUserData(String userId) {
        // Загружаем данные о пользователе
        UserEntity user = userDao.getById(userId);
        currentUser.postValue(user);
        
        if (user != null) {
            // Загружаем статистику пользователя
            UserStatsEntity stats = userStatsDao.getByUserId(userId);
            
            if (stats == null) {
                // Если статистики нет, создаем новую
                stats = new UserStatsEntity(userId);
                userStatsDao.insert(stats);
            }
            
            currentUserStats.postValue(stats);
            
            // Рассчитываем прогресс до следующего уровня
            int progress = XpLevelCalculator.calculateLevelProgress(user.getExperience(), user.getLevel());
            levelProgress.postValue(progress);
            
            // Получаем ранг пользователя
            levelRank.postValue(XpLevelCalculator.getLevelRank(user.getLevel()));
            
            // Загружаем количество выполненных и общих достижений
            completedAchievements.postValue(gamificationManager.getCompletedAchievementsCount(userId));
            totalAchievements.postValue(gamificationManager.getTotalAchievementsCount());
        }
    }
    
    /**
     * Получить текущего пользователя
     * 
     * @return LiveData с пользователем
     */
    public LiveData<UserEntity> getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Получить статистику пользователя
     * 
     * @return LiveData со статистикой
     */
    public LiveData<UserStatsEntity> getCurrentUserStats() {
        return currentUserStats;
    }
    
    /**
     * Получить прогресс до следующего уровня (0-100%)
     * 
     * @return LiveData с прогрессом
     */
    public LiveData<Integer> getLevelProgress() {
        return levelProgress;
    }
    
    /**
     * Получить ранг/звание пользователя
     * 
     * @return LiveData с рангом
     */
    public LiveData<String> getLevelRank() {
        return levelRank;
    }
    
    /**
     * Получить количество выполненных достижений
     * 
     * @return LiveData с количеством
     */
    public LiveData<Integer> getCompletedAchievements() {
        return completedAchievements;
    }
    
    /**
     * Получить общее количество достижений
     * 
     * @return LiveData с количеством
     */
    public LiveData<Integer> getTotalAchievements() {
        return totalAchievements;
    }
    
    /**
     * Обновить статистику пользователя
     * 
     * @param userStats обновленная статистика
     */
    public void updateUserStats(UserStatsEntity userStats) {
        userStatsDao.update(userStats);
        currentUserStats.postValue(userStats);
    }
    
    /**
     * Обновить пользователя
     * 
     * @param user обновленный пользователь
     */
    public void updateUser(UserEntity user) {
        userDao.update(user);
        currentUser.postValue(user);
        
        // Обновляем прогресс уровня и ранг
        int progress = XpLevelCalculator.calculateLevelProgress(user.getExperience(), user.getLevel());
        levelProgress.postValue(progress);
        levelRank.postValue(XpLevelCalculator.getLevelRank(user.getLevel()));
    }
    
    /**
     * Обработать действие пользователя (свайп, оценка, и т.д.)
     * 
     * @param userId ID пользователя
     * @param action тип действия
     * @param data дополнительные данные
     * @return true если повышен уровень
     */
    public boolean processUserAction(String userId, String action, String data) {
        boolean levelUp = gamificationManager.processUserAction(userId, action, data);
        
        // Обновляем данные пользователя
        loadUserData(userId);
        
        return levelUp;
    }
}