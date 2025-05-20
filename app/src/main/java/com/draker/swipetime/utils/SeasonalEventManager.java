package com.draker.swipetime.utils;

import android.content.Context;
import android.util.Log;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.SeasonalEventDao;
import com.draker.swipetime.database.entities.SeasonalEventEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Менеджер для управления сезонными событиями
 */
public class SeasonalEventManager {

    private static final String TAG = "SeasonalEventManager";
    
    private final Context context;
    private final AppDatabase database;
    private final SeasonalEventDao seasonalEventDao;
    
    private static SeasonalEventManager instance;
    
    // Интерфейс для обратных вызовов о событиях
    public interface OnEventChangeListener {
        void onEventStarted(SeasonalEventEntity event);
        void onEventEnded(SeasonalEventEntity event);
    }
    
    private OnEventChangeListener eventChangeListener;
    
    // Приватный конструктор (Singleton)
    private SeasonalEventManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
        this.seasonalEventDao = database.seasonalEventDao();
        
        // Проверяем, есть ли сезонные события
        if (seasonalEventDao.getAll().size() == 0) {
            initializeDefaultEvents();
        }
    }
    
    // Получение экземпляра (Singleton)
    public static synchronized SeasonalEventManager getInstance(Context context) {
        if (instance == null) {
            instance = new SeasonalEventManager(context);
        }
        return instance;
    }
    
    /**
     * Установить слушатель изменений событий
     * @param listener слушатель
     */
    public void setEventChangeListener(OnEventChangeListener listener) {
        this.eventChangeListener = listener;
    }
    
    /**
     * Инициализирует набор стандартных сезонных событий
     */
    private void initializeDefaultEvents() {
        List<SeasonalEventEntity> events = new ArrayList<>();
        
        // Получаем текущий год
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        
        // Создаем календарь для установки дат
        Calendar calendar = Calendar.getInstance();
        
        // 1. Новогоднее событие (15 декабря - 15 января)
        calendar.set(currentYear, Calendar.DECEMBER, 15, 0, 0, 0);
        long newYearStart = calendar.getTimeInMillis();
        
        calendar.set(currentYear + 1, Calendar.JANUARY, 15, 23, 59, 59);
        long newYearEnd = calendar.getTimeInMillis();
        
        events.add(new SeasonalEventEntity(
            UUID.randomUUID().toString(),
            "Новогоднее волшебство",
            "Встречайте Новый год с особыми рекомендациями и праздничными достижениями!",
            "ic_event_new_year",
            "#2196F3", // Синий цвет
            newYearStart,
            newYearEnd,
            true,
            true, // Есть особые предметы
            true, // Есть особые задания
            1.5f, // Бонус к опыту (множитель)
            "holiday"
        ));
        
        // 2. Весеннее событие (1 марта - 31 мая)
        calendar.set(currentYear, Calendar.MARCH, 1, 0, 0, 0);
        long springStart = calendar.getTimeInMillis();
        
        calendar.set(currentYear, Calendar.MAY, 31, 23, 59, 59);
        long springEnd = calendar.getTimeInMillis();
        
        events.add(new SeasonalEventEntity(
            UUID.randomUUID().toString(),
            "Весеннее обновление",
            "Новая весна - новые открытия! Ищите свежие релизы и рекомендации.",
            "ic_event_spring",
            "#4CAF50", // Зеленый цвет
            springStart,
            springEnd,
            true,
            true,
            true,
            1.2f,
            "seasonal"
        ));
        
        // 3. Летнее событие (1 июня - 31 августа)
        calendar.set(currentYear, Calendar.JUNE, 1, 0, 0, 0);
        long summerStart = calendar.getTimeInMillis();
        
        calendar.set(currentYear, Calendar.AUGUST, 31, 23, 59, 59);
        long summerEnd = calendar.getTimeInMillis();
        
        events.add(new SeasonalEventEntity(
            UUID.randomUUID().toString(),
            "Летний марафон",
            "Время для новых открытий! Составьте свой летний список и выполняйте особые испытания.",
            "ic_event_summer",
            "#FF9800", // Оранжевый цвет
            summerStart,
            summerEnd,
            true,
            true,
            true,
            1.2f,
            "seasonal"
        ));
        
        // 4. Осеннее событие (1 сентября - 30 ноября)
        calendar.set(currentYear, Calendar.SEPTEMBER, 1, 0, 0, 0);
        long autumnStart = calendar.getTimeInMillis();
        
        calendar.set(currentYear, Calendar.NOVEMBER, 30, 23, 59, 59);
        long autumnEnd = calendar.getTimeInMillis();
        
        events.add(new SeasonalEventEntity(
            UUID.randomUUID().toString(),
            "Осенний фестиваль",
            "Уютная атмосфера и премьеры сезона! Собирайте особые достижения и открывайте новые жанры.",
            "ic_event_autumn",
            "#FF5722", // Темно-оранжевый цвет
            autumnStart,
            autumnEnd,
            true,
            true,
            true,
            1.2f,
            "seasonal"
        ));
        
        // 5. Хэллоуин (20 октября - 2 ноября)
        calendar.set(currentYear, Calendar.OCTOBER, 20, 0, 0, 0);
        long halloweenStart = calendar.getTimeInMillis();
        
        calendar.set(currentYear, Calendar.NOVEMBER, 2, 23, 59, 59);
        long halloweenEnd = calendar.getTimeInMillis();
        
        events.add(new SeasonalEventEntity(
            UUID.randomUUID().toString(),
            "Хэллоуин",
            "Жуткие истории и ужастики ждут вас! Соберите коллекцию страшных фильмов и историй.",
            "ic_event_halloween",
            "#9C27B0", // Пурпурный цвет
            halloweenStart,
            halloweenEnd,
            true,
            true,
            true,
            1.5f,
            "holiday"
        ));
        
        // 6. День всех влюбленных (7-21 февраля)
        calendar.set(currentYear, Calendar.FEBRUARY, 7, 0, 0, 0);
        long valentineStart = calendar.getTimeInMillis();
        
        calendar.set(currentYear, Calendar.FEBRUARY, 21, 23, 59, 59);
        long valentineEnd = calendar.getTimeInMillis();
        
        events.add(new SeasonalEventEntity(
            UUID.randomUUID().toString(),
            "День всех влюбленных",
            "Романтические истории и фильмы для двоих. Откройте новые жанры и соберите особые достижения.",
            "ic_event_valentine",
            "#E91E63", // Розовый цвет
            valentineStart,
            valentineEnd,
            true,
            true,
            true,
            1.3f,
            "holiday"
        ));
        
        // 7. Специальное событие на текущий месяц (для демонстрации)
        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH);
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        
        calendar.set(currentYear, currentMonth, 1, 0, 0, 0);
        long specialStart = calendar.getTimeInMillis();
        
        calendar.set(currentYear, currentMonth, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        long specialEnd = calendar.getTimeInMillis();
        
        // Устанавливаем событие на текущий месяц, если оно еще не прошло
        if (currentDay < 15) {
            events.add(new SeasonalEventEntity(
                UUID.randomUUID().toString(),
                "Специальное событие",
                "Ограниченное по времени событие с эксклюзивными наградами и достижениями!",
                "ic_event_special",
                "#3F51B5", // Индиго цвет
                specialStart,
                specialEnd,
                true,
                true,
                true,
                2.0f,
                "special"
            ));
        }
        
        // Сохраняем все события в базу данных
        seasonalEventDao.insertAll(events);
        Log.d(TAG, "Инициализировано " + events.size() + " стандартных сезонных событий");
    }
    
    /**
     * Обновляет состояние всех событий (активирует текущие, деактивирует завершенные)
     */
    public void refreshEvents() {
        try {
            long currentTime = System.currentTimeMillis();
            
            // Деактивируем завершенные события
            seasonalEventDao.deactivateEndedEvents(currentTime);
            
            // Активируем текущие события
            seasonalEventDao.activateCurrentEvents(currentTime);
            
            // Проверяем, какие события начались или закончились с момента последнего обновления
            if (eventChangeListener != null) {
                List<SeasonalEventEntity> allEvents = seasonalEventDao.getAll();
                for (SeasonalEventEntity event : allEvents) {
                    // Проверяем, началось ли событие недавно
                    if (event.isActive() && event.getStartDate() <= currentTime && 
                            currentTime - event.getStartDate() < TimeUnit.HOURS.toMillis(12)) {
                        eventChangeListener.onEventStarted(event);
                    }
                    
                    // Проверяем, закончилось ли событие недавно
                    if (!event.isActive() && event.getEndDate() <= currentTime && 
                            currentTime - event.getEndDate() < TimeUnit.HOURS.toMillis(12)) {
                        eventChangeListener.onEventEnded(event);
                    }
                }
            }
            
            Log.d(TAG, "Обновлено состояние сезонных событий");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обновлении состояния сезонных событий: " + e.getMessage());
        }
    }
    
    /**
     * Получает список всех активных в настоящий момент сезонных событий
     * @return список активных событий
     */
    public List<SeasonalEventEntity> getCurrentlyActiveEvents() {
        try {
            return seasonalEventDao.getCurrentlyActive(System.currentTimeMillis());
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении активных сезонных событий: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Получает список предстоящих событий
     * @return список предстоящих событий
     */
    public List<SeasonalEventEntity> getUpcomingEvents() {
        try {
            return seasonalEventDao.getUpcomingEvents(System.currentTimeMillis());
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении предстоящих сезонных событий: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Получает информацию о конкретном событии
     * @param eventId ID события
     * @return информация о событии или null, если событие не найдено
     */
    public SeasonalEventEntity getEventById(String eventId) {
        try {
            return seasonalEventDao.getById(eventId);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении информации о событии: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Получает текущий бонусный множитель опыта на основе активных событий
     * @return множитель опыта (1.0 по умолчанию, если нет активных событий)
     */
    public float getCurrentXpMultiplier() {
        try {
            List<SeasonalEventEntity> activeEvents = getCurrentlyActiveEvents();
            if (activeEvents.isEmpty()) {
                return 1.0f;
            }
            
            // Берем максимальный множитель из всех активных событий
            float maxMultiplier = 1.0f;
            for (SeasonalEventEntity event : activeEvents) {
                if (event.getBonusXpMultiplier() > maxMultiplier) {
                    maxMultiplier = event.getBonusXpMultiplier();
                }
            }
            
            return maxMultiplier;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при расчете множителя опыта: " + e.getMessage());
            return 1.0f;
        }
    }
    
    /**
     * Создает новое сезонное событие
     * @param title заголовок события
     * @param description описание события
     * @param iconName имя иконки
     * @param themeColor цвет темы
     * @param startDate дата начала
     * @param endDate дата окончания
     * @param hasSpecialItems есть ли особые предметы
     * @param hasSpecialQuests есть ли особые задания
     * @param bonusXpMultiplier множитель бонусного опыта
     * @param eventType тип события
     * @return ID созданного события или null в случае ошибки
     */
    public String createEvent(String title, String description, String iconName, String themeColor,
                              long startDate, long endDate, boolean hasSpecialItems, 
                              boolean hasSpecialQuests, float bonusXpMultiplier, String eventType) {
        try {
            String eventId = UUID.randomUUID().toString();
            
            SeasonalEventEntity event = new SeasonalEventEntity(
                eventId,
                title,
                description,
                iconName,
                themeColor,
                startDate,
                endDate,
                true,
                hasSpecialItems,
                hasSpecialQuests,
                bonusXpMultiplier,
                eventType
            );
            
            seasonalEventDao.insert(event);
            Log.d(TAG, "Создано новое сезонное событие: " + title);
            
            return eventId;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при создании сезонного события: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Удаляет сезонное событие
     * @param eventId ID события для удаления
     * @return true если событие было успешно удалено
     */
    public boolean deleteEvent(String eventId) {
        try {
            seasonalEventDao.deleteById(eventId);
            Log.d(TAG, "Удалено сезонное событие с ID: " + eventId);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при удалении сезонного события: " + e.getMessage());
            return false;
        }
    }
}
