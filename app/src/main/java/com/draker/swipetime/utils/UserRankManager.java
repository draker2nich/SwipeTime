package com.draker.swipetime.utils;

import android.content.Context;
import android.util.Log;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.UserDao;
import com.draker.swipetime.database.dao.UserRankDao;
import com.draker.swipetime.database.dao.UserRankProgressDao;
import com.draker.swipetime.database.dao.UserStatsDao;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.database.entities.UserRankEntity;
import com.draker.swipetime.database.entities.UserRankProgressEntity;
import com.draker.swipetime.database.entities.UserStatsEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Менеджер для управления системой рангов пользователей
 */
public class UserRankManager {

    private static final String TAG = "UserRankManager";
    
    private final Context context;
    private final AppDatabase database;
    private final UserRankDao userRankDao;
    private final UserRankProgressDao userRankProgressDao;
    private final UserDao userDao;
    private final UserStatsDao userStatsDao;
    private final GamificationManager gamificationManager;
    
    private static UserRankManager instance;
    
    // Интерфейс для обратных вызовов о изменениях рангов
    public interface OnRankChangeListener {
        void onRankUnlocked(UserRankEntity rank, int experienceGained);
        void onRankActivated(UserRankEntity rank);
    }
    
    private OnRankChangeListener rankChangeListener;
    
    // Приватный конструктор (Singleton)
    private UserRankManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
        this.userRankDao = database.userRankDao();
        this.userRankProgressDao = database.userRankProgressDao();
        this.userDao = database.userDao();
        this.userStatsDao = database.userStatsDao();
        this.gamificationManager = GamificationManager.getInstance(context);
        
        // Проверяем, есть ли ранги в системе
        if (userRankDao.getCount() == 0) {
            initializeDefaultRanks();
        }
    }
    
    // Получение экземпляра (Singleton)
    public static synchronized UserRankManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserRankManager(context);
        }
        return instance;
    }
    
    /**
     * Установить слушатель изменений рангов
     * @param listener слушатель
     */
    public void setRankChangeListener(OnRankChangeListener listener) {
        this.rankChangeListener = listener;
    }
    
    /**
     * Получает список всех рангов в системе
     * @return список рангов отсортированный по порядку
     */
    public List<UserRankEntity> getAllRanks() {
        return userRankDao.getAllOrderedByIndex();
    }
    
    /**
     * Получает список рангов определенной категории
     * @param category категория рангов (general, movie_buff, bookworm, gamer, etc.)
     * @return список рангов указанной категории
     */
    public List<UserRankEntity> getRanksByCategory(String category) {
        return userRankDao.getByCategory(category);
    }
    
    /**
     * Получает активный ранг пользователя
     * @param userId ID пользователя
     * @return активный ранг пользователя или null, если у пользователя нет активного ранга
     */
    public UserRankEntity getUserActiveRank(String userId) {
        List<UserRankProgressEntity> activeProgressList = userRankProgressDao.getActiveByUserId(userId);
        UserRankProgressEntity activeProgress = activeProgressList.isEmpty() ? null : activeProgressList.get(0);
        if (activeProgress != null) {
            return userRankDao.getById(activeProgress.getRankId());
        }
        return null;
    }
    
    /**
     * Получает список разблокированных рангов пользователя
     * @param userId ID пользователя
     * @return список разблокированных рангов
     */
    public List<UserRankEntity> getUserUnlockedRanks(String userId) {
        List<UserRankProgressEntity> unlockedProgress = userRankProgressDao.getUnlockedByUserId(userId);
        List<UserRankEntity> result = new ArrayList<>();
        
        for (UserRankProgressEntity progress : unlockedProgress) {
            UserRankEntity rank = userRankDao.getById(progress.getRankId());
            if (rank != null) {
                result.add(rank);
            }
        }
        
        return result;
    }
    
    /**
     * Получает информацию о прогрессе пользователя по всем рангам
     * @param userId ID пользователя
     * @return список информации о прогрессе рангов
     */
    public List<UserRankInfo> getUserRankProgress(String userId) {
        List<UserRankInfo> result = new ArrayList<>();
        List<UserRankEntity> allRanks = getAllRanks();
        
        for (UserRankEntity rank : allRanks) {
            // Получаем прогресс пользователя для этого ранга
            UserRankProgressEntity progress = userRankProgressDao.getByIds(userId, rank.getId());
            
            // Если прогресса нет, создаем новую запись
            if (progress == null) {
                progress = new UserRankProgressEntity(userId, rank.getId());
                userRankProgressDao.insert(progress);
            }
            
            // Обновляем прогресс на основе текущих статистик пользователя
            updateRankProgress(userId, rank.getId());
            
            // Перезагружаем обновленный прогресс
            progress = userRankProgressDao.getByIds(userId, rank.getId());
            
            result.add(new UserRankInfo(rank, progress));
        }
        
        return result;
    }
    
    /**
     * Обновляет прогресс пользователя для всех рангов
     * @param userId ID пользователя
     */
    public void updateUserRankProgress(String userId) {
        try {
            // Получаем статистику пользователя
            UserEntity user = userDao.getById(userId);
            UserStatsEntity stats = userStatsDao.getByUserId(userId);
            
            if (user == null || stats == null) {
                Log.e(TAG, "Пользователь или статистика не найдены для ID: " + userId);
                return;
            }
            
            // Получаем количество достижений пользователя
            int achievementsCount = gamificationManager.getCompletedAchievementsCount(userId);
            
            // Получаем количество активных категорий (пока используем заглушку)
            int categoriesCount = getCategoriesCountForUser(userId);
            
            // Обновляем прогресс для всех рангов
            List<UserRankEntity> allRanks = getAllRanks();
            for (UserRankEntity rank : allRanks) {
                updateRankProgress(userId, rank.getId(), user.getLevel(), achievementsCount, categoriesCount);
            }
            
            // Проверяем, нужно ли активировать новый ранг
            checkForRankPromotion(userId);
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обновлении прогресса рангов: " + e.getMessage());
        }
    }
    
    /**
     * Обновляет прогресс для конкретного ранга
     * @param userId ID пользователя
     * @param rankId ID ранга
     */
    private void updateRankProgress(String userId, String rankId) {
        UserEntity user = userDao.getById(userId);
        UserStatsEntity stats = userStatsDao.getByUserId(userId);
        
        if (user != null && stats != null) {
            int achievementsCount = gamificationManager.getCompletedAchievementsCount(userId);
            int categoriesCount = getCategoriesCountForUser(userId);
            
            updateRankProgress(userId, rankId, user.getLevel(), achievementsCount, categoriesCount);
        }
    }
    
    /**
     * Обновляет прогресс для конкретного ранга с указанными параметрами
     * @param userId ID пользователя
     * @param rankId ID ранга
     * @param userLevel уровень пользователя
     * @param achievementsCount количество достижений
     * @param categoriesCount количество категорий
     */
    private void updateRankProgress(String userId, String rankId, int userLevel, int achievementsCount, int categoriesCount) {
        try {
            UserRankEntity rank = userRankDao.getById(rankId);
            if (rank == null) {
                return;
            }
            
            UserRankProgressEntity progress = userRankProgressDao.getByIds(userId, rankId);
            if (progress == null) {
                progress = new UserRankProgressEntity(userId, rankId);
                userRankProgressDao.insert(progress);
            }
            
            // Обновляем прогресс
            boolean wasUnlocked = progress.updateProgress(
                userLevel, achievementsCount, categoriesCount,
                rank.getRequiredLevel(), rank.getRequiredAchievementsCount(), rank.getRequiredCategoriesCount()
            );
            
            userRankProgressDao.update(progress);
            
            // Если ранг был разблокирован этим обновлением
            if (wasUnlocked) {
                // Начисляем бонусный опыт за разблокировку ранга
                UserEntity user = userDao.getById(userId);
                if (user != null) {
                    int bonusXp = 100; // Базовый бонус за разблокировку ранга
                    boolean levelUp = user.addExperience(bonusXp);
                    userDao.update(user);
                    
                    // Логируем разблокировку ранга
                    Log.d(TAG, "Пользователь " + userId + " разблокировал ранг: " + rank.getName());
                    
                    // Уведомляем о разблокировке ранга
                    if (rankChangeListener != null) {
                        rankChangeListener.onRankUnlocked(rank, bonusXp);
                    }
                    
                    // Если произошло повышение уровня, уведомляем об этом
                    if (levelUp && gamificationManager != null) {
                        GamificationManager.OnAchievementListener listener = gamificationManager.getAchievementListener();
                        if (listener != null) {
                            listener.onLevelUp(user.getLevel(), bonusXp);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обновлении прогресса ранга: " + e.getMessage());
        }
    }
    
    /**
     * Проверяет, нужно ли активировать новый ранг для пользователя
     * @param userId ID пользователя
     */
    private void checkForRankPromotion(String userId) {
        try {
            // Получаем текущий активный ранг
            UserRankEntity currentActiveRank = getUserActiveRank(userId);
            
            // Получаем все разблокированные ранги пользователя
            List<UserRankEntity> unlockedRanks = getUserUnlockedRanks(userId);
            
            // Находим ранг с наивысшим порядковым номером среди разблокированных
            UserRankEntity bestRank = null;
            int highestOrder = -1;
            
            for (UserRankEntity rank : unlockedRanks) {
                if (rank.getOrderIndex() > highestOrder) {
                    highestOrder = rank.getOrderIndex();
                    bestRank = rank;
                }
            }
            
            // Если найден лучший ранг и он отличается от текущего активного
            if (bestRank != null && (currentActiveRank == null || !bestRank.getId().equals(currentActiveRank.getId()))) {
                activateRank(userId, bestRank.getId());
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при проверке активации ранга: " + e.getMessage());
        }
    }
    
    /**
     * Активирует указанный ранг для пользователя
     * @param userId ID пользователя
     * @param rankId ID ранга для активации
     * @return true если ранг был успешно активирован
     */
    public boolean activateRank(String userId, String rankId) {
        try {
            // Проверяем, разблокирован ли ранг
            UserRankProgressEntity progress = userRankProgressDao.getByIds(userId, rankId);
            if (progress == null || !progress.isUnlocked()) {
                Log.e(TAG, "Ранг с ID " + rankId + " не разблокирован для пользователя " + userId);
                return false;
            }
            
            // Деактивируем все текущие ранги пользователя
            userRankProgressDao.deactivateAllForUser(userId);
            
            // Активируем указанный ранг
            progress.setActive(true);
            userRankProgressDao.update(progress);
            
            // Получаем информацию о ранге
            UserRankEntity rank = userRankDao.getById(rankId);
            if (rank != null) {
                Log.d(TAG, "Пользователь " + userId + " активировал ранг: " + rank.getName());
                
                // Уведомляем об активации ранга
                if (rankChangeListener != null) {
                    rankChangeListener.onRankActivated(rank);
                }
            }
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при активации ранга: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Получает текущий множитель опыта для пользователя на основе активного ранга
     * @param userId ID пользователя
     * @return множитель опыта (1.0 по умолчанию)
     */
    public float getUserXpMultiplier(String userId) {
        try {
            UserRankEntity activeRank = getUserActiveRank(userId);
            if (activeRank != null) {
                return activeRank.getBonusXpMultiplier();
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении множителя опыта: " + e.getMessage());
        }
        return 1.0f;
    }
    
    /**
     * Получает количество активных категорий для пользователя
     * TODO: Реализовать подсчет реальных категорий на основе истории пользователя
     * @param userId ID пользователя
     * @return количество активных категорий
     */
    private int getCategoriesCountForUser(String userId) {
        // Пока используем заглушку
        // В реальном приложении здесь должен быть запрос к базе данных
        // для подсчета уникальных категорий, в которых пользователь был активен
        return 3; // Заглушка: предполагаем, что пользователь активен в 3 категориях
    }
    
    /**
     * Инициализирует набор стандартных рангов
     */
    private void initializeDefaultRanks() {
        try {
            List<UserRankEntity> ranks = new ArrayList<>();
            
            // Общие ранги
            ranks.add(new UserRankEntity(
                UUID.randomUUID().toString(),
                "Новичок",
                "Начинающий пользователь SwipeTime",
                "ic_rank_beginner",
                "#9E9E9E", // Серый
                1, // требуемый уровень
                0, // требуемые достижения
                1, // требуемые категории
                1.0f, // множитель опыта
                "general",
                1 // порядковый номер
            ));
            
            ranks.add(new UserRankEntity(
                UUID.randomUUID().toString(),
                "Исследователь",
                "Активный исследователь контента",
                "ic_rank_explorer",
                "#4CAF50", // Зеленый
                5, // требуемый уровень
                5, // требуемые достижения
                2, // требуемые категории
                1.1f, // множитель опыта
                "general",
                2 // порядковый номер
            ));
            
            ranks.add(new UserRankEntity(
                UUID.randomUUID().toString(),
                "Знаток",
                "Опытный пользователь с широкими интересами",
                "ic_rank_expert",
                "#2196F3", // Синий
                10, // требуемый уровень
                10, // требуемые достижения
                3, // требуемые категории
                1.2f, // множитель опыта
                "general",
                3 // порядковый номер
            ));
            
            ranks.add(new UserRankEntity(
                UUID.randomUUID().toString(),
                "Мастер",
                "Мастер развлечений и культуры",
                "ic_rank_master",
                "#FF9800", // Оранжевый
                20, // требуемый уровень
                20, // требуемые достижения
                4, // требуемые категории
                1.3f, // множитель опыта
                "general",
                4 // порядковый номер
            ));
            
            ranks.add(new UserRankEntity(
                UUID.randomUUID().toString(),
                "Гуру",
                "Гуру контента со всесторонними знаниями",
                "ic_rank_guru",
                "#9C27B0", // Пурпурный
                35, // требуемый уровень
                35, // требуемые достижения
                5, // требуемые категории
                1.5f, // множитель опыта
                "general",
                5 // порядковый номер
            ));
            
            ranks.add(new UserRankEntity(
                UUID.randomUUID().toString(),
                "Легенда",
                "Легендарный пользователь SwipeTime",
                "ic_rank_legend",
                "#F44336", // Красный
                50, // требуемый уровень
                50, // требуемые достижения
                5, // требуемые категории
                2.0f, // множитель опыта
                "general",
                6 // порядковый номер
            ));
            
            // Специализированные ранги для киноманов
            ranks.add(new UserRankEntity(
                UUID.randomUUID().toString(),
                "Киноман",
                "Любитель кинематографа",
                "ic_rank_movie_buff",
                "#FF5722", // Темно-оранжевый
                8, // требуемый уровень
                8, // требуемые достижения (с фокусом на фильмы)
                1, // требуемые категории (только фильмы)
                1.2f, // множитель опыта
                "movie_buff",
                10 // порядковый номер
            ));
            
            ranks.add(new UserRankEntity(
                UUID.randomUUID().toString(),
                "Кинокритик",
                "Профессиональный знаток кино",
                "ic_rank_movie_critic",
                "#795548", // Коричневый
                15, // требуемый уровень
                15, // требуемые достижения
                1, // требуемые категории
                1.4f, // множитель опыта
                "movie_buff",
                11 // порядковый номер
            ));
            
            // Специализированные ранги для книголюбов
            ranks.add(new UserRankEntity(
                UUID.randomUUID().toString(),
                "Книголюб",
                "Страстный читатель",
                "ic_rank_bookworm",
                "#8BC34A", // Светло-зеленый
                8, // требуемый уровень
                8, // требуемые достижения (с фокусом на книги)
                1, // требуемые категории (только книги)
                1.2f, // множитель опыта
                "bookworm",
                20 // порядковый номер
            ));
            
            ranks.add(new UserRankEntity(
                UUID.randomUUID().toString(),
                "Литературный критик",
                "Эксперт в области литературы",
                "ic_rank_literary_critic",
                "#607D8B", // Сине-серый
                15, // требуемый уровень
                15, // требуемые достижения
                1, // требуемые категории
                1.4f, // множитель опыта
                "bookworm",
                21 // порядковый номер
            ));
            
            // Специализированные ранги для геймеров
            ranks.add(new UserRankEntity(
                UUID.randomUUID().toString(),
                "Геймер",
                "Любитель видеоигр",
                "ic_rank_gamer",
                "#E91E63", // Розовый
                8, // требуемый уровень
                8, // требуемые достижения (с фокусом на игры)
                1, // требуемые категории (только игры)
                1.2f, // множитель опыта
                "gamer",
                30 // порядковый номер
            ));
            
            ranks.add(new UserRankEntity(
                UUID.randomUUID().toString(),
                "Про-геймер",
                "Профессиональный игрок",
                "ic_rank_pro_gamer",
                "#673AB7", // Темно-пурпурный
                15, // требуемый уровень
                15, // требуемые достижения
                1, // требуемые категории
                1.4f, // множитель опыта
                "gamer",
                31 // порядковый номер
            ));
            
            // Сохраняем ранги в базу данных
            userRankDao.insertAll(ranks);
            Log.d(TAG, "Инициализировано " + ranks.size() + " стандартных рангов");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при инициализации рангов: " + e.getMessage());
        }
    }
    
    /**
     * Класс для хранения информации о ранге пользователя
     */
    public static class UserRankInfo {
        private final UserRankEntity rank;
        private final UserRankProgressEntity progress;
        
        public UserRankInfo(UserRankEntity rank, UserRankProgressEntity progress) {
            this.rank = rank;
            this.progress = progress;
        }
        
        public UserRankEntity getRank() {
            return rank;
        }
        
        public UserRankProgressEntity getProgress() {
            return progress;
        }
        
        public boolean isUnlocked() {
            return progress.isUnlocked();
        }
        
        public boolean isActive() {
            return progress.isActive();
        }
        
        public long getUnlockDate() {
            return progress.getUnlockDate();
        }
        
        public int getLevelProgress() {
            return progress.getLevelProgress();
        }
        
        public int getAchievementsProgress() {
            return progress.getAchievementsProgress();
        }
        
        public int getCategoriesProgress() {
            return progress.getCategoriesProgress();
        }
        
        public int getOverallProgress() {
            return progress.calculateOverallProgress();
        }
        
        /**
         * Проверяет, готов ли ранг к разблокировке
         * @return true если все требования выполнены
         */
        public boolean isReadyToUnlock() {
            return getLevelProgress() >= 100 && 
                   getAchievementsProgress() >= 100 && 
                   getCategoriesProgress() >= 100 &&
                   !isUnlocked();
        }
    }
    
    /**
     * Создает новый ранг
     * @param name название ранга
     * @param description описание ранга
     * @param iconName имя иконки
     * @param badgeColor цвет значка
     * @param requiredLevel требуемый уровень
     * @param requiredAchievements требуемые достижения
     * @param requiredCategories требуемые категории
     * @param bonusXpMultiplier множитель бонусного опыта
     * @param category категория ранга
     * @param orderIndex порядковый номер
     * @return ID созданного ранга или null в случае ошибки
     */
    public String createRank(String name, String description, String iconName, String badgeColor,
                           int requiredLevel, int requiredAchievements, int requiredCategories,
                           float bonusXpMultiplier, String category, int orderIndex) {
        try {
            String rankId = UUID.randomUUID().toString();
            
            UserRankEntity rank = new UserRankEntity(
                rankId,
                name,
                description,
                iconName,
                badgeColor,
                requiredLevel,
                requiredAchievements,
                requiredCategories,
                bonusXpMultiplier,
                category,
                orderIndex
            );
            
            userRankDao.insert(rank);
            Log.d(TAG, "Создан новый ранг: " + name);
            
            return rankId;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при создании ранга: " + e.getMessage());
            return null;
        }
    }
}
