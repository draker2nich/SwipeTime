package com.draker.swipetime.utils;

import android.content.Context;
import android.util.Log;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.CollectibleItemDao;
import com.draker.swipetime.database.dao.UserItemDao;
import com.draker.swipetime.database.entities.CollectibleItemEntity;
import com.draker.swipetime.database.entities.SeasonalEventEntity;
import com.draker.swipetime.database.entities.UserItemEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Менеджер для управления коллекционными виртуальными предметами
 */
public class CollectibleItemManager {

    private static final String TAG = "CollectibleItemManager";
    
    private final Context context;
    private final AppDatabase database;
    private final CollectibleItemDao collectibleItemDao;
    private final UserItemDao userItemDao;
    
    private static CollectibleItemManager instance;
    
    // Интерфейс для обратных вызовов о получении предметов
    public interface OnItemObtainedListener {
        void onItemObtained(CollectibleItemEntity item, String source);
    }
    
    private OnItemObtainedListener itemObtainedListener;
    
    // Приватный конструктор (Singleton)
    private CollectibleItemManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
        this.collectibleItemDao = database.collectibleItemDao();
        this.userItemDao = database.userItemDao();
        
        // Проверяем, есть ли предметы в базе
        if (collectibleItemDao.getCount() == 0) {
            initializeDefaultItems();
        }
    }
    
    // Получение экземпляра (Singleton)
    public static synchronized CollectibleItemManager getInstance(Context context) {
        if (instance == null) {
            instance = new CollectibleItemManager(context);
        }
        return instance;
    }
    
    /**
     * Установить слушатель получения предметов
     * @param listener слушатель
     */
    public void setItemObtainedListener(OnItemObtainedListener listener) {
        this.itemObtainedListener = listener;
    }
    
    /**
     * Получает список всех доступных предметов
     * @return список предметов
     */
    public List<CollectibleItemEntity> getAllItems() {
        return collectibleItemDao.getAll();
    }
    
    /**
     * Получает список предметов, связанных с определенным событием
     * @param eventId ID события
     * @return список предметов
     */
    public List<CollectibleItemEntity> getItemsByEvent(String eventId) {
        return collectibleItemDao.getByEventId(eventId);
    }
    
    /**
     * Получает список предметов, связанных с определенной категорией
     * @param category категория
     * @return список предметов
     */
    public List<CollectibleItemEntity> getItemsByCategory(String category) {
        return collectibleItemDao.getByCategory(category);
    }
    
    /**
     * Получает список предметов пользователя
     * @param userId ID пользователя
     * @return список предметов пользователя
     */
    public List<UserItemInfo> getUserItems(String userId) {
        List<UserItemInfo> result = new ArrayList<>();
        
        // Получаем записи о предметах пользователя
        List<UserItemEntity> userItems = userItemDao.getByUserId(userId);
        
        // Для каждой записи получаем информацию о предмете
        for (UserItemEntity userItem : userItems) {
            CollectibleItemEntity item = collectibleItemDao.getById(userItem.getItemId());
            if (item != null) {
                result.add(new UserItemInfo(item, userItem));
            }
        }
        
        return result;
    }
    
    /**
     * Проверяет, есть ли у пользователя указанный предмет
     * @param userId ID пользователя
     * @param itemId ID предмета
     * @return true если предмет есть у пользователя
     */
    public boolean hasUserItem(String userId, String itemId) {
        return userItemDao.getByIds(userId, itemId) != null;
    }
    
    /**
     * Добавляет предмет пользователю
     * @param userId ID пользователя
     * @param itemId ID предмета
     * @param source источник получения (quest, achievement, event, purchase, etc.)
     * @return true если предмет был успешно добавлен
     */
    public boolean addItemToUser(String userId, String itemId, String source) {
        try {
            // Проверяем, есть ли уже этот предмет у пользователя
            if (hasUserItem(userId, itemId)) {
                Log.d(TAG, "Предмет уже есть у пользователя: " + itemId);
                return false;
            }
            
            // Проверяем, существует ли предмет
            CollectibleItemEntity item = collectibleItemDao.getById(itemId);
            if (item == null) {
                Log.e(TAG, "Предмет с ID " + itemId + " не найден");
                return false;
            }
            
            // Добавляем предмет пользователю
            UserItemEntity userItem = new UserItemEntity(userId, itemId, System.currentTimeMillis(), source);
            userItemDao.insert(userItem);
            
            // Уведомляем о получении предмета
            if (itemObtainedListener != null) {
                itemObtainedListener.onItemObtained(item, source);
            }
            
            Log.d(TAG, "Предмет " + item.getName() + " добавлен пользователю " + userId);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при добавлении предмета пользователю: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Оснащает предмет (надевает на аватар, устанавливает как рамку профиля и т.д.)
     * @param userId ID пользователя
     * @param itemId ID предмета
     * @param slot слот для оснащения
     * @return true если предмет был успешно оснащен
     */
    public boolean equipItem(String userId, String itemId, String slot) {
        try {
            // Проверяем, есть ли предмет у пользователя
            UserItemEntity userItem = userItemDao.getByIds(userId, itemId);
            if (userItem == null) {
                Log.e(TAG, "Предмет с ID " + itemId + " не найден у пользователя " + userId);
                return false;
            }
            
            // Снимаем все остальные предметы с этого слота
            userItemDao.unequipAllInSlot(userId, slot);
            
            // Оснащаем указанный предмет
            userItem.setEquipped(true);
            userItem.setEquippedSlot(slot);
            userItemDao.update(userItem);
            
            Log.d(TAG, "Предмет " + itemId + " оснащен пользователем " + userId + " в слот " + slot);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при оснащении предмета: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Снимает предмет
     * @param userId ID пользователя
     * @param itemId ID предмета
     * @return true если предмет был успешно снят
     */
    public boolean unequipItem(String userId, String itemId) {
        try {
            // Проверяем, есть ли предмет у пользователя
            UserItemEntity userItem = userItemDao.getByIds(userId, itemId);
            if (userItem == null) {
                Log.e(TAG, "Предмет с ID " + itemId + " не найден у пользователя " + userId);
                return false;
            }
            
            // Снимаем предмет
            userItem.setEquipped(false);
            userItem.setEquippedSlot(null);
            userItemDao.update(userItem);
            
            Log.d(TAG, "Предмет " + itemId + " снят пользователем " + userId);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при снятии предмета: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Получает экипированный предмет для указанного слота
     * @param userId ID пользователя
     * @param slot слот
     * @return информация о предмете или null, если слот пуст
     */
    public UserItemInfo getEquippedItemInSlot(String userId, String slot) {
        try {
            List<UserItemEntity> userItems = userItemDao.getEquippedInSlot(userId, slot);
            UserItemEntity userItem = userItems.isEmpty() ? null : userItems.get(0);
            if (userItem != null) {
                CollectibleItemEntity item = collectibleItemDao.getById(userItem.getItemId());
                if (item != null) {
                    return new UserItemInfo(item, userItem);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении оснащенного предмета: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Инициализирует базовый набор коллекционных предметов
     */
    private void initializeDefaultItems() {
        try {
            List<CollectibleItemEntity> items = new ArrayList<>();
            
            // Добавляем рамки для профиля
            items.add(new CollectibleItemEntity(
                UUID.randomUUID().toString(),
                "Начальная рамка",
                "Простая рамка для вашего профиля. Доступна всем пользователям.",
                "ic_frame_basic",
                1, // обычная редкость
                null, // не привязана к категории
                null, // не привязана к событию
                false, // не ограниченная
                0, // всегда доступна
                0, // всегда доступна
                "starter", // получается при регистрации
                "profile_frame" // эффект использования - рамка профиля
            ));
            
            items.add(new CollectibleItemEntity(
                UUID.randomUUID().toString(),
                "Золотая рамка",
                "Золотая рамка для вашего профиля. Получите 10 достижений, чтобы разблокировать её.",
                "ic_frame_gold",
                3, // эпическая редкость
                null, // не привязана к категории
                null, // не привязана к событию
                false, // не ограниченная
                0, // всегда доступна
                0, // всегда доступна
                "achievement", // получается за достижения
                "profile_frame" // эффект использования - рамка профиля
            ));
            
            // Добавляем значки для аватара
            items.add(new CollectibleItemEntity(
                UUID.randomUUID().toString(),
                "Значок киномана",
                "Показывает, что вы истинный любитель кино. Получите 5 достижений в категории фильмов.",
                "ic_badge_movie",
                2, // редкая редкость
                "movies", // привязана к категории фильмов
                null, // не привязана к событию
                false, // не ограниченная
                0, // всегда доступна
                0, // всегда доступна
                "achievement", // получается за достижения
                "avatar_badge" // эффект использования - значок на аватаре
            ));
            
            items.add(new CollectibleItemEntity(
                UUID.randomUUID().toString(),
                "Значок книголюба",
                "Показывает вашу любовь к литературе. Получите 5 достижений в категории книг.",
                "ic_badge_book",
                2, // редкая редкость
                "books", // привязана к категории книг
                null, // не привязана к событию
                false, // не ограниченная
                0, // всегда доступна
                0, // всегда доступна
                "achievement", // получается за достижения
                "avatar_badge" // эффект использования - значок на аватаре
            ));
            
            items.add(new CollectibleItemEntity(
                UUID.randomUUID().toString(),
                "Значок геймера",
                "Для настоящих любителей игр. Получите 5 достижений в категории игр.",
                "ic_badge_game",
                2, // редкая редкость
                "games", // привязана к категории игр
                null, // не привязана к событию
                false, // не ограниченная
                0, // всегда доступна
                0, // всегда доступна
                "achievement", // получается за достижения
                "avatar_badge" // эффект использования - значок на аватаре
            ));
            
            // Добавляем сезонные предметы
            // Для Нового года
            items.add(new CollectibleItemEntity(
                UUID.randomUUID().toString(),
                "Новогодняя шапка",
                "Праздничная шапка для вашего аватара. Доступна только во время зимних праздников.",
                "ic_item_new_year_hat",
                4, // легендарная редкость
                null, // не привязана к категории
                null, // привязка к событию будет установлена позже
                true, // ограниченное по времени
                0, // дата начала будет установлена позже
                0, // дата окончания будет установлена позже
                "event", // получается во время события
                "avatar_accessory" // аксессуар для аватара
            ));
            
            // Для Хэллоуина
            items.add(new CollectibleItemEntity(
                UUID.randomUUID().toString(),
                "Тыквенная маска",
                "Страшная тыквенная маска для вашего аватара. Доступна только во время Хэллоуина.",
                "ic_item_halloween_mask",
                4, // легендарная редкость
                null, // не привязана к категории
                null, // привязка к событию будет установлена позже
                true, // ограниченное по времени
                0, // дата начала будет установлена позже
                0, // дата окончания будет установлена позже
                "event", // получается во время события
                "avatar_accessory" // аксессуар для аватара
            ));
            
            // Для Дня всех влюбленных
            items.add(new CollectibleItemEntity(
                UUID.randomUUID().toString(),
                "Сердечко",
                "Романтическое сердечко для вашего аватара. Доступно только ко Дню всех влюбленных.",
                "ic_item_valentine_heart",
                4, // легендарная редкость
                null, // не привязана к категории
                null, // привязка к событию будет установлена позже
                true, // ограниченное по времени
                0, // дата начала будет установлена позже
                0, // дата окончания будет установлена позже
                "event", // получается во время события
                "avatar_accessory" // аксессуар для аватара
            ));
            
            // Особые предметы за выполнение ежедневных заданий
            items.add(new CollectibleItemEntity(
                UUID.randomUUID().toString(),
                "Кубок исследователя",
                "Награда за выполнение 30 ежедневных заданий.",
                "ic_item_quest_cup",
                3, // эпическая редкость
                null, // не привязана к категории
                null, // не привязана к событию
                false, // не ограниченная
                0, // всегда доступна
                0, // всегда доступна
                "quest", // получается за задания
                "profile_decoration" // декорация профиля
            ));
            
            // Уникальные предметы за ранги
            items.add(new CollectibleItemEntity(
                UUID.randomUUID().toString(),
                "Корона эксперта",
                "Корона для аватара, доступная только пользователям с рангом Эксперт.",
                "ic_item_expert_crown",
                5, // уникальная редкость
                null, // не привязана к категории
                null, // не привязана к событию
                false, // не ограниченная
                0, // всегда доступна
                0, // всегда доступна
                "rank", // получается за ранг
                "avatar_accessory" // аксессуар для аватара
            ));
            
            // Сохраняем предметы в базу данных
            collectibleItemDao.insertAll(items);
            Log.d(TAG, "Инициализировано " + items.size() + " коллекционных предметов");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при инициализации коллекционных предметов: " + e.getMessage());
        }
    }
    
    /**
     * Обновляет привязку сезонных предметов к активным событиям
     */
    public void updateEventItemsAssociation() {
        try {
            // Получаем активные сезонные события
            SeasonalEventManager eventManager = SeasonalEventManager.getInstance(context);
            List<SeasonalEventEntity> activeEvents = eventManager.getCurrentlyActiveEvents();
            
            // Обновляем даты доступности для всех предметов
            for (SeasonalEventEntity event : activeEvents) {
                // Ищем предметы, которые можно связать с этим событием
                // (проверяем по ключевым словам в названии события и предмета)
                List<CollectibleItemEntity> allItems = collectibleItemDao.getBySource("event");
                
                for (CollectibleItemEntity item : allItems) {
                    String eventTitle = event.getTitle().toLowerCase();
                    String itemName = item.getName().toLowerCase();
                    
                    // Проверяем соответствие предмета событию
                    boolean matchesNewYear = (eventTitle.contains("новогод") || eventTitle.contains("new year")) && 
                                           (itemName.contains("новогод") || itemName.contains("шапка") || 
                                            itemName.contains("new year") || itemName.contains("hat"));
                    
                    boolean matchesHalloween = (eventTitle.contains("хэллоуин") || eventTitle.contains("halloween")) && 
                                             (itemName.contains("тыкв") || itemName.contains("хэллоуин") || 
                                              itemName.contains("pumpkin") || itemName.contains("halloween"));
                    
                    boolean matchesValentine = (eventTitle.contains("влюбленных") || eventTitle.contains("valentine")) && 
                                             (itemName.contains("сердц") || itemName.contains("влюбленных") || 
                                              itemName.contains("heart") || itemName.contains("valentine"));
                    
                    // Если предмет соответствует событию, связываем его с этим событием
                    if (matchesNewYear || matchesHalloween || matchesValentine) {
                        item.setAssociatedEventId(event.getId());
                        item.setAvailabilityStartDate(event.getStartDate());
                        item.setAvailabilityEndDate(event.getEndDate());
                        collectibleItemDao.update(item);
                    }
                }
            }
            
            Log.d(TAG, "Обновлена привязка сезонных предметов к событиям");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обновлении привязки предметов к событиям: " + e.getMessage());
        }
    }
    
    /**
     * Класс для хранения информации о предмете пользователя
     */
    public static class UserItemInfo {
        private final CollectibleItemEntity item;
        private final UserItemEntity userItem;
        
        public UserItemInfo(CollectibleItemEntity item, UserItemEntity userItem) {
            this.item = item;
            this.userItem = userItem;
        }
        
        public CollectibleItemEntity getItem() {
            return item;
        }
        
        public UserItemEntity getUserItem() {
            return userItem;
        }
        
        public boolean isEquipped() {
            return userItem.isEquipped();
        }
        
        public String getEquippedSlot() {
            return userItem.getEquippedSlot();
        }
        
        public long getObtainedDate() {
            return userItem.getObtainedDate();
        }
        
        public String getSource() {
            return userItem.getSource();
        }
    }
    
    /**
     * Создает новый предмет
     * @param name название предмета
     * @param description описание предмета
     * @param iconName имя иконки
     * @param rarity редкость (1-5)
     * @param category категория (может быть null)
     * @param eventId ID события (может быть null)
     * @param isLimited ограничен ли по времени
     * @param startDate дата начала доступности (может быть 0)
     * @param endDate дата окончания доступности (может быть 0)
     * @param obtainedFrom способ получения
     * @param usageEffect эффект использования
     * @return ID созданного предмета или null в случае ошибки
     */
    public String createItem(String name, String description, String iconName, int rarity,
                           String category, String eventId, boolean isLimited, 
                           long startDate, long endDate, String obtainedFrom, String usageEffect) {
        try {
            String itemId = UUID.randomUUID().toString();
            
            CollectibleItemEntity item = new CollectibleItemEntity(
                itemId,
                name,
                description,
                iconName,
                rarity,
                category,
                eventId,
                isLimited,
                startDate,
                endDate,
                obtainedFrom,
                usageEffect
            );
            
            collectibleItemDao.insert(item);
            Log.d(TAG, "Создан новый предмет: " + name);
            
            return itemId;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при создании предмета: " + e.getMessage());
            return null;
        }
    }
}
