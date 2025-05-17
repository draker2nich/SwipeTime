package com.draker.swipetime.api;

import android.app.Application;
import android.util.Log;

import com.draker.swipetime.api.repositories.GoogleBooksRepository;
import com.draker.swipetime.api.repositories.JikanRepository;
import com.draker.swipetime.api.repositories.RawgRepository;
import com.draker.swipetime.api.repositories.TMDbRepository;
import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.repository.AnimeRepository;
import com.draker.swipetime.repository.BookRepository;
import com.draker.swipetime.repository.GameRepository;
import com.draker.swipetime.repository.MovieRepository;
import com.draker.swipetime.repository.TVShowRepository;

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
    }

    /**
     * Загрузить популярные фильмы
     * @param page номер страницы
     * @param callback обратный вызов с результатом
     */
    public void loadPopularMovies(int page, ApiCallback<MovieEntity> callback) {
        Disposable disposable = tmdbRepository.getPopularMovies(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        movies -> {
                            // Сохраняем в базу данных
                            for (MovieEntity movie : movies) {
                                movieRepository.insert(movie);
                            }
                            callback.onSuccess(movies);
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
        Disposable disposable = tmdbRepository.getPopularTVShows(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        tvShows -> {
                            // Сохраняем в базу данных
                            for (TVShowEntity tvShow : tvShows) {
                                tvShowRepository.insert(tvShow);
                            }
                            callback.onSuccess(tvShows);
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
        Disposable disposable = rawgRepository.getPopularGames(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        games -> {
                            // Сохраняем в базу данных
                            for (GameEntity game : games) {
                                gameRepository.insert(game);
                            }
                            callback.onSuccess(games);
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
        Disposable disposable = googleBooksRepository.searchBooks(query, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        books -> {
                            // Сохраняем в базу данных
                            for (BookEntity book : books) {
                                bookRepository.insert(book);
                            }
                            callback.onSuccess(books);
                        },
                        error -> {
                            Log.e(TAG, "Error searching books: " + error.getMessage());
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
        Disposable disposable = jikanRepository.getTopAnime(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        animeList -> {
                            // Сохраняем в базу данных
                            for (AnimeEntity anime : animeList) {
                                animeRepository.insert(anime);
                            }
                            callback.onSuccess(animeList);
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
        Disposable disposable = tmdbRepository.searchMovies(query, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        movies -> {
                            // Сохраняем в базу данных
                            for (MovieEntity movie : movies) {
                                movieRepository.insert(movie);
                            }
                            callback.onSuccess(movies);
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
        Disposable disposable = tmdbRepository.searchTVShows(query, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        tvShows -> {
                            // Сохраняем в базу данных
                            for (TVShowEntity tvShow : tvShows) {
                                tvShowRepository.insert(tvShow);
                            }
                            callback.onSuccess(tvShows);
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
        Disposable disposable = rawgRepository.searchGames(query, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        games -> {
                            // Сохраняем в базу данных
                            for (GameEntity game : games) {
                                gameRepository.insert(game);
                            }
                            callback.onSuccess(games);
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
        Disposable disposable = jikanRepository.searchAnime(query, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        animeList -> {
                            // Сохраняем в базу данных
                            for (AnimeEntity anime : animeList) {
                                animeRepository.insert(anime);
                            }
                            callback.onSuccess(animeList);
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
     * Очистить все Disposable объекты при уничтожении
     */
    public void clear() {
        disposables.clear();
    }
}