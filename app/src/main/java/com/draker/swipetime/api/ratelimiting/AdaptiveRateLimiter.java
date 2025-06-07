package com.draker.swipetime.api.ratelimiting;

import android.util.Log;
import retrofit2.Response;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Адаптивный лимитер скорости, который настраивается на основе заголовков API ответов
 */
public class AdaptiveRateLimiter {
    private static final String TAG = "AdaptiveRateLimiter";
    
    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitTracker = new ConcurrentHashMap<>();
    
    /**
     * Информация о лимитах для конкретного endpoint
     */
    public static class RateLimitInfo {
        public final long resetTime;
        public final int remainingCalls;
        public final int totalCalls;
        public final long windowSize;
        public final long lastUpdated;
        
        public RateLimitInfo(long resetTime, int remainingCalls, int totalCalls, long windowSize) {
            this.resetTime = resetTime;
            this.remainingCalls = remainingCalls;
            this.totalCalls = totalCalls;
            this.windowSize = windowSize;
            this.lastUpdated = System.currentTimeMillis();
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > resetTime;
        }
        
        public double getUsageRatio() {
            if (totalCalls <= 0) return 0.0;
            return (double) (totalCalls - remainingCalls) / totalCalls;
        }
    }
    
    /**
     * Обновить информацию о лимитах на основе заголовков ответа
     * @param endpoint название endpoint
     * @param response ответ от API
     */
    public void updateFromHeaders(String endpoint, Response<?> response) {
        try {
            // Обработка заголовков для Jikan API
            Long resetTime = extractLongHeader(response, "x-ratelimit-reset");
            Integer remaining = extractIntHeader(response, "x-ratelimit-remaining");
            Integer total = extractIntHeader(response, "x-ratelimit-limit");
            
            // Обработка стандартных заголовков
            if (resetTime == null) {
                resetTime = extractLongHeader(response, "ratelimit-reset");
            }
            if (remaining == null) {
                remaining = extractIntHeader(response, "ratelimit-remaining");
            }
            if (total == null) {
                total = extractIntHeader(response, "ratelimit-limit");
            }
            
            // Значения по умолчанию для Jikan API
            if (resetTime == null) {
                resetTime = System.currentTimeMillis() + 60000L; // +1 минута
            }
            if (remaining == null) {
                remaining = 30; // Консервативная оценка
            }
            if (total == null) {
                total = 60; // Лимит Jikan API в минуту
            }
            
            RateLimitInfo info = new RateLimitInfo(resetTime, remaining, total, 60000L);
            rateLimitTracker.put(endpoint, info);
            
            Log.d(TAG, String.format("Обновлены лимиты для %s: %d/%d осталось, сброс через %d сек", 
                    endpoint, remaining, total, (resetTime - System.currentTimeMillis()) / 1000));
                    
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обработке заголовков rate limit для " + endpoint, e);
        }
    }
    
    /**
     * Рассчитать оптимальную задержку перед следующим запросом
     * @param endpoint название endpoint
     * @return рекомендуемая задержка в миллисекундах
     */
    public long calculateOptimalDelay(String endpoint) {
        RateLimitInfo info = rateLimitTracker.get(endpoint);
        
        if (info == null) {
            // Для Jikan API используем безопасную задержку по умолчанию
            return getDefaultDelay(endpoint);
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Если лимиты сброшены, обновляем информацию
        if (info.isExpired()) {
            rateLimitTracker.remove(endpoint);
            return getDefaultDelay(endpoint);
        }
        
        // Если запросы закончились, ждем сброса
        if (info.remainingCalls <= 0) {
            long waitTime = info.resetTime - currentTime;
            Log.w(TAG, "Лимит исчерпан для " + endpoint + ", ожидание " + waitTime + "мс");
            return Math.max(waitTime, 1000L);
        }
        
        // Адаптивная задержка на основе оставшихся запросов
        long timeToReset = info.resetTime - currentTime;
        
        if (info.remainingCalls < 5) {
            // Критически мало запросов - увеличиваем задержку
            long conservativeDelay = timeToReset / Math.max(info.remainingCalls, 1);
            Log.d(TAG, "Критически мало запросов для " + endpoint + 
                    ", консервативная задержка: " + conservativeDelay + "мс");
            return Math.max(conservativeDelay, 2000L);
        } else if (info.remainingCalls < 10) {
            // Умеренно мало запросов - стандартная задержка
            long moderateDelay = timeToReset / (info.remainingCalls * 2);
            return Math.max(moderateDelay, 1000L);
        } else {
            // Достаточно запросов - минимальная задержка
            return getDefaultDelay(endpoint);
        }
    }
    
    /**
     * Проверить, можно ли выполнить запрос сейчас
     * @param endpoint название endpoint
     * @return true если запрос можно выполнить
     */
    public boolean canMakeRequest(String endpoint) {
        RateLimitInfo info = rateLimitTracker.get(endpoint);
        
        if (info == null || info.isExpired()) {
            return true; // Нет информации о лимитах или они сброшены
        }
        
        return info.remainingCalls > 0;
    }
    
    /**
     * Получить статистику использования лимитов
     * @param endpoint название endpoint
     * @return статистика или null если данных нет
     */
    public RateLimitInfo getRateLimitInfo(String endpoint) {
        return rateLimitTracker.get(endpoint);
    }
    
    /**
     * Очистить устаревшую информацию о лимитах
     */
    public void cleanupExpiredInfo() {
        rateLimitTracker.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Получить задержку по умолчанию для endpoint
     * @param endpoint название endpoint
     * @return задержка в миллисекундах
     */
    private long getDefaultDelay(String endpoint) {
        if (endpoint.contains("jikan") || endpoint.contains("anime")) {
            return 350L; // Минимальная безопасная задержка для Jikan API (1/3 секунды)
        } else if (endpoint.contains("tmdb")) {
            return 100L; // TMDB более щедрый
        } else if (endpoint.contains("rawg")) {
            return 150L; // RAWG умеренный
        } else if (endpoint.contains("googleapis")) {
            return 200L; // Google Books консервативный
        } else {
            return 500L; // Неизвестный API - консервативно
        }
    }
    
    /**
     * Извлечь long значение из заголовка
     */
    private Long extractLongHeader(Response<?> response, String headerName) {
        try {
            String value = response.headers().get(headerName);
            if (value != null && !value.isEmpty()) {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Не удалось преобразовать заголовок " + headerName + " в long", e);
        }
        return null;
    }
    
    /**
     * Извлечь int значение из заголовка
     */
    private Integer extractIntHeader(Response<?> response, String headerName) {
        try {
            String value = response.headers().get(headerName);
            if (value != null && !value.isEmpty()) {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Не удалось преобразовать заголовок " + headerName + " в int", e);
        }
        return null;
    }
    
    /**
     * Получить общую статистику всех endpoint
     * @return строка с информацией
     */
    public String getOverallStatus() {
        StringBuilder status = new StringBuilder("AdaptiveRateLimiter Status:\n");
        
        for (String endpoint : rateLimitTracker.keySet()) {
            RateLimitInfo info = rateLimitTracker.get(endpoint);
            status.append(String.format("  %s: %d/%d (%.1f%% использовано)\n", 
                    endpoint, info.remainingCalls, info.totalCalls, info.getUsageRatio() * 100));
        }
        
        return status.toString();
    }
}
