package com.draker.swipetime.utils;

import android.content.Context;
import android.util.Log;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.DailyQuestDao;
import com.draker.swipetime.database.dao.UserQuestProgressDao;
import com.draker.swipetime.database.entities.DailyQuestEntity;
import com.draker.swipetime.database.entities.SeasonalEventEntity;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.database.entities.UserQuestProgressEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Менеджер для управления ежедневными заданиями
 */
public class DailyQuestManager {

    private static final String TAG = "DailyQuestManager";
    
    private final Context context;
    private final AppDatabase database;
    private final DailyQuestDao dailyQuestDao;
    private final UserQuestProgressDao userQuestProgressDao;
    private final GamificationManager gamificationManager;
    
    private static DailyQuestManager instance;
    
    // Интерфейс для обратных вызовов о выполнении заданий
    public interface OnQuestCompleteListener {
        void onQuestCompleted(DailyQuestEntity quest, int experienceGained);
        void onQuestRewardClaimed(DailyQuestEntity quest);
    }
    
    private OnQuestCompleteListener questCompleteListener;
    
    // Приватный конструктор (Singleton)
    private DailyQuestManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
        this.dailyQuestDao = database.dailyQuestDao();
        this.userQuestProgressDao = database.userQuestProgressDao();
        this.gamificationManager = GamificationManager.getInstance(context);
    }
    
    // Получение экземпляра (Singleton)
    public static synchronized DailyQuestManager getInstance(Context context) {
        if (instance == null) {
            instance = new DailyQuestManager(context);
        }
        return instance;
    }
    
    /**
     * Установить слушатель выполнения заданий
     * @param listener слушатель
     */
    public void setQuestCompleteListener(OnQuestCompleteListener listener) {
        this.questCompleteListener = listener;
    }
    
    /**
     * Инициализация ежедневных заданий для пользователя
     * @param userId ID пользователя
     */
    public void initDailyQuests(String userId) {
        try {
            // Проверяем, есть ли активные задания
            List<DailyQuestEntity> activeQuests = getActiveQuestsForUser(userId);
            
            // Если активных заданий нет или их меньше нормы, генерируем новые
            if (activeQuests.size() < 3) {
                // Деактивируем просроченные задания
                dailyQuestDao.deactivateExpired(System.currentTimeMillis());
                
                // Получаем категории, которые пользователь активно использует
                List<String> userCategories = getUserActiveCategories(userId);
                
                // Генерируем новые задания (до 3-х штук)
                int questsToGenerate = 3 - activeQuests.size();
                List<DailyQuestEntity> newQuests = generateDailyQuests(questsToGenerate, userCategories);
                
                // Сохраняем новые задания в БД
                dailyQuestDao.insertAll(newQuests);
                
                // Создаем записи прогресса для пользователя
                for (DailyQuestEntity quest : newQuests) {
                    UserQuestProgressEntity progress = new UserQuestProgressEntity(userId, quest.getId());
                    userQuestProgressDao.insert(progress);
                }
                
                Log.d(TAG, "Сгенерировано новых ежедневных заданий: " + newQuests.size());
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при инициализации ежедневных заданий: " + e.getMessage());
        }
    }
    
    /**
     * Получает активные задания для пользователя
     * @param userId ID пользователя
     * @return список активных заданий
     */
    public List<DailyQuestEntity> getActiveQuestsForUser(String userId) {
        List<DailyQuestEntity> result = new ArrayList<>();
        try {
            // Получаем активные и не истекшие задания
            List<DailyQuestEntity> activeQuests = dailyQuestDao.getNonExpired(System.currentTimeMillis());
            
            // Для каждого задания проверяем прогресс пользователя
            for (DailyQuestEntity quest : activeQuests) {
                UserQuestProgressEntity progress = userQuestProgressDao.getByIds(userId, quest.getId());
                
                // Если у пользователя нет записи прогресса для этого задания, создаем её
                if (progress == null) {
                    progress = new UserQuestProgressEntity(userId, quest.getId());
                    userQuestProgressDao.insert(progress);
                }
                
                // Добавляем задание в результат, только если оно не выполнено или награда не получена
                if (!progress.isCompleted() || !progress.isRewardClaimed()) {
                    result.add(quest);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении активных заданий: " + e.getMessage());
        }
        return result;
    }
    
    /**
     * Получает выполненные, но не полученные награды задания для пользователя
     * @param userId ID пользователя
     * @return список заданий с неполученными наградами
     */
    public List<DailyQuestEntity> getCompletedUnclaimedQuestsForUser(String userId) {
        List<DailyQuestEntity> result = new ArrayList<>();
        try {
            // Получаем прогресс для выполненных, но не полученных заданий
            List<UserQuestProgressEntity> progressList = userQuestProgressDao.getCompletedUnclaimedByUserId(userId);
            
            // Для каждой записи прогресса получаем информацию о задании
            for (UserQuestProgressEntity progress : progressList) {
                DailyQuestEntity quest = dailyQuestDao.getById(progress.getQuestId());
                if (quest != null) {
                    result.add(quest);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении выполненных заданий с неполученными наградами: " + e.getMessage());
        }
        return result;
    }
    
    /**
     * Обновляет прогресс пользователя для заданий, связанных с указанным действием
     * @param userId ID пользователя
     * @param action тип действия (swipe, rate, review, complete)
     * @param count количество для добавления к прогрессу
     * @param category категория контента (может быть null)
     */
    public void updateQuestProgress(String userId, String action, int count, String category) {
        try {
            // Получаем все активные задания по указанному действию
            List<DailyQuestEntity> quests = dailyQuestDao.getByRequiredAction(action);
            
            for (DailyQuestEntity quest : quests) {
                // Проверяем, что задание активно и не истекло
                if (quest.isActive() && !quest.isExpired()) {
                    // Если задание требует определенную категорию, проверяем её
                    if (quest.getRequiredCategory() == null || quest.getRequiredCategory().equals(category)) {
                        // Получаем текущий прогресс пользователя
                        UserQuestProgressEntity progress = userQuestProgressDao.getByIds(userId, quest.getId());
                        
                        // Если прогресс не найден, создаем новую запись
                        if (progress == null) {
                            progress = new UserQuestProgressEntity(userId, quest.getId());
                            userQuestProgressDao.insert(progress);
                        }
                        
                        // Если задание уже выполнено, пропускаем
                        if (progress.isCompleted()) {
                            continue;
                        }
                        
                        // Обновляем прогресс
                        boolean questCompleted = progress.updateProgress(count, quest.getRequiredCount());
                        userQuestProgressDao.update(progress);
                        
                        // Если задание было выполнено этим обновлением
                        if (questCompleted) {
                            // Начисляем опыт за выполнение задания
                            UserEntity user = database.userDao().getById(userId);
                            if (user != null) {
                                int experienceReward = quest.getExperienceReward();
                                boolean levelUp = user.addExperience(experienceReward);
                                database.userDao().update(user);
                                
                                // Логируем выполнение задания
                                Log.d(TAG, "Задание выполнено: " + quest.getTitle() + ", награда: " + experienceReward + " XP");
                                
                                // Уведомляем слушателя
                                if (questCompleteListener != null) {
                                    questCompleteListener.onQuestCompleted(quest, experienceReward);
                                }
                                
                                // Если произошло повышение уровня, обрабатываем его
                                if (levelUp && gamificationManager != null) {
                                    GamificationManager.OnAchievementListener listener = 
                                        gamificationManager.getAchievementListener();
                                    if (listener != null) {
                                        listener.onLevelUp(user.getLevel(), experienceReward);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обновлении прогресса заданий: " + e.getMessage());
        }
    }
    
    /**
     * Получить прогресс пользователя для конкретного задания
     * @param userId ID пользователя
     * @param questId ID задания
     * @return прогресс (0-100%)
     */
    public int getQuestProgress(String userId, String questId) {
        try {
            DailyQuestEntity quest = dailyQuestDao.getById(questId);
            UserQuestProgressEntity progress = userQuestProgressDao.getByIds(userId, questId);
            
            if (quest != null && progress != null) {
                if (progress.isCompleted()) {
                    return 100;
                } else {
                    int requiredCount = quest.getRequiredCount();
                    int currentProgress = progress.getCurrentProgress();
                    return (requiredCount > 0) ? Math.min(100, (currentProgress * 100) / requiredCount) : 0;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении прогресса задания: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Отметить награду за задание как полученную
     * @param userId ID пользователя
     * @param questId ID задания
     * @return true если награда была успешно отмечена
     */
    public boolean claimQuestReward(String userId, String questId) {
        try {
            UserQuestProgressEntity progress = userQuestProgressDao.getByIds(userId, questId);
            DailyQuestEntity quest = dailyQuestDao.getById(questId);
            
            if (progress != null && quest != null && progress.isCompleted() && !progress.isRewardClaimed()) {
                // Отмечаем награду как полученную
                progress.setRewardClaimed(true);
                userQuestProgressDao.update(progress);
                
                // Если есть предметная награда, добавляем предмет пользователю
                if (quest.getItemRewardId() != null && !quest.getItemRewardId().isEmpty()) {
                    CollectibleItemManager itemManager = CollectibleItemManager.getInstance(context);
                    if (itemManager != null) {
                        itemManager.addItemToUser(userId, quest.getItemRewardId(), "quest");
                    }
                }
                
                // Уведомляем слушателя
                if (questCompleteListener != null) {
                    questCompleteListener.onQuestRewardClaimed(quest);
                }
                
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении награды за задание: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Генерирует список категорий, которые пользователь активно использует
     * (В реальном приложении эта информация должна браться из истории пользователя)
     * @param userId ID пользователя
     * @return список активных категорий
     */
    private List<String> getUserActiveCategories(String userId) {
        List<String> categories = new ArrayList<>();
        
        // TODO: Реализовать получение категорий, которые пользователь активно использует
        // Пока просто добавляем заглушки
        categories.add("movies");
        categories.add("tv_shows");
        categories.add("books");
        categories.add("games");
        categories.add("anime");
        
        return categories;
    }
    
    /**
     * Генерирует новые ежедневные задания
     * @param count количество заданий для генерации
     * @param userCategories категории, которые пользователь активно использует
     * @return список новых заданий
     */
    private List<DailyQuestEntity> generateDailyQuests(int count, List<String> userCategories) {
        List<DailyQuestEntity> quests = new ArrayList<>();
        
        // Получаем текущее время
        long currentTime = System.currentTimeMillis();
        
        // Устанавливаем срок действия заданий (24 часа)
        long expirationTime = currentTime + TimeUnit.HOURS.toMillis(24);
        
        // Получаем текущие сезонные события
        List<SeasonalEventEntity> currentEvents = getActiveSeasonalEvents();
        
        // Типы действий для заданий
        String[] actionTypes = {
            GamificationManager.ACTION_SWIPE,
            GamificationManager.ACTION_RATE,
            GamificationManager.ACTION_REVIEW,
            GamificationManager.ACTION_COMPLETE
        };
        
        // Генерируем указанное количество заданий
        for (int i = 0; i < count; i++) {
            // Рандомизируем уровень сложности (от 1 до 3)
            int difficulty = new Random().nextInt(3) + 1;
            
            // Выбираем случайный тип действия
            String action = actionTypes[new Random().nextInt(actionTypes.length)];
            
            // Устанавливаем количество в зависимости от сложности и типа действия
            int requiredCount;
            switch (action) {
                case GamificationManager.ACTION_SWIPE:
                    requiredCount = difficulty * 10; // 10/20/30 свайпов
                    break;
                case GamificationManager.ACTION_RATE:
                    requiredCount = difficulty; // 1/2/3 оценки
                    break;
                case GamificationManager.ACTION_REVIEW:
                    requiredCount = 1; // Всегда 1 рецензия
                    break;
                case GamificationManager.ACTION_COMPLETE:
                    requiredCount = difficulty; // 1/2/3 завершения
                    break;
                default:
                    requiredCount = difficulty * 5;
            }
            
            // Выбираем категорию (может быть null)
            String category = null;
            if (!userCategories.isEmpty() && new Random().nextDouble() < 0.7) { // 70% шанс привязки к категории
                category = userCategories.get(new Random().nextInt(userCategories.size()));
            }
            
            // Генерируем заголовок задания
            String title = generateQuestTitle(action, requiredCount, category);
            
            // Генерируем описание задания
            String description = generateQuestDescription(action, requiredCount, category);
            
            // Определяем иконку задания
            String iconName = "ic_quest_" + action.toLowerCase();
            if (category != null) {
                iconName += "_" + category.toLowerCase();
            }
            
            // Определяем награду в XP в зависимости от сложности
            int xpReward = difficulty * 50; // 50/100/150 XP
            
            // Определяем предметную награду (если есть активное событие)
            String itemRewardId = null;
            if (!currentEvents.isEmpty() && new Random().nextDouble() < 0.3) { // 30% шанс на предметную награду
                // TODO: Подставить реальный ID предмета из активного события
                // Пока используем заглушку
                itemRewardId = "event_item_" + currentEvents.get(0).getId() + "_" + new Random().nextInt(100);
            }
            
            // Создаем новое задание
            DailyQuestEntity quest = new DailyQuestEntity(
                UUID.randomUUID().toString(),
                title,
                description,
                iconName,
                xpReward,
                itemRewardId,
                action,
                requiredCount,
                category,
                currentTime,
                expirationTime,
                true,
                difficulty
            );
            
            quests.add(quest);
        }
        
        return quests;
    }
    
    /**
     * Генерирует заголовок задания
     * @param action тип действия
     * @param count требуемое количество
     * @param category категория (может быть null)
     * @return заголовок задания
     */
    private String generateQuestTitle(String action, int count, String category) {
        String title;
        String categoryStr = "";
        
        if (category != null) {
            switch (category) {
                case "movies":
                    categoryStr = "фильм";
                    break;
                case "tv_shows":
                    categoryStr = "сериал";
                    break;
                case "books":
                    categoryStr = "книг";
                    break;
                case "games":
                    categoryStr = "игр";
                    break;
                case "anime":
                    categoryStr = "аниме";
                    break;
                default:
                    categoryStr = category;
            }
        }
        
        switch (action) {
            case GamificationManager.ACTION_SWIPE:
                if (category != null) {
                    title = "Оцените " + count + " " + getCategoryNameWithCount(categoryStr, count);
                } else {
                    title = "Сделайте " + count + " свайпов";
                }
                break;
            case GamificationManager.ACTION_RATE:
                if (category != null) {
                    title = "Поставьте оценку " + count + " " + getCategoryNameWithCount(categoryStr, count);
                } else {
                    title = "Поставьте " + count + " оцен" + (count == 1 ? "ку" : "ки");
                }
                break;
            case GamificationManager.ACTION_REVIEW:
                if (category != null) {
                    title = "Напишите рецензию на " + categoryStr;
                } else {
                    title = "Напишите рецензию";
                }
                break;
            case GamificationManager.ACTION_COMPLETE:
                if (category != null) {
                    title = "Отметьте как просмотренные " + count + " " + getCategoryNameWithCount(categoryStr, count);
                } else {
                    title = "Отметьте " + count + " элемент" + (count == 1 ? "" : "ов") + " как просмотренные";
                }
                break;
            default:
                title = "Выполните задание дня";
        }
        
        return title;
    }
    
    /**
     * Генерирует описание задания
     * @param action тип действия
     * @param count требуемое количество
     * @param category категория (может быть null)
     * @return описание задания
     */
    private String generateQuestDescription(String action, int count, String category) {
        String description;
        String categoryStr = category != null ? " в категории " + category : "";
        
        switch (action) {
            case GamificationManager.ACTION_SWIPE:
                description = "Прокрутите " + count + " карточек" + categoryStr + 
                              ", чтобы открыть для себя новый контент и заработать опыт.";
                break;
            case GamificationManager.ACTION_RATE:
                description = "Поставьте оценки " + count + " элементам" + categoryStr + 
                              ". Это помогает улучшить рекомендации!";
                break;
            case GamificationManager.ACTION_REVIEW:
                description = "Напишите подробную рецензию на контент" + categoryStr + 
                              ". Поделитесь своим мнением с сообществом!";
                break;
            case GamificationManager.ACTION_COMPLETE:
                description = "Отметьте " + count + " элемент" + (count == 1 ? "" : "ов") + 
                              " как просмотренные" + categoryStr + 
                              ". Это поможет отслеживать ваш прогресс.";
                break;
            default:
                description = "Выполните это задание, чтобы получить награду и опыт.";
        }
        
        return description;
    }
    
    /**
     * Возвращает название категории с учетом множественного числа
     * @param category название категории
     * @param count количество
     * @return строка с правильным окончанием
     */
    private String getCategoryNameWithCount(String category, int count) {
        if (category.equals("фильм")) {
            if (count == 1) {
                return "фильм";
            } else if (count >= 2 && count <= 4) {
                return "фильма";
            } else {
                return "фильмов";
            }
        } else if (category.equals("сериал")) {
            if (count == 1) {
                return "сериал";
            } else if (count >= 2 && count <= 4) {
                return "сериала";
            } else {
                return "сериалов";
            }
        } else if (category.equals("книг")) {
            if (count == 1) {
                return "книгу";
            } else if (count >= 2 && count <= 4) {
                return "книги";
            } else {
                return "книг";
            }
        } else if (category.equals("игр")) {
            if (count == 1) {
                return "игру";
            } else if (count >= 2 && count <= 4) {
                return "игры";
            } else {
                return "игр";
            }
        } else if (category.equals("аниме")) {
            return "аниме";
        }
        
        return category;
    }
    
    /**
     * Получает список активных сезонных событий
     * @return список активных событий
     */
    private List<SeasonalEventEntity> getActiveSeasonalEvents() {
        try {
            SeasonalEventManager eventManager = SeasonalEventManager.getInstance(context);
            return eventManager.getCurrentlyActiveEvents();
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении активных сезонных событий: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Обновляет состояние ежедневных заданий (деактивирует истекшие)
     */
    public void refreshQuests() {
        try {
            // Деактивируем истекшие задания
            dailyQuestDao.deactivateExpired(System.currentTimeMillis());
            Log.d(TAG, "Обновлено состояние ежедневных заданий");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обновлении состояния ежедневных заданий: " + e.getMessage());
        }
    }
}
