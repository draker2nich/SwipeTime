package com.draker.swipetime.utils;

import android.content.Context;
import android.util.Log;

import com.draker.swipetime.R;
import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.repository.AnimeRepository;
import com.draker.swipetime.repository.BookRepository;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.repository.GameRepository;
import com.draker.swipetime.repository.MovieRepository;
import com.draker.swipetime.repository.TVShowRepository;

/**
 * Вспомогательный класс для работы с избранными элементами
 */
public class LikedItemsHelper {
    private static final String TAG = "LikedItemsHelper";

    /**
     * Добавляет элемент в избранное в соответствующей категории
     */
    public static void addToLiked(ContentItem item, String categoryName,
                                   MovieRepository movieRepository,
                                   TVShowRepository tvShowRepository,
                                   GameRepository gameRepository,
                                   BookRepository bookRepository,
                                   AnimeRepository animeRepository,
                                   ContentRepository contentRepository) {
        try {
            Log.d(TAG, "Добавление элемента в избранное: " + item.getTitle() + " (ID: " + item.getId() + ")");
            
            // Сначала проверяем и обновляем запись в общем контенте для согласованности
            ContentEntity existingContent = contentRepository.getById(item.getId());
            if (existingContent != null) {
                existingContent.setLiked(true);
                contentRepository.update(existingContent);
                Log.d(TAG, "Общий контент обновлен: " + existingContent.getTitle() + " (категория: " + existingContent.getCategory() + ")");
            } else {
                // Если запись отсутствует в общем контенте, создаем новую
                createNewContentEntity(item, categoryName, contentRepository);
            }
            
            // В зависимости от категории обновляем соответствующую сущность
            if (categoryName.equals("Фильмы")) {
                MovieEntity movie = movieRepository.getById(item.getId());
                if (movie != null) {
                    movie.setLiked(true);
                    movieRepository.update(movie);
                    Log.d(TAG, "Фильм обновлен в избранном: " + movie.getTitle());
                } else {
                    // Если сущность не найдена, создаем новую
                    createNewMovieEntity(item, categoryName, movieRepository);
                }
            } else if (categoryName.equals("Сериалы")) {
                TVShowEntity tvShow = tvShowRepository.getById(item.getId());
                if (tvShow != null) {
                    tvShow.setLiked(true);
                    tvShowRepository.update(tvShow);
                    Log.d(TAG, "Сериал обновлен в избранном: " + tvShow.getTitle());
                } else {
                    // Если сущность не найдена, создаем новую
                    createNewTVShowEntity(item, categoryName, tvShowRepository);
                }
            } else if (categoryName.equals("Игры")) {
                GameEntity game = gameRepository.getById(item.getId());
                if (game != null) {
                    game.setLiked(true);
                    gameRepository.update(game);
                    Log.d(TAG, "Игра обновлена в избранном: " + game.getTitle());
                } else {
                    // Если сущность не найдена, создаем новую
                    createNewGameEntity(item, categoryName, gameRepository);
                }
            } else if (categoryName.equals("Книги")) {
                BookEntity book = bookRepository.getById(item.getId());
                if (book != null) {
                    book.setLiked(true);
                    bookRepository.update(book);
                    Log.d(TAG, "Книга обновлена в избранном: " + book.getTitle());
                } else {
                    // Если сущность не найдена, создаем новую
                    createNewBookEntity(item, categoryName, bookRepository);
                }
            } else if (categoryName.equals("Аниме")) {
                AnimeEntity anime = animeRepository.getById(item.getId());
                if (anime != null) {
                    anime.setLiked(true);
                    animeRepository.update(anime);
                    Log.d(TAG, "Аниме обновлено в избранном: " + anime.getTitle());
                } else {
                    // Если сущность не найдена, создаем новую
                    createNewAnimeEntity(item, categoryName, animeRepository);
                }
            } else {
                // Для всех остальных категорий, включая Музыку
                ContentEntity content = contentRepository.getById(item.getId());
                if (content != null) {
                    content.setLiked(true);
                    contentRepository.update(content);
                    Log.d(TAG, "Контент обновлен в избранном: " + content.getTitle() + " (категория: " + content.getCategory() + ")");
                } else {
                    // Если контент не найден, создаем новый
                    createNewContentEntity(item, categoryName, contentRepository);
                }
            }
            
            // Также добавляем запись в общий контент для согласованности
            ContentEntity existingCheck = contentRepository.getById(item.getId());
            if (existingCheck != null) {
                existingCheck.setLiked(true);
                contentRepository.update(existingCheck);
                Log.d(TAG, "Общий контент обновлен: " + existingCheck.getTitle() + " (категория: " + existingCheck.getCategory() + ")");
            } else {
                // Если запись отсутствует в общем контенте, создаем новую
                createNewContentEntity(item, categoryName, contentRepository);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при добавлении в избранное: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Вспомогательные методы для создания новых сущностей
    private static void createNewMovieEntity(ContentItem item, String categoryName, MovieRepository movieRepository) {
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
    
    private static void createNewTVShowEntity(ContentItem item, String categoryName, TVShowRepository tvShowRepository) {
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
    
    private static void createNewGameEntity(ContentItem item, String categoryName, GameRepository gameRepository) {
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
    
    private static void createNewBookEntity(ContentItem item, String categoryName, BookRepository bookRepository) {
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
    
    private static void createNewAnimeEntity(ContentItem item, String categoryName, AnimeRepository animeRepository) {
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
    
    private static void createNewContentEntity(ContentItem item, String categoryName, ContentRepository contentRepository) {
        ContentEntity content = new ContentEntity();
        content.setId(item.getId());
        content.setTitle(item.getTitle());
        content.setDescription(item.getDescription());
        content.setImageUrl(item.getImageUrl());
        content.setCategory(categoryName); // Важно: используем категорию из параметра
        content.setContentType(categoryName.toLowerCase());
        content.setLiked(true); // Явно устанавливаем liked = true
        contentRepository.insert(content);
        Log.d(TAG, "Создан новый контент в избранном: " + content.getTitle() + " (категория: " + categoryName + ")");
        
        // Проверяем, был ли элемент добавлен успешно
        ContentEntity check = contentRepository.getById(item.getId());
        if (check != null) {
            Log.d(TAG, "Проверка: элемент найден в базе, liked=" + check.isLiked());
        } else {
            Log.e(TAG, "Ошибка: элемент не был добавлен в базу");
        }
    }
    
    /**
     * Обновляет статус "нравится" в базе данных для элемента
     */
    public static void updateLikedStatus(String id, boolean liked, String categoryName,
                                         MovieRepository movieRepository,
                                         TVShowRepository tvShowRepository,
                                         GameRepository gameRepository,
                                         BookRepository bookRepository,
                                         AnimeRepository animeRepository,
                                         ContentRepository contentRepository) {
        if (id != null && !id.isEmpty()) {
            try {
                // Обновляем статус только для соответствующей категории
                Log.d(TAG, "Обновляем статус liked=" + liked + " для id=" + id + " в категории " + categoryName);
                
                boolean updated = false;
                
                if (categoryName.equals("Фильмы")) {
                    // Обновляем только MovieEntity
                    MovieEntity movie = movieRepository.getById(id);
                    if (movie != null) {
                        movie.setLiked(liked);
                        movieRepository.update(movie);
                        updated = true;
                        Log.d(TAG, "Обновлен статус liked в MovieEntity");
                    } else {
                        Log.d(TAG, "MovieEntity с id=" + id + " не найдена");
                    }
                } else if (categoryName.equals("Сериалы")) {
                    // Обновляем только TVShowEntity
                    TVShowEntity tvShow = tvShowRepository.getById(id);
                    if (tvShow != null) {
                        tvShow.setLiked(liked);
                        tvShowRepository.update(tvShow);
                        updated = true;
                        Log.d(TAG, "Обновлен статус liked в TVShowEntity");
                    } else {
                        Log.d(TAG, "TVShowEntity с id=" + id + " не найдена");
                    }
                } else if (categoryName.equals("Игры")) {
                    // Обновляем только GameEntity
                    GameEntity game = gameRepository.getById(id);
                    if (game != null) {
                        game.setLiked(liked);
                        gameRepository.update(game);
                        updated = true;
                        Log.d(TAG, "Обновлен статус liked в GameEntity");
                    } else {
                        Log.d(TAG, "GameEntity с id=" + id + " не найдена");
                    }
                } else if (categoryName.equals("Книги")) {
                    // Обновляем только BookEntity
                    BookEntity book = bookRepository.getById(id);
                    if (book != null) {
                        book.setLiked(liked);
                        bookRepository.update(book);
                        updated = true;
                        Log.d(TAG, "Обновлен статус liked в BookEntity");
                    } else {
                        Log.d(TAG, "BookEntity с id=" + id + " не найдена");
                    }
                } else if (categoryName.equals("Аниме")) {
                    // Обновляем только AnimeEntity
                    AnimeEntity anime = animeRepository.getById(id);
                    if (anime != null) {
                        anime.setLiked(liked);
                        animeRepository.update(anime);
                        updated = true;
                        Log.d(TAG, "Обновлен статус liked в AnimeEntity");
                    } else {
                        Log.d(TAG, "AnimeEntity с id=" + id + " не найдена");
                    }
                } else {
                    // Для других категорий, включая музыку, используем ContentEntity
                    ContentEntity content = contentRepository.getById(id);
                    if (content != null) {
                        content.setLiked(liked);
                        contentRepository.update(content);
                        updated = true;
                        Log.d(TAG, "Обновлен статус liked в ContentEntity");
                    } else {
                        Log.d(TAG, "ContentEntity с id=" + id + " не найдена");
                    }
                }
                
                // Если обновление не произошло ни в одной из таблиц, пишем предупреждение
                if (!updated) {
                    Log.w(TAG, "Не удалось обновить статус liked ни для одной сущности с id=" + id);
                }
                
                // Дополнительно обновляем в общей таблице контента для согласованности
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
     * в зависимости от типа контента
     * @param category категория контента
     * @param context контекст для доступа к ресурсам
     * @return строковый ресурс для отображения на переключателе
     */
    public static String getWatchedSwitchLabel(String category, Context context) {
        if (category == null || category.isEmpty()) {
            return context.getString(R.string.watched_status); // Значение по умолчанию
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
     * в зависимости от типа контента
     * @param category категория контента
     * @return идентификатор ресурса drawable
     */
    public static int getWatchedIcon(String category) {
        if (category == null || category.isEmpty()) {
            return R.drawable.ic_check; // Значение по умолчанию
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
}