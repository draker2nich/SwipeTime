package com.draker.swipetime.fragments;

import android.util.Log;

import com.draker.swipetime.api.ApiContentLoader;
import com.draker.swipetime.api.ApiManager;
import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.repository.AnimeRepository;
import com.draker.swipetime.repository.BookRepository;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.GameRepository;
import com.draker.swipetime.repository.MovieRepository;
import com.draker.swipetime.repository.TVShowRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Вспомогательный класс для работы CardStackFragment с внешними API
 */
public class CardStackFragmentHelper {
    private static final String TAG = "CardStackFragmentHelper";

    /**
     * Интерфейс для обратного вызова после загрузки данных из API
     */
    public interface ApiLoadCallback {
        void onComplete(boolean success);
        void onError(String errorMessage);
    }

    /**
     * Загрузить данные из внешних API для указанной категории
     * @param categoryName название категории
     * @param apiManager менеджер API
     * @param movieRepository репозиторий фильмов
     * @param tvShowRepository репозиторий сериалов
     * @param gameRepository репозиторий игр
     * @param bookRepository репозиторий книг
     * @param animeRepository репозиторий аниме
     * @param callback обратный вызов по завершении загрузки
     */
    public static void loadApiDataForCategory(
            String categoryName,
            ApiManager apiManager,
            MovieRepository movieRepository,
            TVShowRepository tvShowRepository,
            GameRepository gameRepository,
            BookRepository bookRepository,
            AnimeRepository animeRepository,
            ApiLoadCallback callback
    ) {
        // Определяем тип контента по названию категории
        String contentType = convertCategoryNameToContentType(categoryName);
        
        if (contentType == null) {
            Log.e(TAG, "Неизвестная категория: " + categoryName);
            callback.onError("Неизвестная категория: " + categoryName);
            return;
        }
        
        Log.d(TAG, "Начало загрузки данных для категории: " + categoryName + " (тип: " + contentType + ")");
        
        // Проверяем, есть ли уже данные в базе данных
        boolean hasData = checkIfDataExistsForCategory(
                contentType,
                movieRepository,
                tvShowRepository,
                gameRepository,
                bookRepository,
                animeRepository
        );
        
        if (hasData) {
            Log.d(TAG, "Данные для категории " + categoryName + " уже есть в базе данных");
            callback.onComplete(true);
            return;
        }
        
        // Загружаем данные из API
        final AtomicInteger loadedPages = new AtomicInteger(0);
        final int pagesToLoad = 3; // Загружаем первые 3 страницы данных
        
        for (int page = 1; page <= pagesToLoad; page++) {
            final int currentPage = page;
            apiManager.loadContentForCategory(contentType, currentPage, new ApiManager.ApiCallback<ContentEntity>() {
                @Override
                public void onSuccess(List<ContentEntity> data) {
                    Log.d(TAG, "Загружена страница " + currentPage + " для категории " + categoryName + ": " + data.size() + " элементов");
                    
                    // Если все страницы загружены, вызываем обратный вызов
                    if (loadedPages.incrementAndGet() >= pagesToLoad) {
                        callback.onComplete(true);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    Log.e(TAG, "Ошибка загрузки данных для категории " + categoryName + ": " + error.getMessage());
                    
                    // Если хотя бы одна страница загружена, считаем загрузку успешной
                    if (loadedPages.get() > 0) {
                        callback.onComplete(true);
                    } else {
                        callback.onError("Ошибка загрузки данных: " + error.getMessage());
                    }
                }
            });
        }
    }
    
    /**
     * Преобразовать название категории в тип контента
     * @param categoryName название категории
     * @return тип контента или null, если категория неизвестна
     */
    private static String convertCategoryNameToContentType(String categoryName) {
        switch (categoryName.toLowerCase()) {
            case "фильмы":
                return "movie";
            case "сериалы":
                return "tv_show";
            case "игры":
                return "game";
            case "книги":
                return "book";
            case "аниме":
                return "anime";
            default:
                return null;
        }
    }
    
    /**
     * Проверить, есть ли данные для указанной категории в базе данных
     * @param contentType тип контента
     * @param movieRepository репозиторий фильмов
     * @param tvShowRepository репозиторий сериалов
     * @param gameRepository репозиторий игр
     * @param bookRepository репозиторий книг
     * @param animeRepository репозиторий аниме
     * @return true, если данные есть в базе данных
     */
    private static boolean checkIfDataExistsForCategory(
            String contentType,
            MovieRepository movieRepository,
            TVShowRepository tvShowRepository,
            GameRepository gameRepository,
            BookRepository bookRepository,
            AnimeRepository animeRepository
    ) {
        int count = 0;
        
        switch (contentType) {
            case "movie":
                count = movieRepository.getCount();
                break;
            case "tv_show":
                count = tvShowRepository.getCount();
                break;
            case "game":
                count = gameRepository.getCount();
                break;
            case "book":
                count = bookRepository.getCount();
                break;
            case "anime":
                count = animeRepository.getCount();
                break;
        }
        
        // Считаем, что данные есть, если в базе есть хотя бы 10 элементов
        return count >= 10;
    }
    
    /**
     * Поиск контента по запросу для указанной категории
     * @param query поисковый запрос
     * @param categoryName название категории
     * @param apiManager менеджер API
     * @param callback обратный вызов по завершении загрузки
     */
    public static void searchContentForCategory(
            String query,
            String categoryName,
            ApiManager apiManager,
            ApiLoadCallback callback
    ) {
        // Определяем тип контента по названию категории
        String contentType = convertCategoryNameToContentType(categoryName);
        
        if (contentType == null) {
            Log.e(TAG, "Неизвестная категория: " + categoryName);
            callback.onError("Неизвестная категория: " + categoryName);
            return;
        }
        
        Log.d(TAG, "Поиск контента по запросу: " + query + " для категории: " + categoryName);
        
        // Выполняем поиск
        apiManager.searchContentForCategory(contentType, query, 1, new ApiManager.ApiCallback<ContentEntity>() {
            @Override
            public void onSuccess(List<ContentEntity> data) {
                Log.d(TAG, "Найдено элементов: " + data.size());
                callback.onComplete(true);
            }

            @Override
            public void onError(Throwable error) {
                Log.e(TAG, "Ошибка поиска контента: " + error.getMessage());
                callback.onError("Ошибка поиска контента: " + error.getMessage());
            }
        });
    }
}