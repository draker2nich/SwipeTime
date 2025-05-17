package com.draker.swipetime.api.services;

import com.draker.swipetime.api.models.rawg.RawgGame;
import com.draker.swipetime.api.models.rawg.RawgGameResponse;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Интерфейс для работы с RAWG API
 */
public interface RawgService {
    // Получение популярных игр
    @GET("games")
    Observable<RawgGameResponse> getGames(
            @Query("key") String apiKey,
            @Query("page") int page,
            @Query("page_size") int pageSize,
            @Query("ordering") String ordering
    );

    // Поиск игр
    @GET("games")
    Observable<RawgGameResponse> searchGames(
            @Query("key") String apiKey,
            @Query("search") String search,
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    // Получение подробной информации об игре
    @GET("games/{id}")
    Observable<RawgGame> getGameDetails(
            @Query("key") String apiKey,
            @Path("id") int gameId
    );

    // Получение игр по жанру
    @GET("games")
    Observable<RawgGameResponse> getGamesByGenre(
            @Query("key") String apiKey,
            @Query("genres") String genres,
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    // Получение игр по платформе
    @GET("games")
    Observable<RawgGameResponse> getGamesByPlatform(
            @Query("key") String apiKey,
            @Query("platforms") String platforms,
            @Query("page") int page,
            @Query("page_size") int pageSize
    );
}