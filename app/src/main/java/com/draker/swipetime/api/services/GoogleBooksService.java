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
    @GET("volumes")
    Observable<GoogleBooksResponse> searchBooks(
            @Query("q") String query,
            @Query("maxResults") int maxResults,
            @Query("startIndex") int startIndex,
            @Query("langRestrict") String langRestrict
    );

    // Получение подробной информации о книге
    @GET("volumes/{volumeId}")
    Observable<GoogleBook> getBookDetails(
            @Path("volumeId") String volumeId
    );

    // Получение книг по категории
    @GET("volumes")
    Observable<GoogleBooksResponse> getBooksByCategory(
            @Query("q") String query,
            @Query("maxResults") int maxResults,
            @Query("startIndex") int startIndex,
            @Query("langRestrict") String langRestrict
    );

    // Получение книг по автору
    @GET("volumes")
    Observable<GoogleBooksResponse> getBooksByAuthor(
            @Query("q") String query,
            @Query("maxResults") int maxResults,
            @Query("startIndex") int startIndex,
            @Query("langRestrict") String langRestrict
    );
}