package com.draker.swipetime.utils;

import android.util.Log;

import com.draker.swipetime.models.ContentItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Класс для перемешивания контента для более разнообразного отображения
 * Обеспечивает отсутствие повторений в списке карточек
 */
public class ContentShuffler {
    private static final String TAG = "ContentShuffler";
    
    // Постоянное хранилище уже показанных ID элементов для каждой категории
    private static final Map<String, Set<String>> shownContentIds = new HashMap<>();
    
    // Транзакционное хранилище (для текущей сессии) показанных ID
    private static final Map<String, Set<String>> sessionShownIds = new HashMap<>();
    
    // Хранилище последних использованных порядков элементов
    private static final Map<String, List<String>> lastShuffleOrders = new HashMap<>();
    
    /**
     * Перемешать список элементов контента, избегая повторения последнего порядка
     * и уже показанных элементов
     * @param items исходный список элементов
     * @param categoryName название категории
     * @return перемешанный список элементов
     */
    public static List<ContentItem> shuffleContent(List<ContentItem> items, String categoryName) {
        if (items == null || items.isEmpty()) {
            return items;
        }
        
        // Получаем или создаем набор уже показанных ID для данной категории
        Set<String> permanentShownIds = shownContentIds.computeIfAbsent(categoryName, k -> new HashSet<>());
        Set<String> currentShownIds = sessionShownIds.computeIfAbsent(categoryName, k -> new HashSet<>());
        
        // Объединяем постоянные и сессионные ID
        Set<String> allShownIds = new HashSet<>(permanentShownIds);
        allShownIds.addAll(currentShownIds);
        
        // Создаем копию входного списка, чтобы не изменять оригинал
        List<ContentItem> uniqueItems = new ArrayList<>();
        List<ContentItem> previouslyShownItems = new ArrayList<>();
        
        // Разделяем элементы на новые и уже показанные
        for (ContentItem item : items) {
            if (allShownIds.contains(item.getId())) {
                previouslyShownItems.add(item);
            } else {
                uniqueItems.add(item);
            }
        }
        
        Log.d(TAG, "Категория " + categoryName + ": элементов всего " + items.size() + 
                ", новых " + uniqueItems.size() + ", показанных " + previouslyShownItems.size());
        
        // Если все элементы уже были показаны, сбрасываем историю для этой категории
        if (uniqueItems.isEmpty()) {
            Log.d(TAG, "Все элементы категории " + categoryName + " уже были показаны. Сбрасываем историю.");
            sessionShownIds.get(categoryName).clear();
            uniqueItems = new ArrayList<>(items);
            previouslyShownItems.clear();
        }
        
        // Перемешиваем новые элементы
        Collections.shuffle(uniqueItems);
        
        // Перемешиваем дополнительно, если их мало
        if (uniqueItems.size() <= 10) {
            intensiveShuffle(uniqueItems);
        }
        
        // Проверяем, не слишком ли похож порядок на предыдущий
        List<String> newOrder = extractIds(uniqueItems);
        if (lastShuffleOrders.containsKey(categoryName) && 
                isSimilarOrder(lastShuffleOrders.get(categoryName), newOrder)) {
            // Если порядок похож, применяем более интенсивное перемешивание
            Log.d(TAG, "Обнаружен похожий порядок элементов, применяем интенсивное перемешивание");
            intensiveShuffle(uniqueItems);
            newOrder = extractIds(uniqueItems);
        }
        
        // Обновляем историю показанных ID и последний использованный порядок
        Set<String> sessionIds = sessionShownIds.get(categoryName);
        for (ContentItem item : uniqueItems) {
            sessionIds.add(item.getId());
        }
        lastShuffleOrders.put(categoryName, newOrder);
        
        List<ContentItem> result = new ArrayList<>(uniqueItems);
        
        Log.d(TAG, "Перемешивание завершено для категории " + categoryName + 
                ", элементов в результате: " + result.size());
        
        return result;
    }
    
    /**
     * Более интенсивное перемешивание списка элементов
     * Использует алгоритм Fisher-Yates в нескольких проходах с различными частями списка
     * @param items список элементов для перемешивания
     */
    private static void intensiveShuffle(List<ContentItem> items) {
        if (items.size() <= 1) return;
        
        Random random = new Random();
        int size = items.size();
        
        // Первый проход - полное перемешивание
        for (int i = size - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Collections.swap(items, i, j);
        }
        
        // Второй проход - перемешивание первой половины
        int halfSize = size / 2;
        for (int i = halfSize - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Collections.swap(items, i, j);
        }
        
        // Третий проход - перемешивание второй половины
        for (int i = size - 1; i >= halfSize; i--) {
            int j = halfSize + random.nextInt(size - halfSize);
            Collections.swap(items, i, j);
        }
        
        // Четвертый проход - случайное перемешивание блоков
        int blockSize = Math.max(3, size / 5);
        for (int block = 0; block < size / blockSize; block++) {
            int startIndex = block * blockSize;
            int endIndex = Math.min(startIndex + blockSize, size);
            for (int i = endIndex - 1; i > startIndex; i--) {
                int j = startIndex + random.nextInt(i - startIndex + 1);
                Collections.swap(items, i, j);
            }
        }
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
     * @return true, если порядки похожи (совпадают более чем на 60%)
     */
    private static boolean isSimilarOrder(List<String> order1, List<String> order2) {
        if (order1 == null || order2 == null || order1.isEmpty() || order2.isEmpty()) {
            return false;
        }
        
        int matchCount = 0;
        int checkSize = Math.min(order1.size(), order2.size());
        checkSize = Math.min(checkSize, 20); // Проверяем не более 20 элементов
        
        // Совпадения в начале списка более важны, чем в конце
        for (int i = 0; i < checkSize; i++) {
            if (i < order1.size() && i < order2.size() && order1.get(i).equals(order2.get(i))) {
                // Даем больший вес совпадениям в начале списка
                matchCount += (checkSize - i) / (i + 1);
            }
        }
        
        int threshold = checkSize * 2 / 5; // 40% порог для совпадения (с учетом весов)
        return matchCount >= threshold;
    }
    
    /**
     * Навсегда отмечает элемент как просмотренный (не будет показываться в будущем)
     * @param categoryName название категории
     * @param contentId ID элемента
     */
    public static void markContentAsPermanentlyShown(String categoryName, String contentId) {
        Set<String> shownIds = shownContentIds.computeIfAbsent(categoryName, k -> new HashSet<>());
        shownIds.add(contentId);
    }
    
    /**
     * Отмечает элемент как просмотренный в текущей сессии
     * @param categoryName название категории
     * @param contentId ID элемента
     */
    public static void markContentAsShown(String categoryName, String contentId) {
        Set<String> sessionIds = sessionShownIds.computeIfAbsent(categoryName, k -> new HashSet<>());
        sessionIds.add(contentId);
    }
    
    /**
     * Сбросить сессионную историю для указанной категории
     * @param categoryName название категории
     */
    public static void resetSessionHistory(String categoryName) {
        if (sessionShownIds.containsKey(categoryName)) {
            sessionShownIds.get(categoryName).clear();
        }
        if (lastShuffleOrders.containsKey(categoryName)) {
            lastShuffleOrders.remove(categoryName);
        }
        Log.d(TAG, "Сессионная история перемешивания сброшена для категории: " + categoryName);
    }
    
    /**
     * Сбросить всю историю показанных элементов для категории (и постоянную, и сессионную)
     * @param categoryName название категории
     */
    public static void resetHistory(String categoryName) {
        resetSessionHistory(categoryName);
        
        if (shownContentIds.containsKey(categoryName)) {
            shownContentIds.get(categoryName).clear();
        }
        
        Log.d(TAG, "Вся история перемешивания сброшена для категории: " + categoryName);
    }
    
    /**
     * Сбросить всю сессионную историю
     */
    public static void resetAllSessionHistory() {
        sessionShownIds.clear();
        lastShuffleOrders.clear();
        Log.d(TAG, "Вся сессионная история перемешивания сброшена");
    }
    
    /**
     * Сбросить всю историю показанных элементов
     */
    public static void resetAllHistory() {
        resetAllSessionHistory();
        shownContentIds.clear();
        Log.d(TAG, "Вся история перемешивания сброшена");
    }
    
    /**
     * Получить количество уже показанных элементов для категории
     * @param categoryName название категории
     * @return количество элементов
     */
    public static int getShownCount(String categoryName) {
        int count = 0;
        
        if (shownContentIds.containsKey(categoryName)) {
            count += shownContentIds.get(categoryName).size();
        }
        
        if (sessionShownIds.containsKey(categoryName)) {
            count += sessionShownIds.get(categoryName).size();
        }
        
        return count;
    }
}