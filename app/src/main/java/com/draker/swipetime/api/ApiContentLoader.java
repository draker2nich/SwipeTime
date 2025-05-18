package com.draker.swipetime.api;

import android.app.Application;
import android.util.Log;

import com.draker.swipetime.adapters.CardStackAdapter;
import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.utils.ContentShuffler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    
    // Набор для хранения ID загруженных элементов
    private final Set<String> loadedItemIds = new HashSet<>();

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
        
        // Сбрасываем кеш для новой категории
        apiManager.resetCategoryCache(category);
        loadedItemIds.clear();
        
        // Сбрасываем перемешивание для категории
        ContentShuffler.resetHistory(category);

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
                    // Проверяем, не был ли этот элемент уже загружен
                    if (!loadedItemIds.contains(entity.getId())) {
                        items.add(convertEntityToContentItem(entity));
                        loadedItemIds.add(entity.getId());
                    }
                }
                
                // Перемешиваем элементы для разнообразия
                List<ContentItem> shuffledItems = ContentShuffler.shuffleContent(items, category);
                
                adapter.addItems(shuffledItems);
                currentPage++;
                
                // Если полученных элементов мало, загружаем еще
                if (items.size() < 10) {
                    loadNextPage();
                }
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
                    // Проверяем, не был ли этот элемент уже загружен
                    if (!loadedItemIds.contains(entity.getId())) {
                        items.add(convertEntityToContentItem(entity));
                        loadedItemIds.add(entity.getId());
                    }
                }
                
                if (items.isEmpty()) {
                    // Если все элементы уже были загружены, пробуем следующую страницу
                    currentPage++;
                    loadNextPage();
                    return;
                }
                
                // Перемешиваем элементы для разнообразия
                List<ContentItem> shuffledItems = ContentShuffler.shuffleContent(items, currentCategory);
                
                adapter.addItems(shuffledItems);
                currentPage++;
                
                Log.d(TAG, "Добавлено " + items.size() + " новых элементов контента, текущая страница: " + currentPage);
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
        
        // Сбрасываем кеш для новых результатов поиска
        loadedItemIds.clear();

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
                    // Проверяем, не был ли этот элемент уже загружен
                    if (!loadedItemIds.contains(entity.getId())) {
                        items.add(convertEntityToContentItem(entity));
                        loadedItemIds.add(entity.getId());
                    }
                }
                
                adapter.addItems(items);
                currentPage++;
                
                Log.d(TAG, "Поиск по запросу '" + query + "' вернул " + items.size() + " результатов");
                
                // Если результатов мало, загружаем еще
                if (items.size() < 5) {
                    loadNextSearchPage(query);
                }
            }

            @Override
            public void onError(Throwable error) {
                isLoading = false;
                Log.e(TAG, "Error searching content for category " + currentCategory + ": " + error.getMessage());
            }
        });
    }
    
    /**
     * Загрузить следующую страницу результатов поиска
     * @param query поисковый запрос
     */
    private void loadNextSearchPage(String query) {
        if (isLoading || isLastPage) {
            return;
        }

        isLoading = true;

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
                    // Проверяем, не был ли этот элемент уже загружен
                    if (!loadedItemIds.contains(entity.getId())) {
                        items.add(convertEntityToContentItem(entity));
                        loadedItemIds.add(entity.getId());
                    }
                }
                
                if (items.isEmpty()) {
                    // Если все элементы уже были загружены, пробуем следующую страницу
                    currentPage++;
                    loadNextSearchPage(query);
                    return;
                }
                
                adapter.addItems(items);
                currentPage++;
                
                Log.d(TAG, "Добавлено " + items.size() + " дополнительных результатов поиска, текущая страница: " + currentPage);
            }

            @Override
            public void onError(Throwable error) {
                isLoading = false;
                Log.e(TAG, "Error loading next search page for category " + currentCategory + ": " + error.getMessage());
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
        
        // Добавляем детали в зависимости от типа контента
        String contentType = entity.getContentType();
        
        if (contentType == null) {
            contentType = "";
        }
        
        if (contentType.equals("movie") && entity instanceof MovieEntity) {
            MovieEntity movie = (MovieEntity) entity;
            
            if (movie.getGenres() != null) {
                item.setGenre(movie.getGenres());
            }
            
            if (movie.getReleaseYear() > 0) {
                item.setYear(movie.getReleaseYear());
            }
            
            if (movie.getDirector() != null) {
                item.setDirector(movie.getDirector());
            }
        } 
        else if (contentType.equals("tv_show") && entity instanceof TVShowEntity) {
            TVShowEntity tvShow = (TVShowEntity) entity;
            
            if (tvShow.getGenres() != null) {
                item.setGenre(tvShow.getGenres());
            }
            
            if (tvShow.getStartYear() > 0) {
                item.setYear(tvShow.getStartYear());
            }
            
            item.setSeasons(tvShow.getSeasons());
            item.setEpisodes(tvShow.getEpisodes());
        } 
        else if (contentType.equals("game") && entity instanceof GameEntity) {
            GameEntity game = (GameEntity) entity;
            
            if (game.getGenres() != null) {
                item.setGenre(game.getGenres());
            }
            
            if (game.getReleaseYear() > 0) {
                item.setYear(game.getReleaseYear());
            }
            
            if (game.getDeveloper() != null) {
                item.setDeveloper(game.getDeveloper());
            }
            
            if (game.getPlatforms() != null) {
                item.setPlatforms(game.getPlatforms());
            }
        } 
        else if (contentType.equals("book") && entity instanceof BookEntity) {
            BookEntity book = (BookEntity) entity;
            
            if (book.getGenres() != null) {
                item.setGenre(book.getGenres());
            }
            
            if (book.getPublishYear() > 0) {
                item.setYear(book.getPublishYear());
            }
            
            if (book.getAuthor() != null) {
                item.setAuthor(book.getAuthor());
            }
            
            if (book.getPublisher() != null) {
                item.setPublisher(book.getPublisher());
            }
            
            item.setPages(book.getPageCount());
        } 
        else if (contentType.equals("anime") && entity instanceof AnimeEntity) {
            AnimeEntity anime = (AnimeEntity) entity;
            
            if (anime.getGenres() != null) {
                item.setGenre(anime.getGenres());
            }
            
            if (anime.getReleaseYear() > 0) {
                item.setYear(anime.getReleaseYear());
            }
            
            if (anime.getStudio() != null) {
                item.setStudio(anime.getStudio());
            }
            
            item.setEpisodes(anime.getEpisodes());
        }
        
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
     * Отметить карточку как просмотренную
     * @param itemId ID элемента
     */
    public void markCardAsViewed(String itemId) {
        if (itemId != null && !itemId.isEmpty()) {
            loadedItemIds.add(itemId);
            ContentShuffler.markContentAsShown(currentCategory, itemId);
        }
    }
    
    /**
     * Сбросить историю просмотра для текущей категории
     */
    public void resetViewHistory() {
        loadedItemIds.clear();
        ContentShuffler.resetHistory(currentCategory);
        Log.d(TAG, "История просмотра сброшена для категории: " + currentCategory);
    }

    /**
     * Очистить ресурсы
     */
    public void clear() {
        apiManager.clear();
    }
}