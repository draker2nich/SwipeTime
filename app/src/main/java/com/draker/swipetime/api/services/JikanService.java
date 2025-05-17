package com.draker.swipetime.api.services;

import com.draker.swipetime.api.models.jikan.JikanAnime;
import com.draker.swipetime.api.models.jikan.JikanResponse;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Интерфейс для работы с Jikan API
 */
public interface JikanService {
    // Получение аниме по популярности
    @GET("anime")
    Observable<JikanResponse> getTopAnime(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("order_by") String orderBy,
            @Query("sort") String sort
    );

    // Поиск аниме
    @GET("anime")
    Observable<JikanResponse> searchAnime(
            @Query("q") String query,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Получение подробной информации об аниме
    @GET("anime/{id}/full")
    Observable<JikanAnime> getAnimeDetails(
            @Path("id") int animeId
    );

    // Получение аниме по жанру
    @GET("anime")
    Observable<JikanResponse> getAnimeByGenre(
            @Query("genres") String genres,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Получение аниме по статусу
    @GET("anime")
    Observable<JikanResponse> getAnimeByStatus(
            @Query("status") String status,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Получение аниме по типу
    @GET("anime")
    Observable<JikanResponse> getAnimeByType(
            @Query("type") String type,
            @Query("page") int page,
            @Query("limit") int limit
    );
}