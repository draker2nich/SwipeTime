package com.draker.swipetime.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Клиент для работы с Retrofit
 */
public class RetrofitClient {
    private static Retrofit tmdbRetrofit = null;
    private static Retrofit rawgRetrofit = null;
    private static Retrofit googleBooksRetrofit = null;
    private static Retrofit jikanRetrofit = null;
    private static Retrofit yandexRetrofit = null;

    /**
     * Получить клиент для TMDB API
     * @return Retrofit клиент для TMDB API
     */
    public static Retrofit getTmdbClient() {
        if (tmdbRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);

            tmdbRetrofit = new Retrofit.Builder()
                    .baseUrl(ApiConstants.TMDB_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return tmdbRetrofit;
    }

    /**
     * Получить клиент для RAWG API
     * @return Retrofit клиент для RAWG API
     */
    public static Retrofit getRawgClient() {
        if (rawgRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);

            rawgRetrofit = new Retrofit.Builder()
                    .baseUrl(ApiConstants.RAWG_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return rawgRetrofit;
    }

    /**
     * Получить клиент для Google Books API
     * @return Retrofit клиент для Google Books API
     */
    public static Retrofit getGoogleBooksClient() {
        if (googleBooksRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);

            googleBooksRetrofit = new Retrofit.Builder()
                    .baseUrl(ApiConstants.GOOGLE_BOOKS_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return googleBooksRetrofit;
    }

    /**
     * Получить клиент для Jikan API
     * @return Retrofit клиент для Jikan API
     */
    public static Retrofit getJikanClient() {
        if (jikanRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);

            jikanRetrofit = new Retrofit.Builder()
                    .baseUrl(ApiConstants.JIKAN_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return jikanRetrofit;
    }

    /**
     * Получить клиент для Yandex API
     * @return Retrofit клиент для Yandex API
     */
    public static Retrofit getYandexClient() {
        if (yandexRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        // Добавляем токен авторизации для Yandex API
                        okhttp3.Request original = chain.request();
                        okhttp3.Request request = original.newBuilder()
                                .header("Authorization", "OAuth " + ApiConstants.YANDEX_TOKEN)
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);

            yandexRetrofit = new Retrofit.Builder()
                    .baseUrl(ApiConstants.YANDEX_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return yandexRetrofit;
    }
}
