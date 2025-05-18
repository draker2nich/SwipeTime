package com.draker.swipetime.utils;

import com.draker.swipetime.models.ContentItem;

import java.util.List;

/**
 * Интерфейс обратного вызова для загрузки контента
 */
public interface ContentLoadCallback {
    /**
     * Вызывается при завершении загрузки контента
     * @param items загруженные элементы
     * @param status статус загрузки
     */
    void onContentLoaded(List<ContentItem> items, String status);
}