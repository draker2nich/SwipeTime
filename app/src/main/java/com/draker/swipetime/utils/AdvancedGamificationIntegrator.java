package com.draker.swipetime.utils;

import android.content.Context;
import android.util.Log;

import com.draker.swipetime.database.entities.AchievementEntity;
import com.draker.swipetime.database.entities.ChallengeMilestoneEntity;
import com.draker.swipetime.database.entities.CollectibleItemEntity;
import com.draker.swipetime.database.entities.DailyQuestEntity;
import com.draker.swipetime.database.entities.SeasonalEventEntity;
import com.draker.swipetime.database.entities.ThematicChallengeEntity;
import com.draker.swipetime.database.entities.UserRankEntity;

import java.util.List;

/**
 * Интегратор для продвинутой системы геймификации
 * Объединяет все компоненты геймификации в единую систему
 */
public class AdvancedGamificationIntegrator {

    private static final String TAG = "AdvancedGamificationIntegrator";
    
    private final Context context;
    private final GamificationManager gamificationManager;
    private final DailyQuestManager dailyQuestManager;
    private final SeasonalEventManager seasonalEventManager;
    private final CollectibleItemManager collectibleItemManager;
    private final ThematicChallengeManager thematicChallengeManager;
    private final UserRankManager userRankManager;
    
    private static AdvancedGamificationIntegrator instance;
    
    // Интерфейс для обратных вызовов о всех событиях геймификации
    public interface OnGamificationEventListener {
        void onAchievementUnlocked(AchievementEntity achievement, int experienceGained);
        void onLevelUp(int newLevel, int experienceGained);
        void onQuestCompleted(DailyQuestEntity quest, int experienceGained);
        void onEventStarted(SeasonalEventEntity event);
        void onEventEnded(SeasonalEventEntity event);
        void onItemObtained(CollectibleItemEntity item, String source);
        void onChallengeCompleted(ThematicChallengeEntity challenge, int experienceGained);
        void onChallengeMilestoneReached(ChallengeMilestoneEntity milestone, int experienceGained);
        void onRankUnlocked(UserRankEntity rank, int experienceGained);
        void onRankActivated(UserRankEntity rank);
    }
    
    private OnGamificationEventListener gamificationEventListener;
    
    // Приватный конструктор (Singleton)
    private AdvancedGamificationIntegrator(Context context) {
        this.context = context.getApplicationContext();
        this.gamificationManager = GamificationManager.getInstance(context);
        this.dailyQuestManager = DailyQuestManager.getInstance(context);
        this.seasonalEventManager = SeasonalEventManager.getInstance(context);
        this.collectibleItemManager = CollectibleItemManager.getInstance(context);
        this.thematicChallengeManager = ThematicChallengeManager.getInstance(context);
        this.userRankManager = UserRankManager.getInstance(context);
        
        // Настраиваем обратные вызовы для всех менеджеров
        setupListeners();
    }
    
    // Получение экземпляра (Singleton)
    public static synchronized AdvancedGamificationIntegrator getInstance(Context context) {
        if (instance == null) {
            instance = new AdvancedGamificationIntegrator(context);
        }
        return instance;
    }
    
    /**
     * Установить слушателя всех событий геймификации
     * @param listener слушатель
     */
    public void setGamificationEventListener(OnGamificationEventListener listener) {
        this.gamificationEventListener = listener;
    }
    
    /**
     * Настраивает слушателей для всех менеджеров геймификации
     */
    private void setupListeners() {
        // Настраиваем слушатель для основной системы геймификации
        gamificationManager.setAchievementListener(new GamificationManager.OnAchievementListener() {
            @Override
            public void onAchievementUnlocked(AchievementEntity achievement, int experienceGained) {
                // Проверяем, есть ли предметные награды за это достижение
                checkAchievementRewards(achievement);
                
                // Уведомляем общего слушателя
                if (gamificationEventListener != null) {
                    gamificationEventListener.onAchievementUnlocked(achievement, experienceGained);
                }
            }

            @Override
            public void onLevelUp(int newLevel, int experienceGained) {
                // Обновляем прогресс пользователя по рангам при повышении уровня
                updateUserRankProgressForLevelUp(newLevel);
                
                // Уведомляем общего слушателя
                if (gamificationEventListener != null) {
                    gamificationEventListener.onLevelUp(newLevel, experienceGained);
                }
            }
        });
        
        // Настраиваем слушатель для ежедневных заданий
        dailyQuestManager.setQuestCompleteListener(new DailyQuestManager.OnQuestCompleteListener() {
            @Override
            public void onQuestCompleted(DailyQuestEntity quest, int experienceGained) {
                // Обновляем прогресс тематических испытаний при выполнении квеста
                updateChallengeProgressForQuest(quest);
                
                // Уведомляем общего слушателя
                if (gamificationEventListener != null) {
                    gamificationEventListener.onQuestCompleted(quest, experienceGained);
                }
            }

            @Override
            public void onQuestRewardClaimed(DailyQuestEntity quest) {
                // Дополнительная логика при получении награды за квест
                Log.d(TAG, "Получена награда за квест: " + quest.getTitle());
            }
        });
        
        // Настраиваем слушатель для сезонных событий
        seasonalEventManager.setEventChangeListener(new SeasonalEventManager.OnEventChangeListener() {
            @Override
            public void onEventStarted(SeasonalEventEntity event) {
                // При начале события создаем тематические испытания и обновляем предметы
                handleEventStart(event);
                
                // Уведомляем общего слушателя
                if (gamificationEventListener != null) {
                    gamificationEventListener.onEventStarted(event);
                }
            }

            @Override
            public void onEventEnded(SeasonalEventEntity event) {
                // При завершении события деактивируем связанные испытания
                handleEventEnd(event);
                
                // Уведомляем общего слушателя
                if (gamificationEventListener != null) {
                    gamificationEventListener.onEventEnded(event);
                }
            }
        });
        
        // Настраиваем слушатель для коллекционных предметов
        collectibleItemManager.setItemObtainedListener(new CollectibleItemManager.OnItemObtainedListener() {
            @Override
            public void onItemObtained(CollectibleItemEntity item, String source) {
                // Дополнительная логика при получении предмета
                Log.d(TAG, "Получен предмет: " + item.getName() + " из источника: " + source);
                
                // Уведомляем общего слушателя
                if (gamificationEventListener != null) {
                    gamificationEventListener.onItemObtained(item, source);
                }
            }
        });
        
        // Настраиваем слушатель для тематических испытаний
        thematicChallengeManager.setChallengeListener(new ThematicChallengeManager.OnChallengeListener() {
            @Override
            public void onChallengeCompleted(ThematicChallengeEntity challenge, int experienceGained) {
                // Дополнительная логика при завершении испытания
                handleChallengeCompletion(challenge);
                
                // Уведомляем общего слушателя
                if (gamificationEventListener != null) {
                    gamificationEventListener.onChallengeCompleted(challenge, experienceGained);
                }
            }

            @Override
            public void onChallengeMilestoneReached(ChallengeMilestoneEntity milestone, int experienceGained) {
                // Дополнительная логика при достижении этапа испытания
                Log.d(TAG, "Достигнут этап испытания: " + milestone.getTitle());
                
                // Уведомляем общего слушателя
                if (gamificationEventListener != null) {
                    gamificationEventListener.onChallengeMilestoneReached(milestone, experienceGained);
                }
            }
        });
        
        // Настраиваем слушатель для системы рангов
        userRankManager.setRankChangeListener(new UserRankManager.OnRankChangeListener() {
            @Override
            public void onRankUnlocked(UserRankEntity rank, int experienceGained) {
                // При разблокировке ранга выдаем предметную награду
                handleRankUnlock(rank);
                
                // Уведомляем общего слушателя
                if (gamificationEventListener != null) {
                    gamificationEventListener.onRankUnlocked(rank, experienceGained);
                }
            }

            @Override
            public void onRankActivated(UserRankEntity rank) {
                // Дополнительная логика при активации ранга
                Log.d(TAG, "Активирован ранг: " + rank.getName());
                
                // Уведомляем общего слушателя
                if (gamificationEventListener != null) {
                    gamificationEventListener.onRankActivated(rank);
                }
            }
        });
    }
    
    /**
     * Обрабатывает действие пользователя и обновляет все системы геймификации
     * @param userId ID пользователя
     * @param action тип действия (swipe, rate, review, complete)
     * @param data дополнительные данные
     * @param category категория контента (может быть null)
     */
    public void processUserAction(String userId, String action, String data, String category) {
        try {
            // Обрабатываем действие в основной системе геймификации
            boolean levelUp = gamificationManager.processUserAction(userId, action, data);
            
            // Обновляем прогресс ежедневных заданий
            int actionCount = 1; // Обычно одно действие = +1 к прогрессу
            if (action.equals(GamificationManager.ACTION_SWIPE)) {
                // Для свайпов можем обрабатывать пачками
                actionCount = 1;
            }
            dailyQuestManager.updateQuestProgress(userId, action, actionCount, category);
            
            // Обновляем прогресс рангов пользователя
            userRankManager.updateUserRankProgress(userId);
            
            // Получаем текущий множитель опыта с учетом активного ранга и сезонных событий
            float totalMultiplier = calculateTotalXpMultiplier(userId);
            
            // Логируем общую активность
            Log.d(TAG, "Обработано действие пользователя " + userId + ": " + action + 
                    " (категория: " + category + ", множитель XP: " + totalMultiplier + ")");
                    
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обработке действия пользователя: " + e.getMessage());
        }
    }
    
    /**
     * Инициализирует пользователя во всех системах геймификации
     * @param userId ID пользователя
     */
    public void initializeUserInAllSystems(String userId) {
        try {
            // Инициализируем пользователя в базовой системе геймификации
            GamificationManager.initUserStats(userId);
            
            // Инициализируем ежедневные задания для пользователя
            dailyQuestManager.initDailyQuests(userId);
            
            // Обновляем прогресс рангов пользователя
            userRankManager.updateUserRankProgress(userId);
            
            // Выдаем начальные предметы
            giveStarterItems(userId);
            
            Log.d(TAG, "Пользователь " + userId + " инициализирован во всех системах геймификации");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при инициализации пользователя: " + e.getMessage());
        }
    }
    
    /**
     * Обновляет все системы геймификации (вызывается периодически)
     */
    public void refreshAllSystems() {
        try {
            // Обновляем состояние сезонных событий
            seasonalEventManager.refreshEvents();
            
            // Обновляем ежедневные задания
            dailyQuestManager.refreshQuests();
            
            // Обновляем тематические испытания
            thematicChallengeManager.refreshChallenges();
            
            // Обновляем связь предметов с событиями
            collectibleItemManager.updateEventItemsAssociation();
            
            Log.d(TAG, "Все системы геймификации обновлены");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обновлении систем геймификации: " + e.getMessage());
        }
    }
    
    /**
     * Вычисляет общий множитель опыта для пользователя
     * @param userId ID пользователя
     * @return общий множитель опыта
     */
    private float calculateTotalXpMultiplier(String userId) {
        // Получаем множитель от активного ранга
        float rankMultiplier = userRankManager.getUserXpMultiplier(userId);
        
        // Получаем множитель от сезонных событий
        float eventMultiplier = seasonalEventManager.getCurrentXpMultiplier();
        
        // Комбинируем множители (можно настроить формулу)
        return Math.max(rankMultiplier, eventMultiplier);
    }
    
    /**
     * Проверяет и выдает предметные награды за достижение
     * @param achievement достижение
     */
    private void checkAchievementRewards(AchievementEntity achievement) {
        // Здесь можно реализовать логику выдачи особых предметов за определенные достижения
        // Например, за достижения в конкретных категориях
        try {
            if (achievement.getCategory().equals("movie_buff")) {
                // За достижения в фильмах даем значок киномана
                // (логика поиска подходящих предметов)
            } else if (achievement.getCategory().equals("bookworm")) {
                // За достижения в книгах даем значок книголюба
            }
            // И так далее для других категорий
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при проверке предметных наград: " + e.getMessage());
        }
    }
    
    /**
     * Обновляет прогресс рангов при повышении уровня
     * @param newLevel новый уровень пользователя
     */
    private void updateUserRankProgressForLevelUp(int newLevel) {
        // Логика уже реализована в самом UserRankManager
        // Здесь можно добавить дополнительную обработку при необходимости
        Log.d(TAG, "Обновлен прогресс рангов для нового уровня: " + newLevel);
    }
    
    /**
     * Обновляет прогресс тематических испытаний при выполнении квеста
     * @param quest выполненный квест
     */
    private void updateChallengeProgressForQuest(DailyQuestEntity quest) {
        // Можно реализовать логику, где выполнение определенных квестов 
        // автоматически продвигает пользователя в тематических испытаниях
        Log.d(TAG, "Проверяем влияние квеста на тематические испытания: " + quest.getTitle());
    }
    
    /**
     * Обрабатывает начало сезонного события
     * @param event сезонное событие
     */
    private void handleEventStart(SeasonalEventEntity event) {
        try {
            // Создаем тематические испытания для события
            if (event.hasSpecialQuests()) {
                thematicChallengeManager.createChallengesForEvent(
                    event.getId(), 
                    event.getTitle(), 
                    event.getStartDate(), 
                    event.getEndDate()
                );
            }
            
            Log.d(TAG, "Обработано начало события: " + event.getTitle());
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обработке начала события: " + e.getMessage());
        }
    }
    
    /**
     * Обрабатывает завершение сезонного события
     * @param event сезонное событие
     */
    private void handleEventEnd(SeasonalEventEntity event) {
        try {
            // Деактивируем связанные с событием испытания
            List<ThematicChallengeEntity> eventChallenges = 
                thematicChallengeManager.getActiveChallengesForEvent(event.getId());
            
            for (ThematicChallengeEntity challenge : eventChallenges) {
                // Здесь можно добавить логику завершения испытаний
                Log.d(TAG, "Завершается испытание события: " + challenge.getTitle());
            }
            
            Log.d(TAG, "Обработано завершение события: " + event.getTitle());
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обработке завершения события: " + e.getMessage());
        }
    }
    
    /**
     * Обрабатывает завершение тематического испытания
     * @param challenge завершенное испытание
     */
    private void handleChallengeCompletion(ThematicChallengeEntity challenge) {
        try {
            // Дополнительная логика при завершении испытания
            // Например, можно начислить бонусы или открыть новые возможности
            Log.d(TAG, "Завершено тематическое испытание: " + challenge.getTitle());
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обработке завершения испытания: " + e.getMessage());
        }
    }
    
    /**
     * Обрабатывает разблокировку ранга
     * @param rank разблокированный ранг
     */
    private void handleRankUnlock(UserRankEntity rank) {
        try {
            // Выдаем особые предметы за ранги
            if (rank.getName().equals("Корона эксперта") || rank.getOrderIndex() >= 5) {
                // За высокие ранги выдаем особые предметы
                // Логика поиска и выдачи предметов для ранга
            }
            
            Log.d(TAG, "Разблокирован ранг: " + rank.getName());
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обработке разблокировки ранга: " + e.getMessage());
        }
    }
    
    /**
     * Выдает начальные предметы новому пользователю
     * @param userId ID пользователя
     */
    private void giveStarterItems(String userId) {
        try {
            // Находим начальные предметы (obtainedFrom = "starter")
            List<CollectibleItemEntity> allItems = collectibleItemManager.getAllItems();
            
            for (CollectibleItemEntity item : allItems) {
                if ("starter".equals(item.getObtainedFrom())) {
                    collectibleItemManager.addItemToUser(userId, item.getId(), "starter");
                }
            }
            
            Log.d(TAG, "Выданы начальные предметы пользователю: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при выдаче начальных предметов: " + e.getMessage());
        }
    }
    
    /**
     * Получает сводную статистику геймификации для пользователя
     * @param userId ID пользователя
     * @return объект со сводной статистикой
     */
    public GamificationSummary getUserGamificationSummary(String userId) {
        try {
            // Базовая статистика
            com.draker.swipetime.database.entities.UserStatsEntity stats = 
                gamificationManager.getUserStats(userId);
            
            // Активный ранг
            UserRankEntity activeRank = userRankManager.getUserActiveRank(userId);
            
            // Количество разблокированных рангов
            int unlockedRanksCount = userRankManager.getUserUnlockedRanks(userId).size();
            
            // Количество предметов
            int itemsCount = collectibleItemManager.getUserItems(userId).size();
            
            // Количество выполненных достижений
            int achievementsCount = gamificationManager.getCompletedAchievementsCount(userId);
            
            // Активные ежедневные задания
            List<DailyQuestEntity> activeQuests = dailyQuestManager.getActiveQuestsForUser(userId);
            
            // Активные тематические испытания
            List<ThematicChallengeEntity> activeChallenges = thematicChallengeManager.getActiveChallenges();
            
            // Активные сезонные события
            List<SeasonalEventEntity> activeEvents = seasonalEventManager.getCurrentlyActiveEvents();
            
            return new GamificationSummary(
                stats,
                activeRank,
                unlockedRanksCount,
                itemsCount,
                achievementsCount,
                activeQuests.size(),
                activeChallenges.size(),
                activeEvents.size()
            );
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении сводной статистики: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Класс для хранения сводной статистики геймификации
     */
    public static class GamificationSummary {
        private final com.draker.swipetime.database.entities.UserStatsEntity userStats;
        private final UserRankEntity activeRank;
        private final int unlockedRanksCount;
        private final int itemsCount;
        private final int achievementsCount;
        private final int activeQuestsCount;
        private final int activeChallengesCount;
        private final int activeEventsCount;
        
        public GamificationSummary(com.draker.swipetime.database.entities.UserStatsEntity userStats,
                                 UserRankEntity activeRank, int unlockedRanksCount, int itemsCount,
                                 int achievementsCount, int activeQuestsCount, int activeChallengesCount,
                                 int activeEventsCount) {
            this.userStats = userStats;
            this.activeRank = activeRank;
            this.unlockedRanksCount = unlockedRanksCount;
            this.itemsCount = itemsCount;
            this.achievementsCount = achievementsCount;
            this.activeQuestsCount = activeQuestsCount;
            this.activeChallengesCount = activeChallengesCount;
            this.activeEventsCount = activeEventsCount;
        }
        
        // Геттеры для всех полей
        public com.draker.swipetime.database.entities.UserStatsEntity getUserStats() { return userStats; }
        public UserRankEntity getActiveRank() { return activeRank; }
        public int getUnlockedRanksCount() { return unlockedRanksCount; }
        public int getItemsCount() { return itemsCount; }
        public int getAchievementsCount() { return achievementsCount; }
        public int getActiveQuestsCount() { return activeQuestsCount; }
        public int getActiveChallengesCount() { return activeChallengesCount; }
        public int getActiveEventsCount() { return activeEventsCount; }
    }
}
