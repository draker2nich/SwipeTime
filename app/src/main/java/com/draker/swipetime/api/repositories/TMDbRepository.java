package com.draker.swipetime.api.repositories;

import android.util.Log;

import com.draker.swipetime.api.ApiConstants;
import com.draker.swipetime.api.RetrofitClient;
import com.draker.swipetime.api.models.tmdb.TMDbCredits;
import com.draker.swipetime.api.models.tmdb.TMDbMovie;
import com.draker.swipetime.api.models.tmdb.TMDbMovieResponse;
import com.draker.swipetime.api.models.tmdb.TMDbTVShow;
import com.draker.swipetime.api.models.tmdb.TMDbTVShowResponse;
import com.draker.swipetime.api.services.TMDbService;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Репозиторий для работы с TMDB API
 */
public class TMDbRepository {
    private static final String TAG = "TMDbRepository";
    private static final String BEARER_AUTH = "Bearer " + ApiConstants.TMDB_API_KEY;
    private static final String LANGUAGE = "ru-RU";

    private TMDbService service;

    public TMDbRepository() {
        service = RetrofitClient.getTmdbClient().create(TMDbService.class);
    }

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
                    // Возвращаем пустой список в случае ошибки, чтобы не ломать поток
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
                    // Возвращаем пустой список в случае ошибки, чтобы не ломать поток
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
                    movie.setDirector(credits.getDirector());
                    return convertMovieToEntity(movie);
                }
        ).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

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
                    // Возвращаем пустой список в случае ошибки, чтобы не ломать поток
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
                    // Возвращаем пустой список в случае ошибки, чтобы не ломать поток
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
                .map(this::convertTVShowToEntity);
    }

    /**
     * Конвертировать ответ с фильмами в список MovieEntity
     * @param response ответ от TMDB API
     * @return список MovieEntity
     */
    private List<MovieEntity> convertMovieResponseToEntities(TMDbMovieResponse response) {
        List<MovieEntity> movies = new ArrayList<>();
        if (response != null && response.getResults() != null) {
            for (TMDbMovie movie : response.getResults()) {
                movies.add(convertMovieToEntity(movie));
            }
        }
        return movies;
    }

    /**
     * Конвертировать модель фильма в MovieEntity
     * @param movie модель фильма из TMDB API
     * @return MovieEntity
     */
    private MovieEntity convertMovieToEntity(TMDbMovie movie) {
        String imageUrl = null;
        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            imageUrl = ApiConstants.TMDB_IMAGE_BASE_URL + movie.getPosterPath();
        }

        int releaseYear = 0;
        if (movie.getReleaseDate() != null && !movie.getReleaseDate().isEmpty() && movie.getReleaseDate().length() >= 4) {
            try {
                releaseYear = Integer.parseInt(movie.getReleaseDate().substring(0, 4));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing release year: " + e.getMessage());
            }
        }

        String genres = "";
        if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
            StringBuilder genresBuilder = new StringBuilder();
            for (int i = 0; i < movie.getGenres().size(); i++) {
                genresBuilder.append(movie.getGenres().get(i).getName());
                if (i < movie.getGenres().size() - 1) {
                    genresBuilder.append(", ");
                }
            }
            genres = genresBuilder.toString();
        }

        return new MovieEntity(
                "tmdb_" + movie.getId(),
                movie.getTitle(),
                movie.getOverview(),
                imageUrl,
                movie.getDirector() != null ? movie.getDirector() : "",
                releaseYear,
                movie.getRuntime(),
                genres
        );
    }

    /**
     * Конвертировать ответ с сериалами в список TVShowEntity
     * @param response ответ от TMDB API
     * @return список TVShowEntity
     */
    private List<TVShowEntity> convertTVShowResponseToEntities(TMDbTVShowResponse response) {
        List<TVShowEntity> tvShows = new ArrayList<>();
        if (response != null && response.getResults() != null) {
            for (TMDbTVShow tvShow : response.getResults()) {
                tvShows.add(convertTVShowToEntity(tvShow));
            }
        }
        return tvShows;
    }

    /**
     * Конвертировать модель сериала в TVShowEntity
     * @param tvShow модель сериала из TMDB API
     * @return TVShowEntity
     */
    private TVShowEntity convertTVShowToEntity(TMDbTVShow tvShow) {
        String imageUrl = null;
        if (tvShow.getPosterPath() != null && !tvShow.getPosterPath().isEmpty()) {
            imageUrl = ApiConstants.TMDB_IMAGE_BASE_URL + tvShow.getPosterPath();
        }

        int startYear = 0;
        if (tvShow.getFirstAirDate() != null && !tvShow.getFirstAirDate().isEmpty() && tvShow.getFirstAirDate().length() >= 4) {
            try {
                startYear = Integer.parseInt(tvShow.getFirstAirDate().substring(0, 4));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing start year: " + e.getMessage());
            }
        }

        int endYear = 0;
        if (tvShow.getLastAirDate() != null && !tvShow.getLastAirDate().isEmpty() && tvShow.getLastAirDate().length() >= 4) {
            try {
                endYear = Integer.parseInt(tvShow.getLastAirDate().substring(0, 4));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing end year: " + e.getMessage());
            }
        }

        String genres = "";
        if (tvShow.getGenres() != null && !tvShow.getGenres().isEmpty()) {
            StringBuilder genresBuilder = new StringBuilder();
            for (int i = 0; i < tvShow.getGenres().size(); i++) {
                genresBuilder.append(tvShow.getGenres().get(i).getName());
                if (i < tvShow.getGenres().size() - 1) {
                    genresBuilder.append(", ");
                }
            }
            genres = genresBuilder.toString();
        }

        String status = "Unknown";
        if (tvShow.getStatus() != null) {
            switch (tvShow.getStatus()) {
                case "Ended":
                    status = "finished";
                    break;
                case "Returning Series":
                    status = "ongoing";
                    break;
                case "Canceled":
                    status = "cancelled";
                    break;
                default:
                    status = "ongoing";
                    break;
            }
        }

        return new TVShowEntity(
                "tmdb_" + tvShow.getId(),
                tvShow.getName(),
                tvShow.getOverview(),
                imageUrl,
                tvShow.getCreatorName(),
                startYear,
                endYear,
                tvShow.getNumberOfSeasons(),
                tvShow.getNumberOfEpisodes(),
                genres,
                status
        );
    }
}