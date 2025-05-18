package com.draker.swipetime.api;

import android.app.Application;
import android.util.Log;

import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.repository.AnimeRepository;
import com.draker.swipetime.repository.BookRepository;
import com.draker.swipetime.repository.GameRepository;
import com.draker.swipetime.repository.MovieRepository;
import com.draker.swipetime.repository.TVShowRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Вспомогательный класс для упрощения интеграции с внешними API и устранения повторений
 */
public class ApiIntegrationHelper {
    private static final String TAG = "ApiIntegrationHelper";
    
    private final Application application;
    private final ApiManager apiManager;
    private final MovieRepository movieRepository;
    private final TVShowRepository tvShowRepository;
    private final GameRepository gameRepository;
    private final BookRepository bookRepository;
    private final AnimeRepository animeRepository;
    
    // Для отслеживания уже загруженных элементов
    private final Set<String> loadedMovieIds = new HashSet<>();
    private final Set<String> loadedTVShowIds = new HashSet<>();
    private final Set<String> loadedGameIds = new HashSet<>();
    private final Set<String> loadedBookIds = new HashSet<>();
    private final Set<String> loadedAnimeIds = new HashSet<>();
    
    /**
     * Интерфейс для обратного вызова после загрузки всех данных
     */
    public interface LoadCallback {
        void onComplete(boolean success, String status);
    }
    
    /**
     * Конструктор
     * @param application Application
     */
    public ApiIntegrationHelper(Application application) {
        this.application = application;
        this.apiManager = new ApiManager(application);
        
        // Инициализация репозиториев
        movieRepository = new MovieRepository(application);
        tvShowRepository = new TVShowRepository(application);
        gameRepository = new GameRepository(application);
        bookRepository = new BookRepository(application);
        animeRepository = new AnimeRepository(application);
    }
    
    /**
     * Загрузить все типы контента
     * @param itemsPerType количество элементов каждого типа
     * @param callback обратный вызов после загрузки
     */
    public void loadAllContentTypes(int itemsPerType, LoadCallback callback) {
        // Начальные страницы для загрузки
        int moviePage = 1;
        int tvShowPage = 1;
        int gamePage = 1;
        int bookPage = 1;
        int animePage = 1;
        
        // Счетчик завершенных загрузок
        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicBoolean hasError = new AtomicBoolean(false);
        
        // Очищаем предыдущие загрузки
        loadedMovieIds.clear();
        loadedTVShowIds.clear();
        loadedGameIds.clear();
        loadedBookIds.clear();
        loadedAnimeIds.clear();
        
        // Загружаем фильмы
        loadMoviesRecursively(moviePage, itemsPerType, new ArrayList<>(), new ApiHelper.LoadTypeCallback() {
            @Override
            public void onComplete(boolean success, List<? extends ContentEntity> items) {
                if (hasError.get()) return;
                
                if (!success) {
                    hasError.set(true);
                    callback.onComplete(false, "Ошибка загрузки фильмов");
                    return;
                }
                
                Log.d(TAG, "Загрузка фильмов завершена, получено " + items.size() + " элементов");
                
                // Увеличиваем счетчик завершенных загрузок
                if (completedCount.incrementAndGet() == 5) {
                    callback.onComplete(true, "Все типы контента загружены успешно");
                }
            }
        });
        
        // Загружаем сериалы
        loadTVShowsRecursively(tvShowPage, itemsPerType, new ArrayList<>(), new ApiHelper.LoadTypeCallback() {
            @Override
            public void onComplete(boolean success, List<? extends ContentEntity> items) {
                if (hasError.get()) return;
                
                if (!success) {
                    hasError.set(true);
                    callback.onComplete(false, "Ошибка загрузки сериалов");
                    return;
                }
                
                Log.d(TAG, "Загрузка сериалов завершена, получено " + items.size() + " элементов");
                
                // Увеличиваем счетчик завершенных загрузок
                if (completedCount.incrementAndGet() == 5) {
                    callback.onComplete(true, "Все типы контента загружены успешно");
                }
            }
        });
        
        // Загружаем игры
        loadGamesRecursively(gamePage, itemsPerType, new ArrayList<>(), new ApiHelper.LoadTypeCallback() {
            @Override
            public void onComplete(boolean success, List<? extends ContentEntity> items) {
                if (hasError.get()) return;
                
                if (!success) {
                    hasError.set(true);
                    callback.onComplete(false, "Ошибка загрузки игр");
                    return;
                }
                
                Log.d(TAG, "Загрузка игр завершена, получено " + items.size() + " элементов");
                
                // Увеличиваем счетчик завершенных загрузок
                if (completedCount.incrementAndGet() == 5) {
                    callback.onComplete(true, "Все типы контента загружены успешно");
                }
            }
        });
        
        // Загружаем книги
        loadBooksRecursively(bookPage, itemsPerType, new ArrayList<>(), new ApiHelper.LoadTypeCallback() {
            @Override
            public void onComplete(boolean success, List<? extends ContentEntity> items) {
                if (hasError.get()) return;
                
                if (!success) {
                    hasError.set(true);
                    callback.onComplete(false, "Ошибка загрузки книг");
                    return;
                }
                
                Log.d(TAG, "Загрузка книг завершена, получено " + items.size() + " элементов");
                
                // Увеличиваем счетчик завершенных загрузок
                if (completedCount.incrementAndGet() == 5) {
                    callback.onComplete(true, "Все типы контента загружены успешно");
                }
            }
        });
        
        // Загружаем аниме
        loadAnimeRecursively(animePage, itemsPerType, new ArrayList<>(), new ApiHelper.LoadTypeCallback() {
            @Override
            public void onComplete(boolean success, List<? extends ContentEntity> items) {
                if (hasError.get()) return;
                
                if (!success) {
                    hasError.set(true);
                    callback.onComplete(false, "Ошибка загрузки аниме");
                    return;
                }
                
                Log.d(TAG, "Загрузка аниме завершена, получено " + items.size() + " элементов");
                
                // Увеличиваем счетчик завершенных загрузок
                if (completedCount.incrementAndGet() == 5) {
                    callback.onComplete(true, "Все типы контента загружены успешно");
                }
            }
        });
    }
    
    /**
     * Рекурсивно загружает фильмы до достижения нужного количества
     * @param page текущая страница
     * @param targetCount целевое количество
     * @param collectedItems уже собранные элементы
     * @param callback обратный вызов после загрузки
     */
    private void loadMoviesRecursively(int page, int targetCount, List<MovieEntity> collectedItems, ApiHelper.LoadTypeCallback callback) {
        // Ограничение по количеству страниц для предотвращения бесконечной рекурсии
        if (page > 10) {
            callback.onComplete(true, collectedItems);
            return;
        }
        
        // Загружаем текущую страницу
        apiManager.loadPopularMovies(page, new ApiManager.ApiCallback<MovieEntity>() {
            @Override
            public void onSuccess(List<MovieEntity> data) {
                // Фильтруем уже загруженные элементы
                List<MovieEntity> uniqueItems = new ArrayList<>();
                for (MovieEntity item : data) {
                    if (!loadedMovieIds.contains(item.getId())) {
                        uniqueItems.add(item);
                        loadedMovieIds.add(item.getId());
                    }
                }
                
                // Добавляем уникальные элементы к уже собранным
                collectedItems.addAll(uniqueItems);
                
                // Проверяем, достигли ли мы целевого количества
                if (collectedItems.size() >= targetCount || uniqueItems.isEmpty()) {
                    // Если да, вызываем колбэк и завершаем рекурсию
                    callback.onComplete(true, collectedItems);
                } else {
                    // Если нет, загружаем следующую страницу
                    loadMoviesRecursively(page + 1, targetCount, collectedItems, callback);
                }
            }

            @Override
            public void onError(Throwable error) {
                Log.e(TAG, "Error loading movies page " + page + ": " + error.getMessage());
                
                // Если произошла ошибка, возвращаем то, что успели загрузить
                callback.onComplete(false, collectedItems);
            }
        });
    }
    
    /**
     * Рекурсивно загружает сериалы до достижения нужного количества
     * @param page текущая страница
     * @param targetCount целевое количество
     * @param collectedItems уже собранные элементы
     * @param callback обратный вызов после загрузки
     */
    private void loadTVShowsRecursively(int page, int targetCount, List<TVShowEntity> collectedItems, ApiHelper.LoadTypeCallback callback) {
        // Ограничение по количеству страниц для предотвращения бесконечной рекурсии
        if (page > 10) {
            callback.onComplete(true, collectedItems);
            return;
        }
        
        // Загружаем текущую страницу
        apiManager.loadPopularTVShows(page, new ApiManager.ApiCallback<TVShowEntity>() {
            @Override
            public void onSuccess(List<TVShowEntity> data) {
                // Фильтруем уже загруженные элементы
                List<TVShowEntity> uniqueItems = new ArrayList<>();
                for (TVShowEntity item : data) {
                    if (!loadedTVShowIds.contains(item.getId())) {
                        uniqueItems.add(item);
                        loadedTVShowIds.add(item.getId());
                    }
                }
                
                // Добавляем уникальные элементы к уже собранным
                collectedItems.addAll(uniqueItems);
                
                // Проверяем, достигли ли мы целевого количества
                if (collectedItems.size() >= targetCount || uniqueItems.isEmpty()) {
                    // Если да, вызываем колбэк и завершаем рекурсию
                    callback.onComplete(true, collectedItems);
                } else {
                    // Если нет, загружаем следующую страницу
                    loadTVShowsRecursively(page + 1, targetCount, collectedItems, callback);
                }
            }

            @Override
            public void onError(Throwable error) {
                Log.e(TAG, "Error loading TV shows page " + page + ": " + error.getMessage());
                
                // Если произошла ошибка, возвращаем то, что успели загрузить
                callback.onComplete(false, collectedItems);
            }
        });
    }
    
    /**
     * Рекурсивно загружает игры до достижения нужного количества
     * @param page текущая страница
     * @param targetCount целевое количество
     * @param collectedItems уже собранные элементы
     * @param callback обратный вызов после загрузки
     */
    private void loadGamesRecursively(int page, int targetCount, List<GameEntity> collectedItems, ApiHelper.LoadTypeCallback callback) {
        // Ограничение по количеству страниц для предотвращения бесконечной рекурсии
        if (page > 10) {
            callback.onComplete(true, collectedItems);
            return;
        }
        
        // Загружаем текущую страницу
        apiManager.loadPopularGames(page, new ApiManager.ApiCallback<GameEntity>() {
            @Override
            public void onSuccess(List<GameEntity> data) {
                // Фильтруем уже загруженные элементы
                List<GameEntity> uniqueItems = new ArrayList<>();
                for (GameEntity item : data) {
                    if (!loadedGameIds.contains(item.getId())) {
                        uniqueItems.add(item);
                        loadedGameIds.add(item.getId());
                    }
                }
                
                // Добавляем уникальные элементы к уже собранным
                collectedItems.addAll(uniqueItems);
                
                // Проверяем, достигли ли мы целевого количества
                if (collectedItems.size() >= targetCount || uniqueItems.isEmpty()) {
                    // Если да, вызываем колбэк и завершаем рекурсию
                    callback.onComplete(true, collectedItems);
                } else {
                    // Если нет, загружаем следующую страницу
                    loadGamesRecursively(page + 1, targetCount, collectedItems, callback);
                }
            }

            @Override
            public void onError(Throwable error) {
                Log.e(TAG, "Error loading games page " + page + ": " + error.getMessage());
                
                // Если произошла ошибка, возвращаем то, что успели загрузить
                callback.onComplete(false, collectedItems);
            }
        });
    }
    
    /**
     * Рекурсивно загружает книги до достижения нужного количества
     * @param page текущая страница
     * @param targetCount целевое количество
     * @param collectedItems уже собранные элементы
     * @param callback обратный вызов после загрузки
     */
    private void loadBooksRecursively(int page, int targetCount, List<BookEntity> collectedItems, ApiHelper.LoadTypeCallback callback) {
        // Ограничение по количеству страниц для предотвращения бесконечной рекурсии
        if (page > 10) {
            callback.onComplete(true, collectedItems);
            return;
        }
        
        // Загружаем текущую страницу
        apiManager.searchBooks("", page, new ApiManager.ApiCallback<BookEntity>() {
            @Override
            public void onSuccess(List<BookEntity> data) {
                // Фильтруем уже загруженные элементы
                List<BookEntity> uniqueItems = new ArrayList<>();
                for (BookEntity item : data) {
                    if (!loadedBookIds.contains(item.getId())) {
                        uniqueItems.add(item);
                        loadedBookIds.add(item.getId());
                    }
                }
                
                // Добавляем уникальные элементы к уже собранным
                collectedItems.addAll(uniqueItems);
                
                // Проверяем, достигли ли мы целевого количества
                if (collectedItems.size() >= targetCount || uniqueItems.isEmpty()) {
                    // Если да, вызываем колбэк и завершаем рекурсию
                    callback.onComplete(true, collectedItems);
                } else {
                    // Если нет, загружаем следующую страницу
                    loadBooksRecursively(page + 1, targetCount, collectedItems, callback);
                }
            }

            @Override
            public void onError(Throwable error) {
                Log.e(TAG, "Error loading books page " + page + ": " + error.getMessage());
                
                // Если произошла ошибка, возвращаем то, что успели загрузить
                callback.onComplete(false, collectedItems);
            }
        });
    }
    
    /**
     * Рекурсивно загружает аниме до достижения нужного количества
     * @param page текущая страница
     * @param targetCount целевое количество
     * @param collectedItems уже собранные элементы
     * @param callback обратный вызов после загрузки
     */
    private void loadAnimeRecursively(int page, int targetCount, List<AnimeEntity> collectedItems, ApiHelper.LoadTypeCallback callback) {
        // Ограничение по количеству страниц для предотвращения бесконечной рекурсии
        if (page > 10) {
            callback.onComplete(true, collectedItems);
            return;
        }
        
        // Загружаем текущую страницу
        apiManager.loadTopAnime(page, new ApiManager.ApiCallback<AnimeEntity>() {
            @Override
            public void onSuccess(List<AnimeEntity> data) {
                // Фильтруем уже загруженные элементы
                List<AnimeEntity> uniqueItems = new ArrayList<>();
                for (AnimeEntity item : data) {
                    if (!loadedAnimeIds.contains(item.getId())) {
                        uniqueItems.add(item);
                        loadedAnimeIds.add(item.getId());
                    }
                }
                
                // Добавляем уникальные элементы к уже собранным
                collectedItems.addAll(uniqueItems);
                
                // Проверяем, достигли ли мы целевого количества
                if (collectedItems.size() >= targetCount || uniqueItems.isEmpty()) {
                    // Если да, вызываем колбэк и завершаем рекурсию
                    callback.onComplete(true, collectedItems);
                } else {
                    // Если нет, загружаем следующую страницу
                    loadAnimeRecursively(page + 1, targetCount, collectedItems, callback);
                }
            }

            @Override
            public void onError(Throwable error) {
                Log.e(TAG, "Error loading anime page " + page + ": " + error.getMessage());
                
                // Если произошла ошибка, возвращаем то, что успели загрузить
                callback.onComplete(false, collectedItems);
            }
        });
    }
    
    /**
     * Очистить все ресурсы
     */
    public void clear() {
        apiManager.clear();
    }
    
    /**
     * Вложенный статический класс ApiHelper для вспомогательных функций
     */
    public static class ApiHelper {
        /**
         * Интерфейс для обратного вызова после загрузки одного типа контента
         */
        public interface LoadTypeCallback {
            void onComplete(boolean success, List<? extends ContentEntity> items);
        }
    }
}