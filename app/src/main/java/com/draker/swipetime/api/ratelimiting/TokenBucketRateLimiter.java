package com.draker.swipetime.api.ratelimiting;

import android.util.Log;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Token Bucket алгоритм для управления скоростью запросов к API
 * Специально настроен для Jikan API с лимитом 3 запроса в секунду
 */
public class TokenBucketRateLimiter {
    private static final String TAG = "TokenBucketRateLimiter";
    
    private final int capacity;
    private final long refillRate; // токенов в секунду
    private final long refillPeriod; // период в миллисекундах
    
    private int tokens;
    private long lastRefillTime;
    private final ReentrantLock lock = new ReentrantLock();
    
    /**
     * Конструктор для Jikan API
     */
    public TokenBucketRateLimiter() {
        this(3, 1, 1000L); // 3 токена, 1 токен в секунду
    }
    
    /**
     * Конструктор с настраиваемыми параметрами
     * @param capacity максимальное количество токенов
     * @param refillRate количество токенов, добавляемых за период
     * @param refillPeriod период пополнения в миллисекундах
     */
    public TokenBucketRateLimiter(int capacity, long refillRate, long refillPeriod) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.refillPeriod = refillPeriod;
        this.tokens = capacity;
        this.lastRefillTime = System.currentTimeMillis();
        
        Log.d(TAG, "Инициализирован TokenBucketRateLimiter: capacity=" + capacity + 
                ", refillRate=" + refillRate + ", refillPeriod=" + refillPeriod);
    }
    
    /**
     * Попытка получить токен для выполнения запроса
     * @return true если токен получен, false если нужно подождать
     */
    public boolean tryAcquire() {
        lock.lock();
        try {
            refill();
            if (tokens > 0) {
                tokens--;
                Log.d(TAG, "Токен получен. Осталось токенов: " + tokens);
                return true;
            } else {
                Log.d(TAG, "Токены закончились. Нужно подождать пополнения.");
                return false;
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Получить токен с ожиданием (блокирующий вызов)
     * @param timeoutMs максимальное время ожидания в миллисекундах
     * @return true если токен получен, false если превышен таймаут
     */
    public boolean acquire(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (tryAcquire()) {
                return true;
            }
            
            try {
                Thread.sleep(100); // Проверяем каждые 100мс
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        Log.w(TAG, "Превышен таймаут ожидания токена: " + timeoutMs + "мс");
        return false;
    }
    
    /**
     * Получить время до следующего пополнения токенов
     * @return время в миллисекундах
     */
    public long getTimeToNextRefill() {
        lock.lock();
        try {
            if (tokens >= capacity) {
                return 0; // Корзина полная
            }
            
            long currentTime = System.currentTimeMillis();
            long timeSinceLastRefill = currentTime - lastRefillTime;
            return Math.max(0, refillPeriod - timeSinceLastRefill);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Получить количество доступных токенов
     * @return количество токенов
     */
    public int getAvailableTokens() {
        lock.lock();
        try {
            refill();
            return tokens;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Сбросить состояние лимитера (заполнить корзину токенов)
     */
    public void reset() {
        lock.lock();
        try {
            tokens = capacity;
            lastRefillTime = System.currentTimeMillis();
            Log.d(TAG, "TokenBucketRateLimiter сброшен");
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Пополнить токены на основе прошедшего времени
     */
    private void refill() {
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastRefillTime;
        
        if (timePassed >= refillPeriod) {
            long tokensToAdd = (timePassed * refillRate) / refillPeriod;
            
            if (tokensToAdd > 0) {
                int newTokens = (int) Math.min(capacity, tokens + tokensToAdd);
                
                if (newTokens != tokens) {
                    Log.d(TAG, "Пополнение токенов: было=" + tokens + ", стало=" + newTokens);
                }
                
                tokens = newTokens;
                lastRefillTime = currentTime;
            }
        }
    }
    
    /**
     * Получить информацию о состоянии лимитера
     * @return строка с информацией
     */
    public String getStatus() {
        lock.lock();
        try {
            refill();
            return String.format("TokenBucket[tokens=%d/%d, nextRefill=%dms]", 
                    tokens, capacity, getTimeToNextRefill());
        } finally {
            lock.unlock();
        }
    }
}
