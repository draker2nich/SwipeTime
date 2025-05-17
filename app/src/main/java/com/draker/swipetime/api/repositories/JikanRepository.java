package com.draker.swipetime.api.repositories;

import android.util.Log;

import com.draker.swipetime.api.ApiConstants;
import com.draker.swipetime.api.RetrofitClient;
import com.draker.swipetime.api.models.jikan.JikanAnime;
import com.draker.swipetime.api.models.jikan.JikanResponse;
import com.draker.swipetime.api.services.JikanService;
import com.draker.swipetime.database.entities.AnimeEntity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Репозиторий для работы с Jikan API
 */
public class JikanRepository {
    private static final String TAG = "JikanRepository";
    private JikanService service;

    public JikanRepository() {
        service = RetrofitClient.getJikanClient().create(JikanService.class);
    }

    /**
     * Загрузить топ аниме
     * @param page номер страницы
     * @return Observable со списком AnimeEntity
     */
    public Observable<List<AnimeEntity>> getTopAnime(int page) {
        return service.getTopAnime(page, ApiConstants.PAGE_SIZE, "popularity", "desc")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertAnimeResponseToEntities)
                .onErrorReturn(error -> {
                    Log.e(TAG, "Error loading top anime: " + error.getMessage(), error);
                    // Возвращаем пустой список в случае ошибки, чтобы не ломать поток
                    return new ArrayList<>();
                });
    }

    /**
     * Поиск аниме
     * @param query поисковый запрос
     * @param page номер страницы
     * @return Observable со списком AnimeEntity
     */
    public Observable<List<AnimeEntity>> searchAnime(String query, int page) {
        return service.searchAnime(query, page, ApiConstants.PAGE_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertAnimeResponseToEntities)
                .onErrorReturn(error -> {
                    Log.e(TAG, "Error searching anime: " + error.getMessage(), error);
                    // Возвращаем пустой список в случае ошибки, чтобы не ломать поток
                    return new ArrayList<>();
                });
    }

    /**
     * Получить детальную информацию об аниме
     * @param animeId ID аниме
     * @return Observable с AnimeEntity
     */
    public Observable<AnimeEntity> getAnimeDetails(int animeId) {
        return service.getAnimeDetails(animeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertAnimeToEntity);
    }

    /**
     * Получить аниме по жанру
     * @param genres ID жанров через запятую
     * @param page номер страницы
     * @return Observable со списком AnimeEntity
     */
    public Observable<List<AnimeEntity>> getAnimeByGenre(String genres, int page) {
        return service.getAnimeByGenre(genres, page, ApiConstants.PAGE_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertAnimeResponseToEntities);
    }

    /**
     * Получить аниме по статусу
     * @param status статус (airing, complete, upcoming)
     * @param page номер страницы
     * @return Observable со списком AnimeEntity
     */
    public Observable<List<AnimeEntity>> getAnimeByStatus(String status, int page) {
        return service.getAnimeByStatus(status, page, ApiConstants.PAGE_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertAnimeResponseToEntities);
    }

    /**
     * Получить аниме по типу
     * @param type тип (TV, Movie, OVA, etc.)
     * @param page номер страницы
     * @return Observable со списком AnimeEntity
     */
    public Observable<List<AnimeEntity>> getAnimeByType(String type, int page) {
        return service.getAnimeByType(type, page, ApiConstants.PAGE_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertAnimeResponseToEntities);
    }

    /**
     * Конвертировать ответ с аниме в список AnimeEntity
     * @param response ответ от Jikan API
     * @return список AnimeEntity
     */
    private List<AnimeEntity> convertAnimeResponseToEntities(JikanResponse response) {
        List<AnimeEntity> animeList = new ArrayList<>();
        if (response != null && response.getData() != null) {
            for (JikanAnime anime : response.getData()) {
                animeList.add(convertAnimeToEntity(anime));
            }
        }
        return animeList;
    }

    /**
     * Конвертировать модель аниме в AnimeEntity
     * @param anime модель аниме из Jikan API
     * @return AnimeEntity
     */
    private AnimeEntity convertAnimeToEntity(JikanAnime anime) {
        String status = "unknown";
        if (anime.getStatus() != null) {
            if (anime.getStatus().contains("Finished")) {
                status = "finished";
            } else if (anime.getStatus().contains("Currently")) {
                status = "ongoing";
            } else if (anime.getStatus().contains("Not yet")) {
                status = "upcoming";
            }
        }
        
        String type = anime.getType() != null ? anime.getType() : "Unknown";
        int episodes = anime.getEpisodesCount();
        int releaseYear = anime.getReleaseYear();

        return new AnimeEntity(
                "jikan_" + anime.getMalId(),
                anime.getTitle(),
                anime.getSynopsis() != null ? anime.getSynopsis() : "",
                anime.getImageUrl(),
                anime.getStudiosString(),
                releaseYear,
                episodes,
                anime.getGenresString(),
                status,
                type
        );
    }
}