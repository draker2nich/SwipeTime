package com.draker.swipetime.api.retry;

import android.util.Log;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import retrofit2.HttpException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Интеллигентная стратегия повторения запросов с exponential backoff и jitter
 * Специально оптимизирована для работы с различными API и типами ошибок
 */
public class IntelligentRetryStrategy {
    private static final String TAG = "IntelligentRetryStrategy";
    
    private final int maxRetries;
    private final long baseDelay;
    private final long maxDelay;
    private final long jitterRange;
    private final Random random = new Random();
    
    /**
     * Конструктор для Jikan API с консервативными настройками
     */
    public IntelligentRetryStrategy() {
        this(3, 1000L, 32000L, 1000L);
    }
    
    /**
     * Конструктор с настраиваемыми параметрами
     * @param maxRetries максимальное количество повторов
     * @param baseDelay базовая задержка в миллисекундах
     * @param maxDelay максимальная задержка в миллисекундах
     * @param jitterRange диапазон случайного jitter в миллисекундах
     */
    public IntelligentRetryStrategy(int maxRetries, long baseDelay, long maxDelay, long jitterRange) {
        this.maxRetries = maxRetries;
        this.baseDelay = baseDelay;
        this.maxDelay = maxDelay;
        this.jitterRange = jitterRange;
        
        Log.d(TAG, String.format("Инициализирована стратегия повторов: maxRetries=%d, baseDelay=%dms, maxDelay=%dms, jitter=%dms",
                maxRetries, baseDelay, maxDelay, jitterRange));
    }
    
    /**
     * Применить стратегию повторов к Single
     * @param source источник Single
     * @param <T> тип данных
     * @return Single с примененной стратегией повторов
     */
    public <T> Single<T> applyTo(Single<T> source) {
        return source.retryWhen(errors -> 
            errors.toObservable()
                    .zipWith(Observable.range(1, maxRetries + 1), this::createRetryAttempt)
                    .flatMap(this::processRetryAttempt)
                    .toFlowable(io.reactivex.rxjava3.core.BackpressureStrategy.BUFFER)
        );
    }

    /**
     * Применить стратегию повторов к Observable
     * @param source источник Observable
     * @param <T> тип данных
     * @return Observable с примененной стратегией повторов
     */
    public <T> Observable<T> applyTo(Observable<T> source) {
        return source.retryWhen(this::createRetryHandler);
    }
    
    /**
     * Создать обработчик повторов
     * @param errors поток ошибок
     * @return поток с задержками
     */
    private Observable<Long> createRetryHandler(Observable<Throwable> errors) {
        return errors
                .zipWith(Observable.range(1, maxRetries + 1), this::createRetryAttempt)
                .flatMap(this::processRetryAttempt);
    }
    
    /**
     * Обработать попытку повтора
     * @param attempt информация о попытке
     * @return Observable с задержкой или ошибкой
     */
    private Observable<Long> processRetryAttempt(RetryAttempt attempt) {
        if (attempt.shouldRetry) {
            Log.d(TAG, String.format("Повтор %d/%d через %dms для ошибки: %s", 
                    attempt.attemptNumber, maxRetries, attempt.delay, 
                    attempt.error.getClass().getSimpleName()));
            
            return Observable.timer(attempt.delay, TimeUnit.MILLISECONDS);
        } else {
            Log.w(TAG, String.format("Превышено максимальное количество повторов (%d) или ошибка не подлежит повтору: %s", 
                    maxRetries, attempt.error.getMessage()));
            
            return Observable.error(attempt.error);
        }
    }
    
    /**
     * Создать информацию о попытке повтора
     * @param error ошибка
     * @param attemptNumber номер попытки
     * @return информация о попытке
     */
    private RetryAttempt createRetryAttempt(Throwable error, int attemptNumber) {
        boolean shouldRetry = attemptNumber <= maxRetries && shouldRetry(error);
        long delay = shouldRetry ? calculateBackoffWithJitter(attemptNumber, error) : 0;
        
        return new RetryAttempt(error, attemptNumber, shouldRetry, delay);
    }
    
    /**
     * Определить, следует ли повторять запрос для данной ошибки
     * @param error ошибка
     * @return true если следует повторить
     */
    private boolean shouldRetry(Throwable error) {
        if (error instanceof HttpException) {
            HttpException httpException = (HttpException) error;
            int code = httpException.code();
            
            switch (code) {
                case 429: // Too Many Requests - всегда повторяем
                    Log.d(TAG, "HTTP 429 - будем повторять с увеличенной задержкой");
                    return true;
                    
                case 500: // Internal Server Error
                case 502: // Bad Gateway
                case 503: // Service Unavailable
                case 504: // Gateway Timeout
                    Log.d(TAG, "HTTP " + code + " - серверная ошибка, повторяем");
                    return true;
                    
                case 400: // Bad Request
                case 401: // Unauthorized
                case 403: // Forbidden
                case 404: // Not Found
                    Log.d(TAG, "HTTP " + code + " - клиентская ошибка, не повторяем");
                    return false;
                    
                default:
                    // Для других HTTP кодов повторяем только серверные ошибки (5xx)
                    boolean isServerError = code >= 500 && code < 600;
                    Log.d(TAG, "HTTP " + code + " - " + (isServerError ? "повторяем" : "не повторяем"));
                    return isServerError;
            }
        }
        
        // Сетевые ошибки - повторяем
        if (error instanceof SocketTimeoutException) {
            Log.d(TAG, "SocketTimeoutException - повторяем");
            return true;
        }
        
        if (error instanceof UnknownHostException) {
            Log.d(TAG, "UnknownHostException - повторяем");
            return true;
        }
        
        if (error instanceof IOException) {
            Log.d(TAG, "IOException - повторяем");
            return true;
        }
        
        // Все остальные ошибки не повторяем
        Log.d(TAG, "Неизвестная ошибка " + error.getClass().getSimpleName() + " - не повторяем");
        return false;
    }
    
    /**
     * Рассчитать задержку с exponential backoff и jitter
     * @param attempt номер попытки
     * @param error ошибка для адаптации задержки
     * @return задержка в миллисекундах
     */
    private long calculateBackoffWithJitter(int attempt, Throwable error) {
        // Базовая экспоненциальная задержка
        long exponentialDelay = (long) (baseDelay * Math.pow(2.0, attempt - 1));
        
        // Специальная обработка для HTTP 429
        if (error instanceof HttpException && ((HttpException) error).code() == 429) {
            // Для 429 ошибок используем более агрессивную задержку
            exponentialDelay = Math.max(exponentialDelay, 5000L); // Минимум 5 секунд
            
            // Пытаемся извлечь Retry-After заголовок
            try {
                String retryAfter = ((HttpException) error).response().headers().get("Retry-After");
                if (retryAfter != null && !retryAfter.isEmpty()) {
                    long retryAfterMs = Long.parseLong(retryAfter) * 1000L;
                    exponentialDelay = Math.max(exponentialDelay, retryAfterMs);
                    Log.d(TAG, "Используем Retry-After: " + retryAfterMs + "ms");
                }
            } catch (Exception e) {
                Log.w(TAG, "Не удалось извлечь Retry-After заголовок", e);
            }
        }
        
        // Добавляем случайный jitter для распределения нагрузки
        long jitter = random.nextLong() % (jitterRange * 2) - jitterRange;
        long finalDelay = exponentialDelay + jitter;
        
        // Ограничиваем максимальной задержкой
        finalDelay = Math.min(finalDelay, maxDelay);
        
        // Гарантируем минимальную задержку
        finalDelay = Math.max(finalDelay, 100L);
        
        Log.d(TAG, String.format("Рассчитанная задержка: exponential=%dms, jitter=%dms, final=%dms", 
                exponentialDelay, jitter, finalDelay));
        
        return finalDelay;
    }
    
    /**
     * Создать специализированную стратегию для Jikan API
     * @return стратегия для Jikan API
     */
    public static IntelligentRetryStrategy forJikanApi() {
        return new IntelligentRetryStrategy(2, 2000L, 30000L, 1000L);
    }
    
    /**
     * Создать стратегию для TMDB API
     * @return стратегия для TMDB API
     */
    public static IntelligentRetryStrategy forTmdbApi() {
        return new IntelligentRetryStrategy(3, 500L, 10000L, 500L);
    }
    
    /**
     * Создать стратегию для RAWG API
     * @return стратегия для RAWG API
     */
    public static IntelligentRetryStrategy forRawgApi() {
        return new IntelligentRetryStrategy(3, 750L, 15000L, 750L);
    }
    
    /**
     * Создать стратегию для Google Books API
     * @return стратегия для Google Books API
     */
    public static IntelligentRetryStrategy forGoogleBooksApi() {
        return new IntelligentRetryStrategy(3, 1000L, 20000L, 1000L);
    }
    
    /**
     * Информация о попытке повтора
     */
    private static class RetryAttempt {
        final Throwable error;
        final int attemptNumber;
        final boolean shouldRetry;
        final long delay;
        
        RetryAttempt(Throwable error, int attemptNumber, boolean shouldRetry, long delay) {
            this.error = error;
            this.attemptNumber = attemptNumber;
            this.shouldRetry = shouldRetry;
            this.delay = delay;
        }
    }
    
    /**
     * Создать композитную стратегию с circuit breaker
     * @param circuitBreaker circuit breaker для проверки
     * @return функция для retryWhen
     */
    public Function<Observable<Throwable>, Observable<Long>> withCircuitBreaker(
            com.draker.swipetime.api.resilience.ApiCircuitBreaker circuitBreaker) {
        
        return errors -> errors
                .zipWith(Observable.range(1, maxRetries + 1), this::createRetryAttempt)
                .flatMap(attempt -> {
                    // Проверяем circuit breaker перед повтором
                    if (!circuitBreaker.canExecute()) {
                        Log.w(TAG, "Circuit breaker открыт, прекращаем повторы");
                        return Observable.error(new RuntimeException("Circuit breaker is open"));
                    }
                    
                    if (attempt.shouldRetry) {
                        Log.d(TAG, String.format("Повтор %d/%d через %dms с circuit breaker проверкой", 
                                attempt.attemptNumber, maxRetries, attempt.delay));
                        
                        return Observable.timer(attempt.delay, TimeUnit.MILLISECONDS);
                    } else {
                        return Observable.error(attempt.error);
                    }
                });
    }
    
    /**
     * Получить статистику стратегии
     * @return строка с информацией
     */
    public String getStrategyInfo() {
        return String.format("RetryStrategy[maxRetries=%d, baseDelay=%dms, maxDelay=%dms, jitter=%dms]",
                maxRetries, baseDelay, maxDelay, jitterRange);
    }
}
