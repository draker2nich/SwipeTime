package com.draker.swipetime.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.draker.swipetime.api.monitoring.ApiPerformanceMonitor;

/**
 * Утилита для проверки наличия интернет-соединения, мониторинга его состояния и API производительности
 */
public class NetworkHelper {
    private static final String TAG = "NetworkHelper";
    
    private static NetworkHelper instance;
    private final MutableLiveData<Boolean> isNetworkAvailable = new MutableLiveData<>();
    private final ConnectivityManager connectivityManager;
    private final ConnectivityManager.NetworkCallback networkCallback;
    private final ApiPerformanceMonitor performanceMonitor;

    private NetworkHelper(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        performanceMonitor = ApiPerformanceMonitor.getInstance();
        
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                isNetworkAvailable.postValue(true);
                Log.d(TAG, "Сеть стала доступной");
            }

            @Override
            public void onLost(@NonNull Network network) {
                isNetworkAvailable.postValue(false);
                Log.w(TAG, "Сеть потеряна");
            }
        };
        
        // Инициализация начального состояния
        isNetworkAvailable.postValue(isInternetAvailable());
        
        // Регистрация колбэка для мониторинга сети
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        
        Log.d(TAG, "NetworkHelper инициализирован с мониторингом API");
    }

    /**
     * Получить экземпляр NetworkHelper
     * @param context контекст приложения
     * @return экземпляр NetworkHelper
     */
    public static synchronized NetworkHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Проверить доступность интернета в данный момент
     * @return true, если интернет доступен
     */
    public boolean isInternetAvailable() {
        if (connectivityManager == null) {
            return false;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Для Android 6.0 (API 23) и выше
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }
            
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            // Для более старых версий Android
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    /**
     * Получить LiveData для наблюдения за состоянием сети
     * @return LiveData с состоянием сети (true - доступна, false - недоступна)
     */
    public LiveData<Boolean> getNetworkAvailability() {
        return isNetworkAvailable;
    }
    
    /**
     * Записать успешный API вызов
     * @param endpoint название endpoint
     * @param responseTime время ответа в миллисекундах
     */
    public void recordSuccessfulApiCall(String endpoint, long responseTime) {
        performanceMonitor.recordApiCall(endpoint, responseTime, true, 200);
        Log.d(TAG, "Записан успешный вызов " + endpoint + " (" + responseTime + "ms)");
    }
    
    /**
     * Записать неудачный API вызов
     * @param endpoint название endpoint
     * @param responseTime время ответа в миллисекундах
     * @param httpCode код ответа HTTP
     */
    public void recordFailedApiCall(String endpoint, long responseTime, int httpCode) {
        performanceMonitor.recordApiCall(endpoint, responseTime, false, httpCode);
        Log.w(TAG, "Записана ошибка вызова " + endpoint + " (" + responseTime + "ms, HTTP " + httpCode + ")");
    }
    
    /**
     * Проверить здоровье API
     * @param endpoint название endpoint
     * @return true если API работает нормально
     */
    public boolean isApiHealthy(String endpoint) {
        boolean isHealthy = performanceMonitor.isApiHealthy(endpoint);
        Log.d(TAG, "API " + endpoint + " " + (isHealthy ? "здоров" : "имеет проблемы"));
        return isHealthy;
    }
    
    /**
     * Получить рекомендуемую задержку для API на основе его здоровья
     * @param endpoint название endpoint
     * @return задержка в миллисекундах
     */
    public long getRecommendedDelay(String endpoint) {
        double healthScore = performanceMonitor.getHealthScore(endpoint);
        ApiPerformanceMonitor.ApiMetrics metrics = performanceMonitor.getMetrics(endpoint);
        
        // Базовые задержки для разных API
        long baseDelay;
        if (endpoint.contains("jikan")) {
            baseDelay = 350L; // Jikan API очень строгий
        } else if (endpoint.contains("tmdb")) {
            baseDelay = 100L; // TMDB более щедрый
        } else if (endpoint.contains("rawg")) {
            baseDelay = 150L; // RAWG умеренный
        } else if (endpoint.contains("google")) {
            baseDelay = 200L; // Google Books консервативный
        } else {
            baseDelay = 500L; // Неизвестный API
        }
        
        // Адаптируем задержку на основе здоровья API
        if (healthScore < 0.3) {
            baseDelay *= 5; // Очень больной API
        } else if (healthScore < 0.5) {
            baseDelay *= 3; // Больной API
        } else if (healthScore < 0.7) {
            baseDelay *= 2; // Не очень здоровый API
        }
        
        // Учитываем последовательные ошибки
        if (metrics != null && metrics.getConsecutiveFailures() > 0) {
            baseDelay += metrics.getConsecutiveFailures() * 1000L;
        }
        
        Log.d(TAG, "Рекомендуемая задержка для " + endpoint + ": " + baseDelay + "ms (health: " + 
                String.format("%.2f", healthScore) + ")");
        
        return baseDelay;
    }
    
    /**
     * Получить статистику производительности API
     * @return строка с детальной статистикой
     */
    public String getApiPerformanceReport() {
        String report = performanceMonitor.getOverallStatus();
        Log.i(TAG, "Сгенерирован отчет о производительности API");
        return report;
    }
    
    /**
     * Сбросить статистику для всех API
     */
    public void resetApiStatistics() {
        performanceMonitor.resetAllMetrics();
        Log.i(TAG, "Статистика API сброшена");
    }
    
    /**
     * Проверить, следует ли делать запрос к API на основе его состояния
     * @param endpoint название endpoint
     * @return true если запрос можно делать
     */
    public boolean shouldMakeApiCall(String endpoint) {
        // Проверяем доступность сети
        if (!isInternetAvailable()) {
            Log.w(TAG, "Сеть недоступна, запрос к " + endpoint + " отклонен");
            return false;
        }
        
        // Проверяем здоровье API
        if (!isApiHealthy(endpoint)) {
            Log.w(TAG, "API " + endpoint + " нездоров, запрос отклонен");
            return false;
        }
        
        // Проверяем последовательные ошибки
        ApiPerformanceMonitor.ApiMetrics metrics = performanceMonitor.getMetrics(endpoint);
        if (metrics != null && metrics.getConsecutiveFailures() >= 5) {
            Log.w(TAG, "API " + endpoint + " имеет " + metrics.getConsecutiveFailures() + 
                    " последовательных ошибок, запрос отклонен");
            return false;
        }
        
        Log.d(TAG, "Запрос к " + endpoint + " разрешен");
        return true;
    }
    
    /**
     * Получить название endpoint по URL
     * @param url URL запроса
     * @return стандартизированное название endpoint
     */
    public static String getEndpointName(String url) {
        if (url.contains("jikan.moe")) {
            if (url.contains("/anime")) {
                return "jikan_anime";
            } else {
                return "jikan_other";
            }
        } else if (url.contains("themoviedb.org")) {
            if (url.contains("/movie")) {
                return "tmdb_movie";
            } else if (url.contains("/tv")) {
                return "tmdb_tv";
            } else {
                return "tmdb_other";
            }
        } else if (url.contains("rawg.io")) {
            return "rawg_games";
        } else if (url.contains("googleapis.com/books")) {
            return "google_books";
        } else {
            return "unknown_api";
        }
    }
    
    /**
     * Статический метод для обратной совместимости
     * @param context контекст приложения
     * @return true если сеть доступна
     */
    public static boolean isNetworkAvailable(Context context) {
        return getInstance(context).isInternetAvailable();
    }

    /**
     * Освободить ресурсы сетевого монитора
     */
    public void cleanup() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
            Log.d(TAG, "NetworkHelper очищен");
        } catch (Exception e) {
            Log.w(TAG, "Ошибка при очистке NetworkHelper", e);
        }
    }
}
