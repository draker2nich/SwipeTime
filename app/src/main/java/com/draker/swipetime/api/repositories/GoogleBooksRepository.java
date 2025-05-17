package com.draker.swipetime.api.repositories;

import android.util.Log;

import com.draker.swipetime.api.ApiConstants;
import com.draker.swipetime.api.RetrofitClient;
import com.draker.swipetime.api.models.googlebooks.GoogleBook;
import com.draker.swipetime.api.models.googlebooks.GoogleBooksResponse;
import com.draker.swipetime.api.services.GoogleBooksService;
import com.draker.swipetime.database.entities.BookEntity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Репозиторий для работы с Google Books API
 */
public class GoogleBooksRepository {
    private static final String TAG = "GoogleBooksRepository";
    private GoogleBooksService service;

    public GoogleBooksRepository() {
        service = RetrofitClient.getGoogleBooksClient().create(GoogleBooksService.class);
    }

    /**
     * Поиск книг
     * @param query поисковый запрос
     * @param page номер страницы (начинается с 0)
     * @return Observable со списком BookEntity
     */
    public Observable<List<BookEntity>> searchBooks(String query, int page) {
        int startIndex = page * ApiConstants.PAGE_SIZE;
        
        // Если запрос пустой, ищем популярные книги или конкретные известные книги
        if (query == null || query.isEmpty()) {
            query = "Harry Potter OR The Lord of the Rings OR Game of Thrones";
        }
        
        android.util.Log.d("GoogleBooksApi", "Searching books with query: " + query + " and API key: AIzaSyCXClzjGf7cUiMCPPJUDy8CH4w6n4C639g");
        
        final String finalQuery = query;
        
        // Добавляем API key для авторизации запросов
        return service.searchBooks(finalQuery, ApiConstants.PAGE_SIZE, startIndex, "ru", "AIzaSyCXClzjGf7cUiMCPPJUDy8CH4w6n4C639g")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    android.util.Log.d("GoogleBooksApi", "Response received: " + 
                            (response != null ? "total items: " + response.getTotalItems() : "null response"));
                    if (response != null && response.getItems() != null) {
                        android.util.Log.d("GoogleBooksApi", "Items count: " + response.getItems().size());
                    }
                    return convertBookResponseToEntities(response);
                })
                .doOnError(error -> {
                    android.util.Log.e("GoogleBooksApi", "Error searching books: " + error.getMessage(), error);
                })
                .onErrorReturn(error -> {
                    android.util.Log.e("GoogleBooksApi", "Error searching books: " + error.getMessage(), error);
                    // Возвращаем пустой список в случае ошибки, чтобы не ломать поток
                    return new ArrayList<>();
                });
    }

    /**
     * Получить детальную информацию о книге
     * @param volumeId ID книги
     * @return Observable с BookEntity
     */
    public Observable<BookEntity> getBookDetails(String volumeId) {
        return service.getBookDetails(volumeId, "AIzaSyCXClzjGf7cUiMCPPJUDy8CH4w6n4C639g")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertBookToEntity);
    }

    /**
     * Получить книги по категории
     * @param category категория
     * @param page номер страницы (начинается с 0)
     * @return Observable со списком BookEntity
     */
    public Observable<List<BookEntity>> getBooksByCategory(String category, int page) {
        int startIndex = page * ApiConstants.PAGE_SIZE;
        String query = "subject:" + category;
        return service.getBooksByCategory(query, ApiConstants.PAGE_SIZE, startIndex, "ru", "AIzaSyCXClzjGf7cUiMCPPJUDy8CH4w6n4C639g")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertBookResponseToEntities);
    }

    /**
     * Альтернативный метод поиска книг без API ключа
     * @param query поисковый запрос
     * @param page номер страницы (начинается с 0)
     * @return Observable со списком BookEntity
     */
    public Observable<List<BookEntity>> searchBooksNoKey(String query, int page) {
        int startIndex = page * ApiConstants.PAGE_SIZE;
        
        // Если запрос пустой, ищем популярные книги или конкретные известные книги
        if (query == null || query.isEmpty()) {
            query = "Harry Potter";
        }
        
        android.util.Log.d("GoogleBooksApi", "Searching books without API key, query: " + query);
        
        final String finalQuery = query;
        
        return service.searchBooksNoKey(finalQuery, ApiConstants.PAGE_SIZE, startIndex)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    android.util.Log.d("GoogleBooksApi", "Response received (no key): " + 
                            (response != null ? "total items: " + response.getTotalItems() : "null response"));
                    if (response != null && response.getItems() != null) {
                        android.util.Log.d("GoogleBooksApi", "Items count: " + response.getItems().size());
                    }
                    return convertBookResponseToEntities(response);
                })
                .doOnError(error -> {
                    android.util.Log.e("GoogleBooksApi", "Error searching books (no key): " + error.getMessage(), error);
                })
                .onErrorReturn(error -> {
                    android.util.Log.e("GoogleBooksApi", "Error searching books (no key): " + error.getMessage(), error);
                    // Возвращаем пустой список в случае ошибки, чтобы не ломать поток
                    return new ArrayList<>();
                });
    }

    /**
     * Конвертировать ответ с книгами в список BookEntity
     * @param response ответ от Google Books API
     * @return список BookEntity
     */
    private List<BookEntity> convertBookResponseToEntities(GoogleBooksResponse response) {
        List<BookEntity> books = new ArrayList<>();
        if (response != null && response.getItems() != null) {
            for (GoogleBook book : response.getItems()) {
                books.add(convertBookToEntity(book));
            }
        }
        return books;
    }

    /**
     * Конвертировать модель книги в BookEntity
     * @param book модель книги из Google Books API
     * @return BookEntity
     */
    private BookEntity convertBookToEntity(GoogleBook book) {
        if (book.getVolumeInfo() == null) {
            Log.e(TAG, "Book with ID " + book.getId() + " has no volume info");
            return new BookEntity(
                    "gbooks_" + book.getId(),
                    "Unknown Title",
                    "No description available",
                    null,
                    "Unknown Author",
                    "Unknown Publisher",
                    0,
                    0,
                    "Unknown Genre",
                    ""
            );
        }

        String title = book.getVolumeInfo().getTitle() != null ? book.getVolumeInfo().getTitle() : "Unknown Title";
        
        String description = book.getVolumeInfo().getDescription() != null ? 
                book.getVolumeInfo().getDescription() : "No description available";
        
        String imageUrl = null;
        if (book.getVolumeInfo().getImageLinks() != null) {
            imageUrl = book.getVolumeInfo().getImageLinks().getBestAvailableImage();
        }
        
        String author = book.getVolumeInfo().getAuthorsString();
        
        String publisher = book.getVolumeInfo().getPublisher() != null ? 
                book.getVolumeInfo().getPublisher() : "Unknown Publisher";
        
        int publishYear = book.getVolumeInfo().getPublishYear();
        
        int pageCount = book.getVolumeInfo().getPageCount();
        
        String genres = book.getVolumeInfo().getCategoriesString();
        
        String isbn = book.getVolumeInfo().getIsbn();

        return new BookEntity(
                "gbooks_" + book.getId(),
                title,
                description,
                imageUrl,
                author,
                publisher,
                publishYear,
                pageCount,
                genres,
                isbn
        );
    }
}