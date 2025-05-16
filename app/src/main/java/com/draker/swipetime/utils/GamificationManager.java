package com.draker.swipetime.utils;

import android.content.Context;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.AchievementDao;
import com.draker.swipetime.database.dao.UserAchievementDao;
import com.draker.swipetime.database.dao.UserDao;
import com.draker.swipetime.database.dao.UserStatsDao;
import com.draker.swipetime.database.entities.AchievementEntity;
import com.draker.swipetime.database.entities.UserAchievementCrossRef;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.database.entities.UserStatsEntity;
import com.draker.swipetime.utils.ActionLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import android.util.Log;

/**
 * Менеджер геймификации для управления опытом, уровнями и достижениями
 */
public class GamificationManager {

    private final Context context;
    private final AppDatabase database;
    private final UserDao userDao;
    private final AchievementDao achievementDao;
    private final UserAchievementDao userAchievementDao;
    private final UserStatsDao userStatsDao;
    
    private static GamificationManager instance;
    
    // Типы действий
    public static final String ACTION_SWIPE = "swipe";
    public static final String ACTION_RATE = "rate";
    public static final String ACTION_REVIEW = "review";
    public static final String ACTION_COMPLETE = "complete"; // просмотрено, прочитано, пройдено
    
    // Категории достижений
    public static final String CATEGORY_BEGINNER = "beginner";
    public static final String CATEGORY_INTERMEDIATE = "intermediate";
    public static final String CATEGORY_ADVANCED = "advanced";
    public static final String CATEGORY_EXPERT = "expert";
    public static final String CATEGORY_COLLECTOR = "collector";
    public static final String CATEGORY_SOCIAL = "social";
    public static final String CATEGORY_STREAK = "streak";
    
    // Интерфейс для обратных вызовов о новых достижениях
    public interface OnAchievementListener {
        void onAchievementUnlocked(AchievementEntity achievement, int experienceGained);
        void onLevelUp(int newLevel, int experienceGained);
    }
    
    private OnAchievementListener achievementListener;
    
    // Приватный конструктор (Singleton)
    private GamificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
        this.userDao = database.userDao();
        this.achievementDao = database.achievementDao();
        this.userAchievementDao = database.userAchievementDao();
        this.userStatsDao = database.userStatsDao();
        
        // Если достижений нет, создаем базовый набор
        if (achievementDao.getCount() == 0) {
            initializeAchievements();
        }
    }
    
    // Получение экземпляра (Singleton)
    public static synchronized GamificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new GamificationManager(context);
        }
        return instance;
    }
    
    /**
     * Установить слушатель достижений
     * @param listener слушатель
     */
    public void setAchievementListener(OnAchievementListener listener) {
        this.achievementListener = listener;
    }
    
    /**
     * Получить статистику пользователя
     * @param userId ID пользователя
     * @return статистика или null если пользователь не найден
     */
    public UserStatsEntity getUserStats(String userId) {
        UserStatsEntity stats = userStatsDao.getByUserId(userId);
        if (stats == null) {
            // Если статистики нет, создаем новую
            stats = new UserStatsEntity(userId);
            userStatsDao.insert(stats);
            
            // После вставки загружаем статистику снова, чтобы получить ID
            stats = userStatsDao.getByUserId(userId);
        }
        return stats;
    }
    
    /**
     * Зарегистрировать действие пользователя и начислить опыт
     * 
     * @param userId ID пользователя
     * @param action тип действия (swipe, rate, review)
     * @param data дополнительные данные (например, ID контента)
     * @return true если был повышен уровень
     */
    public boolean processUserAction(String userId, String action, String data) {
        // Получаем пользователя
        UserEntity user = userDao.getById(userId);
        if (user == null) {
            Log.e("GamificationManager", "Пользователь с ID " + userId + " не найден");
            return false;
        }
        
        // Получаем статистику пользователя
        UserStatsEntity stats = getUserStats(userId);
        
        // Обрабатываем действие
        long currentTime = System.currentTimeMillis();
        boolean levelUp = false;
        int experienceGained = 0;
        int oldLevel = user.getLevel();
        
        switch (action) {
            case ACTION_SWIPE:
                boolean direction = Boolean.parseBoolean(data); // true для свайпа вправо, false для свайпа влево
                
                // Обновляем статистику свайпов
                userStatsDao.incrementSwipes(userId, direction ? 1 : 0, currentTime);
                
                // Начисляем опыт
                experienceGained = XpLevelCalculator.getXpForAction(ACTION_SWIPE);
                levelUp = user.addExperience(experienceGained);
                userDao.update(user);
                
                // Проверяем достижения, связанные со свайпами
                checkSwipeAchievements(userId, stats.getSwipesCount() + 1);
                
                // Логируем действие
                Log.d("GamificationManager", "Начислено " + experienceGained + " XP за свайп " + (direction ? "вправо" : "влево"));
                
                break;
                
            case ACTION_RATE:
                // Обновляем статистику оценок
                userStatsDao.incrementRatings(userId, currentTime);
                
                // Начисляем опыт
                experienceGained = XpLevelCalculator.getXpForAction(ACTION_RATE);
                levelUp = user.addExperience(experienceGained);
                userDao.update(user);
                
                // Проверяем достижения, связанные с оценками
                checkRatingAchievements(userId, stats.getRatingsCount() + 1);
                
                // Логируем действие
                Log.d("GamificationManager", "Начислено " + experienceGained + " XP за оценку контента");
                
                break;
                
            case ACTION_REVIEW:
                // Обновляем статистику рецензий
                userStatsDao.incrementReviews(userId, currentTime);
                
                // Начисляем опыт
                experienceGained = XpLevelCalculator.getXpForAction(ACTION_REVIEW);
                levelUp = user.addExperience(experienceGained);
                userDao.update(user);
                
                // Проверяем достижения, связанные с рецензиями
                checkReviewAchievements(userId, stats.getReviewsCount() + 1);
                
                // Логируем действие
                Log.d("GamificationManager", "Начислено " + experienceGained + " XP за написание рецензии");
                
                break;
                
            case ACTION_COMPLETE:
                // Обновляем статистику просмотренного/прочитанного контента
                userStatsDao.incrementConsumed(userId, currentTime);
                
                // Начисляем небольшой опыт за отметку "просмотрено"
                experienceGained = XpLevelCalculator.getXpForAction(ACTION_COMPLETE);
                levelUp = user.addExperience(experienceGained);
                userDao.update(user);
                
                // Проверяем достижения, связанные с просмотром/чтением
                checkCompletedAchievements(userId, stats.getConsumedCount() + 1);
                
                // Логируем действие
                Log.d("GamificationManager", "Начислено " + experienceGained + " XP за отметку о завершении контента");
                
                break;
        }
        
        // Проверяем достижения, связанные с общими действиями
        checkGeneralAchievements(userId, stats.getTotalActions() + 1);
        
        // Обновляем статистику дней активности (streak)
        updateStreakDays(userId, currentTime);
        
        // Уведомляем о повышении уровня
        if (levelUp && achievementListener != null) {
            achievementListener.onLevelUp(user.getLevel(), experienceGained);
            
            // Логируем повышение уровня
            String rank = XpLevelCalculator.getLevelRank(user.getLevel());
            ActionLogger.logLevelUp(oldLevel, user.getLevel(), rank);
        }
        
        return levelUp;
    }
    
    /**
     * Обновление статистики дней активности
     * 
     * @param userId ID пользователя
     * @param currentTime текущее время
     */
    private void updateStreakDays(String userId, long currentTime) {
        UserStatsEntity stats = userStatsDao.getByUserId(userId);
        if (stats != null) {
            // Обновляем дни активности
            stats.updateStreak(currentTime);
            userStatsDao.update(stats);
            
            // Проверяем достижения, связанные с днями активности
            checkStreakAchievements(userId, stats.getStreakDays());
        }
    }
    
    /**
     * Разблокирует достижение для пользователя и начисляет опыт
     * 
     * @param userId ID пользователя
     * @param achievementId ID достижения
     * @return количество начисленного опыта
     */
    private int unlockAchievement(String userId, String achievementId) {
        // Проверяем, существует ли уже запись о достижении
        UserAchievementCrossRef userAchievement = userAchievementDao.getByIds(userId, achievementId);
        
        if (userAchievement == null) {
            // Создаем новую запись
            userAchievement = new UserAchievementCrossRef(userId, achievementId, 0);
        }
        
        // Получаем информацию о достижении
        AchievementEntity achievement = achievementDao.getById(achievementId);
        if (achievement == null) {
            return 0;
        }
        
        // Если достижение еще не выполнено, отмечаем его как выполненное
        if (!userAchievement.isCompleted()) {
            userAchievement.setCompleted(true);
            userAchievementDao.insert(userAchievement);
            
            // Обновляем счетчик достижений
            userStatsDao.incrementAchievements(userId, System.currentTimeMillis());
            
            // Начисляем опыт за достижение
            UserEntity user = userDao.getById(userId);
            if (user != null) {
                int xp = achievement.getExperienceReward();
                boolean levelUp = user.addExperience(xp);
                userDao.update(user);
                
                // Уведомляем о новом достижении
                if (achievementListener != null) {
                    achievementListener.onAchievementUnlocked(achievement, xp);
                    
                    // Если произошло повышение уровня, также уведомляем об этом
                    if (levelUp) {
                        achievementListener.onLevelUp(user.getLevel(), xp);
                    }
                }
                
                return xp;
            }
        }
        
        return 0;
    }
    
    /**
     * Проверяет достижения, связанные со свайпами
     * 
     * @param userId ID пользователя
     * @param count количество свайпов
     */
    private void checkSwipeAchievements(String userId, int count) {
        // Получаем все достижения, связанные со свайпами, которые можно выполнить
        List<AchievementEntity> achievements = achievementDao.getByRequiredAction(ACTION_SWIPE);
        
        for (AchievementEntity achievement : achievements) {
            if (count >= achievement.getRequiredCount()) {
                // Пользователь выполнил достижение
                unlockAchievement(userId, achievement.getId());
            }
        }
    }
    
    /**
     * Проверяет достижения, связанные с оценками
     * 
     * @param userId ID пользователя
     * @param count количество оценок
     */
    private void checkRatingAchievements(String userId, int count) {
        List<AchievementEntity> achievements = achievementDao.getByRequiredAction(ACTION_RATE);
        
        for (AchievementEntity achievement : achievements) {
            if (count >= achievement.getRequiredCount()) {
                unlockAchievement(userId, achievement.getId());
            }
        }
    }
    
    /**
     * Проверяет достижения, связанные с рецензиями
     * 
     * @param userId ID пользователя
     * @param count количество рецензий
     */
    private void checkReviewAchievements(String userId, int count) {
        List<AchievementEntity> achievements = achievementDao.getByRequiredAction(ACTION_REVIEW);
        
        for (AchievementEntity achievement : achievements) {
            if (count >= achievement.getRequiredCount()) {
                unlockAchievement(userId, achievement.getId());
            }
        }
    }
    
    /**
     * Проверяет достижения, связанные с просмотренным/прочитанным контентом
     * 
     * @param userId ID пользователя
     * @param count количество просмотренного/прочитанного контента
     */
    private void checkCompletedAchievements(String userId, int count) {
        List<AchievementEntity> achievements = achievementDao.getByRequiredAction(ACTION_COMPLETE);
        
        for (AchievementEntity achievement : achievements) {
            if (count >= achievement.getRequiredCount()) {
                unlockAchievement(userId, achievement.getId());
            }
        }
    }
    
    /**
     * Проверяет достижения, связанные с общим количеством действий
     * 
     * @param userId ID пользователя
     * @param count общее количество действий
     */
    private void checkGeneralAchievements(String userId, int count) {
        List<AchievementEntity> achievements = achievementDao.getByRequiredAction("total");
        
        for (AchievementEntity achievement : achievements) {
            if (count >= achievement.getRequiredCount()) {
                unlockAchievement(userId, achievement.getId());
            }
        }
    }
    
    /**
     * Проверяет достижения, связанные с днями активности (streak)
     * 
     * @param userId ID пользователя
     * @param streakDays количество дней подряд с активностью
     */
    private void checkStreakAchievements(String userId, int streakDays) {
        List<AchievementEntity> achievements = achievementDao.getByRequiredAction("streak");
        
        for (AchievementEntity achievement : achievements) {
            if (streakDays >= achievement.getRequiredCount()) {
                unlockAchievement(userId, achievement.getId());
            }
        }
    }
    
    /**
     * Инициализация базового набора достижений
     */
    private void initializeAchievements() {
        List<AchievementEntity> achievements = new ArrayList<>();
        
        // Достижения для свайпов
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Первый шаг",
                "Сделайте первый свайп",
                "ic_achievement_first_swipe",
                10, // опыт за достижение
                ACTION_SWIPE,
                1, // требуемое количество
                CATEGORY_BEGINNER
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Исследователь",
                "Сделайте 50 свайпов",
                "ic_achievement_explorer",
                50,
                ACTION_SWIPE,
                50,
                CATEGORY_BEGINNER
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Мастер свайпа",
                "Сделайте 500 свайпов",
                "ic_achievement_swipe_master",
                200,
                ACTION_SWIPE,
                500,
                CATEGORY_INTERMEDIATE
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Свайп-мастер",
                "Сделайте 2000 свайпов",
                "ic_achievement_swipe_guru",
                500,
                ACTION_SWIPE,
                2000,
                CATEGORY_ADVANCED
        ));
        
        // Достижения для оценок
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Первая оценка",
                "Поставьте первую оценку",
                "ic_achievement_first_rating",
                20,
                ACTION_RATE,
                1,
                CATEGORY_BEGINNER
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Критик",
                "Поставьте 25 оценок",
                "ic_achievement_critic",
                100,
                ACTION_RATE,
                25,
                CATEGORY_INTERMEDIATE
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Опытный критик",
                "Поставьте 100 оценок",
                "ic_achievement_experienced_critic",
                300,
                ACTION_RATE,
                100,
                CATEGORY_ADVANCED
        ));
        
        // Достижения для рецензий
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Рецензент",
                "Напишите первую рецензию",
                "ic_achievement_reviewer",
                50,
                ACTION_REVIEW,
                1,
                CATEGORY_BEGINNER
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Обозреватель",
                "Напишите 10 рецензий",
                "ic_achievement_observer",
                200,
                ACTION_REVIEW,
                10,
                CATEGORY_INTERMEDIATE
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Профессиональный критик",
                "Напишите 50 рецензий",
                "ic_achievement_pro_critic",
                500,
                ACTION_REVIEW,
                50,
                CATEGORY_ADVANCED
        ));
        
        // Достижения для просмотренного/прочитанного контента
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Первый опыт",
                "Отметьте первый элемент как просмотренный/прочитанный",
                "ic_achievement_first_complete",
                30,
                ACTION_COMPLETE,
                1,
                CATEGORY_BEGINNER
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Коллекционер",
                "Отметьте 20 элементов как просмотренные/прочитанные",
                "ic_achievement_collector",
                150,
                ACTION_COMPLETE,
                20,
                CATEGORY_INTERMEDIATE
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Энциклопедист",
                "Отметьте 100 элементов как просмотренные/прочитанные",
                "ic_achievement_encyclopedist",
                400,
                ACTION_COMPLETE,
                100,
                CATEGORY_ADVANCED
        ));
        
        // Достижения для общего количества действий
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Путь начинается",
                "Совершите 10 действий",
                "ic_achievement_path_begins",
                20,
                "total",
                10,
                CATEGORY_BEGINNER
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Активный пользователь",
                "Совершите 100 действий",
                "ic_achievement_active_user",
                100,
                "total",
                100,
                CATEGORY_INTERMEDIATE
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Эксперт",
                "Совершите 1000 действий",
                "ic_achievement_expert",
                500,
                "total",
                1000,
                CATEGORY_ADVANCED
        ));
        
        // Достижения для дней активности
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Еженедельник",
                "Будьте активны 7 дней подряд",
                "ic_achievement_weekly",
                100,
                "streak",
                7,
                CATEGORY_STREAK
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Месяц с нами",
                "Будьте активны 30 дней подряд",
                "ic_achievement_monthly",
                300,
                "streak",
                30,
                CATEGORY_STREAK
        ));
        
        achievements.add(new AchievementEntity(
                UUID.randomUUID().toString(),
                "Преданный фанат",
                "Будьте активны 100 дней подряд",
                "ic_achievement_loyal_fan",
                1000,
                "streak",
                100,
                CATEGORY_STREAK
        ));
        
        // Сохраняем достижения в базу данных
        achievementDao.insertAll(achievements);
    }
    
    /**
     * Получить прогресс пользователя для достижения
     * 
     * @param userId ID пользователя
     * @param achievementId ID достижения
     * @return прогресс (0-100%)
     */
    public int getAchievementProgress(String userId, String achievementId) {
        UserAchievementCrossRef userAchievement = userAchievementDao.getByIds(userId, achievementId);
        
        if (userAchievement != null && userAchievement.isCompleted()) {
            return 100;
        } else {
            AchievementEntity achievement = achievementDao.getById(achievementId);
            UserStatsEntity stats = userStatsDao.getByUserId(userId);
            
            if (achievement != null && stats != null) {
                int progress = 0;
                int requiredCount = achievement.getRequiredCount();
                
                switch (achievement.getRequiredAction()) {
                    case ACTION_SWIPE:
                        progress = stats.getSwipesCount();
                        break;
                    case ACTION_RATE:
                        progress = stats.getRatingsCount();
                        break;
                    case ACTION_REVIEW:
                        progress = stats.getReviewsCount();
                        break;
                    case ACTION_COMPLETE:
                        progress = stats.getConsumedCount();
                        break;
                    case "total":
                        progress = stats.getTotalActions();
                        break;
                    case "streak":
                        progress = stats.getStreakDays();
                        break;
                }
                
                // Рассчитываем процент выполнения
                return Math.min(100, (progress * 100) / requiredCount);
            }
        }
        
        return 0;
    }
    
    /**
     * Получить количество выполненных достижений пользователя
     * 
     * @param userId ID пользователя
     * @return количество выполненных достижений
     */
    public int getCompletedAchievementsCount(String userId) {
        return userAchievementDao.getCompletedCountByUserId(userId);
    }
    
    /**
     * Получить общее количество достижений
     * 
     * @return общее количество достижений в системе
     */
    public int getTotalAchievementsCount() {
        return achievementDao.getCount();
    }
    
    /**
     * Получить все достижения пользователя
     * 
     * @param userId ID пользователя
     * @return список достижений с индикацией выполнения
     */
    public List<UserAchievementInfo> getUserAchievements(String userId) {
        List<UserAchievementInfo> result = new ArrayList<>();
        List<AchievementEntity> allAchievements = achievementDao.getAll();
        List<UserAchievementCrossRef> userAchievements = userAchievementDao.getByUserId(userId);
        
        for (AchievementEntity achievement : allAchievements) {
            boolean completed = false;
            long completionDate = 0;
            
            // Ищем информацию о выполнении достижения
            for (UserAchievementCrossRef userAch : userAchievements) {
                if (userAch.getAchievementId().equals(achievement.getId())) {
                    completed = userAch.isCompleted();
                    completionDate = userAch.getCompletionDate();
                    break;
                }
            }
            
            int progress = getAchievementProgress(userId, achievement.getId());
            
            // Создаем объект с информацией о достижении
            UserAchievementInfo info = new UserAchievementInfo(
                    achievement,
                    completed,
                    completionDate,
                    progress
            );
            
            result.add(info);
        }
        
        return result;
    }
    
    /**
     * Класс для хранения информации о достижении пользователя
     */
    public static class UserAchievementInfo {
        private final AchievementEntity achievement;
        private final boolean completed;
        private final long completionDate;
        private final int progress; // 0-100%
        
        public UserAchievementInfo(AchievementEntity achievement, boolean completed, 
                                  long completionDate, int progress) {
            this.achievement = achievement;
            this.completed = completed;
            this.completionDate = completionDate;
            this.progress = progress;
        }
        
        public AchievementEntity getAchievement() {
            return achievement;
        }
        
        public boolean isCompleted() {
            return completed;
        }
        
        public long getCompletionDate() {
            return completionDate;
        }
        
        public int getProgress() {
            return progress;
        }
    }
    
    /**
     * Инициализация статистики пользователя
     * @param userId ID пользователя
     */
    public static void initUserStats(String userId) {
        try {
            AppDatabase db = AppDatabase.getInstance(null);
            if (db != null) {
                UserStatsEntity stats = new UserStatsEntity(userId);
                stats.setSwipesCount(0);
                stats.setRatingsCount(0);
                stats.setReviewsCount(0);
                stats.setConsumedCount(0);
                stats.setStreakDays(0);
                db.userStatsDao().insert(stats);
            }
        } catch (Exception e) {
            Log.e("GamificationManager", "Ошибка при инициализации статистики пользователя: " + e.getMessage());
        }
    }
}