package com.draker.swipetime.api;

import android.app.Application;
import android.util.Log;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.fragments.CardStackFragmentHelper;
import com.draker.swipetime.repository.AnimeRepository;
import com.draker.swipetime.repository.BookRepository;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.GameRepository;
import com.draker.swipetime.repository.MovieRepository;
import com.draker.swipetime.repository.TVShowRepository;
import com.draker.swipetime.utils.ContentShuffler;

import java.util.List;

/**
 * Менеджер интеграции внешних API в приложение
 */
public class ApiIntegrationManager {
    private static final String TAG = "ApiIntegrationManager";
    private static ApiIntegrationManager instance;
    
    private final Application application;
    private final ApiManager apiManager;
    private final MovieRepository movieRepository;
    private final TVShowRepository tvShowRepository;
    private final GameRepository gameRepository;
    private final BookRepository bookRepository;
    private final AnimeRepository animeRepository;
    private final ContentRepository contentRepository;
    
    // Добавляем ApiIntegrationHelper
    private final ApiIntegrationHelper apiIntegrationHelper;
    
    /**
     * Интерфейс для обратного вызова при инициализации API
     */
    public interface ApiInitCallback {
        void onComplete(boolean success);
        void onError(String errorMessage);
    }
    
    /**
     * Получить экземпляр менеджера интеграции API
     * @param application Application
     * @return экземпляр ApiIntegrationManager
     */
    public static synchronized ApiIntegrationManager getInstance(Application application) {
        if (instance == null) {
            instance = new ApiIntegrationManager(application);
        }
        return instance;
    }
    
    /**
     * Конструктор
     * @param application Application
     */
    private ApiIntegrationManager(Application application) {
        this.application = application;
        this.apiManager = new ApiManager(application);
        this.movieRepository = new MovieRepository(application);
        this.tvShowRepository = new TVShowRepository(application);
        this.gameRepository = new GameRepository(application);
        this.bookRepository = new BookRepository(application);
        this.animeRepository = new AnimeRepository(application);
        this.contentRepository = new ContentRepository(application);
        
        // Инициализируем ApiIntegrationHelper
        this.apiIntegrationHelper = new ApiIntegrationHelper(application);
    }
    
    /**
     * Инициализировать интеграцию внешних API
     * @param callback обратный вызов по завершении инициализации
     */
    public void initializeApiIntegration(ApiInitCallback callback) {
        Log.d(TAG, "Начало инициализации интеграции API");
        
        // Сбрасываем все кеши API и перемешивания
        apiManager.resetAllCaches();
        ContentShuffler.resetAllHistory();
        
        // Проверяем, есть ли уже данные в базе данных
        boolean hasMovies = movieRepository.getCount() >= 5;
        boolean hasTVShows = tvShowRepository.getCount() >= 5;
        boolean hasGames = gameRepository.getCount() >= 5;
        boolean hasBooks = bookRepository.getCount() >= 5;
        boolean hasAnime = animeRepository.getCount() >= 5;
        
        Log.d(TAG, "Данные в базе: фильмы=" + movieRepository.getCount() + 
                ", сериалы=" + tvShowRepository.getCount() + 
                ", игры=" + gameRepository.getCount() +
                ", книги=" + bookRepository.getCount() +
                ", аниме=" + animeRepository.getCount());
        
        // Загружаем много страниц контента для всех категорий
        loadMultiplePagesForAllCategories(callback);
    }
    
    /**
     * Загрузить множество страниц контента для всех категорий
     * @param callback обратный вызов по завершении
     */
    private void loadMultiplePagesForAllCategories(ApiInitCallback callback) {
        final int PAGES_PER_CATEGORY = 5; // Загружаем по 5 страниц для каждой категории
        final AtomicInteger loadedCategories = new AtomicInteger(0);
        
        // Запускаем глубокую загрузку для каждой категории
        loadMultiplePagesForMovies(PAGES_PER_CATEGORY, new ApiInitCallback() {
            @Override
            public void onComplete(boolean success) {
                if (loadedCategories.incrementAndGet() == 5) {
                    callback.onComplete(true);
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Ошибка при загрузке фильмов: " + errorMessage);
                if (loadedCategories.incrementAndGet() == 5) {
                    callback.onComplete(true);
                }
            }
        });
        
        loadMultiplePagesForTVShows(PAGES_PER_CATEGORY, new ApiInitCallback() {
            @Override
            public void onComplete(boolean success) {
                if (loadedCategories.incrementAndGet() == 5) {
                    callback.onComplete(true);
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Ошибка при загрузке сериалов: " + errorMessage);
                if (loadedCategories.incrementAndGet() == 5) {
                    callback.onComplete(true);
                }
            }
        });
        
        loadMultiplePagesForGames(PAGES_PER_CATEGORY, new ApiInitCallback() {
            @Override
            public void onComplete(boolean success) {
                if (loadedCategories.incrementAndGet() == 5) {
                    callback.onComplete(true);
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Ошибка при загрузке игр: " + errorMessage);
                if (loadedCategories.incrementAndGet() == 5) {
                    callback.onComplete(true);
                }
            }
        });
        
        loadMultiplePagesForBooks(PAGES_PER_CATEGORY, new ApiInitCallback() {
            @Override
            public void onComplete(boolean success) {
                if (loadedCategories.incrementAndGet() == 5) {
                    callback.onComplete(true);
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Ошибка при загрузке книг: " + errorMessage);
                if (loadedCategories.incrementAndGet() == 5) {
                    callback.onComplete(true);
                }
            }
        });
        
        loadMultiplePagesForAnime(PAGES_PER_CATEGORY, new ApiInitCallback() {
            @Override
            public void onComplete(boolean success) {
                if (loadedCategories.incrementAndGet() == 5) {
                    callback.onComplete(true);
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Ошибка при загрузке аниме: " + errorMessage);
                if (loadedCategories.incrementAndGet() == 5) {
                    callback.onComplete(true);
                }
            }
        });
    }
    
    /**
     * Загрузить множество страниц фильмов
     * @param pagesCount количество страниц
     * @param callback обратный вызов
     */
    private void loadMultiplePagesForMovies(int pagesCount, ApiInitCallback callback) {
        final AtomicInteger loadedPages = new AtomicInteger(0);
        
        for (int page = 1; page <= pagesCount; page++) {
            final int currentPage = page;
            apiManager.loadPopularMovies(currentPage, new ApiManager.ApiCallback<MovieEntity>() {
                @Override
                public void onSuccess(List<MovieEntity> data) {
                    Log.d(TAG, "Загружено " + data.size() + " фильмов (страница " + currentPage + ")");
                    
                    // Получаем и загружаем также похожие фильмы для некоторых популярных
                    if (data.size() > 0 && currentPage <= 2) {
                        // Загружаем похожие для первых 5 фильмов из первых двух страниц
                        for (int i = 0; i < Math.min(data.size(), 5); i++) {
                            final MovieEntity movie = data.get(i);
                            loadSimilarMovies(movie.getId());
                        }
                    }
                    
                    if (loadedPages.incrementAndGet() == pagesCount) {
                        callback.onComplete(true);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    Log.e(TAG, "Ошибка загрузки фильмов (страница " + currentPage + "): " + error.getMessage());
                    if (loadedPages.incrementAndGet() == pagesCount) {
                        callback.onComplete(false);
                    }
                }
            });
        }
    }
    
    /**
     * Загрузить похожие фильмы
     * @param movieId ID фильма
     */
    private void loadSimilarMovies(String movieId) {
        // Загружаем похожие фильмы через TMDb API
        // Это зависит от реализации TMDb репозитория
        // Примерная реализация:
        /*
        apiManager.loadSimilarMovies(movieId, new ApiManager.ApiCallback<MovieEntity>() {
            @Override
            public void onSuccess(List<MovieEntity> data) {
                Log.d(TAG, "Загружено " + data.size() + " похожих фильмов для ID " + movieId);
            }

            @Override
            public void onError(Throwable error) {
                Log.e(TAG, "Ошибка загрузки похожих фильмов: " + error.getMessage());
            }
        });
        */
        
        // На данный момент эта функция пустая, но может быть реализована позже
    }
    
    /**
     * Загрузить множество страниц сериалов
     * @param pagesCount количество страниц
     * @param callback обратный вызов
     */
    private void loadMultiplePagesForTVShows(int pagesCount, ApiInitCallback callback) {
        final AtomicInteger loadedPages = new AtomicInteger(0);
        
        for (int page = 1; page <= pagesCount; page++) {
            final int currentPage = page;
            apiManager.loadPopularTVShows(currentPage, new ApiManager.ApiCallback<TVShowEntity>() {
                @Override
                public void onSuccess(List<TVShowEntity> data) {
                    Log.d(TAG, "Загружено " + data.size() + " сериалов (страница " + currentPage + ")");
                    
                    // Также загружаем сериалы по категориям для разнообразия
                    if (currentPage == 1) {
                        loadTVShowsByCategories();
                    }
                    
                    if (loadedPages.incrementAndGet() == pagesCount) {
                        callback.onComplete(true);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    Log.e(TAG, "Ошибка загрузки сериалов (страница " + currentPage + "): " + error.getMessage());
                    if (loadedPages.incrementAndGet() == pagesCount) {
                        callback.onComplete(false);
                    }
                }
            });
        }
    }
    
    /**
     * Загрузить сериалы по категориям
     */
    private void loadTVShowsByCategories() {
        // Здесь можно загрузить сериалы по разным категориям/жанрам
        // Зависит от реализации TMDb репозитория
        // На данный момент эта функция пустая, но может быть реализована позже
    }
    
    /**
     * Загрузить множество страниц игр
     * @param pagesCount количество страниц
     * @param callback обратный вызов
     */
    private void loadMultiplePagesForGames(int pagesCount, ApiInitCallback callback) {
        final AtomicInteger loadedPages = new AtomicInteger(0);
        
        for (int page = 1; page <= pagesCount; page++) {
            final int currentPage = page;
            apiManager.loadPopularGames(currentPage, new ApiManager.ApiCallback<GameEntity>() {
                @Override
                public void onSuccess(List<GameEntity> data) {
                    Log.d(TAG, "Загружено " + data.size() + " игр (страница " + currentPage + ")");
                    if (loadedPages.incrementAndGet() == pagesCount) {
                        callback.onComplete(true);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    Log.e(TAG, "Ошибка загрузки игр (страница " + currentPage + "): " + error.getMessage());
                    if (loadedPages.incrementAndGet() == pagesCount) {
                        callback.onComplete(false);
                    }
                }
            });
        }
    }
    
    /**
     * Загрузить множество страниц книг
     * @param pagesCount количество страниц
     * @param callback обратный вызов
     */
    private void loadMultiplePagesForBooks(int pagesCount, ApiInitCallback callback) {
        final AtomicInteger loadedPages = new AtomicInteger(0);
        
        // Для книг используем разные поисковые запросы для получения разнообразного контента
        String[] queries = {"fantasy", "science", "history", "novel", "classic", 
                           "biography", "adventure", "mystery", "romance", 
                           "thriller", "horror", "fiction", "nonfiction", 
                           "crime", "poetry", "psychology", "philosophy"};
        
        // Загружаем по разным запросам
        for (int i = 0; i < Math.min(queries.length, pagesCount); i++) {
            final int queryIndex = i;
            apiManager.searchBooks(queries[i], 1, new ApiManager.ApiCallback<BookEntity>() {
                @Override
                public void onSuccess(List<BookEntity> data) {
                    Log.d(TAG, "Загружено " + data.size() + " книг (запрос: " + queries[queryIndex] + ")");
                    if (loadedPages.incrementAndGet() == pagesCount) {
                        callback.onComplete(true);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    Log.e(TAG, "Ошибка загрузки книг (запрос: " + queries[queryIndex] + "): " + error.getMessage());
                    if (loadedPages.incrementAndGet() == pagesCount) {
                        callback.onComplete(false);
                    }
                }
            });
        }
        
        // Догружаем оставшиеся страницы поиском по популярности
        for (int i = queries.length; i < pagesCount; i++) {
            final int page = i - queries.length + 1;
            apiManager.searchBooks("", page, new ApiManager.ApiCallback<BookEntity>() {
                @Override
                public void onSuccess(List<BookEntity> data) {
                    Log.d(TAG, "Загружено " + data.size() + " популярных книг (страница " + page + ")");
                    if (loadedPages.incrementAndGet() == pagesCount) {
                        callback.onComplete(true);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    Log.e(TAG, "Ошибка загрузки популярных книг (страница " + page + "): " + error.getMessage());
                    if (loadedPages.incrementAndGet() == pagesCount) {
                        callback.onComplete(false);
                    }
                }
            });
        }
    }
    
    /**
     * Загрузить множество страниц аниме
     * @param pagesCount количество страниц
     * @param callback обратный вызов
     */
    private void loadMultiplePagesForAnime(int pagesCount, ApiInitCallback callback) {
        final AtomicInteger loadedPages = new AtomicInteger(0);
        
        for (int page = 1; page <= pagesCount; page++) {
            final int currentPage = page;
            apiManager.loadTopAnime(currentPage, new ApiManager.ApiCallback<AnimeEntity>() {
                @Override
                public void onSuccess(List<AnimeEntity> data) {
                    Log.d(TAG, "Загружено " + data.size() + " аниме (страница " + currentPage + ")");
                    if (loadedPages.incrementAndGet() == pagesCount) {
                        callback.onComplete(true);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    Log.e(TAG, "Ошибка загрузки аниме (страница " + currentPage + "): " + error.getMessage());
                    if (loadedPages.incrementAndGet() == pagesCount) {
                        callback.onComplete(false);
                    }
                }
            });
        }
    }
    
    /**
     * Загрузить данные для указанной категории
     * @param categoryName название категории
     * @param skipIfLoaded пропустить загрузку, если данные уже загружены
     * @param callback обратный вызов по завершении загрузки
     */
    private void loadApiDataForCategory(String categoryName, boolean skipIfLoaded, CardStackFragmentHelper.ApiLoadCallback callback) {
        if (skipIfLoaded) {
            Log.d(TAG, "Пропуск загрузки данных для категории " + categoryName + ", т.к. данные уже загружены");
            callback.onComplete(true);
            return;
        }
        
        Log.d(TAG, "Начало загрузки данных для категории " + categoryName);
        
        CardStackFragmentHelper.loadApiDataForCategory(
                categoryName,
                apiManager,
                movieRepository,
                tvShowRepository,
                gameRepository,
                bookRepository,
                animeRepository,
                callback
        );
    }
    
    /**
     * Обновить данные для указанной категории
     * @param categoryName название категории
     * @param itemsCount количество элементов для загрузки
     * @param callback обратный вызов по завершении обновления
     */
    public void refreshCategoryContent(String categoryName, int itemsCount, ApiInitCallback callback) {
        Log.d(TAG, "Обновление контента для категории: " + categoryName);
        
        // Определяем тип API в зависимости от категории
        String apiCategory;
        switch (categoryName.toLowerCase()) {
            case "фильмы":
                apiCategory = "movie";
                break;
            case "сериалы":
                apiCategory = "tv_show";
                break;
            case "игры":
                apiCategory = "game";
                break;
            case "книги":
                apiCategory = "book";
                break;
            case "аниме":
                apiCategory = "anime";
                break;
            default:
                apiCategory = "movie"; // По умолчанию используем фильмы
                break;
        }
        
        // Сбрасываем кеш для выбранной категории
        apiManager.resetCategoryCache(apiCategory);
        
        // Загружаем свежие данные для конкретной категории
        switch (apiCategory) {
            case "movie":
                loadMoviesData(itemsCount, callback);
                break;
            case "tv_show":
                loadTVShowsData(itemsCount, callback);
                break;
            case "game":
                loadGamesData(itemsCount, callback);
                break;
            case "book":
                loadBooksData(itemsCount, callback);
                break;
            case "anime":
                loadAnimeData(itemsCount, callback);
                break;
            default:
                callback.onError("Неизвестная категория: " + categoryName);
                break;
        }
    }
    
    /**
     * Загрузить данные о фильмах
     * @param itemsCount количество элементов для загрузки
     * @param callback обратный вызов по завершении загрузки
     */
    private void loadMoviesData(int itemsCount, ApiInitCallback callback) {
        for (int page = 1; page <= 2; page++) {
            final int currentPage = page;
            apiManager.loadPopularMovies(currentPage, new ApiManager.ApiCallback<MovieEntity>() {
                @Override
                public void onSuccess(List<MovieEntity> data) {
                    Log.d(TAG, "Загружено " + data.size() + " фильмов (страница " + currentPage + ")");
                    if (currentPage == 2) {
                        callback.onComplete(true);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    Log.e(TAG, "Ошибка загрузки фильмов: " + error.getMessage());
                    callback.onError(error.getMessage());
                }
            });
        }
    }
    
    /**
     * Загрузить данные о сериалах
     * @param itemsCount количество элементов для загрузки
     * @param callback обратный вызов по завершении загрузки
     */
    private void loadTVShowsData(int itemsCount, ApiInitCallback callback) {
        for (int page = 1; page <= 2; page++) {
            final int currentPage = page;
            apiManager.loadPopularTVShows(currentPage, new ApiManager.ApiCallback<TVShowEntity>() {
                @Override
                public void onSuccess(List<TVShowEntity> data) {
                    Log.d(TAG, "Загружено " + data.size() + " сериалов (страница " + currentPage + ")");
                    if (currentPage == 2) {
                        callback.onComplete(true);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    Log.e(TAG, "Ошибка загрузки сериалов: " + error.getMessage());
                    callback.onError(error.getMessage());
                }
            });
        }
    }
    
    /**
     * Загрузить данные об играх
     * @param itemsCount количество элементов для загрузки
     * @param callback обратный вызов по завершении загрузки
     */
    private void loadGamesData(int itemsCount, ApiInitCallback callback) {
        for (int page = 1; page <= 2; page++) {
            final int currentPage = page;
            apiManager.loadPopularGames(currentPage, new ApiManager.ApiCallback<GameEntity>() {
                @Override
                public void onSuccess(List<GameEntity> data) {
                    Log.d(TAG, "Загружено " + data.size() + " игр (страница " + currentPage + ")");
                    if (currentPage == 2) {
                        callback.onComplete(true);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    Log.e(TAG, "Ошибка загрузки игр: " + error.getMessage());
                    callback.onError(error.getMessage());
                }
            });
        }
    }
    
    /**
     * Загрузить данные о книгах
     * @param itemsCount количество элементов для загрузки
     * @param callback обратный вызов по завершении загрузки
     */
    private void loadBooksData(int itemsCount, ApiInitCallback callback) {
        // Для книг используем разные поисковые запросы для получения разнообразного контента
        String[] queries = {"fantasy", "science", "history", "novel", "classic"};
        
        for (int i = 0; i < Math.min(queries.length, 2); i++) {
            final int index = i;
            apiManager.searchBooks(queries[i], 1, new ApiManager.ApiCallback<BookEntity>() {
                @Override
                public void onSuccess(List<BookEntity> data) {
                    Log.d(TAG, "Загружено " + data.size() + " книг (запрос: " + queries[index] + ")");
                    if (index == Math.min(queries.length, 2) - 1) {
                        callback.onComplete(true);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    Log.e(TAG, "Ошибка загрузки книг: " + error.getMessage());
                    callback.onError(error.getMessage());
                }
            });
        }
    }
    
    /**
     * Загрузить данные об аниме
     * @param itemsCount количество элементов для загрузки
     * @param callback обратный вызов по завершении загрузки
     */
    private void loadAnimeData(int itemsCount, ApiInitCallback callback) {
        for (int page = 1; page <= 2; page++) {
            final int currentPage = page;
            apiManager.loadTopAnime(currentPage, new ApiManager.ApiCallback<AnimeEntity>() {
                @Override
                public void onSuccess(List<AnimeEntity> data) {
                    Log.d(TAG, "Загружено " + data.size() + " аниме (страница " + currentPage + ")");
                    if (currentPage == 2) {
                        callback.onComplete(true);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    Log.e(TAG, "Ошибка загрузки аниме: " + error.getMessage());
                    callback.onError(error.getMessage());
                }
            });
        }
    }
    
    /**
     * Обновить все категории контента новыми данными
     * @param itemsPerCategory количество элементов, которое нужно получить для каждой категории
     * @param callback обратный вызов по завершении обновления
     */
    public void refreshAllContentTypes(int itemsPerCategory, ApiInitCallback callback) {
        Log.d(TAG, "Обновление всех категорий контента");
        
        // Сбрасываем кеши API перед обновлением данных
        apiManager.resetAllCaches();
        
        // Используем ApiIntegrationHelper для загрузки всех типов контента
        apiIntegrationHelper.loadAllContentTypes(itemsPerCategory, new ApiIntegrationHelper.LoadCallback() {
            @Override
            public void onComplete(boolean success, String status) {
                if (success) {
                    Log.d(TAG, "Успешно обновлены все типы контента: " + status);
                    callback.onComplete(true);
                } else {
                    Log.e(TAG, "Ошибка при обновлении контента: " + status);
                    callback.onError(status);
                }
            }
        });
    }
    
    /**
     * Очистить ресурсы
     */
    public void clear() {
        apiIntegrationHelper.clear();
    }
}