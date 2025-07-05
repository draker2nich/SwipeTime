package com.draker.swipetime.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.sqlite.db.SupportSQLiteDatabase;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.database.entities.UserStatsEntity;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.repository.ContentRepository;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Объединенный класс для работы с базой данных, включая:
 * - Очистку базы данных
 * - Генерацию начальных данных
 * - Маппинг сущностей
 * - Управление избранным
 * - Управление историей просмотров
 */
public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";
    private static final Executor executor = Executors.newSingleThreadExecutor();

    // Константы для SharedPreferences
    private static final String FAVORITES_PREFS = "favorites_prefs";
    private static final String VIEWED_HISTORY_PREFS = "viewed_history_prefs";
    private static final String KEY_FAVORITE_IDS = "favorite_content_ids";
    private static final String PREF_KEY_PREFIX = "viewed_items_";
    private static final String PREF_KEY_DISLIKED_PREFIX = "disliked_items_";
    private static final String PREF_KEY_LIKED_PREFIX = "liked_items_";
    private static final int MAX_HISTORY_ITEMS = 1000;

    private static DatabaseHelper instance;
    private final Context context;

    private DatabaseHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    // ==================== DATABASE CLEANING SECTION ====================

    /**
     * Удаляет файл базы данных
     * @param dbName имя базы данных (swipetime-db)
     * @return true если база данных была удалена, false в противном случае
     */
    public boolean deleteDatabase(String dbName) {
        try {
            for (String path : context.databaseList()) {
                Log.d(TAG, "Found database: " + path);
                if (path.contains(dbName) || path.equalsIgnoreCase(dbName)) {
                    File dbFile = context.getDatabasePath(path);
                    boolean deleted = dbFile.delete();

                    Log.d(TAG, "Database " + path + " deleted: " + deleted);

                    // Удаляем также файлы журналов и shm
                    new File(dbFile.getPath() + "-journal").delete();
                    new File(dbFile.getPath() + "-shm").delete();
                    new File(dbFile.getPath() + "-wal").delete();

                    return deleted;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting database: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Полностью удаляет базу данных приложения
     * @return true, если база была успешно удалена
     */
    public boolean deleteDatabaseCompletely() {
        try {
            // Сначала закрываем и уничтожаем экземпляр базы данных
            AppDatabase.destroyInstance();

            // Получаем путь к базе данных и удаляем файл
            File dbFile = context.getDatabasePath("swipetime-db");
            if (dbFile.exists()) {
                boolean deleted = dbFile.delete();
                Log.i(TAG, "База данных удалена: " + deleted);

                // Также удаляем файлы журнала и резервные копии
                new File(dbFile.getPath() + "-shm").delete();
                new File(dbFile.getPath() + "-wal").delete();
                new File(dbFile.getPath() + "-journal").delete();

                return deleted;
            } else {
                Log.i(TAG, "База данных не существует");
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при удалении базы данных: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Радикально удаляет все данные из базы данных, кроме пользовательских настроек
     */
    public void clearAllData() {
        Log.d(TAG, "Запуск полной очистки данных");

        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                SupportSQLiteDatabase sqliteDb = db.getOpenHelper().getWritableDatabase();

                // Отключаем проверку внешних ключей для безопасного удаления
                sqliteDb.execSQL("PRAGMA foreign_keys = OFF");

                // Удаляем все данные из всех таблиц
                sqliteDb.execSQL("DELETE FROM movies");
                sqliteDb.execSQL("DELETE FROM tv_shows");
                sqliteDb.execSQL("DELETE FROM games");
                sqliteDb.execSQL("DELETE FROM books");
                sqliteDb.execSQL("DELETE FROM anime");
                sqliteDb.execSQL("DELETE FROM content");
                sqliteDb.execSQL("DELETE FROM reviews");

                // Сбрасываем автоинкрементные идентификаторы
                sqliteDb.execSQL("DELETE FROM sqlite_sequence");

                // Включаем обратно проверку внешних ключей
                sqliteDb.execSQL("PRAGMA foreign_keys = ON");

                Log.d(TAG, "Полная очистка данных успешно завершена");
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при полной очистке данных: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Удаляет тестовые данные из всех категорий, но сохраняет пользовательские данные
     */
    public void clearTestData() {
        Log.d(TAG, "Начало очистки тестовых данных из всех категорий");

        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                SupportSQLiteDatabase sqliteDb = db.getOpenHelper().getWritableDatabase();

                // Отключаем проверку внешних ключей для безопасного удаления
                sqliteDb.execSQL("PRAGMA foreign_keys = OFF");

                // Удаляем все фильмы, не отмеченные пользователем как понравившиеся
                sqliteDb.execSQL("DELETE FROM movies WHERE liked = 0");
                sqliteDb.execSQL("DELETE FROM tv_shows WHERE liked = 0");
                sqliteDb.execSQL("DELETE FROM games WHERE liked = 0");
                sqliteDb.execSQL("DELETE FROM books WHERE liked = 0");
                sqliteDb.execSQL("DELETE FROM anime WHERE liked = 0");
                sqliteDb.execSQL("DELETE FROM content WHERE liked = 0");

                // Включаем обратно проверку внешних ключей
                sqliteDb.execSQL("PRAGMA foreign_keys = ON");

                Log.d(TAG, "Очистка тестовых данных успешно завершена");
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при очистке тестовых данных: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // ==================== DATA GENERATION SECTION ====================

    /**
     * Инициализировать базу данных с профилем пользователя
     */
    public void populateDatabase() {
        Log.d(TAG, "Начало инициализации базы данных");

        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);

                // Проверяем, есть ли пользователи в базе
                int userCount = db.userDao().getCount();
                Log.d(TAG, "Проверка пользователей в базе: " + userCount);

                // Если нет пользователей, создаем профиль
                if (userCount == 0) {
                    Log.d(TAG, "Создаем пользователя");

                    // Создаем пользователя
                    UserEntity defaultUser = getDefaultUser();
                    db.userDao().insert(defaultUser);

                    // Создаем статистику пользователя
                    UserStatsEntity stats = new UserStatsEntity(defaultUser.getId());
                    stats.setSwipesCount(0);
                    stats.setRightSwipesCount(0);
                    stats.setLeftSwipesCount(0);
                    stats.setRatingsCount(0);
                    stats.setReviewsCount(0);
                    stats.setConsumedCount(0);
                    stats.setStreakDays(0);
                    stats.setLastActivityDate(System.currentTimeMillis());
                    db.userStatsDao().insert(stats);

                    // Инициализация базового набора достижений
                    GamificationManager gamificationManager = GamificationManager.getInstance(context);

                    Log.d(TAG, "Пользователь создан");
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при инициализации базы данных: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Создать пользователя по умолчанию
     * @return пользователь по умолчанию
     */
    private UserEntity getDefaultUser() {
        return new UserEntity(
                "user_1",
                "Пользователь",
                "user@swipetime.com",
                "https://i.pravatar.cc/150?img=1"
        );
    }

    // ==================== ENTITY MAPPING SECTION ====================

    /**
     * Преобразует сущность ContentEntity в объект ContentItem
     * @param entity сущность ContentEntity
     * @return объект ContentItem
     */
    public static ContentItem mapToContentItem(ContentEntity entity) {
        if (entity == null) {
            return null;
        }

        ContentItem item = new ContentItem(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getCategory()
        );

        // Копируем общие поля
        item.setLiked(entity.isLiked());
        item.setRating(entity.getRating());

        // Обрабатываем специфические типы сущностей
        try {
            if (entity instanceof MovieEntity) {
                applyMovieFields((MovieEntity) entity, item);
            } else if (entity instanceof TVShowEntity) {
                applyTVShowFields((TVShowEntity) entity, item);
            } else if (entity instanceof GameEntity) {
                applyGameFields((GameEntity) entity, item);
            } else if (entity instanceof BookEntity) {
                applyBookFields((BookEntity) entity, item);
            } else if (entity instanceof AnimeEntity) {
                applyAnimeFields((AnimeEntity) entity, item);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при преобразовании сущности: " + e.getMessage());
        }

        return item;
    }

    private static void applyMovieFields(MovieEntity movie, ContentItem item) {
        if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
            item.setGenre(movie.getGenres());
        }

        if (movie.getReleaseYear() > 0) {
            item.setYear(movie.getReleaseYear());
        }

        if (movie.getDirector() != null && !movie.getDirector().isEmpty()) {
            item.setDirector(movie.getDirector());
        }

        item.setWatched(movie.isWatched());
    }

    private static void applyTVShowFields(TVShowEntity tvShow, ContentItem item) {
        try {
            if (tvShow.getClass().getMethod("getGenres") != null) {
                item.setGenre((String) tvShow.getClass().getMethod("getGenres").invoke(tvShow));
            } else if (tvShow.getClass().getMethod("getGenre") != null) {
                item.setGenre((String) tvShow.getClass().getMethod("getGenre").invoke(tvShow));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            if (tvShow.getClass().getMethod("getReleaseYear") != null) {
                item.setYear((int) tvShow.getClass().getMethod("getReleaseYear").invoke(tvShow));
            } else if (tvShow.getClass().getMethod("getYear") != null) {
                item.setYear((int) tvShow.getClass().getMethod("getYear").invoke(tvShow));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
    }

    private static void applyGameFields(GameEntity game, ContentItem item) {
        try {
            if (game.getClass().getMethod("getGenres") != null) {
                item.setGenre((String) game.getClass().getMethod("getGenres").invoke(game));
            } else if (game.getClass().getMethod("getGenre") != null) {
                item.setGenre((String) game.getClass().getMethod("getGenre").invoke(game));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            if (game.getClass().getMethod("getReleaseYear") != null) {
                item.setYear((int) game.getClass().getMethod("getReleaseYear").invoke(game));
            } else if (game.getClass().getMethod("getYear") != null) {
                item.setYear((int) game.getClass().getMethod("getYear").invoke(game));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            if (game.getClass().getMethod("getDeveloper") != null) {
                item.setDeveloper((String) game.getClass().getMethod("getDeveloper").invoke(game));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            if (game.getClass().getMethod("getPlatforms") != null) {
                item.setPlatforms((String) game.getClass().getMethod("getPlatforms").invoke(game));
            } else if (game.getClass().getMethod("getPlatform") != null) {
                item.setPlatforms((String) game.getClass().getMethod("getPlatform").invoke(game));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
    }

    private static void applyBookFields(BookEntity book, ContentItem item) {
        try {
            if (book.getClass().getMethod("getGenres") != null) {
                item.setGenre((String) book.getClass().getMethod("getGenres").invoke(book));
            } else if (book.getClass().getMethod("getGenre") != null) {
                item.setGenre((String) book.getClass().getMethod("getGenre").invoke(book));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            if (book.getClass().getMethod("getReleaseYear") != null) {
                item.setYear((int) book.getClass().getMethod("getReleaseYear").invoke(book));
            } else if (book.getClass().getMethod("getYear") != null) {
                item.setYear((int) book.getClass().getMethod("getYear").invoke(book));
            } else if (book.getClass().getMethod("getPublicationYear") != null) {
                item.setYear((int) book.getClass().getMethod("getPublicationYear").invoke(book));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            if (book.getClass().getMethod("getAuthor") != null) {
                item.setAuthor((String) book.getClass().getMethod("getAuthor").invoke(book));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            if (book.getClass().getMethod("getPublisher") != null) {
                item.setPublisher((String) book.getClass().getMethod("getPublisher").invoke(book));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            if (book.getClass().getMethod("getPages") != null) {
                item.setPages((int) book.getClass().getMethod("getPages").invoke(book));
            } else if (book.getClass().getMethod("getPageCount") != null) {
                item.setPages((int) book.getClass().getMethod("getPageCount").invoke(book));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
    }

    private static void applyAnimeFields(AnimeEntity anime, ContentItem item) {
        try {
            if (anime.getClass().getMethod("getGenres") != null) {
                item.setGenre((String) anime.getClass().getMethod("getGenres").invoke(anime));
            } else if (anime.getClass().getMethod("getGenre") != null) {
                item.setGenre((String) anime.getClass().getMethod("getGenre").invoke(anime));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            if (anime.getClass().getMethod("getReleaseYear") != null) {
                item.setYear((int) anime.getClass().getMethod("getReleaseYear").invoke(anime));
            } else if (anime.getClass().getMethod("getYear") != null) {
                item.setYear((int) anime.getClass().getMethod("getYear").invoke(anime));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            if (anime.getClass().getMethod("getStudio") != null) {
                item.setStudio((String) anime.getClass().getMethod("getStudio").invoke(anime));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            if (anime.getClass().getMethod("getEpisodes") != null) {
                item.setEpisodes((int) anime.getClass().getMethod("getEpisodes").invoke(anime));
            } else if (anime.getClass().getMethod("getEpisodeCount") != null) {
                item.setEpisodes((int) anime.getClass().getMethod("getEpisodeCount").invoke(anime));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
    }

    // ==================== FAVORITES MANAGEMENT SECTION ====================

    /**
     * Добавить элемент в избранное и сохранить состояние
     * @param contentId ID контента
     * @return true, если операция успешна
     */
    public boolean addToFavorites(String contentId) {
        ContentRepository contentRepository = new ContentRepository(context);
        boolean result = contentRepository.updateAndPersistLikedStatus(contentId, true);
        if (result) {
            saveFavoriteIdToPrefs(contentId);
        }
        return result;
    }

    /**
     * Удалить элемент из избранного
     * @param contentId ID контента
     * @return true, если операция успешна
     */
    public boolean removeFromFavorites(String contentId) {
        ContentRepository contentRepository = new ContentRepository(context);
        boolean result = contentRepository.updateAndPersistLikedStatus(contentId, false);
        if (result) {
            removeFavoriteIdFromPrefs(contentId);
        }
        return result;
    }

    private void saveFavoriteIdToPrefs(String contentId) {
        try {
            SharedPreferences preferences = context.getSharedPreferences(FAVORITES_PREFS, Context.MODE_PRIVATE);
            Set<String> favoriteIds = getFavoriteIdsFromPrefs();
            favoriteIds.add(contentId);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putStringSet(KEY_FAVORITE_IDS, favoriteIds);
            editor.apply();

            Log.d(TAG, "ID избранного контента сохранен: " + contentId);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при сохранении ID избранного контента: " + e.getMessage());
        }
    }

    private void removeFavoriteIdFromPrefs(String contentId) {
        try {
            SharedPreferences preferences = context.getSharedPreferences(FAVORITES_PREFS, Context.MODE_PRIVATE);
            Set<String> favoriteIds = getFavoriteIdsFromPrefs();
            favoriteIds.remove(contentId);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putStringSet(KEY_FAVORITE_IDS, favoriteIds);
            editor.apply();

            Log.d(TAG, "ID избранного контента удален: " + contentId);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при удалении ID избранного контента: " + e.getMessage());
        }
    }

    private Set<String> getFavoriteIdsFromPrefs() {
        SharedPreferences preferences = context.getSharedPreferences(FAVORITES_PREFS, Context.MODE_PRIVATE);
        return new HashSet<>(preferences.getStringSet(KEY_FAVORITE_IDS, new HashSet<>()));
    }

    /**
     * Восстановить состояние всех избранных элементов из SharedPreferences
     * @return количество восстановленных элементов
     */
    public int restoreFavoritesState() {
        int restoredCount = 0;
        try {
            ContentRepository contentRepository = new ContentRepository(context);
            Set<String> favoriteIds = getFavoriteIdsFromPrefs();
            Log.d(TAG, "Восстановление избранного, найдено ID: " + favoriteIds.size());

            for (String contentId : favoriteIds) {
                ContentEntity content = contentRepository.getById(contentId);
                if (content != null) {
                    content.setLiked(true);
                    contentRepository.update(content);
                    restoredCount++;
                    Log.d(TAG, "Восстановлено избранное: " + content.getTitle());
                }
            }

            Log.d(TAG, "Восстановлено избранных элементов: " + restoredCount);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при восстановлении избранного: " + e.getMessage());
        }
        return restoredCount;
    }

    // ==================== VIEWED HISTORY MANAGEMENT SECTION ====================

    /**
     * Добавляет элемент в историю просмотренных
     * @param category категория контента
     * @param itemId ID элемента
     * @param isLiked true если элемент понравился, false если нет
     */
    public void addToViewedHistory(String category, String itemId, boolean isLiked) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(VIEWED_HISTORY_PREFS, Context.MODE_PRIVATE);
            String key = PREF_KEY_PREFIX + normalizeKey(category);
            Set<String> viewedItems = getStringSet(prefs, key);

            // Добавляем новый ID в историю
            viewedItems.add(itemId);

            // Ограничиваем размер истории
            if (viewedItems.size() > MAX_HISTORY_ITEMS) {
                Set<String> newSet = new HashSet<>();
                String[] itemsArray = viewedItems.toArray(new String[0]);
                for (int i = itemsArray.length - MAX_HISTORY_ITEMS; i < itemsArray.length; i++) {
                    newSet.add(itemsArray[i]);
                }
                viewedItems = newSet;
            }

            // Сохраняем обновленный набор
            prefs.edit().putStringSet(key, viewedItems).apply();

            // Сохраняем также в историю лайков/дизлайков
            if (isLiked) {
                String likedKey = PREF_KEY_LIKED_PREFIX + normalizeKey(category);
                Set<String> likedItems = getStringSet(prefs, likedKey);
                likedItems.add(itemId);
                prefs.edit().putStringSet(likedKey, likedItems).apply();
            } else {
                String dislikedKey = PREF_KEY_DISLIKED_PREFIX + normalizeKey(category);
                Set<String> dislikedItems = getStringSet(prefs, dislikedKey);
                dislikedItems.add(itemId);
                prefs.edit().putStringSet(dislikedKey, dislikedItems).apply();
            }

            Log.d(TAG, "Добавлен элемент " + itemId + " категории " + category + " в историю просмотров");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при сохранении истории просмотров: " + e.getMessage());
        }
    }

    /**
     * Проверяет, был ли элемент уже просмотрен
     * @param category категория контента
     * @param itemId ID элемента
     * @return true если элемент уже был просмотрен
     */
    public boolean hasBeenViewed(String category, String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return false;
        }
        try {
            SharedPreferences prefs = context.getSharedPreferences(VIEWED_HISTORY_PREFS, Context.MODE_PRIVATE);
            String key = PREF_KEY_PREFIX + normalizeKey(category);
            Set<String> viewedItems = getStringSet(prefs, key);
            return viewedItems.contains(itemId);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при проверке истории просмотров: " + e.getMessage());
            return false;
        }
    }

    /**
     * Получает все просмотренные элементы для указанной категории
     * @param category категория контента
     * @return набор ID просмотренных элементов
     */
    public Set<String> getAllViewedItems(String category) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(VIEWED_HISTORY_PREFS, Context.MODE_PRIVATE);
            String key = PREF_KEY_PREFIX + normalizeKey(category);
            return getStringSet(prefs, key);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении истории просмотров: " + e.getMessage());
            return new HashSet<>();
        }
    }

    /**
     * Получает все понравившиеся элементы для указанной категории
     * @param category категория контента
     * @return набор ID понравившихся элементов
     */
    public Set<String> getLikedItems(String category) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(VIEWED_HISTORY_PREFS, Context.MODE_PRIVATE);
            String key = PREF_KEY_LIKED_PREFIX + normalizeKey(category);
            return getStringSet(prefs, key);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении истории лайков: " + e.getMessage());
            return new HashSet<>();
        }
    }

    /**
     * Получает все непонравившиеся элементы для указанной категории
     * @param category категория контента
     * @return набор ID непонравившихся элементов
     */
    public Set<String> getDislikedItems(String category) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(VIEWED_HISTORY_PREFS, Context.MODE_PRIVATE);
            String key = PREF_KEY_DISLIKED_PREFIX + normalizeKey(category);
            return getStringSet(prefs, key);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении истории дизлайков: " + e.getMessage());
            return new HashSet<>();
        }
    }

    /**
     * Сбрасывает историю просмотров для указанной категории
     * @param category категория контента
     */
    public void clearHistory(String category) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(VIEWED_HISTORY_PREFS, Context.MODE_PRIVATE);
            String viewedKey = PREF_KEY_PREFIX + normalizeKey(category);
            String likedKey = PREF_KEY_LIKED_PREFIX + normalizeKey(category);
            String dislikedKey = PREF_KEY_DISLIKED_PREFIX + normalizeKey(category);

            prefs.edit()
                    .remove(viewedKey)
                    .remove(likedKey)
                    .remove(dislikedKey)
                    .apply();

            Log.d(TAG, "История просмотров для категории " + category + " очищена");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при очистке истории просмотров: " + e.getMessage());
        }
    }

    /**
     * Сбрасывает всю историю просмотров
     */
    public void clearAllHistory() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(VIEWED_HISTORY_PREFS, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
            Log.d(TAG, "Вся история просмотров очищена");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при очистке всей истории просмотров: " + e.getMessage());
        }
    }

    /**
     * Преобразует ключ категории для использования в SharedPreferences
     * @param category категория контента
     * @return нормализованный ключ
     */
    private String normalizeKey(String category) {
        if (category == null) {
            return "unknown";
        }

        return category.toLowerCase()
                .replace(" ", "_")
                .replace(".", "_")
                .replace(",", "_")
                .replace("-", "_");
    }

    /**
     * Получает Set<String> из SharedPreferences с обработкой по умолчанию
     * @param prefs SharedPreferences
     * @param key ключ
     * @return набор строк
     */
    private Set<String> getStringSet(SharedPreferences prefs, String key) {
        Set<String> defaultSet = new HashSet<>();
        Set<String> result = prefs.getStringSet(key, defaultSet);

        // Создаем новую копию для изменяемого набора
        if (result == defaultSet || result.isEmpty()) {
            return new HashSet<>();
        } else {
            return new HashSet<>(result);
        }
    }
}