package com.draker.swipetime.api.repositories;

import android.util.Log;

import com.draker.swipetime.api.ApiConstants;
import com.draker.swipetime.api.RetrofitClient;
import com.draker.swipetime.api.models.tmdb.TMDbCredits;
import com.draker.swipetime.api.models.tmdb.TMDbContent;
import com.draker.swipetime.api.models.tmdb.TMDbResponse;
import com.draker.swipetime.api.services.TMDbService;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Обновленный репозиторий для работы с TMDB API
 * Использует новые универсальные модели TMDbContent
 */
public class TMDbRepository {
    private static final String TAG = "TMDbRepository";
    private static final String BEARER_AUTH = "Bearer " + ApiConstants.TMDB_API_KEY;
    private static final String LANGUAGE = "ru-RU";

    private TMDbService service;

    public TMDbRepository() {
        service = RetrofitClient.getTmdbClient().create(TMDbService.class);
    }

    // ===============================
    // МЕТОДЫ ДЛЯ ФИЛЬМОВ
    // ===============================

    /**
     * Получить список популярных фильмов
     * @param page номер страницы
     * @return Observable со списком MovieEntity
     */
    public Observable<List<MovieEntity>> getPopularMovies(int page) {
        return service.getPopularMovies(BEARER_AUTH, LANGUAGE, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertMovieResponseToEntities)
                .onErrorReturn(error -> {
                    Log.e(TAG, "Error loading popular movies: " + error.getMessage(), error);
                    return new ArrayList<>();
                });
    }

    /**
     * Поиск фильмов
     * @param query поисковый запрос
     * @param page номер страницы
     * @return Observable со списком MovieEntity
     */
    public Observable<List<MovieEntity>> searchMovies(String query, int page) {
        return service.searchMovies(BEARER_AUTH, query, LANGUAGE, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertMovieResponseToEntities)
                .onErrorReturn(error -> {
                    Log.e(TAG, "Error searching movies: " + error.getMessage(), error);
                    return new ArrayList<>();
                });
    }

    /**
     * Получить детальную информацию о фильме
     * @param movieId ID фильма
     * @return Observable с MovieEntity
     */
    public Observable<MovieEntity> getMovieDetails(int movieId) {
        return Observable.zip(
                        service.getMovieDetails(BEARER_AUTH, movieId, LANGUAGE),
                        service.getMovieCredits(BEARER_AUTH, movieId),
                        (movie, credits) -> {
                            movie.setContentType(TMDbContent.ContentType.MOVIE);
                            movie.setDirector(credits.getDirector());
                            return convertMovieToEntity(movie);
                        }
                ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    // ===============================
    // МЕТОДЫ ДЛЯ СЕРИАЛОВ
    // ===============================

    /**
     * Получить список популярных сериалов
     * @param page номер страницы
     * @return Observable со списком TVShowEntity
     */
    public Observable<List<TVShowEntity>> getPopularTVShows(int page) {
        return service.getPopularTVShows(BEARER_AUTH, LANGUAGE, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertTVShowResponseToEntities)
                .onErrorReturn(error -> {
                    Log.e(TAG, "Error loading popular TV shows: " + error.getMessage(), error);
                    return new ArrayList<>();
                });
    }

    /**
     * Поиск сериалов
     * @param query поисковый запрос
     * @param page номер страницы
     * @return Observable со списком TVShowEntity
     */
    public Observable<List<TVShowEntity>> searchTVShows(String query, int page) {
        return service.searchTVShows(BEARER_AUTH, query, LANGUAGE, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertTVShowResponseToEntities)
                .onErrorReturn(error -> {
                    Log.e(TAG, "Error searching TV shows: " + error.getMessage(), error);
                    return new ArrayList<>();
                });
    }

    /**
     * Получить детальную информацию о сериале
     * @param tvShowId ID сериала
     * @return Observable с TVShowEntity
     */
    public Observable<TVShowEntity> getTVShowDetails(int tvShowId) {
        return service.getTVShowDetails(BEARER_AUTH, tvShowId, LANGUAGE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(tvShow -> {
                    tvShow.setContentType(TMDbContent.ContentType.TV_SHOW);
                    return convertTVShowToEntity(tvShow);
                });
    }

    // ===============================
    // МЕТОДЫ КОНВЕРТАЦИИ ДЛЯ ФИЛЬМОВ
    // ===============================

    /**
     * Конвертировать ответ с фильмами в список MovieEntity
     * @param response ответ от TMDB API
     * @return список MovieEntity
     */
    private List<MovieEntity> convertMovieResponseToEntities(TMDbResponse response) {
        List<MovieEntity> movies = new ArrayList<>();
        if (response != null && response.hasResults()) {
            for (TMDbContent content : response.getResults()) {
                content.setContentType(TMDbContent.ContentType.MOVIE);
                movies.add(convertMovieToEntity(content));
            }
        }
        return movies;
    }

    /**
     * Конвертировать модель контента в MovieEntity
     * @param content модель контента из TMDB API
     * @return MovieEntity
     */
    private MovieEntity convertMovieToEntity(TMDbContent content) {
        String imageUrl = null;
        if (content.getPosterPath() != null && !content.getPosterPath().isEmpty()) {
            imageUrl = ApiConstants.TMDB_IMAGE_BASE_URL + content.getPosterPath();
        }

        int releaseYear = content.getYear();

        String genres = content.getGenresString();

        return new MovieEntity(
                "tmdb_" + content.getId(),
                content.getTitle(),
                content.getOverview(),
                imageUrl,
                content.getDirector() != null ? content.getDirector() : "",
                releaseYear,
                content.getRuntime() != null ? content.getRuntime() : 0,
                genres
        );
    }

    // ===============================
    // МЕТОДЫ КОНВЕРТАЦИИ ДЛЯ СЕРИАЛОВ
    // ===============================

    /**
     * Конвертировать ответ с сериалами в список TVShowEntity
     * @param response ответ от TMDB API
     * @return список TVShowEntity
     */
    private List<TVShowEntity> convertTVShowResponseToEntities(TMDbResponse response) {
        List<TVShowEntity> tvShows = new ArrayList<>();
        if (response != null && response.hasResults()) {
            for (TMDbContent content : response.getResults()) {
                content.setContentType(TMDbContent.ContentType.TV_SHOW);
                tvShows.add(convertTVShowToEntity(content));
            }
        }
        return tvShows;
    }

    /**
     * Конвертировать модель контента в TVShowEntity
     * @param content модель контента из TMDB API
     * @return TVShowEntity
     */
    private TVShowEntity convertTVShowToEntity(TMDbContent content) {
        String imageUrl = null;
        if (content.getPosterPath() != null && !content.getPosterPath().isEmpty()) {
            imageUrl = ApiConstants.TMDB_IMAGE_BASE_URL + content.getPosterPath();
        }

        int startYear = content.getYear();

        // Для сериалов пока не получаем endYear из API, можно добавить позже
        int endYear = 0;

        String genres = content.getGenresString();

        // Статус по умолчанию - продолжается
        String status = "ongoing";

        return new TVShowEntity(
                "tmdb_" + content.getId(),
                content.getTitle(),
                content.getOverview(),
                imageUrl,
                content.getCreatorNames(),
                startYear,
                endYear,
                content.getNumberOfSeasons() != null ? content.getNumberOfSeasons() : 0,
                content.getNumberOfEpisodes() != null ? content.getNumberOfEpisodes() : 0,
                genres,
                status
        );
    }
}