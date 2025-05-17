package com.draker.swipetime.api.services;

import com.draker.swipetime.api.models.googlebooks.GoogleBook;
import com.draker.swipetime.api.models.googlebooks.GoogleBooksResponse;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Интерфейс для работы с Google Books API
 */
public interface GoogleBooksService {
    // Поиск книг
    /**
     * Поиск книг
     * @param query поисковый запрос
     * @param maxResults максимальное количество результатов
     * @param startIndex начальный индекс (для пагинации)
     * @param langRestrict ограничение по языку
     * @param apiKey API ключ (опционально)
     * @return
     */
    @GET("volumes")
    Observable<GoogleBooksResponse> searchBooks(
            @Query("q") String query,
            @Query("maxResults") int maxResults,
            @Query("startIndex") int startIndex,
            @Query("langRestrict") String langRestrict,
            @Query("key") String apiKey
    );

    /**
     * Альтернативный метод поиска книг без API ключа
     * @param query поисковый запрос
     * @param maxResults максимальное количество результатов
     * @param startIndex начальный индекс (для пагинации)
     * @return
     */
    @GET("volumes")
    Observable<GoogleBooksResponse> searchBooksNoKey(
            @Query("q") String query,
            @Query("maxResults") int maxResults,
            @Query("startIndex") int startIndex
    );

    // Получение подробной информации о книге
    @GET("volumes/{volumeId}")
    Observable<GoogleBook> getBookDetails(
            @Path("volumeId") String volumeId,
            @Query("key") String apiKey
    );

    // Получение книг по категории
    @GET("volumes")
    Observable<GoogleBooksResponse> getBooksByCategory(
            @Query("q") String query,
            @Query("maxResults") int maxResults,
            @Query("startIndex") int startIndex,
            @Query("langRestrict") String langRestrict,
            @Query("key") String apiKey
    );

    // Получение книг по автору
    @GET("volumes")
    Observable<GoogleBooksResponse> getBooksByAuthor(
            @Query("q") String query,
            @Query("maxResults") int maxResults,
            @Query("startIndex") int startIndex,
            @Query("langRestrict") String langRestrict,
            @Query("key") String apiKey
    );
}