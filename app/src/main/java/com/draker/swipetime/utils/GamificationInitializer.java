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

/**
 * Инициализатор для системы расширенной геймификации
 * Настраивает и запускает все компоненты геймификации при старте приложения
 */
public class GamificationInitializer {
    
    private static final String TAG = "GamificationInitializer";
    
    private final Context context;
    private AdvancedGamificationIntegrator gamificationIntegrator;
    
    public GamificationInitializer(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Инициализирует все системы геймификации
     * Должен вызываться при старте приложения
     */
    public void initializeGamificationSystems() {
        try {
            Log.d(TAG, "Начинается инициализация систем геймификации...");
            
            // Получаем интегратор геймификации
            gamificationIntegrator = AdvancedGamificationIntegrator.getInstance(context);
            
            // Настраиваем глобальный слушатель событий геймификации
            setupGlobalEventListener();
            
            // Обновляем все системы
            gamificationIntegrator.refreshAllSystems();
            
            Log.d(TAG, "Системы геймификации успешно инициализированы");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при инициализации систем геймификации: " + e.getMessage());
        }
    }
    
    /**
     * Инициализирует пользователя в системе геймификации
     * Должен вызываться при входе пользователя в приложение
     * @param userId ID пользователя
     */
    public void initializeUserGamification(String userId) {
        try {
            Log.d(TAG, "Инициализация геймификации для пользователя: " + userId);
            
            if (gamificationIntegrator == null) {
                gamificationIntegrator = AdvancedGamificationIntegrator.getInstance(context);
            }
            
            // Инициализируем пользователя во всех системах
            gamificationIntegrator.initializeUserInAllSystems(userId);
            
            Log.d(TAG, "Геймификация для пользователя " + userId + " успешно инициализирована");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при инициализации геймификации пользователя: " + e.getMessage());
        }
    }
    
    /**
     * Обрабатывает действие пользователя через все системы геймификации
     * @param userId ID пользователя
     * @param action тип действия
     * @param data дополнительные данные
     * @param category категория контента
     */
    public void processUserAction(String userId, String action, String data, String category) {
        try {
            if (gamificationIntegrator == null) {
                gamificationIntegrator = AdvancedGamificationIntegrator.getInstance(context);
            }
            
            gamificationIntegrator.processUserAction(userId, action, data, category);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обработке действия пользователя: " + e.getMessage());
        }
    }
    
    /**
     * Получает сводную статистику геймификации для пользователя
     * @param userId ID пользователя
     * @return сводная статистика
     */
    public AdvancedGamificationIntegrator.GamificationSummary getUserSummary(String userId) {
        try {
            if (gamificationIntegrator == null) {
                gamificationIntegrator = AdvancedGamificationIntegrator.getInstance(context);
            }
            
            return gamificationIntegrator.getUserGamificationSummary(userId);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении сводки пользователя: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Настраивает глобальный слушатель событий геймификации
     */
    private void setupGlobalEventListener() {
        gamificationIntegrator.setGamificationEventListener(new AdvancedGamificationIntegrator.OnGamificationEventListener() {
            @Override
            public void onAchievementUnlocked(AchievementEntity achievement, int experienceGained) {
                Log.d(TAG, "🏆 Достижение разблокировано: " + achievement.getName() + " (+" + experienceGained + " XP)");
                // Здесь можно добавить показ уведомления пользователю
                showAchievementNotification(achievement, experienceGained);
            }

            @Override
            public void onLevelUp(int newLevel, int experienceGained) {
                Log.d(TAG, "⬆️ Повышение уровня: " + newLevel + " (+" + experienceGained + " XP)");
                // Здесь можно добавить показ уведомления о повышении уровня
                showLevelUpNotification(newLevel, experienceGained);
            }

            @Override
            public void onQuestCompleted(DailyQuestEntity quest, int experienceGained) {
                Log.d(TAG, "✅ Ежедневное задание выполнено: " + quest.getTitle() + " (+" + experienceGained + " XP)");
                // Здесь можно добавить показ уведомления о выполнении задания
                showQuestCompletedNotification(quest, experienceGained);
            }

            @Override
            public void onEventStarted(SeasonalEventEntity event) {
                Log.d(TAG, "🎉 Начало сезонного события: " + event.getTitle());
                // Здесь можно добавить показ уведомления о начале события
                showEventStartedNotification(event);
            }

            @Override
            public void onEventEnded(SeasonalEventEntity event) {
                Log.d(TAG, "🎭 Завершение сезонного события: " + event.getTitle());
                // Здесь можно добавить показ уведомления о завершении события
                showEventEndedNotification(event);
            }

            @Override
            public void onItemObtained(CollectibleItemEntity item, String source) {
                Log.d(TAG, "💎 Получен предмет: " + item.getName() + " (источник: " + source + ")");
                // Здесь можно добавить показ уведомления о получении предмета
                showItemObtainedNotification(item, source);
            }

            @Override
            public void onChallengeCompleted(ThematicChallengeEntity challenge, int experienceGained) {
                Log.d(TAG, "🚩 Тематическое испытание завершено: " + challenge.getTitle() + " (+" + experienceGained + " XP)");
                // Здесь можно добавить показ уведомления о завершении испытания
                showChallengeCompletedNotification(challenge, experienceGained);
            }

            @Override
            public void onChallengeMilestoneReached(ChallengeMilestoneEntity milestone, int experienceGained) {
                Log.d(TAG, "🎯 Этап испытания достигнут: " + milestone.getTitle() + " (+" + experienceGained + " XP)");
                // Здесь можно добавить показ уведомления о достижении этапа
            }

            @Override
            public void onRankUnlocked(UserRankEntity rank, int experienceGained) {
                Log.d(TAG, "👑 Ранг разблокирован: " + rank.getName() + " (+" + experienceGained + " XP)");
                // Здесь можно добавить показ уведомления о разблокировке ранга
                showRankUnlockedNotification(rank, experienceGained);
            }

            @Override
            public void onRankActivated(UserRankEntity rank) {
                Log.d(TAG, "⭐ Ранг активирован: " + rank.getName());
                // Здесь можно добавить показ уведомления об активации ранга
                showRankActivatedNotification(rank);
            }
        });
    }
    
    /**
     * Показывает уведомление о разблокированном достижении
     * @param achievement достижение
     * @param experienceGained полученный опыт
     */
    private void showAchievementNotification(AchievementEntity achievement, int experienceGained) {
        // Здесь можно реализовать показ Toast, SnackBar или custom notification
        // Например, используя AchievementNotifier
        try {
            AchievementNotifier notifier = new AchievementNotifier(context);
            notifier.showAchievementUnlocked(achievement, experienceGained);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при показе уведомления о достижении: " + e.getMessage());
        }
    }
    
    /**
     * Показывает уведомление о повышении уровня
     * @param newLevel новый уровень
     * @param experienceGained полученный опыт
     */
    private void showLevelUpNotification(int newLevel, int experienceGained) {
        try {
            AchievementNotifier notifier = new AchievementNotifier(context);
            notifier.showLevelUp(newLevel, experienceGained);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при показе уведомления о повышении уровня: " + e.getMessage());
        }
    }
    
    /**
     * Показывает уведомление о выполнении ежедневного задания
     * @param quest задание
     * @param experienceGained полученный опыт
     */
    private void showQuestCompletedNotification(DailyQuestEntity quest, int experienceGained) {
        try {
            AchievementNotifier notifier = new AchievementNotifier(context);
            notifier.showQuestCompleted(quest, experienceGained);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при показе уведомления о выполнении задания: " + e.getMessage());
        }
    }
    
    /**
     * Показывает уведомление о начале сезонного события
     * @param event событие
     */
    private void showEventStartedNotification(SeasonalEventEntity event) {
        try {
            AchievementNotifier notifier = new AchievementNotifier(context);
            notifier.showEventStarted(event);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при показе уведомления о событии: " + e.getMessage());
        }
    }
    
    /**
     * Показывает уведомление о завершении сезонного события
     * @param event событие
     */
    private void showEventEndedNotification(SeasonalEventEntity event) {
        try {
            AchievementNotifier notifier = new AchievementNotifier(context);
            notifier.showEventEnded(event);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при показе уведомления о завершении события: " + e.getMessage());
        }
    }
    
    /**
     * Показывает уведомление о получении предмета
     * @param item предмет
     * @param source источник
     */
    private void showItemObtainedNotification(CollectibleItemEntity item, String source) {
        try {
            AchievementNotifier notifier = new AchievementNotifier(context);
            notifier.showItemObtained(item, source);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при показе уведомления о предмете: " + e.getMessage());
        }
    }
    
    /**
     * Показывает уведомление о завершении тематического испытания
     * @param challenge испытание
     * @param experienceGained полученный опыт
     */
    private void showChallengeCompletedNotification(ThematicChallengeEntity challenge, int experienceGained) {
        try {
            AchievementNotifier notifier = new AchievementNotifier(context);
            notifier.showChallengeCompleted(challenge, experienceGained);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при показе уведомления об испытании: " + e.getMessage());
        }
    }
    
    /**
     * Показывает уведомление о разблокировке ранга
     * @param rank ранг
     * @param experienceGained полученный опыт
     */
    private void showRankUnlockedNotification(UserRankEntity rank, int experienceGained) {
        try {
            AchievementNotifier notifier = new AchievementNotifier(context);
            notifier.showRankUnlocked(rank, experienceGained);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при показе уведомления о ранге: " + e.getMessage());
        }
    }
    
    /**
     * Показывает уведомление об активации ранга
     * @param rank ранг
     */
    private void showRankActivatedNotification(UserRankEntity rank) {
        try {
            AchievementNotifier notifier = new AchievementNotifier(context);
            notifier.showRankActivated(rank);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при показе уведомления об активации ранга: " + e.getMessage());
        }
    }
    
    /**
     * Выполняет обновление всех систем геймификации
     * Можно вызывать периодически (например, при запуске приложения)
     */
    public void refreshAllSystems() {
        try {
            if (gamificationIntegrator == null) {
                gamificationIntegrator = AdvancedGamificationIntegrator.getInstance(context);
            }
            
            gamificationIntegrator.refreshAllSystems();
            Log.d(TAG, "Все системы геймификации обновлены");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обновлении систем: " + e.getMessage());
        }
    }
    
    /**
     * Получает интегратор геймификации
     * @return интегратор геймификации
     */
    public AdvancedGamificationIntegrator getGamificationIntegrator() {
        if (gamificationIntegrator == null) {
            gamificationIntegrator = AdvancedGamificationIntegrator.getInstance(context);
        }
        return gamificationIntegrator;
    }
    
    /**
     * Проверяет, инициализированы ли системы геймификации
     * @return true если системы инициализированы
     */
    public boolean isInitialized() {
        return gamificationIntegrator != null;
    }
}
