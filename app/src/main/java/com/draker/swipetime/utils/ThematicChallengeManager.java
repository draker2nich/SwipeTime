package com.draker.swipetime.utils;

import android.content.Context;
import android.util.Log;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.ThematicChallengeDao;
import com.draker.swipetime.database.dao.ChallengeMilestoneDao;
import com.draker.swipetime.database.dao.UserChallengeProgressDao;
import com.draker.swipetime.database.entities.ThematicChallengeEntity;
import com.draker.swipetime.database.entities.ChallengeMilestoneEntity;
import com.draker.swipetime.database.entities.UserChallengeProgressEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Calendar;

/**
 * Менеджер для управления тематическими испытаниями
 */
public class ThematicChallengeManager {

    private static final String TAG = "ThematicChallengeManager";
    
    private final Context context;
    private final AppDatabase database;
    private final ThematicChallengeDao thematicChallengeDao;
    private final ChallengeMilestoneDao challengeMilestoneDao;
    private final UserChallengeProgressDao userChallengeProgressDao;
    private final GamificationManager gamificationManager;
    
    private static ThematicChallengeManager instance;
    
    // Интерфейс для обратных вызовов о выполнении испытаний
    public interface OnChallengeListener {
        void onChallengeCompleted(ThematicChallengeEntity challenge, int experienceGained);
        void onChallengeMilestoneReached(ChallengeMilestoneEntity milestone, int experienceGained);
    }
    
    private OnChallengeListener challengeListener;
    
    // Приватный конструктор (Singleton)
    private ThematicChallengeManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
        this.thematicChallengeDao = database.thematicChallengeDao();
        this.challengeMilestoneDao = database.challengeMilestoneDao();
        this.userChallengeProgressDao = database.userChallengeProgressDao();
        this.gamificationManager = GamificationManager.getInstance(context);
    }
    
    // Получение экземпляра (Singleton)
    public static synchronized ThematicChallengeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThematicChallengeManager(context);
        }
        return instance;
    }
    
    /**
     * Установить слушатель выполнения испытаний
     * @param listener слушатель
     */
    public void setChallengeListener(OnChallengeListener listener) {
        this.challengeListener = listener;
    }
    
    /**
     * Создать тематические испытания для сезонного события
     * @param eventId ID события
     * @param eventTitle заголовок события
     * @param startDate дата начала
     * @param endDate дата окончания
     */
    public void createChallengesForEvent(String eventId, String eventTitle, long startDate, long endDate) {
        try {
            // Создаем испытание "Исследователь события"
            ThematicChallengeEntity explorerChallenge = new ThematicChallengeEntity(
                UUID.randomUUID().toString(),
                "Исследователь: " + eventTitle,
                "Откройте для себя новый контент во время события",
                "ic_challenge_explorer",
                "#4CAF50", // зеленый цвет для исследователя
                startDate,
                endDate,
                true,
                2, // средняя сложность
                "all", // все категории
                null, // жанр не указан
                150, // XP награда
                null, // предметная награда будет добавлена позже
                eventId,
                3 // 3 этапа
            );
            
            // Создаем испытание "Критик события"
            ThematicChallengeEntity criticChallenge = new ThematicChallengeEntity(
                UUID.randomUUID().toString(),
                "Критик: " + eventTitle,
                "Оцените и прокомментируйте контент события",
                "ic_challenge_critic",
                "#FF9800", // оранжевый цвет для критика
                startDate,
                endDate,
                true,
                3, // высокая сложность
                "all", // все категории
                null, // жанр не указан
                200, // XP награда
                null, // предметная награда будет добавлена позже
                eventId,
                3 // 3 этапа
            );
            
            // Создаем испытание "Коллекционер события"
            ThematicChallengeEntity collectorChallenge = new ThematicChallengeEntity(
                UUID.randomUUID().toString(),
                "Коллекционер: " + eventTitle,
                "Соберите все предметы, связанные с событием",
                "ic_challenge_collector",
                "#9C27B0", // фиолетовый цвет для коллекционера
                startDate,
                endDate,
                true,
                1, // легкая сложность
                "all", // все категории
                null, // жанр не указан
                100, // XP награда
                null, // предметная награда будет добавлена позже
                eventId,
                3 // 3 этапа
            );
            
            // Сохраняем испытания в БД
            List<ThematicChallengeEntity> challenges = new ArrayList<>();
            challenges.add(explorerChallenge);
            challenges.add(criticChallenge);
            challenges.add(collectorChallenge);
            
            thematicChallengeDao.insertAll(challenges);
            
            // Создаем этапы для каждого испытания
            createMilestonesForChallenge(explorerChallenge);
            createMilestonesForChallenge(criticChallenge);
            createMilestonesForChallenge(collectorChallenge);
            
            Log.d(TAG, "Созданы тематические испытания для события: " + eventTitle);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при создании испытаний для события: " + e.getMessage());
        }
    }
    
    /**
     * Создать этапы для тематического испытания
     * @param challenge испытание
     */
    private void createMilestonesForChallenge(ThematicChallengeEntity challenge) {
        try {
            List<ChallengeMilestoneEntity> milestones = new ArrayList<>();
            
            if (challenge.getCategory().equals("all")) {
                // Этапы для исследователя (основываемся на associatedEventId)
                if (challenge.getAssociatedEventId() != null) {
                    milestones.add(new ChallengeMilestoneEntity(
                        UUID.randomUUID().toString(),
                        challenge.getId(),
                        "Первое открытие",
                        "Найдите 10 новых элементов контента",
                        1,
                        "swipe",
                        10,
                        null,
                        null,
                        null,
                        50
                    ));
                    
                    milestones.add(new ChallengeMilestoneEntity(
                        UUID.randomUUID().toString(),
                        challenge.getId(),
                        "Активный исследователь",
                        "Найдите 25 новых элементов контента",
                        2,
                        "swipe",
                        25,
                        null,
                        null,
                        null,
                        100
                    ));
                    
                    milestones.add(new ChallengeMilestoneEntity(
                        UUID.randomUUID().toString(),
                        challenge.getId(),
                        "Мастер исследований",
                        "Найдите 50 новых элементов контента",
                        3,
                        "swipe",
                        50,
                        null,
                        null,
                        null,
                        200
                    ));
                }
            }
            
            // Сохраняем этапы в БД
            if (!milestones.isEmpty()) {
                challengeMilestoneDao.insertAll(milestones);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при создании этапов испытания: " + e.getMessage());
        }
    }
    
    /**
     * Получить все активные тематические испытания
     * @return список активных испытаний
     */
    public List<ThematicChallengeEntity> getActiveChallenges() {
        try {
            long currentTime = System.currentTimeMillis();
            return thematicChallengeDao.getActiveByTime(currentTime);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении активных испытаний: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Получить активные испытания для конкретного события
     * @param eventId ID события
     * @return список испытаний события
     */
    public List<ThematicChallengeEntity> getActiveChallengesForEvent(String eventId) {
        try {
            long currentTime = System.currentTimeMillis();
            return thematicChallengeDao.getByEventId(eventId);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении испытаний для события: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Получить прогресс пользователя по испытаниям
     * @param userId ID пользователя
     * @param challengeId ID испытания
     * @return прогресс пользователя
     */
    public UserChallengeProgressEntity getUserChallengeProgress(String userId, String challengeId) {
        try {
            UserChallengeProgressEntity progress = userChallengeProgressDao.getByIds(userId, challengeId);
            if (progress == null) {
                // Создаем новую запись прогресса
                progress = new UserChallengeProgressEntity(userId, challengeId);
                userChallengeProgressDao.insert(progress);
            }
            return progress;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении прогресса испытания: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Обновить прогресс пользователя в испытании
     * @param userId ID пользователя
     * @param challengeId ID испытания
     * @param progressIncrement приращение прогресса
     */
    public void updateChallengeProgress(String userId, String challengeId, int progressIncrement) {
        try {
            UserChallengeProgressEntity progress = getUserChallengeProgress(userId, challengeId);
            if (progress != null) {
                // Получаем информацию об испытании
                ThematicChallengeEntity challenge = thematicChallengeDao.getById(challengeId);
                if (challenge == null || !challenge.isActive()) {
                    return; // Испытание не найдено или неактивно
                }
                
                // Обновляем прогресс
                int oldProgress = progress.getTotalProgress();
                progress.setTotalProgress(oldProgress + progressIncrement);
                userChallengeProgressDao.update(progress);
                
                // Проверяем этапы испытания
                checkChallengeMilestones(userId, challengeId, progress.getTotalProgress());
                
                Log.d(TAG, "Обновлен прогресс испытания " + challengeId + " для пользователя " + userId 
                      + ": +" + progressIncrement + " (итого: " + progress.getTotalProgress() + ")");
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обновлении прогресса испытания: " + e.getMessage());
        }
    }
    
    /**
     * Проверить и активировать достигнутые этапы испытания
     * @param userId ID пользователя
     * @param challengeId ID испытания
     * @param totalProgress общий прогресс пользователя
     */
    private void checkChallengeMilestones(String userId, String challengeId, int totalProgress) {
        try {
            // Получаем все этапы испытания
            List<ChallengeMilestoneEntity> milestones = challengeMilestoneDao.getByChallengeId(challengeId);
            
            for (ChallengeMilestoneEntity milestone : milestones) {
                // Проверяем, достиг ли пользователь этого этапа
                if (totalProgress >= milestone.getRequiredProgress()) {
                    // Проверяем, не был ли этап уже засчитан
                    UserChallengeProgressEntity progress = getUserChallengeProgress(userId, challengeId);
                    
                    if (progress != null && !progress.isMilestoneCompleted(milestone.getOrderIndex())) {
                        // Отмечаем этап как выполненный
                        progress.markMilestoneCompleted(milestone.getOrderIndex());
                        userChallengeProgressDao.update(progress);
                        
                        // Начисляем награду за этап
                        rewardMilestoneCompletion(userId, milestone);
                        
                        // Уведомляем слушателя
                        if (challengeListener != null) {
                            challengeListener.onChallengeMilestoneReached(milestone, milestone.getExperienceReward());
                        }
                        
                        Log.d(TAG, "Достигнут этап испытания: " + milestone.getTitle() + 
                              " (награда: " + milestone.getExperienceReward() + " XP)");
                    }
                }
            }
            
            // Проверяем, завершено ли испытание полностью
            checkChallengeCompletion(userId, challengeId);
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при проверке этапов испытания: " + e.getMessage());
        }
    }
    
    /**
     * Выдать награду за завершение этапа
     * @param userId ID пользователя
     * @param milestone этап
     */
    private void rewardMilestoneCompletion(String userId, ChallengeMilestoneEntity milestone) {
        try {
            // Начисляем опыт через GamificationManager
            if (milestone.getExperienceReward() > 0 && gamificationManager != null) {
                // Используем processUserAction для начисления опыта
                gamificationManager.processUserAction(userId, "challenge_milestone", 
                                                   "Completed milestone: " + milestone.getTitle());
            }
            
            // Выдаем предметную награду (пока не реализовано полностью)
            String itemRewardId = milestone.getItemRewardId();
            if (itemRewardId != null && !itemRewardId.isEmpty()) {
                CollectibleItemManager itemManager = CollectibleItemManager.getInstance(context);
                if (itemManager != null) {
                    itemManager.addItemToUser(userId, itemRewardId, "challenge_milestone");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при выдаче награды за этап: " + e.getMessage());
        }
    }
    
    /**
     * Проверить завершение испытания
     * @param userId ID пользователя
     * @param challengeId ID испытания
     */
    private void checkChallengeCompletion(String userId, String challengeId) {
        try {
            UserChallengeProgressEntity progress = getUserChallengeProgress(userId, challengeId);
            ThematicChallengeEntity challenge = thematicChallengeDao.getById(challengeId);
            
            if (progress != null && challenge != null && !progress.isCompleted()) {
                // Получаем все этапы испытания
                List<ChallengeMilestoneEntity> milestones = challengeMilestoneDao.getByChallengeId(challengeId);
                
                // Проверяем, выполнены ли все этапы
                boolean allMilestonesCompleted = true;
                for (ChallengeMilestoneEntity milestone : milestones) {
                    if (!progress.isMilestoneCompleted(milestone.getOrderIndex())) {
                        allMilestonesCompleted = false;
                        break;
                    }
                }
                
                // Если все этапы выполнены, отмечаем испытание как завершенное
                if (allMilestonesCompleted) {
                    progress.setCompleted(true);
                    progress.setCompletedAt(System.currentTimeMillis());
                    userChallengeProgressDao.update(progress);
                    
                    // Выдаем финальную награду за испытание
                    int finalReward = 500; // Базовая награда за завершение испытания
                    if (gamificationManager != null) {
                        gamificationManager.processUserAction(userId, "challenge_complete", 
                                                           "Completed challenge: " + challenge.getTitle());
                    }
                    
                    // Уведомляем слушателя
                    if (challengeListener != null) {
                        challengeListener.onChallengeCompleted(challenge, finalReward);
                    }
                    
                    Log.d(TAG, "Испытание завершено: " + challenge.getTitle() + 
                          " (финальная награда: " + finalReward + " XP)");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при проверке завершения испытания: " + e.getMessage());
        }
    }
    
    /**
     * Получить процент завершения испытания
     * @param userId ID пользователя
     * @param challengeId ID испытания
     * @return процент завершения (0-100)
     */
    public int getChallengeCompletionPercentage(String userId, String challengeId) {
        try {
            UserChallengeProgressEntity progress = getUserChallengeProgress(userId, challengeId);
            List<ChallengeMilestoneEntity> milestones = challengeMilestoneDao.getByChallengeId(challengeId);
            
            if (progress != null && !milestones.isEmpty()) {
                if (progress.isCompleted()) {
                    return 100;
                }
                
                int completedMilestones = 0;
                for (ChallengeMilestoneEntity milestone : milestones) {
                    if (progress.isMilestoneCompleted(milestone.getOrderIndex())) {
                        completedMilestones++;
                    }
                }
                
                return (completedMilestones * 100) / milestones.size();
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при вычислении процента завершения: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Обновить состояние тематических испытаний (деактивировать истекшие)
     */
    public void refreshChallenges() {
        try {
            long currentTime = System.currentTimeMillis();
            
            // Получаем истекшие испытания
            List<ThematicChallengeEntity> expiredChallenges = thematicChallengeDao.getExpiredByTime(currentTime);
            
            // Деактивируем их
            for (ThematicChallengeEntity challenge : expiredChallenges) {
                challenge.setActive(false);
                thematicChallengeDao.update(challenge);
            }
            
            if (!expiredChallenges.isEmpty()) {
                Log.d(TAG, "Деактивировано испытаний: " + expiredChallenges.size());
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обновлении состояния испытаний: " + e.getMessage());
        }
    }
    
    /**
     * Создать постоянные тематические испытания (не связанные с событиями)
     */
    public void createPermanentChallenges() {
        try {
            // Проверяем, есть ли уже постоянные испытания
            List<ThematicChallengeEntity> existingChallenges = thematicChallengeDao.getAll();
            
            // Фильтруем только постоянные испытания (без привязки к событию)
            boolean hasPermanentChallenges = false;
            for (ThematicChallengeEntity challenge : existingChallenges) {
                if (challenge.getAssociatedEventId() == null) {
                    hasPermanentChallenges = true;
                    break;
                }
            }
            
            if (!hasPermanentChallenges) {
                // Создаем постоянные испытания
                long currentTime = System.currentTimeMillis();
                long farFuture = currentTime + (365L * 24 * 60 * 60 * 1000); // Год вперед
                
                List<ThematicChallengeEntity> permanentChallenges = new ArrayList<>();
                
                // Испытание "Мастер свайпов"
                permanentChallenges.add(new ThematicChallengeEntity(
                    UUID.randomUUID().toString(),
                    "Мастер свайпов",
                    "Станьте экспертом в оценке контента",
                    "ic_challenge_swipe_master",
                    "#2196F3", // синий цвет
                    currentTime,
                    farFuture,
                    true,
                    2, // средняя сложность
                    "all", // все категории
                    null, // жанр не указан
                    250, // XP награда
                    null, // предметная награда
                    null, // Не связано с событием
                    3 // 3 этапа
                ));
                
                // Испытание "Критик года"
                permanentChallenges.add(new ThematicChallengeEntity(
                    UUID.randomUUID().toString(),
                    "Критик года",
                    "Оставьте множество качественных рецензий",
                    "ic_challenge_critic_year",
                    "#FF5722", // красно-оранжевый цвет
                    currentTime,
                    farFuture,
                    true,
                    3, // высокая сложность
                    "all", // все категории
                    null, // жанр не указан
                    300, // XP награда
                    null, // предметная награда
                    null, // Не связано с событием
                    3 // 3 этапа
                ));
                
                // Испытание "Исследователь жанров"
                permanentChallenges.add(new ThematicChallengeEntity(
                    UUID.randomUUID().toString(),
                    "Исследователь жанров",
                    "Попробуйте контент из всех категорий",
                    "ic_challenge_genre_explorer",
                    "#4CAF50", // зеленый цвет
                    currentTime,
                    farFuture,
                    true,
                    1, // легкая сложность
                    "all", // все категории
                    null, // жанр не указан
                    200, // XP награда
                    null, // предметная награда
                    null, // Не связано с событием
                    3 // 3 этапа
                ));
                
                thematicChallengeDao.insertAll(permanentChallenges);
                
                // Создаем этапы для постоянных испытаний
                for (ThematicChallengeEntity challenge : permanentChallenges) {
                    createMilestonesForChallenge(challenge);
                }
                
                Log.d(TAG, "Созданы постоянные тематические испытания: " + permanentChallenges.size());
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при создании постоянных испытаний: " + e.getMessage());
        }
    }
}
