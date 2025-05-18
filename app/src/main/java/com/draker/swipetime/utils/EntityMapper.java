package com.draker.swipetime.utils;

import android.util.Log;

import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.models.ContentItem;

/**
 * Утилитарный класс для преобразования сущностей базы данных в объекты модели
 */
public class EntityMapper {
    private static final String TAG = "EntityMapper";

    /**
     * Преобразует сущность ContentEntity в объект ContentItem
     * @param entity сущность ContentEntity
     * @return объект ContentItem
     */
    public static ContentItem mapToContentItem(ContentEntity entity) {
        if (entity == null) {
            return null;
        }

        ContentItem item = new ContentItem(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getCategory()
        );

        // Копируем общие поля
        item.setLiked(entity.isLiked());
        item.setRating(entity.getRating());

        // Обрабатываем специфические типы сущностей
        try {
            if (entity instanceof MovieEntity) {
                applyMovieFields((MovieEntity) entity, item);
            } else if (entity instanceof TVShowEntity) {
                applyTVShowFields((TVShowEntity) entity, item);
            } else if (entity instanceof GameEntity) {
                applyGameFields((GameEntity) entity, item);
            } else if (entity instanceof BookEntity) {
                applyBookFields((BookEntity) entity, item);
            } else if (entity instanceof AnimeEntity) {
                applyAnimeFields((AnimeEntity) entity, item);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при преобразовании сущности: " + e.getMessage());
        }

        return item;
    }

    /**
     * Применяет специфические поля фильма к объекту ContentItem
     * @param movie сущность MovieEntity
     * @param item целевой объект ContentItem
     */
    private static void applyMovieFields(MovieEntity movie, ContentItem item) {
        // У MovieEntity вместо getGenre есть getGenres
        if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
            item.setGenre(movie.getGenres());
        }
        
        // У MovieEntity вместо getYear есть getReleaseYear
        if (movie.getReleaseYear() > 0) {
            item.setYear(movie.getReleaseYear());
        }
        
        if (movie.getDirector() != null && !movie.getDirector().isEmpty()) {
            item.setDirector(movie.getDirector());
        }
        
        item.setWatched(movie.isWatched());
    }

    /**
     * Применяет специфические поля сериала к объекту ContentItem
     * @param tvShow сущность TVShowEntity
     * @param item целевой объект ContentItem
     */
    private static void applyTVShowFields(TVShowEntity tvShow, ContentItem item) {
        // Предполагаем, что в TVShowEntity могут быть аналогичные отличия
        try {
            // Проверяем наличие разных методов для получения жанра
            if (tvShow.getClass().getMethod("getGenres") != null) {
                item.setGenre((String) tvShow.getClass().getMethod("getGenres").invoke(tvShow));
            } else if (tvShow.getClass().getMethod("getGenre") != null) {
                item.setGenre((String) tvShow.getClass().getMethod("getGenre").invoke(tvShow));
            }
        } catch (Exception e) {
            // Игнорируем ошибки - если метод не найден, пробуем другой
        }
        
        try {
            // Проверяем наличие разных методов для получения года
            if (tvShow.getClass().getMethod("getReleaseYear") != null) {
                item.setYear((int) tvShow.getClass().getMethod("getReleaseYear").invoke(tvShow));
            } else if (tvShow.getClass().getMethod("getYear") != null) {
                item.setYear((int) tvShow.getClass().getMethod("getYear").invoke(tvShow));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        
        try {
            if (tvShow.getClass().getMethod("getSeasons") != null) {
                item.setSeasons((int) tvShow.getClass().getMethod("getSeasons").invoke(tvShow));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        
        try {
            if (tvShow.getClass().getMethod("getEpisodes") != null) {
                item.setEpisodes((int) tvShow.getClass().getMethod("getEpisodes").invoke(tvShow));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
    }

    /**
     * Применяет специфические поля игры к объекту ContentItem
     * @param game сущность GameEntity
     * @param item целевой объект ContentItem
     */
    private static void applyGameFields(GameEntity game, ContentItem item) {
        try {
            // Проверяем наличие разных методов для получения жанра
            if (game.getClass().getMethod("getGenres") != null) {
                item.setGenre((String) game.getClass().getMethod("getGenres").invoke(game));
            } else if (game.getClass().getMethod("getGenre") != null) {
                item.setGenre((String) game.getClass().getMethod("getGenre").invoke(game));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        
        try {
            // Проверяем наличие разных методов для получения года
            if (game.getClass().getMethod("getReleaseYear") != null) {
                item.setYear((int) game.getClass().getMethod("getReleaseYear").invoke(game));
            } else if (game.getClass().getMethod("getYear") != null) {
                item.setYear((int) game.getClass().getMethod("getYear").invoke(game));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        
        try {
            if (game.getClass().getMethod("getDeveloper") != null) {
                item.setDeveloper((String) game.getClass().getMethod("getDeveloper").invoke(game));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        
        try {
            // Проверяем наличие разных методов для получения платформ
            if (game.getClass().getMethod("getPlatforms") != null) {
                item.setPlatforms((String) game.getClass().getMethod("getPlatforms").invoke(game));
            } else if (game.getClass().getMethod("getPlatform") != null) {
                item.setPlatforms((String) game.getClass().getMethod("getPlatform").invoke(game));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
    }

    /**
     * Применяет специфические поля книги к объекту ContentItem
     * @param book сущность BookEntity
     * @param item целевой объект ContentItem
     */
    private static void applyBookFields(BookEntity book, ContentItem item) {
        try {
            // Проверяем наличие разных методов для получения жанра
            if (book.getClass().getMethod("getGenres") != null) {
                item.setGenre((String) book.getClass().getMethod("getGenres").invoke(book));
            } else if (book.getClass().getMethod("getGenre") != null) {
                item.setGenre((String) book.getClass().getMethod("getGenre").invoke(book));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        
        try {
            // Проверяем наличие разных методов для получения года
            if (book.getClass().getMethod("getReleaseYear") != null) {
                item.setYear((int) book.getClass().getMethod("getReleaseYear").invoke(book));
            } else if (book.getClass().getMethod("getYear") != null) {
                item.setYear((int) book.getClass().getMethod("getYear").invoke(book));
            } else if (book.getClass().getMethod("getPublicationYear") != null) {
                item.setYear((int) book.getClass().getMethod("getPublicationYear").invoke(book));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        
        try {
            if (book.getClass().getMethod("getAuthor") != null) {
                item.setAuthor((String) book.getClass().getMethod("getAuthor").invoke(book));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        
        try {
            if (book.getClass().getMethod("getPublisher") != null) {
                item.setPublisher((String) book.getClass().getMethod("getPublisher").invoke(book));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        
        try {
            if (book.getClass().getMethod("getPages") != null) {
                item.setPages((int) book.getClass().getMethod("getPages").invoke(book));
            } else if (book.getClass().getMethod("getPageCount") != null) {
                item.setPages((int) book.getClass().getMethod("getPageCount").invoke(book));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
    }

    /**
     * Применяет специфические поля аниме к объекту ContentItem
     * @param anime сущность AnimeEntity
     * @param item целевой объект ContentItem
     */
    private static void applyAnimeFields(AnimeEntity anime, ContentItem item) {
        try {
            // Проверяем наличие разных методов для получения жанра
            if (anime.getClass().getMethod("getGenres") != null) {
                item.setGenre((String) anime.getClass().getMethod("getGenres").invoke(anime));
            } else if (anime.getClass().getMethod("getGenre") != null) {
                item.setGenre((String) anime.getClass().getMethod("getGenre").invoke(anime));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        
        try {
            // Проверяем наличие разных методов для получения года
            if (anime.getClass().getMethod("getReleaseYear") != null) {
                item.setYear((int) anime.getClass().getMethod("getReleaseYear").invoke(anime));
            } else if (anime.getClass().getMethod("getYear") != null) {
                item.setYear((int) anime.getClass().getMethod("getYear").invoke(anime));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        
        try {
            if (anime.getClass().getMethod("getStudio") != null) {
                item.setStudio((String) anime.getClass().getMethod("getStudio").invoke(anime));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        
        try {
            if (anime.getClass().getMethod("getEpisodes") != null) {
                item.setEpisodes((int) anime.getClass().getMethod("getEpisodes").invoke(anime));
            } else if (anime.getClass().getMethod("getEpisodeCount") != null) {
                item.setEpisodes((int) anime.getClass().getMethod("getEpisodeCount").invoke(anime));
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
    }
}
