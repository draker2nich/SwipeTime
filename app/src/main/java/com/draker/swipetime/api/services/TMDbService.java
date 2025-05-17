package com.draker.swipetime.api.services;

import com.draker.swipetime.api.models.tmdb.TMDbCredits;
import com.draker.swipetime.api.models.tmdb.TMDbMovie;
import com.draker.swipetime.api.models.tmdb.TMDbMovieResponse;
import com.draker.swipetime.api.models.tmdb.TMDbTVShow;
import com.draker.swipetime.api.models.tmdb.TMDbTVShowResponse;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Интерфейс для работы с TMDB API
 */
public interface TMDbService {
    // Получение популярных фильмов
    @GET("movie/popular")
    Observable<TMDbMovieResponse> getPopularMovies(
            @Header("Authorization") String apiKey,
            @Query("language") String language,
            @Query("page") int page
    );

    // Поиск фильмов
    @GET("search/movie")
    Observable<TMDbMovieResponse> searchMovies(
            @Header("Authorization") String apiKey,
            @Query("query") String query,
            @Query("language") String language,
            @Query("page") int page
    );

    // Получение подробной информации о фильме
    @GET("movie/{movie_id}")
    Observable<TMDbMovie> getMovieDetails(
            @Header("Authorization") String apiKey,
            @Path("movie_id") int movieId,
            @Query("language") String language
    );

    // Получение информации о создателях фильма
    @GET("movie/{movie_id}/credits")
    Observable<TMDbCredits> getMovieCredits(
            @Header("Authorization") String apiKey,
            @Path("movie_id") int movieId
    );

    // Получение популярных сериалов
    @GET("tv/popular")
    Observable<TMDbTVShowResponse> getPopularTVShows(
            @Header("Authorization") String apiKey,
            @Query("language") String language,
            @Query("page") int page
    );

    // Поиск сериалов
    @GET("search/tv")
    Observable<TMDbTVShowResponse> searchTVShows(
            @Header("Authorization") String apiKey,
            @Query("query") String query,
            @Query("language") String language,
            @Query("page") int page
    );

    // Получение подробной информации о сериале
    @GET("tv/{tv_id}")
    Observable<TMDbTVShow> getTVShowDetails(
            @Header("Authorization") String apiKey,
            @Path("tv_id") int tvId,
            @Query("language") String language
    );

    // Получение информации о создателях сериала
    @GET("tv/{tv_id}/credits")
    Observable<TMDbCredits> getTVShowCredits(
            @Header("Authorization") String apiKey,
            @Path("tv_id") int tvId
    );
}