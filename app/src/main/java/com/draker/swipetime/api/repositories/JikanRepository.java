package com.draker.swipetime.api.repositories;

import android.util.Log;

import com.draker.swipetime.api.ApiConstants;
import com.draker.swipetime.api.RetrofitClient;
import com.draker.swipetime.api.models.jikan.JikanAnime;
import com.draker.swipetime.api.models.jikan.JikanResponse;
import com.draker.swipetime.api.services.JikanService;
import com.draker.swipetime.api.retry.IntelligentRetryStrategy;
import com.draker.swipetime.database.entities.AnimeEntity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Репозиторий для работы с Jikan API с интегрированной защитой от rate limiting
 */
public class JikanRepository {
    private static final String TAG = "JikanRepository";
    private JikanService service;
    private IntelligentRetryStrategy retryStrategy;

    public JikanRepository() {
        service = RetrofitClient.getJikanClient().create(JikanService.class);
        retryStrategy = IntelligentRetryStrategy.forJikanApi();
        
        Log.d(TAG, "JikanRepository инициализирован с защитой от rate limiting");
        Log.d(TAG, "Стратегия повторов: " + retryStrategy.getStrategyInfo());
    }

    /**
     * Загрузить топ аниме с защитой от rate limiting
     * @param page номер страницы
     * @return Observable со списком AnimeEntity
     */
    public Observable<List<AnimeEntity>> getTopAnime(int page) {
        Log.d(TAG, "Запрос топ аниме, страница: " + page);
        
        return retryStrategy.applyTo(
                service.getTopAnime(page, ApiConstants.PAGE_SIZE, "popularity", "desc")
        )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map(this::convertAnimeResponseToEntities)
        .doOnNext(result -> {
            Log.d(TAG, "Успешно загружено " + result.size() + " аниме с страницы " + page);
        })
        .doOnError(error -> {
            Log.e(TAG, "Ошибка загрузки топ аниме (страница " + page + "): " + error.getMessage(), error);
        })
        .onErrorReturn(error -> {
            Log.w(TAG, "Возвращаем пустой список из-за ошибки: " + error.getMessage());
            return new ArrayList<>();
        });
    }

    /**
     * Поиск аниме с защитой от rate limiting
     * @param query поисковый запрос
     * @param page номер страницы
     * @return Observable со списком AnimeEntity
     */
    public Observable<List<AnimeEntity>> searchAnime(String query, int page) {
        Log.d(TAG, "Поиск аниме: '" + query + "', страница: " + page);
        
        return retryStrategy.applyTo(
                service.searchAnime(query, page, ApiConstants.PAGE_SIZE)
        )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map(this::convertAnimeResponseToEntities)
        .doOnNext(result -> {
            Log.d(TAG, "Найдено " + result.size() + " аниме по запросу '" + query + "' (страница " + page + ")");
        })
        .doOnError(error -> {
            Log.e(TAG, "Ошибка поиска аниме '" + query + "' (страница " + page + "): " + error.getMessage(), error);
        })
        .onErrorReturn(error -> {
            Log.w(TAG, "Возвращаем пустой список из-за ошибки поиска: " + error.getMessage());
            return new ArrayList<>();
        });
    }

    /**
     * Получить детальную информацию об аниме с защитой от rate limiting
     * @param animeId ID аниме
     * @return Observable с AnimeEntity
     */
    public Observable<AnimeEntity> getAnimeDetails(int animeId) {
        Log.d(TAG, "Запрос деталей аниме ID: " + animeId);
        
        return retryStrategy.applyTo(
                service.getAnimeDetails(animeId)
        )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map(this::convertAnimeToEntity)
        .doOnNext(result -> {
            Log.d(TAG, "Получены детали аниме: " + result.getTitle());
        })
        .doOnError(error -> {
            Log.e(TAG, "Ошибка загрузки деталей аниме ID " + animeId + ": " + error.getMessage(), error);
        });
    }

    /**
     * Получить аниме по жанру с защитой от rate limiting
     * @param genres ID жанров через запятую
     * @param page номер страницы
     * @return Observable со списком AnimeEntity
     */
    public Observable<List<AnimeEntity>> getAnimeByGenre(String genres, int page) {
        Log.d(TAG, "Запрос аниме по жанрам: " + genres + ", страница: " + page);
        
        return retryStrategy.applyTo(
                service.getAnimeByGenre(genres, page, ApiConstants.PAGE_SIZE)
        )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map(this::convertAnimeResponseToEntities)
        .doOnNext(result -> {
            Log.d(TAG, "Найдено " + result.size() + " аниме по жанрам " + genres + " (страница " + page + ")");
        })
        .doOnError(error -> {
            Log.e(TAG, "Ошибка загрузки аниме по жанрам " + genres + " (страница " + page + "): " + error.getMessage(), error);
        })
        .onErrorReturn(error -> {
            Log.w(TAG, "Возвращаем пустой список из-за ошибки загрузки по жанрам: " + error.getMessage());
            return new ArrayList<>();
        });
    }

    /**
     * Получить аниме по статусу с защитой от rate limiting
     * @param status статус (airing, complete, upcoming)
     * @param page номер страницы
     * @return Observable со списком AnimeEntity
     */
    public Observable<List<AnimeEntity>> getAnimeByStatus(String status, int page) {
        Log.d(TAG, "Запрос аниме по статусу: " + status + ", страница: " + page);
        
        return retryStrategy.applyTo(
                service.getAnimeByStatus(status, page, ApiConstants.PAGE_SIZE)
        )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map(this::convertAnimeResponseToEntities)
        .doOnNext(result -> {
            Log.d(TAG, "Найдено " + result.size() + " аниме со статусом " + status + " (страница " + page + ")");
        })
        .doOnError(error -> {
            Log.e(TAG, "Ошибка загрузки аниме по статусу " + status + " (страница " + page + "): " + error.getMessage(), error);
        })
        .onErrorReturn(error -> {
            Log.w(TAG, "Возвращаем пустой список из-за ошибки загрузки по статусу: " + error.getMessage());
            return new ArrayList<>();
        });
    }

    /**
     * Получить аниме по типу с защитой от rate limiting
     * @param type тип (TV, Movie, OVA, etc.)
     * @param page номер страницы
     * @return Observable со списком AnimeEntity
     */
    public Observable<List<AnimeEntity>> getAnimeByType(String type, int page) {
        Log.d(TAG, "Запрос аниме по типу: " + type + ", страница: " + page);
        
        return retryStrategy.applyTo(
                service.getAnimeByType(type, page, ApiConstants.PAGE_SIZE)
        )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map(this::convertAnimeResponseToEntities)
        .doOnNext(result -> {
            Log.d(TAG, "Найдено " + result.size() + " аниме типа " + type + " (страница " + page + ")");
        })
        .doOnError(error -> {
            Log.e(TAG, "Ошибка загрузки аниме по типу " + type + " (страница " + page + "): " + error.getMessage(), error);
        })
        .onErrorReturn(error -> {
            Log.w(TAG, "Возвращаем пустой список из-за ошибки загрузки по типу: " + error.getMessage());
            return new ArrayList<>();
        });
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