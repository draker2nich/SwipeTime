package com.draker.swipetime.fragments;

import android.util.Log;

import com.draker.swipetime.models.ContentItem;
import com.draker.swipetime.repository.ContentRepository;
import com.draker.swipetime.utils.CardInfoHelper;

/**
 * Вспомогательный класс для CardStackFragment
 */
public class CardStackFragmentHelper {

    private static final String TAG = "CardStackFragmentHelper";

    /**
     * Обновляет информацию о текущей карточке
     * 
     * @param item текущий элемент
     * @param contentRepository репозиторий для доступа к сущностям
     */
    public static void updateCurrentCardInfo(ContentItem item, ContentRepository contentRepository) {
        if (item == null || contentRepository == null) {
            return;
        }
        
        // Логируем подробную информацию о карточке
        CardInfoHelper.logDetailedInfo(item, contentRepository);
    }
    
    /**
     * Вызывается при появлении новой карточки
     * 
     * @param position позиция карточки
     * @param items список элементов
     * @param contentRepository репозиторий для доступа к сущностям
     */
    public static void onCardAppeared(int position, java.util.List<ContentItem> items, ContentRepository contentRepository) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        
        ContentItem currentItem = items.get(position);
        Log.d(TAG, "Отображается карточка: " + currentItem.getTitle() + " (#" + position + ")");
        
        // Обновляем информацию о текущей карточке
        updateCurrentCardInfo(currentItem, contentRepository);
    }
}