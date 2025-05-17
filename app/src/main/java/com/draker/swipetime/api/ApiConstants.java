package com.draker.swipetime.api;

/**
 * Константы для работы с API
 */
public class ApiConstants {
    // TMDB API для фильмов и сериалов
    public static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";
    public static final String TMDB_API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJkZjZjYzRjYTY3OTllMDhmZWQwZDUzNjA3Mzc2ZWRhNyIsIm5iZiI6MTc0NzA2NzAzMy43MzksInN1YiI6IjY4MjIyMDk5MGUwMGNmMjZmNDZlZTU2OCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.4yYForZ_9GRLHerSnmEBfIOz-JlB6C9KgOMnxFHgiyA";
    public static final String TMDB_ACCOUNT_ID = "682220990e00cf26f46ee568";
    public static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    // RAWG API для игр
    public static final String RAWG_BASE_URL = "https://api.rawg.io/api/";
    public static final String RAWG_API_KEY = "d81d4e0fba794cbe8de9d4dcefd14c5b";

    // Google Books API
    public static final String GOOGLE_BOOKS_BASE_URL = "https://www.googleapis.com/books/v1/";
    
    // Jikan API для аниме
    public static final String JIKAN_BASE_URL = "https://api.jikan.moe/v4/";

    // Yandex API
    public static final String YANDEX_BASE_URL = "https://api.content.market.yandex.ru/";
    public static final String YANDEX_TOKEN = "y0__xCuvqe6Bxje-AYgy87jkxNr6U1xa69TOnXNqSLHw3Qpx2DXYQ";
    
    // Количество элементов на странице при запросе API
    public static final int PAGE_SIZE = 20;
}
