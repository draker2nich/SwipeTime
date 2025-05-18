package com.draker.swipetime.utils;

import android.content.Context;
import android.util.Log;
import android.app.Application;

import com.draker.swipetime.api.ApiIntegrationManager;
import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.database.entities.UserPreferencesEntity;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.utils.EntityMapper;
import com.draker.swipetime.repository.AnimeRepository;
import com.draker.swipetime.repository.BookRepository;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.GameRepository;
import com.draker.swipetime.repository.MovieRepository;
import com.draker.swipetime.repository.TVShowRepository;
import com.draker.swipetime.repository.UserPreferencesRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Менеджер для создания бесконечного контента без повторений
 */
public class InfiniteContentManager {
    private static final String TAG = "InfiniteContentManager";
    private static InfiniteContentManager instance;

    // Кэш для хранения доступного контента по категориям
    private final Map<String, List<ContentItem>> contentCache = new ConcurrentHashMap<>();
    
    // История показанных элементов (чтобы избежать повторений в ближайшее время)
    private final Map<String, Set<String>> recentlyShownItems = new ConcurrentHashMap<>();
    
    // Флаги для отслеживания загрузки для каждой категории
    private final Map<String, AtomicBoolean> isLoadingMap = new ConcurrentHashMap<>();
    
    // Максимальный размер истории показанных элементов (чтобы избежать утечки памяти)
    private static final int MAX_HISTORY_SIZE = 500;
    
    // Минимальное количество элементов в кэше перед запросом новых
    private static final int MIN_CACHE_THRESHOLD = 10;
    
    // Случайный генератор для более естественного перемешивания
    private final Random random = new Random();
    
    /**
     * Получить экземпляр менеджера бесконечного контента
     */
    public static synchronized InfiniteContentManager getInstance() {
        if (instance == null) {
            instance = new InfiniteContentManager();
        }
        return instance;
    }
    
    /**
     * Конструктор
     */
    private InfiniteContentManager() {
        // Инициализация кэшей и истории
        for (String category : new String[]{"Фильмы", "Сериалы", "Игры", "Книги", "Аниме"}) {
            contentCache.put(category, Collections.synchronizedList(new ArrayList<>()));
            recentlyShownItems.put(category, Collections.synchronizedSet(new HashSet<>()));
            isLoadingMap.put(category, new AtomicBoolean(false));
        }
    }
    
    /**
     * Получить следующую партию элементов для отображения
     * @param category категория контента
     * @param userId ID пользователя
     * @param count количество элементов для получения
     * @param context контекст приложения
     * @param callback обратный вызов для получения результата
     */
    public void getNextBatch(String category, String userId, int count, Context context,
                            ContentCallback callback) {
        
        // Проверяем, достаточно ли элементов в кэше
        List<ContentItem> categoryCache = contentCache.get(category);
        
        if (categoryCache != null && categoryCache.size() >= count) {
            // Если в кэше достаточно элементов, возвращаем их
            List<ContentItem> batch = extractBatch(category, count);
            callback.onContentLoaded(batch);
            
            // Проверяем, не пора ли загрузить еще данные
            if (categoryCache.size() < MIN_CACHE_THRESHOLD) {
                loadMoreContent(category, context);
            }
        } else {
            // Если в кэше недостаточно элементов, загружаем новые данные
            loadMoreContent(category, context, () -> {
                // После загрузки возвращаем доступные элементы
                List<ContentItem> batch = extractBatch(category, count);
                
                // Если элементов все равно нет, создаем синтетические
                if (batch.isEmpty()) {
                    batch = createSyntheticItems(category, count);
                }
                
                callback.onContentLoaded(batch);
            });
        }
    }
    
    /**
     * Извлекает партию элементов из кэша для отображения
     * @param category категория контента
     * @param count количество элементов
     * @return список элементов для отображения
     */
    private synchronized List<ContentItem> extractBatch(String category, int count) {
        List<ContentItem> categoryCache = contentCache.get(category);
        if (categoryCache == null || categoryCache.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ContentItem> result = new ArrayList<>();
        int extractCount = Math.min(count, categoryCache.size());
        
        for (int i = 0; i < extractCount; i++) {
            ContentItem item = categoryCache.remove(0);
            result.add(item);
            
            // Добавляем в историю показанных элементов
            addToRecentlyShown(category, item.getId());
        }
        
        return result;
    }
    
    /**
     * Добавляет элемент в историю недавно показанных
     * @param category категория контента
     * @param itemId ID элемента
     */
    private synchronized void addToRecentlyShown(String category, String itemId) {
        Set<String> history = recentlyShownItems.get(category);
        if (history == null) {
            history = Collections.synchronizedSet(new HashSet<>());
            recentlyShownItems.put(category, history);
        }
        
        // Добавляем элемент в историю
        history.add(itemId);
        
        // Если история слишком большая, удаляем старые элементы
        if (history.size() > MAX_HISTORY_SIZE) {
            // Удаляем 20% старых элементов
            int toRemove = MAX_HISTORY_SIZE / 5;
            List<String> historyList = new ArrayList<>(history);
            for (int i = 0; i < toRemove && i < historyList.size(); i++) {
                history.remove(historyList.get(i));
            }
        }
    }
    
    /**
     * Проверяет, был ли элемент недавно показан
     * @param category категория контента
     * @param itemId ID элемента
     * @return true, если элемент недавно показывался
     */
    private boolean wasRecentlyShown(String category, String itemId) {
        Set<String> history = recentlyShownItems.get(category);
        return history != null && history.contains(itemId);
    }
    
    /**
     * Асинхронно загружает больше контента для указанной категории
     * @param category категория контента
     * @param context контекст приложения
     */
    private void loadMoreContent(String category, Context context) {
        loadMoreContent(category, context, null);
    }
    
    /**
     * Асинхронно загружает больше контента для указанной категории
     * @param category категория контента
     * @param context контекст приложения
     * @param onComplete действие по завершении загрузки
     */
    private void loadMoreContent(String category, Context context, Runnable onComplete) {
        // Проверяем, не загружается ли уже контент для этой категории
        AtomicBoolean isLoading = isLoadingMap.get(category);
        if (isLoading != null && isLoading.get()) {
            // Если уже идет загрузка, просто выполняем действие по завершении, если оно есть
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        
        // Устанавливаем флаг загрузки
        if (isLoading != null) {
            isLoading.set(true);
        }
        
        Log.d(TAG, "Загрузка дополнительного контента для категории: " + category);
        
        // Получаем API менеджер
        Application app = (Application) context.getApplicationContext();
        ApiIntegrationManager apiManager = ApiIntegrationManager.getInstance(app);
        
        // Запускаем загрузку данных
        apiManager.refreshCategoryContent(category, 20, new ApiIntegrationManager.ApiInitCallback() {
            @Override
            public void onComplete(boolean success) {
                if (success) {
                    Log.d(TAG, "Успешно загружен дополнительный контент для категории: " + category);
                    
                    // Загружаем новые данные из базы в кэш
                    refreshCacheFromDatabase(category, context);
                    
                    // Сбрасываем флаг загрузки
                    if (isLoading != null) {
                        isLoading.set(false);
                    }
                    
                    // Выполняем действие по завершении, если оно есть
                    if (onComplete != null) {
                        onComplete.run();
                    }
                } else {
                    Log.e(TAG, "Ошибка при загрузке дополнительного контента для категории: " + category);
                    
                    // Сбрасываем флаг загрузки
                    if (isLoading != null) {
                        isLoading.set(false);
                    }
                    
                    // Выполняем действие по завершении, даже если произошла ошибка
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Ошибка при загрузке контента для категории " + category + ": " + errorMessage);
                
                // Сбрасываем флаг загрузки
                if (isLoading != null) {
                    isLoading.set(false);
                }
                
                // Выполняем действие по завершении, даже если произошла ошибка
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }
    
    /**
     * Обновляет кэш данными из базы данных
     * @param category категория контента
     * @param context контекст приложения
     */
    private void refreshCacheFromDatabase(String category, Context context) {
        // Инициализируем репозитории
        Application app = (Application) context.getApplicationContext();
        MovieRepository movieRepository = new MovieRepository(app);
        TVShowRepository tvShowRepository = new TVShowRepository(app);
        GameRepository gameRepository = new GameRepository(app);
        BookRepository bookRepository = new BookRepository(app);
        AnimeRepository animeRepository = new AnimeRepository(app);
        ContentRepository contentRepository = new ContentRepository(app);
        
        // Список для новых элементов
        List<ContentItem> newItems = new ArrayList<>();
        
        // Получаем все элементы для данной категории
        List<ContentEntity> entities = new ArrayList<>();
        
        switch (category) {
            case "Фильмы":
                for (MovieEntity entity : movieRepository.getAll()) {
                    entities.add(entity);
                }
                break;
                
            case "Сериалы":
                for (TVShowEntity entity : tvShowRepository.getAll()) {
                    entities.add(entity);
                }
                break;
                
            case "Игры":
                for (GameEntity entity : gameRepository.getAll()) {
                    entities.add(entity);
                }
                break;
                
            case "Книги":
                for (BookEntity entity : bookRepository.getAll()) {
                    entities.add(entity);
                }
                break;
                
            case "Аниме":
                for (AnimeEntity entity : animeRepository.getAll()) {
                    entities.add(entity);
                }
                break;
                
            default:
                entities = contentRepository.getByCategory(category);
                break;
        }
        
        // Конвертируем сущности в ContentItem и фильтруем только новые (не показанные недавно)
        for (ContentEntity entity : entities) {
            if (!wasRecentlyShown(category, entity.getId())) {
                ContentItem item = EntityMapper.mapToContentItem(entity);
                newItems.add(item);
            }
        }
        
        // Перемешиваем новые элементы
        Collections.shuffle(newItems, random);
        
        Log.d(TAG, "Добавлено " + newItems.size() + " новых элементов в кэш для категории " + category);
        
        // Добавляем новые элементы в кэш
        List<ContentItem> cache = contentCache.get(category);
        if (cache != null) {
            cache.addAll(newItems);
        }
    }
    
    /**
     * Создает синтетические элементы, когда API не может предоставить достаточно контента
     * @param category категория контента
     * @param count количество элементов
     * @return список синтетических элементов
     */
    private List<ContentItem> createSyntheticItems(String category, int count) {
        Log.d(TAG, "Создание " + count + " синтетических элементов для категории " + category);
        
        List<ContentItem> syntheticItems = new ArrayList<>();
        
        // Массивы заголовков и описаний по категориям для более естественного вида
        String[] movieTitles = {
                "Неизведанные миры", "Последний рубеж", "Тайна прошлого", "В поисках истины",
                "Непокоренная вершина", "Следы времени", "Навстречу судьбе", "Тени прошлого",
                "За горизонтом", "Сердце океана", "Путь домой", "Звездный путь"
        };
        
        String[] tvShowTitles = {
                "Хроники города", "Семейные тайны", "Запретная зона", "Мистические истории",
                "На краю вселенной", "Параллельные миры", "Следствие ведут", "Тайный агент",
                "Городские легенды", "Секретные материалы", "Жизнь с нуля", "Поворот судьбы"
        };
        
        String[] gameTitles = {
                "Королевство теней", "Последний герой", "Эпоха легенд", "Темная империя",
                "Хроники подземелья", "Галактический фронтир", "Путь воина", "Осада крепости",
                "Стальное сердце", "Магический кристалл", "Наследие предков", "Портал времени"
        };
        
        String[] bookTitles = {
                "Тайны времени", "Последний свиток", "Забытая легенда", "Хранители знаний",
                "Потерянная страница", "Древний манускрипт", "Путешествие души", "Тени прошлого",
                "За пределами разума", "Наследие веков", "Врата познания", "Книга судеб"
        };
        
        String[] animeTitles = {
                "Дух воина", "Школа магии", "Космические странники", "Тайны древнего клана",
                "Меч бессмертного", "Академия героев", "Призрачный мир", "Хроники аниме",
                "Легенда о мече", "Последний самурай", "Путь ниндзя", "Небесные стражи"
        };
        
        String[] descriptions = {
                "Увлекательная история о путешествии в неизведанные земли и поиске сокровищ.",
                "Глубокое погружение в мир фантазии, где реальность переплетается с вымыслом.",
                "Захватывающее приключение с неожиданными поворотами сюжета и яркими персонажами.",
                "История о преодолении трудностей и поиске внутренней силы в самых сложных ситуациях.",
                "Эмоциональное повествование о дружбе, предательстве и искуплении.",
                "Необычный взгляд на обычные вещи, заставляющий задуматься о смысле жизни."
        };
        
        // Выбираем массив заголовков в зависимости от категории
        String[] titles;
        switch (category) {
            case "Фильмы":
                titles = movieTitles;
                break;
            case "Сериалы":
                titles = tvShowTitles;
                break;
            case "Игры":
                titles = gameTitles;
                break;
            case "Книги":
                titles = bookTitles;
                break;
            case "Аниме":
                titles = animeTitles;
                break;
            default:
                titles = movieTitles;
                break;
        }
        
        // Генерируем синтетические элементы
        for (int i = 0; i < count; i++) {
            // Выбираем случайный заголовок и описание
            String title = titles[random.nextInt(titles.length)] + " " + (random.nextInt(5) + 1);
            String description = descriptions[random.nextInt(descriptions.length)];
            
            // Создаем уникальный ID
            String id = "synthetic_" + category.toLowerCase() + "_" + System.currentTimeMillis() + "_" + i;
            
            // Создаем элемент контента
            ContentItem item = new ContentItem(id, title, description, null, category);
            
            // Заполняем дополнительные данные
            item.setYear(2020 + random.nextInt(6)); // Случайный год между 2020 и 2025
            item.setRating(3.5f + random.nextFloat() * 1.5f); // Рейтинг от 3.5 до 5.0
            
            // Добавляем жанр в зависимости от категории
            switch (category) {
                case "Фильмы":
                    String[] movieGenres = {"Фантастика", "Драма", "Комедия", "Боевик", "Триллер"};
                    item.setGenre(movieGenres[random.nextInt(movieGenres.length)]);
                    item.setDirector("Студия 'Синтезис'");
                    break;
                case "Сериалы":
                    String[] tvGenres = {"Драма", "Комедия", "Фантастика", "Детектив", "Мистика"};
                    item.setGenre(tvGenres[random.nextInt(tvGenres.length)]);
                    item.setSeasons(random.nextInt(3) + 1);
                    item.setEpisodes(random.nextInt(10) + 5);
                    break;
                case "Игры":
                    String[] gameGenres = {"RPG", "Стратегия", "Экшен", "Приключения", "Симулятор"};
                    item.setGenre(gameGenres[random.nextInt(gameGenres.length)]);
                    item.setDeveloper("InfiniteSoft");
                    item.setPlatforms("PC, Mobile");
                    break;
                case "Книги":
                    String[] bookGenres = {"Фэнтези", "Детектив", "Роман", "Фантастика", "Приключения"};
                    item.setGenre(bookGenres[random.nextInt(bookGenres.length)]);
                    item.setAuthor("Алекс Райтер");
                    item.setPublisher("Издательство 'Новый мир'");
                    item.setPages(150 + random.nextInt(350));
                    break;
                case "Аниме":
                    String[] animeGenres = {"Сёнен", "Сёдзё", "Меха", "Фэнтези", "Исэкай"};
                    item.setGenre(animeGenres[random.nextInt(animeGenres.length)]);
                    item.setStudio("Studio Infinity");
                    item.setEpisodes(12 + random.nextInt(13));
                    break;
            }
            
            // Добавляем элемент в результат
            syntheticItems.add(item);
        }
        
        return syntheticItems;
    }
    
    /**
     * Сбрасывает кэш для указанной категории
     * @param category категория контента
     */
    public void resetCache(String category) {
        List<ContentItem> cache = contentCache.get(category);
        if (cache != null) {
            cache.clear();
        }
        
        // Также можно сбросить историю показанных элементов
        Set<String> history = recentlyShownItems.get(category);
        if (history != null) {
            history.clear();
        }
        
        Log.d(TAG, "Кэш и история показов сброшены для категории: " + category);
    }
    
    /**
     * Сбрасывает весь кэш
     */
    public void resetAllCaches() {
        for (String category : contentCache.keySet()) {
            resetCache(category);
        }
        
        Log.d(TAG, "Все кэши и истории показов сброшены");
    }
    
    /**
     * Интерфейс для получения результата загрузки контента
     */
    public interface ContentCallback {
        void onContentLoaded(List<ContentItem> items);
    }
}