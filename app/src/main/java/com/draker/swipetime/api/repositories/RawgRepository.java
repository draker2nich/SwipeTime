package com.draker.swipetime.api.repositories;

import android.util.Log;

import com.draker.swipetime.api.ApiConstants;
import com.draker.swipetime.api.RetrofitClient;
import com.draker.swipetime.api.models.rawg.RawgGame;
import com.draker.swipetime.api.models.rawg.RawgGameResponse;
import com.draker.swipetime.api.services.RawgService;
import com.draker.swipetime.database.entities.GameEntity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Репозиторий для работы с RAWG API
 */
public class RawgRepository {
    private static final String TAG = "RawgRepository";
    private RawgService service;

    public RawgRepository() {
        service = RetrofitClient.getRawgClient().create(RawgService.class);
    }

    /**
     * Получить список популярных игр
     * @param page номер страницы
     * @return Observable со списком GameEntity
     */
    public Observable<List<GameEntity>> getPopularGames(int page) {
        return service.getGames(ApiConstants.RAWG_API_KEY, page, ApiConstants.PAGE_SIZE, "-rating")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertGameResponseToEntities)
                .onErrorReturn(error -> {
                    Log.e(TAG, "Error loading popular games: " + error.getMessage(), error);
                    // Возвращаем пустой список в случае ошибки, чтобы не ломать поток
                    return new ArrayList<>();
                });
    }

    /**
     * Поиск игр
     * @param query поисковый запрос
     * @param page номер страницы
     * @return Observable со списком GameEntity
     */
    public Observable<List<GameEntity>> searchGames(String query, int page) {
        return service.searchGames(ApiConstants.RAWG_API_KEY, query, page, ApiConstants.PAGE_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertGameResponseToEntities)
                .onErrorReturn(error -> {
                    Log.e(TAG, "Error searching games: " + error.getMessage(), error);
                    // Возвращаем пустой список в случае ошибки, чтобы не ломать поток
                    return new ArrayList<>();
                });
    }

    /**
     * Получить детальную информацию об игре
     * @param gameId ID игры
     * @return Observable с GameEntity
     */
    public Observable<GameEntity> getGameDetails(int gameId) {
        return service.getGameDetails(ApiConstants.RAWG_API_KEY, gameId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertGameToEntity);
    }

    /**
     * Получить игры по жанру
     * @param genre жанр
     * @param page номер страницы
     * @return Observable со списком GameEntity
     */
    public Observable<List<GameEntity>> getGamesByGenre(String genre, int page) {
        return service.getGamesByGenre(ApiConstants.RAWG_API_KEY, genre, page, ApiConstants.PAGE_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertGameResponseToEntities);
    }

    /**
     * Получить игры по платформе
     * @param platform платформа
     * @param page номер страницы
     * @return Observable со списком GameEntity
     */
    public Observable<List<GameEntity>> getGamesByPlatform(String platform, int page) {
        return service.getGamesByPlatform(ApiConstants.RAWG_API_KEY, platform, page, ApiConstants.PAGE_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertGameResponseToEntities);
    }

    /**
     * Конвертировать ответ с играми в список GameEntity
     * @param response ответ от RAWG API
     * @return список GameEntity
     */
    private List<GameEntity> convertGameResponseToEntities(RawgGameResponse response) {
        List<GameEntity> games = new ArrayList<>();
        if (response != null && response.results != null) {
            for (RawgGame game : response.results) {
                games.add(convertGameToEntity(game));
            }
        }
        return games;
    }

    /**
     * Конвертировать модель игры в GameEntity
     * @param game модель игры из RAWG API
     * @return GameEntity
     */
    private GameEntity convertGameToEntity(RawgGame game) {
        String imageUrl = game.getBackgroundImage();

        int releaseYear = game.getReleaseYear();

        // Получаем названия платформ в виде строки
        String platforms = game.getPlatformNames();

        // Получаем названия жанров в виде строки
        String genres = game.getGenreNames();

        // Получаем название разработчика
        String developer = game.getDeveloperNames();

        // Получаем название издателя
        String publisher = game.getPublisherNames();

        // Получаем рейтинг ESRB
        String esrbRating = game.getEsrbRatingName();

        // Создаем и возвращаем новую сущность игры
        return new GameEntity(
                "rawg_" + game.getId(),
                game.getName(),
                game.getDescriptionRaw() != null ? game.getDescriptionRaw() : "",
                imageUrl,
                developer,
                publisher,
                releaseYear,
                platforms,
                genres,
                esrbRating
        );
    }
}