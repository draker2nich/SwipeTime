package com.draker.swipetime.domain.common;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Базовый класс для всех Use Cases
 * Обеспечивает асинхронное выполнение операций
 * 
 * @param <T> тип возвращаемых данных
 * @param <P> тип параметров
 */
public abstract class UseCase<T, P> {
    private final ExecutorService executor;
    private final Handler mainHandler;

    protected UseCase() {
        this.executor = Executors.newFixedThreadPool(2);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Выполняет основную логику Use Case
     * Выполняется в фоновом потоке
     * 
     * @param params параметры для выполнения
     * @return результат выполнения
     * @throws Exception в случае ошибки
     */
    protected abstract T executeInternal(P params) throws Exception;

    /**
     * Выполняет Use Case асинхронно
     * 
     * @param params параметры для выполнения
     * @param callback колбэк для получения результата
     */
    public void execute(P params, Callback<T> callback) {
        executor.execute(() -> {
            try {
                T result = executeInternal(params);
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Интерфейс для обработки результатов выполнения Use Case
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }

    /**
     * Освобождает ресурсы
     */
    public void dispose() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
