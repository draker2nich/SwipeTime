package com.draker.swipetime.utils;

import android.util.Log;

import com.draker.swipetime.models.ContentItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс для перемешивания контента для более разнообразного отображения
 */
public class ContentShuffler {
    private static final String TAG = "ContentShuffler";
    
    // Хранилище последних порядков перемешивания для каждой категории
    private static final Map<String, List<String>> lastShuffleOrders = new HashMap<>();
    
    /**
     * Перемешать список элементов контента, избегая повторения последнего порядка
     * @param items исходный список элементов
     * @param categoryName название категории
     * @return перемешанный список элементов
     */
    public static List<ContentItem> shuffleContent(List<ContentItem> items, String categoryName) {
        if (items == null || items.isEmpty()) {
            return items;
        }
        
        // Создаем копию списка, чтобы не менять оригинал
        List<ContentItem> shuffledItems = new ArrayList<>(items);
        
        // Получаем IDs элементов до перемешивания для отладки
        List<String> originalIds = extractIds(shuffledItems);
        
        // Перемешиваем элементы
        Collections.shuffle(shuffledItems);
        
        // Проверяем, совпадает ли новый порядок с последним использованным для этой категории
        List<String> newIds = extractIds(shuffledItems);
        if (lastShuffleOrders.containsKey(categoryName) && 
                isSimilarOrder(lastShuffleOrders.get(categoryName), newIds)) {
            // Если порядок похож, перемешиваем ещё раз
            Log.d(TAG, "Обнаружен похожий порядок элементов, выполняем повторное перемешивание");
            Collections.shuffle(shuffledItems);
            newIds = extractIds(shuffledItems);
        }
        
        // Сохраняем новый порядок как последний использованный
        lastShuffleOrders.put(categoryName, newIds);
        
        Log.d(TAG, "Перемешивание завершено для категории " + categoryName + 
                ", элементов: " + shuffledItems.size());
        
        return shuffledItems;
    }
    
    /**
     * Извлечь список ID из списка элементов
     * @param items список элементов
     * @return список ID
     */
    private static List<String> extractIds(List<ContentItem> items) {
        List<String> ids = new ArrayList<>();
        for (ContentItem item : items) {
            ids.add(item.getId());
        }
        return ids;
    }
    
    /**
     * Проверить, похожи ли два порядка элементов
     * @param order1 первый порядок
     * @param order2 второй порядок
     * @return true, если порядки похожи (совпадают более чем на 70%)
     */
    private static boolean isSimilarOrder(List<String> order1, List<String> order2) {
        if (order1 == null || order2 == null || order1.size() != order2.size()) {
            return false;
        }
        
        int matchCount = 0;
        int threshold = Math.min(10, (int)(order1.size() * 0.7)); // 70% или первые 10 элементов
        
        for (int i = 0; i < threshold && i < order1.size(); i++) {
            if (order1.get(i).equals(order2.get(i))) {
                matchCount++;
            }
        }
        
        return matchCount >= threshold / 2; // Если совпадает больше половины элементов
    }
    
    /**
     * Сбросить сохраненные порядки перемешивания
     */
    public static void resetShuffleOrders() {
        lastShuffleOrders.clear();
    }
}