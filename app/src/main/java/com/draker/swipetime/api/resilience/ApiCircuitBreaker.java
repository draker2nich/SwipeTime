package com.draker.swipetime.api.resilience;

import android.util.Log;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit Breaker для защиты API от каскадных сбоев
 * Реализует паттерн Circuit Breaker с тремя состояниями: CLOSED, OPEN, HALF_OPEN
 */
public class ApiCircuitBreaker {
    private static final String TAG = "ApiCircuitBreaker";
    
    public enum State {
        CLOSED,    // Нормальная работа
        OPEN,      // Блокировка запросов
        HALF_OPEN  // Проверочное состояние
    }
    
    private final String name;
    private final int failureThreshold;
    private final long recoveryTimeout;
    private final int successThreshold;
    
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final AtomicLong stateChangeTime = new AtomicLong(System.currentTimeMillis());
    
    /**
     * Конструктор для Jikan API с консервативными настройками
     * @param name имя circuit breaker для логирования
     */
    public ApiCircuitBreaker(String name) {
        this(name, 3, 30000L, 2); // 3 ошибки, 30 сек восстановления, 2 успеха для закрытия
    }
    
    /**
     * Конструктор с настраиваемыми параметрами
     * @param name имя circuit breaker
     * @param failureThreshold количество ошибок для открытия
     * @param recoveryTimeout время восстановления в миллисекундах
     * @param successThreshold количество успехов для закрытия
     */
    public ApiCircuitBreaker(String name, int failureThreshold, long recoveryTimeout, int successThreshold) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.recoveryTimeout = recoveryTimeout;
        this.successThreshold = successThreshold;
        
        Log.d(TAG, String.format("Инициализирован CircuitBreaker[%s]: failureThreshold=%d, recoveryTimeout=%dms, successThreshold=%d",
                name, failureThreshold, recoveryTimeout, successThreshold));
    }
    
    /**
     * Выполнить операцию с защитой Circuit Breaker
     * @param operation операция для выполнения
     * @param <T> тип возвращаемого значения
     * @return результат операции
     * @throws CircuitBreakerOpenException если circuit breaker открыт
     * @throws Exception если операция завершилась ошибкой
     */
    public <T> T execute(CircuitBreakerOperation<T> operation) throws Exception {
        State currentState = state.get();
        
        switch (currentState) {
            case OPEN:
                if (shouldAttemptReset()) {
                    transitionToHalfOpen();
                } else {
                    throw new CircuitBreakerOpenException(
                            String.format("CircuitBreaker[%s] is OPEN. Recovery timeout: %dms", 
                                    name, getRemainingRecoveryTime()));
                }
                break;
                
            case HALF_OPEN:
                // Пропускаем запрос для проверки состояния API
                break;
                
            case CLOSED:
                // Нормальная работа
                break;
        }
        
        try {
            T result = operation.execute();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure(e);
            throw e;
        }
    }
    
    /**
     * Проверить, можно ли выполнить запрос
     * @return true если запрос можно выполнить
     */
    public boolean canExecute() {
        State currentState = state.get();
        
        if (currentState == State.OPEN) {
            return shouldAttemptReset();
        }
        
        return true; // CLOSED или HALF_OPEN позволяют выполнение
    }
    
    /**
     * Получить текущее состояние
     * @return текущее состояние circuit breaker
     */
    public State getState() {
        return state.get();
    }
    
    /**
     * Получить количество текущих ошибок
     * @return количество ошибок
     */
    public int getFailureCount() {
        return failureCount.get();
    }
    
    /**
     * Получить количество текущих успехов (актуально для HALF_OPEN)
     * @return количество успехов
     */
    public int getSuccessCount() {
        return successCount.get();
    }
    
    /**
     * Принудительно открыть circuit breaker
     */
    public void forceOpen() {
        transitionToOpen();
        Log.w(TAG, "CircuitBreaker[" + name + "] принудительно открыт");
    }
    
    /**
     * Принудительно закрыть circuit breaker
     */
    public void forceClose() {
        transitionToClosed();
        Log.i(TAG, "CircuitBreaker[" + name + "] принудительно закрыт");
    }
    
    /**
     * Сбросить все счетчики
     */
    public void reset() {
        failureCount.set(0);
        successCount.set(0);
        lastFailureTime.set(0);
        transitionToClosed();
        Log.i(TAG, "CircuitBreaker[" + name + "] сброшен");
    }
    
    /**
     * Обработка успешного выполнения операции
     */
    private void onSuccess() {
        State currentState = state.get();
        
        switch (currentState) {
            case HALF_OPEN:
                int currentSuccessCount = successCount.incrementAndGet();
                Log.d(TAG, String.format("CircuitBreaker[%s] HALF_OPEN: успех %d/%d", 
                        name, currentSuccessCount, successThreshold));
                        
                if (currentSuccessCount >= successThreshold) {
                    transitionToClosed();
                }
                break;
                
            case CLOSED:
                // Сбрасываем счетчик ошибок при успехе
                if (failureCount.get() > 0) {
                    failureCount.set(0);
                    Log.d(TAG, "CircuitBreaker[" + name + "] сброшен счетчик ошибок после успеха");
                }
                break;
                
            case OPEN:
                // Не должно происходить, но на всякий случай
                Log.w(TAG, "CircuitBreaker[" + name + "] получил успех в состоянии OPEN");
                break;
        }
    }
    
    /**
     * Обработка неудачного выполнения операции
     * @param exception исключение, которое произошло
     */
    private void onFailure(Exception exception) {
        int currentFailureCount = failureCount.incrementAndGet();
        lastFailureTime.set(System.currentTimeMillis());
        
        Log.w(TAG, String.format("CircuitBreaker[%s] ошибка %d/%d: %s", 
                name, currentFailureCount, failureThreshold, exception.getMessage()));
        
        State currentState = state.get();
        
        if (currentState == State.HALF_OPEN) {
            // В HALF_OPEN любая ошибка возвращает в OPEN
            transitionToOpen();
        } else if (currentState == State.CLOSED && currentFailureCount >= failureThreshold) {
            transitionToOpen();
        }
    }
    
    /**
     * Проверить, нужно ли попытаться сбросить circuit breaker
     * @return true если можно попытаться сбросить
     */
    private boolean shouldAttemptReset() {
        return System.currentTimeMillis() - lastFailureTime.get() >= recoveryTimeout;
    }
    
    /**
     * Получить оставшееся время до восстановления
     * @return время в миллисекундах
     */
    private long getRemainingRecoveryTime() {
        return Math.max(0, recoveryTimeout - (System.currentTimeMillis() - lastFailureTime.get()));
    }
    
    /**
     * Переход в состояние CLOSED
     */
    private void transitionToClosed() {
        state.set(State.CLOSED);
        failureCount.set(0);
        successCount.set(0);
        stateChangeTime.set(System.currentTimeMillis());
        Log.i(TAG, "CircuitBreaker[" + name + "] -> CLOSED");
    }
    
    /**
     * Переход в состояние OPEN
     */
    private void transitionToOpen() {
        state.set(State.OPEN);
        successCount.set(0);
        stateChangeTime.set(System.currentTimeMillis());
        Log.w(TAG, String.format("CircuitBreaker[%s] -> OPEN (ошибок: %d, восстановление через %dms)", 
                name, failureCount.get(), recoveryTimeout));
    }
    
    /**
     * Переход в состояние HALF_OPEN
     */
    private void transitionToHalfOpen() {
        state.set(State.HALF_OPEN);
        successCount.set(0);
        stateChangeTime.set(System.currentTimeMillis());
        Log.i(TAG, "CircuitBreaker[" + name + "] -> HALF_OPEN");
    }
    
    /**
     * Получить подробную статистику
     * @return строка с информацией о состоянии
     */
    public String getDetailedStatus() {
        State currentState = state.get();
        long uptime = System.currentTimeMillis() - stateChangeTime.get();
        
        return String.format("CircuitBreaker[%s]: state=%s, failures=%d/%d, successes=%d/%d, uptime=%dms, recovery=%dms",
                name, currentState, failureCount.get(), failureThreshold, 
                successCount.get(), successThreshold, uptime, getRemainingRecoveryTime());
    }
    
    /**
     * Интерфейс для операций, выполняемых с защитой Circuit Breaker
     * @param <T> тип возвращаемого значения
     */
    public interface CircuitBreakerOperation<T> {
        T execute() throws Exception;
    }
    
    /**
     * Исключение, бросаемое когда Circuit Breaker открыт
     */
    public static class CircuitBreakerOpenException extends Exception {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}
