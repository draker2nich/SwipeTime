package com.draker.swipetime.utils;

import com.draker.swipetime.models.ContentItem;

import java.util.List;

/**
 * Интерфейс обратного вызова для загрузки контента
 */
public interface ContentCallback {
    /**
     * Вызывается после загрузки элементов контента
     * @param items загруженные элементы
     */
    void onContentLoaded(List<ContentItem> items);
}
