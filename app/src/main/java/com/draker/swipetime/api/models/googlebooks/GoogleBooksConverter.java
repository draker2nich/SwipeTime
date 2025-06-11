package com.draker.swipetime.api.models.googlebooks;

import android.util.Log;

import com.draker.swipetime.database.entities.BookEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Утилитарный класс для конвертации между моделями Google Books и сущностями базы данных
 * Заменяет логику конвертации из репозитория для лучшей организации кода
 */
public class GoogleBooksConverter {
    private static final String TAG = "GoogleBooksConverter";

    /**
     * Конвертировать ответ с книгами в список BookEntity
     */
    public static List<BookEntity> convertResponseToEntities(GoogleBooksResponse response) {
        List<BookEntity> books = new ArrayList<>();
        if (response != null && response.hasResults()) {
            for (GoogleBook book : response.getItems()) {
                try {
                    BookEntity entity = convertBookToEntity(book);
                    if (entity != null) {
                        books.add(entity);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Failed to convert book: " + book.getId(), e);
                }
            }
        }
        return books;
    }

    /**
     * Конвертировать модель книги в BookEntity
     */
    public static BookEntity convertBookToEntity(GoogleBook book) {
        if (book == null || book.getId() == null) {
            Log.w(TAG, "Cannot convert null book or book without ID");
            return null;
        }

        try {
            String id = "gbooks_" + book.getId();
            String title = getValueOrDefault(book.getTitle(), "Unknown Title");
            String description = getValueOrDefault(book.getDescription(), "No description available");
            String imageUrl = book.getImageUrl();
            String author = getValueOrDefault(book.getAuthorsString(), "Unknown Author");
            String publisher = getValueOrDefault(book.getPublisher(), "Unknown Publisher");
            int publishYear = book.getPublishYear();
            int pageCount = book.getPageCount();
            String genres = getValueOrDefault(book.getCategoriesString(), "Unknown Genre");
            String isbn = book.getIsbn();

            return new BookEntity(
                    id,
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
        } catch (Exception e) {
            Log.e(TAG, "Error converting book to entity: " + book.getId(), e);
            return createFallbackBookEntity(book.getId());
        }
    }

    /**
     * Создать резервную BookEntity в случае ошибки конвертации
     */
    private static BookEntity createFallbackBookEntity(String bookId) {
        return new BookEntity(
                "gbooks_" + bookId,
                "Error loading book",
                "Unable to load book information",
                null,
                "Unknown Author",
                "Unknown Publisher",
                0,
                0,
                "Unknown Genre",
                ""
        );
    }

    /**
     * Получить значение или значение по умолчанию если оно null или пустое
     */
    private static String getValueOrDefault(String value, String defaultValue) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
    }

    /**
     * Валидировать GoogleBook на корректность данных
     */
    public static boolean isValidBook(GoogleBook book) {
        if (book == null || book.getId() == null || book.getId().trim().isEmpty()) {
            return false;
        }

        // Книга считается валидной если у неё есть хотя бы название
        String title = book.getTitle();
        return title != null && !title.trim().isEmpty();
    }

    /**
     * Фильтровать список книг, оставляя только валидные
     */
    public static List<GoogleBook> filterValidBooks(List<GoogleBook> books) {
        if (books == null || books.isEmpty()) {
            return new ArrayList<>();
        }

        List<GoogleBook> validBooks = new ArrayList<>();
        for (GoogleBook book : books) {
            if (isValidBook(book)) {
                validBooks.add(book);
            } else {
                Log.d(TAG, "Filtered out invalid book: " +
                        (book != null ? book.getId() : "null"));
            }
        }
        return validBooks;
    }

    /**
     * Получить статистику конвертации
     */
    public static String getConversionStats(GoogleBooksResponse response) {
        if (response == null) {
            return "Response is null";
        }

        int totalItems = response.getTotalItems();
        int returnedItems = response.getResultCount();
        int validItems = 0;

        if (response.hasResults()) {
            validItems = filterValidBooks(response.getItems()).size();
        }

        return String.format("Total: %d, Returned: %d, Valid: %d",
                totalItems, returnedItems, validItems);
    }
}