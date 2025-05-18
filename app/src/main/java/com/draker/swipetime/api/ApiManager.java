package com.draker.swipetime.api;

import android.app.Application;
import android.util.Log;

import com.draker.swipetime.api.repositories.GoogleBooksRepository;
import com.draker.swipetime.api.repositories.JikanRepository;
import com.draker.swipetime.api.repositories.RawgRepository;
import com.draker.swipetime.api.repositories.TMDbRepository;
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
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Менеджер для работы с внешними API
 */
public class ApiManager {
    private static final String TAG = "ApiManager";

    private final TMDbRepository tmdbRepository;
    private final RawgRepository rawgRepository;
    private final GoogleBooksRepository googleBooksRepository;
    private final JikanRepository jikanRepository;

    private final MovieRepository movieRepository;
    private final TVShowRepository tvShowRepository;
    private final GameRepository gameRepository;
    private final BookRepository bookRepository;
    private final AnimeRepository animeRepository;

    private final CompositeDisposable disposables = new CompositeDisposable();
    
    // Добавляем ApiDataManager для управления уникальными данными
    private final ApiDataManager apiDataManager;

    /**
     * Интерфейс для обратного вызова после загрузки данных
     */
    public interface ApiCallback<T> {
        void onSuccess(List<T> data);
        void onError(Throwable error);
    }

    /**
     * Конструктор
     * @param application Application
     */
    public ApiManager(Application application) {
        tmdbRepository = new TMDbRepository();
        rawgRepository = new RawgRepository();
        googleBooksRepository = new GoogleBooksRepository();
        jikanRepository = new JikanRepository();

        movieRepository = new MovieRepository(application);
        tvShowRepository = new TVShowRepository(application);
        gameRepository = new GameRepository(application);
        bookRepository = new BookRepository(application);
        animeRepository = new AnimeRepository(application);
        
        // Инициализируем ApiDataManager
        apiDataManager = ApiDataManager.getInstance();
    }

    /**
     * Загрузить популярные фильмы
     * @param page номер страницы
     * @param callback обратный вызов с результатом
     */
    public void loadPopularMovies(int page, ApiCallback<MovieEntity> callback) {
        // Получаем номер страницы для загрузки
        if (page <= 0) {
            page = apiDataManager.getNextPageToken("movie");
        } else {
            apiDataManager.setNextPageToken("movie", page + 1);
        }
        
        final int pageToLoad = page;
        Log.d(TAG, "Загрузка популярных фильмов: страница " + pageToLoad);
        
        Disposable disposable = tmdbRepository.getPopularMovies(pageToLoad)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        movies -> {
                            // Фильтруем уже загруженные фильмы
                            List<MovieEntity> uniqueMovies = apiDataManager.filterAlreadyLoaded("movie", movies);
                            
                            if (uniqueMovies.isEmpty()) {
                                // Если нет новых фильмов, загружаем следующую страницу
                                Log.d(TAG, "Нет новых фильмов на странице " + pageToLoad + ", пробуем следующую");
                                loadPopularMovies(pageToLoad + 1, callback);
                                return;
                            }
                            
                            // Сохраняем в базу данных
                            for (MovieEntity movie : uniqueMovies) {
                                movieRepository.insert(movie);
                            }
                            
                            callback.onSuccess(uniqueMovies);
                        },
                        error -> {
                            Log.e(TAG, "Error loading popular movies: " + error.getMessage());
                            callback.onError(error);
                        }
                );
        disposables.add(disposable);
    }

    /**
     * Загрузить популярные сериалы
     * @param page номер страницы
     * @param callback обратный вызов с результатом
     */
    public void loadPopularTVShows(int page, ApiCallback<TVShowEntity> callback) {
        // Получаем номер страницы для загрузки
        if (page <= 0) {
            page = apiDataManager.getNextPageToken("tv_show");
        } else {
            apiDataManager.setNextPageToken("tv_show", page + 1);
        }
        
        final int pageToLoad = page;
        Log.d(TAG, "Загрузка популярных сериалов: страница " + pageToLoad);
        
        Disposable disposable = tmdbRepository.getPopularTVShows(pageToLoad)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        tvShows -> {
                            // Фильтруем уже загруженные сериалы
                            List<TVShowEntity> uniqueTVShows = apiDataManager.filterAlreadyLoaded("tv_show", tvShows);
                            
                            if (uniqueTVShows.isEmpty()) {
                                // Если нет новых сериалов, загружаем следующую страницу
                                Log.d(TAG, "Нет новых сериалов на странице " + pageToLoad + ", пробуем следующую");
                                loadPopularTVShows(pageToLoad + 1, callback);
                                return;
                            }
                            
                            // Сохраняем в базу данных
                            for (TVShowEntity tvShow : uniqueTVShows) {
                                tvShowRepository.insert(tvShow);
                            }
                            
                            callback.onSuccess(uniqueTVShows);
                        },
                        error -> {
                            Log.e(TAG, "Error loading popular TV shows: " + error.getMessage());
                            callback.onError(error);
                        }
                );
        disposables.add(disposable);
    }

    /**
     * Загрузить популярные игры
     * @param page номер страницы
     * @param callback обратный вызов с результатом
     */
    public void loadPopularGames(int page, ApiCallback<GameEntity> callback) {
        // Получаем номер страницы для загрузки
        if (page <= 0) {
            page = apiDataManager.getNextPageToken("game");
        } else {
            apiDataManager.setNextPageToken("game", page + 1);
        }
        
        final int pageToLoad = page;
        Log.d(TAG, "Загрузка популярных игр: страница " + pageToLoad);
        
        Disposable disposable = rawgRepository.getPopularGames(pageToLoad)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        games -> {
                            // Фильтруем уже загруженные игры
                            List<GameEntity> uniqueGames = apiDataManager.filterAlreadyLoaded("game", games);
                            
                            if (uniqueGames.isEmpty()) {
                                // Если нет новых игр, загружаем следующую страницу
                                Log.d(TAG, "Нет новых игр на странице " + pageToLoad + ", пробуем следующую");
                                loadPopularGames(pageToLoad + 1, callback);
                                return;
                            }
                            
                            // Сохраняем в базу данных
                            for (GameEntity game : uniqueGames) {
                                gameRepository.insert(game);
                            }
                            
                            callback.onSuccess(uniqueGames);
                        },
                        error -> {
                            Log.e(TAG, "Error loading popular games: " + error.getMessage());
                            callback.onError(error);
                        }
                );
        disposables.add(disposable);
    }

    /**
     * Поиск книг
     * @param query поисковый запрос
     * @param page номер страницы
     * @param callback обратный вызов с результатом
     */
    public void searchBooks(String query, int page, ApiCallback<BookEntity> callback) {
        // Получаем номер страницы для загрузки
        if (page <= 0) {
            page = apiDataManager.getNextPageToken("book");
        } else {
            apiDataManager.setNextPageToken("book", page + 1);
        }
        
        final int pageToLoad = page;
        Log.d(TAG, "Поиск книг по запросу '" + query + "': страница " + pageToLoad);
        
        // Сначала пробуем с API ключом
        Disposable disposable = googleBooksRepository.searchBooks(query, pageToLoad)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        books -> {
                            if (books != null && !books.isEmpty()) {
                                // Фильтруем уже загруженные книги
                                List<BookEntity> uniqueBooks = apiDataManager.filterAlreadyLoaded("book", books);
                                
                                if (uniqueBooks.isEmpty()) {
                                    // Если нет новых книг, загружаем следующую страницу
                                    Log.d(TAG, "Нет новых книг на странице " + pageToLoad + ", пробуем следующую");
                                    searchBooks(query, pageToLoad + 1, callback);
                                    return;
                                }
                                
                                // Сохраняем в базу данных
                                for (BookEntity book : uniqueBooks) {
                                    bookRepository.insert(book);
                                }
                                
                                callback.onSuccess(uniqueBooks);
                            } else {
                                // Если не получили книги с ключом, пробуем без ключа
                                Log.d(TAG, "No books found with API key, trying without key");
                                trySearchBooksWithoutKey(query, pageToLoad, callback);
                            }
                        },
                        error -> {
                            Log.e(TAG, "Error searching books with API key: " + error.getMessage());
                            // В случае ошибки пробуем без ключа
                            trySearchBooksWithoutKey(query, pageToLoad, callback);
                        }
                );
        disposables.add(disposable);
    }
    
    /**
     * Резервный метод поиска книг без API ключа
     */
    private void trySearchBooksWithoutKey(String query, int page, ApiCallback<BookEntity> callback) {
        Disposable disposable = googleBooksRepository.searchBooksNoKey(query, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        books -> {
                            // Фильтруем уже загруженные книги
                            List<BookEntity> uniqueBooks = apiDataManager.filterAlreadyLoaded("book", books);
                            
                            if (uniqueBooks.isEmpty() && !books.isEmpty()) {
                                // Если были книги, но все уже загружены, пробуем следующую страницу
                                Log.d(TAG, "Нет новых книг без ключа на странице " + page + ", пробуем следующую");
                                searchBooks(query, page + 1, callback);
                                return;
                            }
                            
                            // Сохраняем в базу данных
                            for (BookEntity book : uniqueBooks) {
                                bookRepository.insert(book);
                            }
                            
                            callback.onSuccess(uniqueBooks);
                        },
                        error -> {
                            Log.e(TAG, "Error searching books without API key: " + error.getMessage());
                            callback.onError(error);
                        }
                );
        disposables.add(disposable);
    }

    /**
     * Загрузить топ аниме
     * @param page номер страницы
     * @param callback обратный вызов с результатом
     */
    public void loadTopAnime(int page, ApiCallback<AnimeEntity> callback) {
        // Получаем номер страницы для загрузки
        if (page <= 0) {
            page = apiDataManager.getNextPageToken("anime");
        } else {
            apiDataManager.setNextPageToken("anime", page + 1);
        }
        
        final int pageToLoad = page;
        Log.d(TAG, "Загрузка топ аниме: страница " + pageToLoad);
        
        Disposable disposable = jikanRepository.getTopAnime(pageToLoad)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        animeList -> {
                            // Фильтруем уже загруженные аниме
                            List<AnimeEntity> uniqueAnime = apiDataManager.filterAlreadyLoaded("anime", animeList);
                            
                            if (uniqueAnime.isEmpty()) {
                                // Если нет новых аниме, загружаем следующую страницу
                                Log.d(TAG, "Нет нового аниме на странице " + pageToLoad + ", пробуем следующую");
                                loadTopAnime(pageToLoad + 1, callback);
                                return;
                            }
                            
                            // Сохраняем в базу данных
                            for (AnimeEntity anime : uniqueAnime) {
                                animeRepository.insert(anime);
                            }
                            
                            callback.onSuccess(uniqueAnime);
                        },
                        error -> {
                            Log.e(TAG, "Error loading top anime: " + error.getMessage());
                            callback.onError(error);
                        }
                );
        disposables.add(disposable);
    }

    /**
     * Поиск фильмов
     * @param query поисковый запрос
     * @param page номер страницы
     * @param callback обратный вызов с результатом
     */
    public void searchMovies(String query, int page, ApiCallback<MovieEntity> callback) {
        Log.d(TAG, "Поиск фильмов по запросу '" + query + "': страница " + page);
        
        Disposable disposable = tmdbRepository.searchMovies(query, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        movies -> {
                            // Фильтруем уже загруженные фильмы
                            List<MovieEntity> uniqueMovies = apiDataManager.filterAlreadyLoaded("movie", movies);
                            
                            if (uniqueMovies.isEmpty() && !movies.isEmpty()) {
                                // Если были фильмы, но все уже загружены, пробуем следующую страницу
                                Log.d(TAG, "Нет новых фильмов в поиске на странице " + page + ", пробуем следующую");
                                searchMovies(query, page + 1, callback);
                                return;
                            }
                            
                            // Сохраняем в базу данных
                            for (MovieEntity movie : uniqueMovies) {
                                movieRepository.insert(movie);
                            }
                            
                            callback.onSuccess(uniqueMovies);
                        },
                        error -> {
                            Log.e(TAG, "Error searching movies: " + error.getMessage());
                            callback.onError(error);
                        }
                );
        disposables.add(disposable);
    }

    /**
     * Поиск сериалов
     * @param query поисковый запрос
     * @param page номер страницы
     * @param callback обратный вызов с результатом
     */
    public void searchTVShows(String query, int page, ApiCallback<TVShowEntity> callback) {
        Log.d(TAG, "Поиск сериалов по запросу '" + query + "': страница " + page);
        
        Disposable disposable = tmdbRepository.searchTVShows(query, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        tvShows -> {
                            // Фильтруем уже загруженные сериалы
                            List<TVShowEntity> uniqueTVShows = apiDataManager.filterAlreadyLoaded("tv_show", tvShows);
                            
                            if (uniqueTVShows.isEmpty() && !tvShows.isEmpty()) {
                                // Если были сериалы, но все уже загружены, пробуем следующую страницу
                                Log.d(TAG, "Нет новых сериалов в поиске на странице " + page + ", пробуем следующую");
                                searchTVShows(query, page + 1, callback);
                                return;
                            }
                            
                            // Сохраняем в базу данных
                            for (TVShowEntity tvShow : uniqueTVShows) {
                                tvShowRepository.insert(tvShow);
                            }
                            
                            callback.onSuccess(uniqueTVShows);
                        },
                        error -> {
                            Log.e(TAG, "Error searching TV shows: " + error.getMessage());
                            callback.onError(error);
                        }
                );
        disposables.add(disposable);
    }

    /**
     * Поиск игр
     * @param query поисковый запрос
     * @param page номер страницы
     * @param callback обратный вызов с результатом
     */
    public void searchGames(String query, int page, ApiCallback<GameEntity> callback) {
        Log.d(TAG, "Поиск игр по запросу '" + query + "': страница " + page);
        
        Disposable disposable = rawgRepository.searchGames(query, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        games -> {
                            // Фильтруем уже загруженные игры
                            List<GameEntity> uniqueGames = apiDataManager.filterAlreadyLoaded("game", games);
                            
                            if (uniqueGames.isEmpty() && !games.isEmpty()) {
                                // Если были игры, но все уже загружены, пробуем следующую страницу
                                Log.d(TAG, "Нет новых игр в поиске на странице " + page + ", пробуем следующую");
                                searchGames(query, page + 1, callback);
                                return;
                            }
                            
                            // Сохраняем в базу данных
                            for (GameEntity game : uniqueGames) {
                                gameRepository.insert(game);
                            }
                            
                            callback.onSuccess(uniqueGames);
                        },
                        error -> {
                            Log.e(TAG, "Error searching games: " + error.getMessage());
                            callback.onError(error);
                        }
                );
        disposables.add(disposable);
    }

    /**
     * Поиск аниме
     * @param query поисковый запрос
     * @param page номер страницы
     * @param callback обратный вызов с результатом
     */
    public void searchAnime(String query, int page, ApiCallback<AnimeEntity> callback) {
        Log.d(TAG, "Поиск аниме по запросу '" + query + "': страница " + page);
        
        Disposable disposable = jikanRepository.searchAnime(query, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        animeList -> {
                            // Фильтруем уже загруженные аниме
                            List<AnimeEntity> uniqueAnime = apiDataManager.filterAlreadyLoaded("anime", animeList);
                            
                            if (uniqueAnime.isEmpty() && !animeList.isEmpty()) {
                                // Если был аниме, но все уже загружены, пробуем следующую страницу
                                Log.d(TAG, "Нет нового аниме в поиске на странице " + page + ", пробуем следующую");
                                searchAnime(query, page + 1, callback);
                                return;
                            }
                            
                            // Сохраняем в базу данных
                            for (AnimeEntity anime : uniqueAnime) {
                                animeRepository.insert(anime);
                            }
                            
                            callback.onSuccess(uniqueAnime);
                        },
                        error -> {
                            Log.e(TAG, "Error searching anime: " + error.getMessage());
                            callback.onError(error);
                        }
                );
        disposables.add(disposable);
    }

    /**
     * Загрузить контент для указанной категории
     * @param category категория контента
     * @param page номер страницы
     * @param callback обратный вызов с результатом
     */
    @SuppressWarnings("unchecked")
    public <T> void loadContentForCategory(String category, int page, ApiCallback<T> callback) {
        switch (category) {
            case "movie":
                loadPopularMovies(page, (ApiCallback<MovieEntity>) callback);
                break;
            case "tv_show":
                loadPopularTVShows(page, (ApiCallback<TVShowEntity>) callback);
                break;
            case "game":
                loadPopularGames(page, (ApiCallback<GameEntity>) callback);
                break;
            case "book":
                searchBooks("", page, (ApiCallback<BookEntity>) callback);
                break;
            case "anime":
                loadTopAnime(page, (ApiCallback<AnimeEntity>) callback);
                break;
            default:
                Log.e(TAG, "Unknown category: " + category);
                callback.onError(new IllegalArgumentException("Unknown category: " + category));
                break;
        }
    }

    /**
     * Поиск контента для указанной категории
     * @param category категория контента
     * @param query поисковый запрос
     * @param page номер страницы
     * @param callback обратный вызов с результатом
     */
    @SuppressWarnings("unchecked")
    public <T> void searchContentForCategory(String category, String query, int page, ApiCallback<T> callback) {
        switch (category) {
            case "movie":
                searchMovies(query, page, (ApiCallback<MovieEntity>) callback);
                break;
            case "tv_show":
                searchTVShows(query, page, (ApiCallback<TVShowEntity>) callback);
                break;
            case "game":
                searchGames(query, page, (ApiCallback<GameEntity>) callback);
                break;
            case "book":
                searchBooks(query, page, (ApiCallback<BookEntity>) callback);
                break;
            case "anime":
                searchAnime(query, page, (ApiCallback<AnimeEntity>) callback);
                break;
            default:
                Log.e(TAG, "Unknown category: " + category);
                callback.onError(new IllegalArgumentException("Unknown category: " + category));
                break;
        }
    }
    
    /**
     * Сбросить кеш загруженных элементов для определенной категории
     * @param category категория контента
     */
    public void resetCategoryCache(String category) {
        apiDataManager.resetLoadedItems(category);
        apiDataManager.resetPageToken(category);
        Log.d(TAG, "Сброшен кеш для категории: " + category);
    }
    
    /**
     * Сбросить все кеши загруженных элементов
     */
    public void resetAllCaches() {
        apiDataManager.resetAllLoadedItems();
        apiDataManager.resetAllPageTokens();
        Log.d(TAG, "Сброшены все кеши");
    }

    /**
     * Очистить все Disposable объекты при уничтожении
     */
    public void clear() {
        disposables.clear();
    }
}