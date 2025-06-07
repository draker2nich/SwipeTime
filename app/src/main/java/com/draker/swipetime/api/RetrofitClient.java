package com.draker.swipetime.api;

import android.util.Log;
import com.draker.swipetime.api.ratelimiting.TokenBucketRateLimiter;
import com.draker.swipetime.api.ratelimiting.AdaptiveRateLimiter;
import com.draker.swipetime.api.resilience.ApiCircuitBreaker;
import com.draker.swipetime.utils.NetworkHelper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Клиент для работы с Retrofit с интегрированным rate limiting и circuit breaker
 */
public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    
    private static Retrofit tmdbRetrofit = null;
    private static Retrofit rawgRetrofit = null;
    private static Retrofit googleBooksRetrofit = null;
    private static Retrofit jikanRetrofit = null;
    private static Retrofit yandexRetrofit = null;
    
    // Rate limiters для разных API
    private static final TokenBucketRateLimiter jikanRateLimiter = 
            new TokenBucketRateLimiter(3, 1, 1000L); // 3 запроса в секунду для Jikan
    private static final TokenBucketRateLimiter tmdbRateLimiter = 
            new TokenBucketRateLimiter(40, 10, 1000L); // 40 запросов в секунду для TMDB
    private static final TokenBucketRateLimiter rawgRateLimiter = 
            new TokenBucketRateLimiter(20, 5, 1000L); // 20 запросов в секунду для RAWG
    private static final TokenBucketRateLimiter googleBooksRateLimiter = 
            new TokenBucketRateLimiter(15, 3, 1000L); // 15 запросов в секунду для Google Books
    
    // Adaptive rate limiter для всех API
    private static final AdaptiveRateLimiter adaptiveRateLimiter = new AdaptiveRateLimiter();
    
    // Circuit breakers для каждого API
    private static final ApiCircuitBreaker jikanCircuitBreaker = 
            new ApiCircuitBreaker("Jikan", 3, 30000L, 2);
    private static final ApiCircuitBreaker tmdbCircuitBreaker = 
            new ApiCircuitBreaker("TMDB", 5, 15000L, 3);
    private static final ApiCircuitBreaker rawgCircuitBreaker = 
            new ApiCircuitBreaker("RAWG", 5, 15000L, 3);
    private static final ApiCircuitBreaker googleBooksCircuitBreaker = 
            new ApiCircuitBreaker("GoogleBooks", 4, 20000L, 2);

    /**
     * Получить клиент для TMDB API
     * @return Retrofit клиент для TMDB API
     */
    public static Retrofit getTmdbClient() {
        if (tmdbRetrofit == null) {
            tmdbRetrofit = createRetrofitClient(
                ApiConstants.TMDB_BASE_URL,
                tmdbRateLimiter,
                tmdbCircuitBreaker,
                "TMDB"
            );
        }
        return tmdbRetrofit;
    }

    /**
     * Получить клиент для RAWG API
     * @return Retrofit клиент для RAWG API
     */
    public static Retrofit getRawgClient() {
        if (rawgRetrofit == null) {
            rawgRetrofit = createRetrofitClient(
                ApiConstants.RAWG_BASE_URL,
                rawgRateLimiter,
                rawgCircuitBreaker,
                "RAWG"
            );
        }
        return rawgRetrofit;
    }

    /**
     * Получить клиент для Google Books API
     * @return Retrofit клиент для Google Books API
     */
    public static Retrofit getGoogleBooksClient() {
        if (googleBooksRetrofit == null) {
            googleBooksRetrofit = createRetrofitClient(
                ApiConstants.GOOGLE_BOOKS_BASE_URL,
                googleBooksRateLimiter,
                googleBooksCircuitBreaker,
                "GoogleBooks"
            );
        }
        return googleBooksRetrofit;
    }

    /**
     * Получить клиент для Jikan API с усиленной защитой от rate limiting
     * @return Retrofit клиент для Jikan API
     */
    public static Retrofit getJikanClient() {
        if (jikanRetrofit == null) {
            jikanRetrofit = createJikanRetrofitClient();
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
                        Request original = chain.request();
                        Request request = original.newBuilder()
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
    
    /**
     * Создать специализированный Retrofit клиент для Jikan API с максимальной защитой
     */
    private static Retrofit createJikanRetrofitClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS); // Только заголовки для Jikan
        
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(createJikanRateLimitInterceptor())
                .addInterceptor(createAdaptiveDelayInterceptor("Jikan"))
                .addInterceptor(createRetryAfterInterceptor())
                .connectTimeout(45, TimeUnit.SECONDS) // Увеличенный таймаут для Jikan
                .readTimeout(45, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        return new Retrofit.Builder()
                .baseUrl(ApiConstants.JIKAN_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(httpClient.build())
                .build();
    }
    
    /**
     * Создать универсальный Retrofit клиент с rate limiting
     */
    private static Retrofit createRetrofitClient(String baseUrl, TokenBucketRateLimiter rateLimiter, 
                                               ApiCircuitBreaker circuitBreaker, String apiName) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(createRateLimitInterceptor(rateLimiter, circuitBreaker, apiName))
                .addInterceptor(createAdaptiveDelayInterceptor(apiName))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(httpClient.build())
                .build();
    }
    
    /**
     * Создать специальный rate limit interceptor для Jikan API
     */
    private static Interceptor createJikanRateLimitInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // Проверяем circuit breaker
                if (!jikanCircuitBreaker.canExecute()) {
                    Log.w(TAG, "Jikan Circuit Breaker открыт, блокируем запрос");
                    throw new IOException("Circuit breaker is open for Jikan API");
                }
                
                // Ждем токен с таймаутом
                if (!jikanRateLimiter.acquire(5000L)) {
                    Log.w(TAG, "Не удалось получить токен для Jikan API в течение 5 секунд");
                    throw new IOException("Rate limit timeout for Jikan API");
                }
                
                Log.d(TAG, "Выполняем запрос к Jikan API. " + jikanRateLimiter.getStatus());
                
                try {
                    Response response = chain.proceed(chain.request());
                    
                    // Для мониторинга создаем обертку retrofit2.Response из okhttp3.Response
                    // Поскольку AdaptiveRateLimiter ожидает retrofit2.Response, создаем простую обертку
                    String url = response.request().url().toString();
                    String endpoint = NetworkHelper.getEndpointName(url);
                    
                    if (response.isSuccessful()) {
                        // Успешный запрос - сбрасываем circuit breaker
                        Log.d(TAG, "Успешный запрос к Jikan API");
                    } else if (response.code() == 429) {
                        Log.w(TAG, "Получен HTTP 429 от Jikan API, увеличиваем задержки");
                        // Дополнительная задержка при 429
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        throw new IOException("Rate limit exceeded for Jikan API");
                    }
                    
                    return response;
                    
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при запросе к Jikan API: " + e.getMessage());
                    throw e;
                }
            }
        };
    }
    
    /**
     * Создать универсальный rate limit interceptor
     */
    private static Interceptor createRateLimitInterceptor(TokenBucketRateLimiter rateLimiter, 
                                                        ApiCircuitBreaker circuitBreaker, String apiName) {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // Проверяем circuit breaker
                if (!circuitBreaker.canExecute()) {
                    Log.w(TAG, apiName + " Circuit Breaker открыт, блокируем запрос");
                    throw new IOException("Circuit breaker is open for " + apiName);
                }
                
                // Ждем токен
                if (!rateLimiter.acquire(3000L)) {
                    Log.w(TAG, "Rate limit timeout для " + apiName);
                    throw new IOException("Rate limit timeout for " + apiName);
                }
                
                try {
                    Response response = chain.proceed(chain.request());
                    
                    // Для мониторинга создаем endpoint name из URL
                    String url = response.request().url().toString();
                    String endpoint = NetworkHelper.getEndpointName(url);
                    
                    if (response.code() == 429) {
                        Log.w(TAG, "HTTP 429 от " + apiName);
                        throw new IOException("Rate limit exceeded for " + apiName);
                    }
                    
                    return response;
                    
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при запросе к " + apiName + ": " + e.getMessage());
                    throw e;
                }
            }
        };
    }
    
    /**
     * Создать interceptor для адаптивных задержек
     */
    private static Interceptor createAdaptiveDelayInterceptor(String apiName) {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // Рассчитываем оптимальную задержку
                long delay = adaptiveRateLimiter.calculateOptimalDelay(apiName.toLowerCase());
                
                if (delay > 0) {
                    Log.d(TAG, "Адаптивная задержка для " + apiName + ": " + delay + "мс");
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during adaptive delay");
                    }
                }
                
                return chain.proceed(chain.request());
            }
        };
    }
    
    /**
     * Создать interceptor для обработки Retry-After заголовка
     */
    private static Interceptor createRetryAfterInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                
                if (response.code() == 429) {
                    String retryAfter = response.header("Retry-After");
                    if (retryAfter != null) {
                        try {
                            long retrySeconds = Long.parseLong(retryAfter);
                            Log.w(TAG, "API требует подождать " + retrySeconds + " секунд (Retry-After)");
                            
                            // Для Jikan API строго соблюдаем Retry-After
                            if (retrySeconds <= 120) { // Максимум 2 минуты
                                Thread.sleep(retrySeconds * 1000);
                                // Повторяем запрос один раз
                                response.close();
                                return chain.proceed(chain.request());
                            }
                        } catch (NumberFormatException | InterruptedException e) {
                            Log.w(TAG, "Не удалось обработать Retry-After: " + retryAfter);
                        }
                    }
                }
                
                return response;
            }
        };
    }
    
    /**
     * Получить статистику всех rate limiters
     */
    public static String getRateLimitStatus() {
        StringBuilder status = new StringBuilder("Rate Limit Status:\n");
        status.append("Jikan: ").append(jikanRateLimiter.getStatus()).append("\n");
        status.append("TMDB: ").append(tmdbRateLimiter.getStatus()).append("\n");
        status.append("RAWG: ").append(rawgRateLimiter.getStatus()).append("\n");
        status.append("GoogleBooks: ").append(googleBooksRateLimiter.getStatus()).append("\n");
        status.append("\nCircuit Breakers:\n");
        status.append("Jikan: ").append(jikanCircuitBreaker.getDetailedStatus()).append("\n");
        status.append("TMDB: ").append(tmdbCircuitBreaker.getDetailedStatus()).append("\n");
        status.append("RAWG: ").append(rawgCircuitBreaker.getDetailedStatus()).append("\n");
        status.append("GoogleBooks: ").append(googleBooksCircuitBreaker.getDetailedStatus()).append("\n");
        return status.toString();
    }
    
    /**
     * Сбросить все rate limiters и circuit breakers
     */
    public static void resetAllLimiters() {
        jikanRateLimiter.reset();
        tmdbRateLimiter.reset();
        rawgRateLimiter.reset();
        googleBooksRateLimiter.reset();
        
        jikanCircuitBreaker.reset();
        tmdbCircuitBreaker.reset();
        rawgCircuitBreaker.reset();
        googleBooksCircuitBreaker.reset();
        
        adaptiveRateLimiter.cleanupExpiredInfo();
        
        Log.i(TAG, "Все rate limiters и circuit breakers сброшены");
    }
}
