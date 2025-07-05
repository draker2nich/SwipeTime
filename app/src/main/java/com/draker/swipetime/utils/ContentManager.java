package com.draker.swipetime.utils;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.draker.swipetime.R;
import com.draker.swipetime.api.ApiIntegrationManager;
import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.database.entities.UserPreferencesEntity;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.recommendations.RecommendationService;
import com.draker.swipetime.repository.AnimeRepository;
import com.draker.swipetime.repository.BookRepository;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.GameRepository;
import com.draker.swipetime.repository.MovieRepository;
import com.draker.swipetime.repository.TVShowRepository;
import com.draker.swipetime.repository.UserPreferencesRepository;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Объединенный класс для управления контентом, включая:
 * - Фильтрацию карточек
 * - Помощь с фильтрами контента
 * - Перемешивание контента
 * - Бесконечный контент-менеджер
 * - Помощь с избранными элементами
 */
public class ContentManager {
    private static final String TAG = "ContentManager";

    // Singleton instance
    private static ContentManager instance;

    // Кэш для хранения доступного контента по категориям
    private final Map<String, List<ContentItem>> contentCache = new ConcurrentHashMap<>();

    // Флаги для отслеживания загрузки для каждой категории
    private final Map<String, AtomicBoolean> isLoadingMap = new ConcurrentHashMap<>();

    // Постоянное хранилище уже показанных ID элементов для каждой категории
    private static final Map<String, Set<String>> shownContentIds = new HashMap<>();

    // Транзакционное хранилище (для текущей сессии) показанных ID
    private static final Map<String, Set<String>> sessionShownIds = new HashMap<>();

    // Хранилище последних использованных порядков элементов
    private static final Map<String, List<String>> lastShuffleOrders = new HashMap<>();

    // Константы
    private static final int MIN_CACHE_THRESHOLD = 10;
    private final Random random = new Random();

    // Менеджер для постоянного хранения истории просмотров
    private DatabaseHelper databaseHelper;

    private ContentManager() {
        // Инициализация кэшей и истории
        for (String category : new String[]{"Фильмы", "Сериалы", "Игры", "Книги", "Аниме"}) {
            contentCache.put(category, Collections.synchronizedList(new ArrayList<>()));
            isLoadingMap.put(category, new AtomicBoolean(false));
        }
    }

    public static synchronized ContentManager getInstance() {
        if (instance == null) {
            instance = new ContentManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        this.databaseHelper = DatabaseHelper.getInstance(context);
    }

    // ==================== CARD FILTER INTEGRATION SECTION ====================

    /**
     * Получает отфильтрованный список элементов для отображения в CardStack
     */
    public List<ContentItem> getFilteredContentItems(
            String category,
            String userId,
            MovieRepository movieRepository,
            TVShowRepository tvShowRepository,
            GameRepository gameRepository,
            BookRepository bookRepository,
            AnimeRepository animeRepository,
            ContentRepository contentRepository,
            UserPreferencesRepository preferencesRepository) {

        // Получаем предпочтения пользователя
        UserPreferencesEntity preferences = preferencesRepository.getByUserId(userId);

        // Получаем исходный список элементов в зависимости от категории
        List<ContentEntity> contentEntities = new ArrayList<>();

        switch (category) {
            case "Фильмы":
                List<MovieEntity> movies = movieRepository.getAll();
                contentEntities.addAll(movies);
                break;
            case "Сериалы":
                List<TVShowEntity> tvShows = tvShowRepository.getAll();
                contentEntities.addAll(tvShows);
                break;
            case "Игры":
                List<GameEntity> games = gameRepository.getAll();
                contentEntities.addAll(games);
                break;
            case "Книги":
                List<BookEntity> books = bookRepository.getAll();
                contentEntities.addAll(books);
                break;
            case "Аниме":
                List<AnimeEntity> animes = animeRepository.getAll();
                contentEntities.addAll(animes);
                break;
            default:
                contentEntities = contentRepository.getByCategory(category);
                break;
        }

        Log.d(TAG, "Загружено элементов категории " + category + ": " + contentEntities.size());

        // Если есть предпочтения, применяем фильтрацию
        List<ContentEntity> filteredEntities;
        if (preferences != null && hasActiveFilters(preferences)) {
            filteredEntities = filterContent(contentEntities, preferences);
            Log.d(TAG, "После фильтрации осталось: " + filteredEntities.size());
        } else {
            filteredEntities = contentEntities;
        }

        // Преобразуем отфильтрованные сущности в ContentItem
        List<ContentItem> resultItems = new ArrayList<>();
        for (ContentEntity entity : filteredEntities) {
            resultItems.add(new ContentItem(
                    entity.getId(),
                    entity.getTitle(),
                    entity.getDescription(),
                    entity.getImageUrl(),
                    entity.getCategory()
            ));
        }

        // Перемешиваем элементы для разнообразия отображения
        resultItems = shuffleContent(resultItems, category);
        Log.d(TAG, "Контент перемешан для предотвращения повторяющегося порядка");

        return resultItems;
    }

    /**
     * Получает отфильтрованный и отсортированный по релевантности список элементов с использованием ИИ
     */
    public List<ContentItem> getFilteredAndRecommendedContentItems(
            Application application,
            String category,
            String userId,
            MovieRepository movieRepository,
            TVShowRepository tvShowRepository,
            GameRepository gameRepository,
            BookRepository bookRepository,
            AnimeRepository animeRepository,
            ContentRepository contentRepository,
            UserPreferencesRepository preferencesRepository) {

        // Сначала получаем отфильтрованный список элементов
        List<ContentItem> filteredItems = getFilteredContentItems(
                category,
                userId,
                movieRepository,
                tvShowRepository,
                gameRepository,
                bookRepository,
                animeRepository,
                contentRepository,
                preferencesRepository
        );

        Log.d(TAG, "После фильтрации получено элементов: " + filteredItems.size());

        if (filteredItems.isEmpty()) {
            return filteredItems;
        }

        // Получаем сервис рекомендаций
        RecommendationService recommendationService = RecommendationService.getInstance(application);
        List<ContentItem> recommendedItems = recommendationService.sortByRelevance(application, filteredItems);

        Log.d(TAG, "После применения рекомендательной системы получено элементов: " + recommendedItems.size());
        return recommendedItems;
    }

    /**
     * Получает рекомендованный контент с ограничением количества
     */
    public List<ContentItem> getRecommendedContentItems(
            Application application,
            String category,
            String userId,
            int limit) {

        RecommendationService recommendationService = RecommendationService.getInstance(application);
        List<ContentItem> recommendedItems = recommendationService.getRecommendationsForCurrentUser(application, category, limit);

        Log.d(TAG, "Получено " + recommendedItems.size() + " рекомендаций для пользователя " + userId);
        return recommendedItems;
    }

    // ==================== CONTENT FILTER HELPER SECTION ====================

    /**
     * Проверяет, есть ли активные фильтры у пользователя
     */
    public boolean hasActiveFilters(UserPreferencesEntity preferences) {
        if (preferences == null) {
            return false;
        }

        return (preferences.getPreferredGenres() != null && !preferences.getPreferredGenres().isEmpty()) ||
                (preferences.getPreferredCountries() != null && !preferences.getPreferredCountries().isEmpty()) ||
                (preferences.getPreferredLanguages() != null && !preferences.getPreferredLanguages().isEmpty()) ||
                (preferences.getInterestsTags() != null && !preferences.getInterestsTags().isEmpty()) ||
                preferences.getMinDuration() > 0 ||
                preferences.getMaxDuration() < Integer.MAX_VALUE ||
                preferences.getMinYear() > 1900 ||
                preferences.getMaxYear() < 2025 ||
                preferences.isAdultContentEnabled();
    }

    /**
     * Проверяет, содержит ли JSON-строка указанное значение
     */
    public boolean jsonContains(String jsonString, String valueToCheck) {
        if (jsonString == null || jsonString.isEmpty() || valueToCheck == null) {
            return false;
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                if (valueToCheck.equalsIgnoreCase(jsonArray.getString(i))) {
                    return true;
                }
            }
        } catch (JSONException e) {
            return false;
        }

        return false;
    }

    /**
     * Применяет фильтры пользователя к списку контента
     */
    public List<ContentEntity> filterContent(List<ContentEntity> contentList, UserPreferencesEntity preferences) {
        if (contentList == null || contentList.isEmpty() || preferences == null) {
            return contentList;
        }

        List<ContentEntity> filteredList = new ArrayList<>();

        try {
            List<String> preferredGenres = parseJsonArray(preferences.getPreferredGenres());
            List<String> preferredCountries = parseJsonArray(preferences.getPreferredCountries());
            List<String> preferredLanguages = parseJsonArray(preferences.getPreferredLanguages());
            List<String> interestsTags = parseJsonArray(preferences.getInterestsTags());

            if (preferredGenres.isEmpty() && preferredCountries.isEmpty() &&
                    preferredLanguages.isEmpty() && interestsTags.isEmpty() &&
                    preferences.getMinYear() <= 1900 && preferences.getMaxYear() >= 2100 &&
                    preferences.getMinDuration() <= 0 && preferences.getMaxDuration() >= Integer.MAX_VALUE) {
                return contentList;
            }

            for (ContentEntity content : contentList) {
                boolean include = true;

                if (content instanceof MovieEntity) {
                    MovieEntity movie = (MovieEntity) content;

                    if (!preferredGenres.isEmpty() && !containsAny(movie.getGenres(), preferredGenres)) {
                        include = false;
                    }

                    if (movie.getReleaseYear() < preferences.getMinYear() ||
                            movie.getReleaseYear() > preferences.getMaxYear()) {
                        include = false;
                    }

                    if (movie.getDuration() < preferences.getMinDuration() ||
                            movie.getDuration() > preferences.getMaxDuration()) {
                        include = false;
                    }
                }
                else if (content instanceof TVShowEntity) {
                    TVShowEntity tvShow = (TVShowEntity) content;

                    if (!preferredGenres.isEmpty() && !containsAny(tvShow.getGenres(), preferredGenres)) {
                        include = false;
                    }

                    if (tvShow.getStartYear() < preferences.getMinYear() ||
                            tvShow.getStartYear() > preferences.getMaxYear()) {
                        include = false;
                    }
                }
                else if (content instanceof GameEntity) {
                    GameEntity game = (GameEntity) content;

                    if (!preferredGenres.isEmpty() && !containsAny(game.getGenres(), preferredGenres)) {
                        include = false;
                    }

                    if (game.getReleaseYear() < preferences.getMinYear() ||
                            game.getReleaseYear() > preferences.getMaxYear()) {
                        include = false;
                    }

                    if (!preferences.isAdultContentEnabled() && isAdultRated(game.getEsrbRating())) {
                        include = false;
                    }
                }
                else if (content instanceof BookEntity) {
                    BookEntity book = (BookEntity) content;

                    if (!preferredGenres.isEmpty() && !containsAny(book.getGenres(), preferredGenres)) {
                        include = false;
                    }

                    if (book.getPublishYear() < preferences.getMinYear() ||
                            book.getPublishYear() > preferences.getMaxYear()) {
                        include = false;
                    }
                }
                else if (content instanceof AnimeEntity) {
                    AnimeEntity anime = (AnimeEntity) content;

                    if (!preferredGenres.isEmpty() && !containsAny(anime.getGenres(), preferredGenres)) {
                        include = false;
                    }

                    if (anime.getReleaseYear() < preferences.getMinYear() ||
                            anime.getReleaseYear() > preferences.getMaxYear()) {
                        include = false;
                    }
                }

                if (include) {
                    filteredList.add(content);
                }
            }

            return filteredList;

        } catch (Exception e) {
            Log.e(TAG, "Error filtering content: " + e.getMessage());
            e.printStackTrace();
            return contentList;
        }
    }

    // ==================== CONTENT SHUFFLER SECTION ====================

    /**
     * Перемешать список элементов контента, избегая повторения последнего порядка
     */
    public List<ContentItem> shuffleContent(List<ContentItem> items, String categoryName) {
        if (items == null || items.isEmpty()) {
            return items;
        }

        Set<String> permanentShownIds = shownContentIds.computeIfAbsent(categoryName, k -> new HashSet<>());
        Set<String> currentShownIds = sessionShownIds.computeIfAbsent(categoryName, k -> new HashSet<>());

        Set<String> allShownIds = new HashSet<>(permanentShownIds);
        allShownIds.addAll(currentShownIds);

        List<ContentItem> uniqueItems = new ArrayList<>();
        List<ContentItem> previouslyShownItems = new ArrayList<>();

        for (ContentItem item : items) {
            if (allShownIds.contains(item.getId())) {
                previouslyShownItems.add(item);
            } else {
                uniqueItems.add(item);
            }
        }

        Log.d(TAG, "Категория " + categoryName + ": элементов всего " + items.size() +
                ", новых " + uniqueItems.size() + ", показанных " + previouslyShownItems.size());

        if (uniqueItems.isEmpty()) {
            Log.d(TAG, "Все элементы категории " + categoryName + " уже были показаны. Сбрасываем историю.");
            sessionShownIds.get(categoryName).clear();
            uniqueItems = new ArrayList<>(items);
            previouslyShownItems.clear();
        }

        Collections.shuffle(uniqueItems);

        if (uniqueItems.size() <= 10) {
            intensiveShuffle(uniqueItems);
        }

        List<String> newOrder = extractIds(uniqueItems);
        if (lastShuffleOrders.containsKey(categoryName) &&
                isSimilarOrder(lastShuffleOrders.get(categoryName), newOrder)) {
            Log.d(TAG, "Обнаружен похожий порядок элементов, применяем интенсивное перемешивание");
            intensiveShuffle(uniqueItems);
            newOrder = extractIds(uniqueItems);
        }

        Set<String> sessionIds = sessionShownIds.get(categoryName);
        for (ContentItem item : uniqueItems) {
            sessionIds.add(item.getId());
        }
        lastShuffleOrders.put(categoryName, newOrder);

        List<ContentItem> result = new ArrayList<>(uniqueItems);
        Log.d(TAG, "Перемешивание завершено для категории " + categoryName +
                ", элементов в результате: " + result.size());

        return result;
    }

    // ==================== INFINITE CONTENT MANAGER SECTION ====================

    /**
     * Получить следующую партию элементов для отображения
     */
    public void getNextBatch(String category, String userId, int count, Context context,
                             ContentCallback callback) {

        List<ContentItem> categoryCache = contentCache.get(category);

        if (categoryCache != null && categoryCache.size() >= count) {
            List<ContentItem> batch = extractBatch(category, count, context);
            callback.onContentLoaded(batch);

            if (categoryCache.size() < MIN_CACHE_THRESHOLD) {
                loadMoreContent(category, context);
            }
        } else {
            loadMoreContent(category, context, () -> {
                List<ContentItem> batch = extractBatch(category, count, context);

                if (batch.isEmpty()) {
                    batch = createDiverseSyntheticItems(category, count);
                }

                callback.onContentLoaded(batch);
            });
        }
    }

    // ==================== LIKED ITEMS HELPER SECTION ====================

    /**
     * Добавляет элемент в избранное в соответствующей категории
     */
    public void addToLiked(ContentItem item, String categoryName,
                           MovieRepository movieRepository,
                           TVShowRepository tvShowRepository,
                           GameRepository gameRepository,
                           BookRepository bookRepository,
                           AnimeRepository animeRepository,
                           ContentRepository contentRepository) {
        try {
            Log.d(TAG, "Добавление элемента в избранное: " + item.getTitle() + " (ID: " + item.getId() + ")");

            ContentEntity existingContent = contentRepository.getById(item.getId());
            if (existingContent != null) {
                existingContent.setLiked(true);
                contentRepository.update(existingContent);
                Log.d(TAG, "Общий контент обновлен: " + existingContent.getTitle() + " (категория: " + existingContent.getCategory() + ")");
            } else {
                createNewContentEntity(item, categoryName, contentRepository);
            }

            switch (categoryName) {
                case "Фильмы":
                    MovieEntity movie = movieRepository.getById(item.getId());
                    if (movie != null) {
                        movie.setLiked(true);
                        movieRepository.update(movie);
                        Log.d(TAG, "Фильм обновлен в избранном: " + movie.getTitle());
                    } else {
                        createNewMovieEntity(item, categoryName, movieRepository);
                    }
                    break;
                case "Сериалы":
                    TVShowEntity tvShow = tvShowRepository.getById(item.getId());
                    if (tvShow != null) {
                        tvShow.setLiked(true);
                        tvShowRepository.update(tvShow);
                        Log.d(TAG, "Сериал обновлен в избранном: " + tvShow.getTitle());
                    } else {
                        createNewTVShowEntity(item, categoryName, tvShowRepository);
                    }
                    break;
                case "Игры":
                    GameEntity game = gameRepository.getById(item.getId());
                    if (game != null) {
                        game.setLiked(true);
                        gameRepository.update(game);
                        Log.d(TAG, "Игра обновлена в избранном: " + game.getTitle());
                    } else {
                        createNewGameEntity(item, categoryName, gameRepository);
                    }
                    break;
                case "Книги":
                    BookEntity book = bookRepository.getById(item.getId());
                    if (book != null) {
                        book.setLiked(true);
                        bookRepository.update(book);
                        Log.d(TAG, "Книга обновлена в избранном: " + book.getTitle());
                    } else {
                        createNewBookEntity(item, categoryName, bookRepository);
                    }
                    break;
                case "Аниме":
                    AnimeEntity anime = animeRepository.getById(item.getId());
                    if (anime != null) {
                        anime.setLiked(true);
                        animeRepository.update(anime);
                        Log.d(TAG, "Аниме обновлено в избранном: " + anime.getTitle());
                    } else {
                        createNewAnimeEntity(item, categoryName, animeRepository);
                    }
                    break;
                default:
                    ContentEntity content = contentRepository.getById(item.getId());
                    if (content != null) {
                        content.setLiked(true);
                        contentRepository.update(content);
                        Log.d(TAG, "Контент обновлен в избранном: " + content.getTitle() + " (категория: " + content.getCategory() + ")");
                    } else {
                        createNewContentEntity(item, categoryName, contentRepository);
                    }
                    break;
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при добавлении в избранное: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Обновляет статус "нравится" в базе данных для элемента
     */
    public void updateLikedStatus(String id, boolean liked, String categoryName,
                                  MovieRepository movieRepository,
                                  TVShowRepository tvShowRepository,
                                  GameRepository gameRepository,
                                  BookRepository bookRepository,
                                  AnimeRepository animeRepository,
                                  ContentRepository contentRepository) {
        if (id != null && !id.isEmpty()) {
            try {
                Log.d(TAG, "Обновляем статус liked=" + liked + " для id=" + id + " в категории " + categoryName);

                boolean updated = false;

                switch (categoryName) {
                    case "Фильмы":
                        MovieEntity movie = movieRepository.getById(id);
                        if (movie != null) {
                            movie.setLiked(liked);
                            movieRepository.update(movie);
                            updated = true;
                            Log.d(TAG, "Обновлен статус liked в MovieEntity");
                        }
                        break;
                    case "Сериалы":
                        TVShowEntity tvShow = tvShowRepository.getById(id);
                        if (tvShow != null) {
                            tvShow.setLiked(liked);
                            tvShowRepository.update(tvShow);
                            updated = true;
                            Log.d(TAG, "Обновлен статус liked в TVShowEntity");
                        }
                        break;
                    case "Игры":
                        GameEntity game = gameRepository.getById(id);
                        if (game != null) {
                            game.setLiked(liked);
                            gameRepository.update(game);
                            updated = true;
                            Log.d(TAG, "Обновлен статус liked в GameEntity");
                        }
                        break;
                    case "Книги":
                        BookEntity book = bookRepository.getById(id);
                        if (book != null) {
                            book.setLiked(liked);
                            bookRepository.update(book);
                            updated = true;
                            Log.d(TAG, "Обновлен статус liked в BookEntity");
                        }
                        break;
                    case "Аниме":
                        AnimeEntity anime = animeRepository.getById(id);
                        if (anime != null) {
                            anime.setLiked(liked);
                            animeRepository.update(anime);
                            updated = true;
                            Log.d(TAG, "Обновлен статус liked в AnimeEntity");
                        }
                        break;
                    default:
                        ContentEntity content = contentRepository.getById(id);
                        if (content != null) {
                            content.setLiked(liked);
                            contentRepository.update(content);
                            updated = true;
                            Log.d(TAG, "Обновлен статус liked в ContentEntity");
                        }
                        break;
                }

                if (!updated) {
                    Log.w(TAG, "Не удалось обновить статус liked ни для одной сущности с id=" + id);
                }

                ContentEntity generalContent = contentRepository.getById(id);
                if (generalContent != null) {
                    generalContent.setLiked(liked);
                    contentRepository.update(generalContent);
                    Log.d(TAG, "Обновлен статус liked в общей таблице контента");
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при обновлении статуса liked: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Возвращает соответствующую надпись для переключателя "просмотрено/прочитано"
     */
    public String getWatchedSwitchLabel(String category, Context context) {
        if (category == null || category.isEmpty()) {
            return context.getString(R.string.watched_status);
        }

        switch (category) {
            case "Фильмы":
                return context.getString(R.string.movie_watched_status);
            case "Сериалы":
                return context.getString(R.string.tvshow_watched_status);
            case "Игры":
                return context.getString(R.string.game_completed_status);
            case "Книги":
                return context.getString(R.string.book_read_status);
            case "Аниме":
                return context.getString(R.string.anime_watched_status);
            case "Музыка":
                return context.getString(R.string.music_listened_status);
            default:
                return context.getString(R.string.watched_status);
        }
    }

    /**
     * Получает соответствующий ресурс иконки для отметки "просмотрено/прочитано"
     */
    public int getWatchedIcon(String category) {
        if (category == null || category.isEmpty()) {
            return R.drawable.ic_check;
        }

        switch (category) {
            case "Фильмы":
            case "Сериалы":
            case "Аниме":
                return R.drawable.ic_watched;
            case "Книги":
                return R.drawable.ic_read;
            case "Игры":
                return R.drawable.ic_completed;
            case "Музыка":
                return R.drawable.ic_listened;
            default:
                return R.drawable.ic_check;
        }
    }

    /**
     * Обрабатывает событие свайпа для обновления рекомендательной системы
     */
    public void handleSwipeEvent(Application application, String contentId, boolean liked) {
        RecommendationService recommendationService = RecommendationService.getInstance(application);
        recommendationService.handleSwipeEvent(application, contentId, liked);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private boolean containsAny(String commaString, List<String> values) {
        if (commaString == null || commaString.isEmpty() || values == null || values.isEmpty()) {
            return false;
        }

        String[] items = commaString.split(",");
        for (String item : items) {
            String trimmed = item.trim();
            if (values.contains(trimmed)) {
                return true;
            }
        }

        return false;
    }

    private boolean isAdultRated(String rating) {
        if (rating == null) {
            return false;
        }

        return rating.equals("M") || rating.equals("AO") ||
                rating.equals("18+") || rating.equals("NC-17") ||
                rating.equals("R18+");
    }

    private List<String> parseJsonArray(String jsonString) {
        List<String> result = new ArrayList<>();

        if (jsonString == null || jsonString.isEmpty()) {
            return result;
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                result.add(jsonArray.getString(i).toLowerCase());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Ошибка при парсинге JSON: " + e.getMessage());
        }

        return result;
    }

    private void intensiveShuffle(List<ContentItem> items) {
        if (items.size() <= 1) return;

        int size = items.size();

        for (int i = size - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Collections.swap(items, i, j);
        }

        int halfSize = size / 2;
        for (int i = halfSize - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Collections.swap(items, i, j);
        }

        for (int i = size - 1; i >= halfSize; i--) {
            int j = halfSize + random.nextInt(size - halfSize);
            Collections.swap(items, i, j);
        }

        int blockSize = Math.max(3, size / 5);
        for (int block = 0; block < size / blockSize; block++) {
            int startIndex = block * blockSize;
            int endIndex = Math.min(startIndex + blockSize, size);
            for (int i = endIndex - 1; i > startIndex; i--) {
                int j = startIndex + random.nextInt(i - startIndex + 1);
                Collections.swap(items, i, j);
            }
        }
    }

    private List<String> extractIds(List<ContentItem> items) {
        List<String> ids = new ArrayList<>();
        for (ContentItem item : items) {
            ids.add(item.getId());
        }
        return ids;
    }

    private boolean isSimilarOrder(List<String> order1, List<String> order2) {
        if (order1 == null || order2 == null || order1.isEmpty() || order2.isEmpty()) {
            return false;
        }

        int matchCount = 0;
        int checkSize = Math.min(order1.size(), order2.size());
        checkSize = Math.min(checkSize, 20);

        for (int i = 0; i < checkSize; i++) {
            if (i < order1.size() && i < order2.size() && order1.get(i).equals(order2.get(i))) {
                matchCount += (checkSize - i) / (i + 1);
            }
        }

        int threshold = checkSize * 2 / 5;
        return matchCount >= threshold;
    }

    private synchronized List<ContentItem> extractBatch(String category, int count, Context context) {
        List<ContentItem> categoryCache = contentCache.get(category);
        if (categoryCache == null || categoryCache.isEmpty()) {
            return new ArrayList<>();
        }

        List<ContentItem> result = new ArrayList<>();
        int extractCount = Math.min(count, categoryCache.size());

        for (int i = 0; i < extractCount; i++) {
            ContentItem item = categoryCache.remove(0);
            result.add(item);

            if (databaseHelper != null) {
                databaseHelper.addToViewedHistory(category, item.getId(), false);
            }
        }

        return result;
    }

    private void loadMoreContent(String category, Context context) {
        loadMoreContent(category, context, null);
    }

    private void loadMoreContent(String category, Context context, Runnable onComplete) {
        AtomicBoolean isLoading = isLoadingMap.get(category);
        if (isLoading != null && isLoading.get()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        if (isLoading != null) {
            isLoading.set(true);
        }

        Log.d(TAG, "Загрузка дополнительного контента для категории: " + category);

        Application app = (Application) context.getApplicationContext();
        ApiIntegrationManager apiManager = ApiIntegrationManager.getInstance(app);

        apiManager.refreshCategoryContent(category, 30, new ApiIntegrationManager.ApiInitCallback() {
            @Override
            public void onComplete(boolean success) {
                if (success) {
                    Log.d(TAG, "Успешно загружен дополнительный контент для категории: " + category);
                    refreshCacheFromDatabase(category, context);
                } else {
                    Log.e(TAG, "Ошибка при загрузке дополнительного контента для категории: " + category);
                }

                if (isLoading != null) {
                    isLoading.set(false);
                }

                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Ошибка при загрузке контента для категории " + category + ": " + errorMessage);

                if (isLoading != null) {
                    isLoading.set(false);
                }

                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

    private void refreshCacheFromDatabase(String category, Context context) {
        Application app = (Application) context.getApplicationContext();
        MovieRepository movieRepository = new MovieRepository(app);
        TVShowRepository tvShowRepository = new TVShowRepository(app);
        GameRepository gameRepository = new GameRepository(app);
        BookRepository bookRepository = new BookRepository(app);
        AnimeRepository animeRepository = new AnimeRepository(app);
        ContentRepository contentRepository = new ContentRepository(app);

        List<ContentItem> newItems = new ArrayList<>();
        List<ContentEntity> entities = new ArrayList<>();

        switch (category) {
            case "Фильмы":
                for (MovieEntity entity : movieRepository.getAll()) {
                    entities.add(entity);
                }
                break;
            case "Сериалы":
                for (TVShowEntity entity : tvShowRepository.getAll()) {
                    entities.add(entity);
                }
                break;
            case "Игры":
                for (GameEntity entity : gameRepository.getAll()) {
                    entities.add(entity);
                }
                break;
            case "Книги":
                for (BookEntity entity : bookRepository.getAll()) {
                    entities.add(entity);
                }
                break;
            case "Аниме":
                for (AnimeEntity entity : animeRepository.getAll()) {
                    entities.add(entity);
                }
                break;
            default:
                entities = contentRepository.getByCategory(category);
                break;
        }

        Set<String> viewedItems = new HashSet<>();
        if (databaseHelper != null) {
            viewedItems = databaseHelper.getAllViewedItems(category);
        }

        for (ContentEntity entity : entities) {
            if (!viewedItems.contains(entity.getId())) {
                ContentItem item = DatabaseHelper.mapToContentItem(entity);
                newItems.add(item);
            }
        }

        if (newItems.isEmpty() && !entities.isEmpty()) {
            Log.d(TAG, "Все элементы категории " + category + " уже были показаны. Сбрасываем историю.");

            Set<String> likedItems = new HashSet<>();
            if (databaseHelper != null) {
                likedItems = databaseHelper.getLikedItems(category);
                databaseHelper.clearHistory(category);
            }

            for (ContentEntity entity : entities) {
                if (!likedItems.contains(entity.getId())) {
                    ContentItem item = DatabaseHelper.mapToContentItem(entity);
                    newItems.add(item);
                }
            }
        }

        Collections.shuffle(newItems, random);

        Log.d(TAG, "Добавлено " + newItems.size() + " новых элементов в кэш для категории " + category);

        List<ContentItem> cache = contentCache.get(category);
        if (cache != null) {
            cache.addAll(newItems);
        }
    }

    private List<ContentItem> createDiverseSyntheticItems(String category, int count) {
        Log.d(TAG, "Создание " + count + " синтетических элементов для категории " + category);

        List<ContentItem> syntheticItems = new ArrayList<>();

        String[] movieTitles = {
                "Неизведанные миры", "Последний рубеж", "Тайна прошлого", "В поисках истины",
                "Непокоренная вершина", "Следы времени", "Навстречу судьбе", "Тени прошлого"
        };

        String[] descriptions = {
                "Увлекательная история о путешествии в неизведанные земли и поиске сокровищ.",
                "Глубокое погружение в мир фантазии, где реальность переплетается с вымыслом.",
                "Захватывающее приключение с неожиданными поворотами сюжета и яркими персонажами."
        };

        for (int i = 0; i < count; i++) {
            String title = movieTitles[random.nextInt(movieTitles.length)] + " " + (random.nextInt(20) + 1);
            String description = descriptions[random.nextInt(descriptions.length)];
            String id = "synthetic_" + category.toLowerCase() + "_" + System.currentTimeMillis() + "_" + i;

            ContentItem item = new ContentItem(id, title, description, null, category);
            item.setYear(2020 + random.nextInt(6));
            item.setRating(3.5f + random.nextFloat() * 1.5f);

            syntheticItems.add(item);
        }

        return syntheticItems;
    }

    // Helper methods for creating new entities
    private void createNewMovieEntity(ContentItem item, String categoryName, MovieRepository movieRepository) {
        MovieEntity movie = new MovieEntity();
        movie.setId(item.getId());
        movie.setTitle(item.getTitle());
        movie.setDescription(item.getDescription());
        movie.setImageUrl(item.getImageUrl());
        movie.setCategory(categoryName);
        movie.setContentType("movie");
        movie.setLiked(true);
        movieRepository.insert(movie);
        Log.d(TAG, "Создан новый фильм в избранном: " + movie.getTitle());
    }

    private void createNewTVShowEntity(ContentItem item, String categoryName, TVShowRepository tvShowRepository) {
        TVShowEntity tvShow = new TVShowEntity();
        tvShow.setId(item.getId());
        tvShow.setTitle(item.getTitle());
        tvShow.setDescription(item.getDescription());
        tvShow.setImageUrl(item.getImageUrl());
        tvShow.setCategory(categoryName);
        tvShow.setContentType("tvshow");
        tvShow.setLiked(true);
        tvShowRepository.insert(tvShow);
        Log.d(TAG, "Создан новый сериал в избранном: " + tvShow.getTitle());
    }

    private void createNewGameEntity(ContentItem item, String categoryName, GameRepository gameRepository) {
        GameEntity game = new GameEntity();
        game.setId(item.getId());
        game.setTitle(item.getTitle());
        game.setDescription(item.getDescription());
        game.setImageUrl(item.getImageUrl());
        game.setCategory(categoryName);
        game.setContentType("game");
        game.setLiked(true);
        gameRepository.insert(game);
        Log.d(TAG, "Создана новая игра в избранном: " + game.getTitle());
    }

    private void createNewBookEntity(ContentItem item, String categoryName, BookRepository bookRepository) {
        BookEntity book = new BookEntity();
        book.setId(item.getId());
        book.setTitle(item.getTitle());
        book.setDescription(item.getDescription());
        book.setImageUrl(item.getImageUrl());
        book.setCategory(categoryName);
        book.setContentType("book");
        book.setLiked(true);
        bookRepository.insert(book);
        Log.d(TAG, "Создана новая книга в избранном: " + book.getTitle());
    }

    private void createNewAnimeEntity(ContentItem item, String categoryName, AnimeRepository animeRepository) {
        AnimeEntity anime = new AnimeEntity();
        anime.setId(item.getId());
        anime.setTitle(item.getTitle());
        anime.setDescription(item.getDescription());
        anime.setImageUrl(item.getImageUrl());
        anime.setCategory(categoryName);
        anime.setContentType("anime");
        anime.setLiked(true);
        animeRepository.insert(anime);
        Log.d(TAG, "Создано новое аниме в избранном: " + anime.getTitle());
    }

    private void createNewContentEntity(ContentItem item, String categoryName, ContentRepository contentRepository) {
        ContentEntity content = new ContentEntity();
        content.setId(item.getId());
        content.setTitle(item.getTitle());
        content.setDescription(item.getDescription());
        content.setImageUrl(item.getImageUrl());
        content.setCategory(categoryName);
        content.setContentType(categoryName.toLowerCase());
        content.setLiked(true);
        contentRepository.insert(content);
        Log.d(TAG, "Создан новый контент в избранном: " + content.getTitle() + " (категория: " + categoryName + ")");

        ContentEntity check = contentRepository.getById(item.getId());
        if (check != null) {
            Log.d(TAG, "Проверка: элемент найден в базе, liked=" + check.isLiked());
        } else {
            Log.e(TAG, "Ошибка: элемент не был добавлен в базу");
        }
    }

    // ==================== PUBLIC MANAGEMENT METHODS ====================

    /**
     * Отмечает элемент как понравившийся или не понравившийся
     */
    public void markContentRated(Context context, String category, String itemId, boolean isLiked) {
        if (databaseHelper != null) {
            databaseHelper.addToViewedHistory(category, itemId, isLiked);
        }
    }

    /**
     * Сбрасывает кэш для указанной категории
     */
    public void resetCache(String category) {
        List<ContentItem> cache = contentCache.get(category);
        if (cache != null) {
            cache.clear();
        }
        Log.d(TAG, "Кэш сброшен для категории: " + category);
    }

    /**
     * Сбрасывает весь кэш
     */
    public void resetAllCaches() {
        for (String category : contentCache.keySet()) {
            resetCache(category);
        }
        Log.d(TAG, "Все кэши сброшены");
    }

    /**
     * Сбрасывает историю просмотров для категории
     */
    public void resetHistory(Context context, String category) {
        if (databaseHelper != null) {
            databaseHelper.clearHistory(category);
        }
        Log.d(TAG, "История просмотров сброшена для категории: " + category);
    }

    /**
     * Сбрасывает всю историю просмотров
     */
    public void resetAllHistory(Context context) {
        if (databaseHelper != null) {
            databaseHelper.clearAllHistory();
        }
        Log.d(TAG, "Вся история просмотров сброшена");
    }

    /**
     * Получить количество уже показанных элементов для категории
     */
    public int getShownCount(String category) {
        int count = 0;

        if (shownContentIds.containsKey(category)) {
            count += shownContentIds.get(category).size();
        }

        if (sessionShownIds.containsKey(category)) {
            count += sessionShownIds.get(category).size();
        }

        return count;
    }
}