package com.draker.swipetime.api;

import android.app.Application;
import android.util.Log;

import com.draker.swipetime.adapters.CardStackAdapter;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.models.ContentItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для загрузки контента из API и обновления адаптера
 */
public class ApiContentLoader {
    private static final String TAG = "ApiContentLoader";
    private final ApiManager apiManager;
    private final CardStackAdapter adapter;
    private String currentCategory;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    /**
     * Конструктор
     * @param application Application
     * @param adapter адаптер для карточек
     */
    public ApiContentLoader(Application application, CardStackAdapter adapter) {
        this.apiManager = new ApiManager(application);
        this.adapter = adapter;
    }

    /**
     * Загрузить контент для категории
     * @param category категория контента
     */
    public void loadContentForCategory(String category) {
        if (isLoading) {
            return;
        }

        this.currentCategory = category;
        this.currentPage = 1;
        this.isLastPage = false;
        this.isLoading = true;

        // Очищаем текущий список
        adapter.clear();

        // Загружаем данные из API
        apiManager.loadContentForCategory(category, currentPage, new ApiManager.ApiCallback<ContentEntity>() {
            @Override
            public void onSuccess(List<ContentEntity> data) {
                isLoading = false;
                
                if (data.isEmpty()) {
                    isLastPage = true;
                    return;
                }

                // Конвертируем и добавляем данные в адаптер
                List<ContentItem> items = new ArrayList<>();
                for (ContentEntity entity : data) {
                    items.add(convertEntityToContentItem(entity));
                }
                
                adapter.addItems(items);
                currentPage++;
            }

            @Override
            public void onError(Throwable error) {
                isLoading = false;
                Log.e(TAG, "Error loading content for category " + category + ": " + error.getMessage());
            }
        });
    }

    /**
     * Загрузить следующую страницу контента
     */
    public void loadNextPage() {
        if (isLoading || isLastPage) {
            return;
        }

        isLoading = true;

        apiManager.loadContentForCategory(currentCategory, currentPage, new ApiManager.ApiCallback<ContentEntity>() {
            @Override
            public void onSuccess(List<ContentEntity> data) {
                isLoading = false;
                
                if (data.isEmpty()) {
                    isLastPage = true;
                    return;
                }

                // Конвертируем и добавляем данные в адаптер
                List<ContentItem> items = new ArrayList<>();
                for (ContentEntity entity : data) {
                    items.add(convertEntityToContentItem(entity));
                }
                
                adapter.addItems(items);
                currentPage++;
            }

            @Override
            public void onError(Throwable error) {
                isLoading = false;
                Log.e(TAG, "Error loading next page for category " + currentCategory + ": " + error.getMessage());
            }
        });
    }

    /**
     * Поиск контента
     * @param query поисковый запрос
     */
    public void searchContent(String query) {
        if (isLoading) {
            return;
        }

        this.currentPage = 1;
        this.isLastPage = false;
        this.isLoading = true;

        // Очищаем текущий список
        adapter.clear();

        // Загружаем данные из API
        apiManager.searchContentForCategory(currentCategory, query, currentPage, new ApiManager.ApiCallback<ContentEntity>() {
            @Override
            public void onSuccess(List<ContentEntity> data) {
                isLoading = false;
                
                if (data.isEmpty()) {
                    isLastPage = true;
                    return;
                }

                // Конвертируем и добавляем данные в адаптер
                List<ContentItem> items = new ArrayList<>();
                for (ContentEntity entity : data) {
                    items.add(convertEntityToContentItem(entity));
                }
                
                adapter.addItems(items);
                currentPage++;
            }

            @Override
            public void onError(Throwable error) {
                isLoading = false;
                Log.e(TAG, "Error searching content for category " + currentCategory + ": " + error.getMessage());
            }
        });
    }

    /**
     * Конвертировать сущность в модель для отображения
     * @param entity сущность контента
     * @return модель для отображения
     */
    private ContentItem convertEntityToContentItem(ContentEntity entity) {
        ContentItem item = new ContentItem(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getCategory()
        );
        item.setLiked(entity.isLiked());
        item.setWatched(entity.isWatched());
        item.setRating(entity.getRating());
        return item;
    }

    /**
     * Получить текущую категорию
     * @return текущая категория
     */
    public String getCurrentCategory() {
        return currentCategory;
    }

    /**
     * Проверить, загружается ли контент
     * @return true, если контент загружается
     */
    public boolean isLoading() {
        return isLoading;
    }

    /**
     * Проверить, достигнута ли последняя страница
     * @return true, если достигнута последняя страница
     */
    public boolean isLastPage() {
        return isLastPage;
    }

    /**
     * Очистить ресурсы
     */
    public void clear() {
        apiManager.clear();
    }
}