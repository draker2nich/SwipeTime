package com.draker.swipetime.api.monitoring;

import android.util.Log;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Монитор производительности API для отслеживания метрик запросов
 */
public class ApiPerformanceMonitor {
    private static final String TAG = "ApiPerformanceMonitor";
    
    private static ApiPerformanceMonitor instance;
    private final ConcurrentHashMap<String, ApiMetrics> metrics = new ConcurrentHashMap<>();
    
    /**
     * Метрики для конкретного API endpoint
     */
    public static class ApiMetrics {
        private final AtomicLong totalCalls = new AtomicLong(0);
        private final AtomicLong successfulCalls = new AtomicLong(0);
        private final AtomicLong failedCalls = new AtomicLong(0);
        private final AtomicLong rateLimitHits = new AtomicLong(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private final AtomicLong lastCallTimestamp = new AtomicLong(0);
        private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
        private final AtomicLong firstCallTimestamp = new AtomicLong(System.currentTimeMillis());
        
        // Коды ошибок
        private final ConcurrentHashMap<Integer, AtomicInteger> errorCodes = new ConcurrentHashMap<>();
        
        public void recordCall(long responseTime, boolean isSuccess, int responseCode) {
            totalCalls.incrementAndGet();
            lastCallTimestamp.set(System.currentTimeMillis());
            totalResponseTime.addAndGet(responseTime);
            
            if (isSuccess) {
                successfulCalls.incrementAndGet();
                consecutiveFailures.set(0);
            } else {
                failedCalls.incrementAndGet();
                consecutiveFailures.incrementAndGet();
                
                if (responseCode == 429) {
                    rateLimitHits.incrementAndGet();
                }
                
                // Записываем код ошибки
                errorCodes.computeIfAbsent(responseCode, k -> new AtomicInteger(0)).incrementAndGet();
            }
        }
        
        public double getSuccessRate() {
            long total = totalCalls.get();
            return total > 0 ? (double) successfulCalls.get() / total : 0.0;
        }
        
        public double getAverageResponseTime() {
            long total = totalCalls.get();
            return total > 0 ? (double) totalResponseTime.get() / total : 0.0;
        }
        
        public double getRateLimitHitRate() {
            long total = totalCalls.get();
            return total > 0 ? (double) rateLimitHits.get() / total : 0.0;
        }
        
        public double getHealthScore() {
            double successRate = getSuccessRate();
            double rateLimitPenalty = getRateLimitHitRate() * 0.5;
            double consecutiveFailurePenalty = Math.min(consecutiveFailures.get() * 0.1, 0.5);
            
            return Math.max(0.0, successRate - rateLimitPenalty - consecutiveFailurePenalty);
        }
        
        public long getCallsPerMinute() {
            long uptime = System.currentTimeMillis() - firstCallTimestamp.get();
            if (uptime <= 0) return 0;
            
            return (totalCalls.get() * 60000L) / uptime;
        }
        
        public String getDetailedStatus() {
            return String.format(
                "Calls: %d (%.1f%% success), RateLimits: %d (%.1f%%), AvgTime: %.0fms, Health: %.2f, RPM: %d, ConsecutiveFails: %d",
                totalCalls.get(), getSuccessRate() * 100,
                rateLimitHits.get(), getRateLimitHitRate() * 100,
                getAverageResponseTime(), getHealthScore(),
                getCallsPerMinute(), consecutiveFailures.get()
            );
        }
        
        public ConcurrentHashMap<Integer, AtomicInteger> getErrorCodes() {
            return errorCodes;
        }
        
        // Геттеры для основных метрик
        public long getTotalCalls() { return totalCalls.get(); }
        public long getSuccessfulCalls() { return successfulCalls.get(); }
        public long getFailedCalls() { return failedCalls.get(); }
        public long getRateLimitHits() { return rateLimitHits.get(); }
        public long getLastCallTimestamp() { return lastCallTimestamp.get(); }
        public int getConsecutiveFailures() { return consecutiveFailures.get(); }
    }
    
    private ApiPerformanceMonitor() {
        Log.d(TAG, "ApiPerformanceMonitor инициализирован");
    }
    
    /**
     * Получить экземпляр монитора
     * @return экземпляр ApiPerformanceMonitor
     */
    public static synchronized ApiPerformanceMonitor getInstance() {
        if (instance == null) {
            instance = new ApiPerformanceMonitor();
        }
        return instance;
    }
    
    /**
     * Записать вызов API
     * @param endpoint название endpoint
     * @param responseTime время ответа в миллисекундах
     * @param isSuccess успешен ли вызов
     * @param responseCode код ответа HTTP
     */
    public void recordApiCall(String endpoint, long responseTime, boolean isSuccess, int responseCode) {
        ApiMetrics metric = metrics.computeIfAbsent(endpoint, k -> new ApiMetrics());
        metric.recordCall(responseTime, isSuccess, responseCode);
        
        if (!isSuccess) {
            Log.w(TAG, String.format("API Call Failed - %s: %dms, HTTP %d", 
                    endpoint, responseTime, responseCode));
        } else {
            Log.d(TAG, String.format("API Call Success - %s: %dms", endpoint, responseTime));
        }
        
        // Логируем критические состояния
        if (metric.getConsecutiveFailures() >= 5) {
            Log.e(TAG, String.format("CRITICAL: %s имеет %d последовательных ошибок!", 
                    endpoint, metric.getConsecutiveFailures()));
        }
        
        if (metric.getRateLimitHitRate() > 0.3) {
            Log.w(TAG, String.format("WARNING: %s превышает rate limits в %.1f%% случаев", 
                    endpoint, metric.getRateLimitHitRate() * 100));
        }
    }
    
    /**
     * Получить метрики для endpoint
     * @param endpoint название endpoint
     * @return метрики или null если нет данных
     */
    public ApiMetrics getMetrics(String endpoint) {
        return metrics.get(endpoint);
    }
    
    /**
     * Получить оценку здоровья API
     * @param endpoint название endpoint
     * @return оценка от 0.0 до 1.0
     */
    public double getHealthScore(String endpoint) {
        ApiMetrics metric = metrics.get(endpoint);
        return metric != null ? metric.getHealthScore() : 1.0;
    }
    
    /**
     * Проверить, является ли API здоровым
     * @param endpoint название endpoint
     * @param threshold порог здоровья (по умолчанию 0.7)
     * @return true если API здоров
     */
    public boolean isApiHealthy(String endpoint, double threshold) {
        return getHealthScore(endpoint) >= threshold;
    }
    
    public boolean isApiHealthy(String endpoint) {
        return isApiHealthy(endpoint, 0.7);
    }
    
    /**
     * Получить рекомендации по использованию API
     * @param endpoint название endpoint
     * @return строка с рекомендациями
     */
    public String getApiRecommendations(String endpoint) {
        ApiMetrics metric = metrics.get(endpoint);
        if (metric == null) {
            return "Недостаточно данных для рекомендаций";
        }
        
        StringBuilder recommendations = new StringBuilder();
        
        double successRate = metric.getSuccessRate();
        double rateLimitRate = metric.getRateLimitHitRate();
        long avgResponseTime = (long) metric.getAverageResponseTime();
        int consecutiveFailures = metric.getConsecutiveFailures();
        
        if (successRate < 0.8) {
            recommendations.append("• Низкий процент успешных запросов (").append(String.format("%.1f%%", successRate * 100))
                    .append("). Рассмотрите увеличение таймаутов или проверку сетевого соединения.\n");
        }
        
        if (rateLimitRate > 0.2) {
            recommendations.append("• Высокий процент rate limit ошибок (").append(String.format("%.1f%%", rateLimitRate * 100))
                    .append("). Увеличьте задержки между запросами.\n");
        }
        
        if (avgResponseTime > 5000) {
            recommendations.append("• Медленные ответы (").append(avgResponseTime).append("ms в среднем). ")
                    .append("Рассмотрите кеширование или оптимизацию запросов.\n");
        }
        
        if (consecutiveFailures >= 3) {
            recommendations.append("• Множественные последовательные ошибки (").append(consecutiveFailures)
                    .append("). API может быть недоступен.\n");
        }
        
        long rpm = metric.getCallsPerMinute();
        if (rpm > 180 && endpoint.contains("jikan")) {
            recommendations.append("• Слишком много запросов к Jikan API (").append(rpm).append(" RPM). ")
                    .append("Jikan имеет лимит ~60 запросов в минуту.\n");
        }
        
        return recommendations.length() > 0 ? recommendations.toString() : "API работает в пределах нормы";
    }
    
    /**
     * Получить общую статистику всех API
     * @return строка с подробной статистикой
     */
    public String getOverallStatus() {
        StringBuilder status = new StringBuilder("=== API Performance Monitor ===\n");
        
        if (metrics.isEmpty()) {
            status.append("Нет данных о вызовах API\n");
            return status.toString();
        }
        
        for (String endpoint : metrics.keySet()) {
            ApiMetrics metric = metrics.get(endpoint);
            status.append(String.format("\n%s:\n", endpoint.toUpperCase()));
            status.append("  ").append(metric.getDetailedStatus()).append("\n");
            
            // Показываем топ ошибок
            ConcurrentHashMap<Integer, AtomicInteger> errorCodes = metric.getErrorCodes();
            if (!errorCodes.isEmpty()) {
                status.append("  Ошибки: ");
                errorCodes.entrySet().stream()
                        .sorted((e1, e2) -> e2.getValue().get() - e1.getValue().get())
                        .limit(3)
                        .forEach(entry -> status.append("HTTP").append(entry.getKey())
                                .append("(").append(entry.getValue().get()).append(") "));
                status.append("\n");
            }
            
            // Рекомендации для проблемных API
            if (metric.getHealthScore() < 0.7) {
                status.append("  🚨 РЕКОМЕНДАЦИИ:\n");
                String recommendations = getApiRecommendations(endpoint);
                for (String line : recommendations.split("\n")) {
                    if (!line.trim().isEmpty()) {
                        status.append("    ").append(line).append("\n");
                    }
                }
            }
        }
        
        return status.toString();
    }
    
    /**
     * Сбросить все метрики
     */
    public void resetAllMetrics() {
        metrics.clear();
        Log.i(TAG, "Все метрики API сброшены");
    }
    
    /**
     * Сбросить метрики для конкретного endpoint
     * @param endpoint название endpoint
     */
    public void resetMetrics(String endpoint) {
        metrics.remove(endpoint);
        Log.i(TAG, "Метрики для " + endpoint + " сброшены");
    }
    
    /**
     * Получить список всех отслеживаемых endpoints
     * @return список названий endpoints
     */
    public String[] getTrackedEndpoints() {
        return metrics.keySet().toArray(new String[0]);
    }
    
    /**
     * Экспортировать метрики в CSV формате
     * @return CSV строка с метриками
     */
    public String exportToCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("Endpoint,TotalCalls,SuccessfulCalls,FailedCalls,RateLimitHits,SuccessRate,AvgResponseTime,HealthScore,RPM\n");
        
        for (String endpoint : metrics.keySet()) {
            ApiMetrics metric = metrics.get(endpoint);
            csv.append(String.format("%s,%d,%d,%d,%d,%.2f,%.0f,%.2f,%d\n",
                    endpoint,
                    metric.getTotalCalls(),
                    metric.getSuccessfulCalls(),
                    metric.getFailedCalls(),
                    metric.getRateLimitHits(),
                    metric.getSuccessRate(),
                    metric.getAverageResponseTime(),
                    metric.getHealthScore(),
                    metric.getCallsPerMinute()
            ));
        }
        
        return csv.toString();
    }
}
