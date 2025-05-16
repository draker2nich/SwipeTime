package com.draker.swipetime.utils;

import android.util.Log;

import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.repository.ContentRepository;

/**
 * Вспомогательный класс для работы с информацией о карточках
 */
public class CardInfoHelper {
    
    private static final String TAG = "CardInfoHelper";

    /**
     * Получает подробную информацию о карточке
     * 
     * @param item элемент контента
     * @param contentRepository репозиторий для доступа к сущностям
     * @return строка с подробной информацией
     */
    public static String getDetailedInfo(ContentItem item, ContentRepository contentRepository) {
        if (item == null || contentRepository == null) {
            return "Нет информации";
        }
        
        StringBuilder info = new StringBuilder();
        
        // Получаем сущность из репозитория
        ContentEntity entity = contentRepository.getById(item.getId());
        
        if (entity == null) {
            return "Нет подробной информации";
        }
        
        String contentType = "Общий контент";
        
        if (entity instanceof MovieEntity) {
            MovieEntity movie = (MovieEntity) entity;
            contentType = "Фильм";
            
            info.append("Режиссер: ").append(movie.getDirector()).append("\n");
            info.append("Год выпуска: ").append(movie.getReleaseYear()).append("\n");
            info.append("Длительность: ").append(movie.getDuration()).append(" мин.\n");
            info.append("Жанры: ").append(movie.getGenres()).append("\n");
            
        } else if (entity instanceof TVShowEntity) {
            TVShowEntity tvShow = (TVShowEntity) entity;
            contentType = "Сериал";
            
            info.append("Создатель: ").append(tvShow.getCreator()).append("\n");
            info.append("Годы выхода: ").append(tvShow.getStartYear());
            if (tvShow.getEndYear() > tvShow.getStartYear()) {
                info.append("-").append(tvShow.getEndYear());
            }
            info.append("\n");
            info.append("Сезоны: ").append(tvShow.getSeasons()).append("\n");
            info.append("Эпизоды: ").append(tvShow.getEpisodes()).append("\n");
            info.append("Статус: ").append(tvShow.getStatus()).append("\n");
            info.append("Жанры: ").append(tvShow.getGenres()).append("\n");
            
        } else if (entity instanceof GameEntity) {
            GameEntity game = (GameEntity) entity;
            contentType = "Игра";
            
            info.append("Разработчик: ").append(game.getDeveloper()).append("\n");
            info.append("Издатель: ").append(game.getPublisher()).append("\n");
            info.append("Год выпуска: ").append(game.getReleaseYear()).append("\n");
            info.append("Платформы: ").append(game.getPlatforms()).append("\n");
            info.append("Жанры: ").append(game.getGenres()).append("\n");
            info.append("Рейтинг ESRB: ").append(game.getEsrbRating()).append("\n");
            
        } else if (entity instanceof BookEntity) {
            BookEntity book = (BookEntity) entity;
            contentType = "Книга";
            
            info.append("Автор: ").append(book.getAuthor()).append("\n");
            info.append("Издательство: ").append(book.getPublisher()).append("\n");
            info.append("Год публикации: ").append(book.getPublishYear()).append("\n");
            info.append("Количество страниц: ").append(book.getPageCount()).append("\n");
            info.append("Жанры: ").append(book.getGenres()).append("\n");
            info.append("ISBN: ").append(book.getIsbn()).append("\n");
            
        } else if (entity instanceof AnimeEntity) {
            AnimeEntity anime = (AnimeEntity) entity;
            contentType = "Аниме";
            
            info.append("Студия: ").append(anime.getStudio()).append("\n");
            info.append("Год выпуска: ").append(anime.getReleaseYear()).append("\n");
            info.append("Эпизоды: ").append(anime.getEpisodes()).append("\n");
            info.append("Статус: ").append(anime.getStatus()).append("\n");
            info.append("Тип: ").append(anime.getType()).append("\n");
            info.append("Жанры: ").append(anime.getGenres()).append("\n");
        }
        
        Log.d(TAG, contentType + ": " + item.getTitle() + "\n" + info.toString());
        
        return contentType + "\n" + info.toString();
    }
    
    /**
     * Логирует подробную информацию о карточке
     * 
     * @param item элемент контента
     * @param contentRepository репозиторий для доступа к сущностям
     */
    public static void logDetailedInfo(ContentItem item, ContentRepository contentRepository) {
        String info = getDetailedInfo(item, contentRepository);
        Log.d(TAG, "Подробная информация о " + item.getTitle() + ":\n" + info);
    }
}