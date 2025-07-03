package com.draker.swipetime.domain.common;

/**
 * Wrapper для результатов операций
 * Используется для унификации возврата данных/ошибок/состояний загрузки
 */
public class Result<T> {
    private final T data;
    private final String error;
    private final boolean isLoading;
    private final boolean isSuccess;

    private Result(T data, String error, boolean isLoading, boolean isSuccess) {
        this.data = data;
        this.error = error;
        this.isLoading = isLoading;
        this.isSuccess = isSuccess;
    }

    /**
     * Создает успешный результат с данными
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(data, null, false, true);
    }

    /**
     * Создает результат с ошибкой
     */
    public static <T> Result<T> error(String error) {
        return new Result<>(null, error, false, false);
    }

    /**
     * Создает результат в состоянии загрузки
     */
    public static <T> Result<T> loading() {
        return new Result<>(null, null, true, false);
    }

    /**
     * Создает результат с данными и состоянием загрузки
     */
    public static <T> Result<T> loading(T data) {
        return new Result<>(data, null, true, false);
    }

    // Getters
    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public boolean isError() {
        return !isSuccess && !isLoading;
    }

    public boolean hasData() {
        return data != null;
    }

    @Override
    public String toString() {
        return "Result{" +
                "data=" + data +
                ", error='" + error + '\'' +
                ", isLoading=" + isLoading +
                ", isSuccess=" + isSuccess +
                '}';
    }
}
