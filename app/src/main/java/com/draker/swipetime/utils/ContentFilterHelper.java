package com.draker.swipetime.utils;

import android.util.Log;

import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.database.entities.UserPreferencesEntity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Утилитарный класс для фильтрации контента в соответствии с предпочтениями пользователя
 */
public class ContentFilterHelper {

    private static final String TAG = "ContentFilterHelper";

    /**
     * Применяет фильтры пользователя к списку контента
     * @param contentList список контента
     * @param preferences предпочтения пользователя
     * @return отфильтрованный список
     */
    public static List<ContentEntity> filterContent(List<ContentEntity> contentList, UserPreferencesEntity preferences) {
        if (contentList == null || contentList.isEmpty() || preferences == null) {
            return contentList;
        }

        List<ContentEntity> filteredList = new ArrayList<>();

        try {
            List<String> preferredGenres = parseJsonArray(preferences.getPreferredGenres());
            List<String> preferredCountries = parseJsonArray(preferences.getPreferredCountries());
            List<String> preferredLanguages = parseJsonArray(preferences.getPreferredLanguages());
            List<String> interestsTags = parseJsonArray(preferences.getInterestsTags());
            
            // Если нет никаких фильтров, то возвращаем исходный список
            if (preferredGenres.isEmpty() && preferredCountries.isEmpty() && 
                preferredLanguages.isEmpty() && interestsTags.isEmpty() && 
                preferences.getMinYear() <= 1900 && preferences.getMaxYear() >= 2100 && 
                preferences.getMinDuration() <= 0 && preferences.getMaxDuration() >= Integer.MAX_VALUE) {
                return contentList;
            }

            for (ContentEntity content : contentList) {
                boolean include = true;
                
                // Проверяем по типу контента
                if (content instanceof MovieEntity) {
                    MovieEntity movie = (MovieEntity) content;
                    
                    // Фильтр по жанрам
                    if (!preferredGenres.isEmpty() && !containsAny(movie.getGenres(), preferredGenres)) {
                        include = false;
                    }
                    
                    // Фильтр по году выпуска
                    if (movie.getReleaseYear() < preferences.getMinYear() || 
                        movie.getReleaseYear() > preferences.getMaxYear()) {
                        include = false;
                    }
                    
                    // Фильтр по длительности
                    if (movie.getDuration() < preferences.getMinDuration() || 
                        movie.getDuration() > preferences.getMaxDuration()) {
                        include = false;
                    }
                } 
                else if (content instanceof TVShowEntity) {
                    TVShowEntity tvShow = (TVShowEntity) content;
                    
                    // Фильтр по жанрам
                    if (!preferredGenres.isEmpty() && !containsAny(tvShow.getGenres(), preferredGenres)) {
                        include = false;
                    }
                    
                    // Фильтр по году выпуска (используем год начала)
                    if (tvShow.getStartYear() < preferences.getMinYear() || 
                        tvShow.getStartYear() > preferences.getMaxYear()) {
                        include = false;
                    }
                } 
                else if (content instanceof GameEntity) {
                    GameEntity game = (GameEntity) content;
                    
                    // Фильтр по жанрам
                    if (!preferredGenres.isEmpty() && !containsAny(game.getGenres(), preferredGenres)) {
                        include = false;
                    }
                    
                    // Фильтр по году выпуска
                    if (game.getReleaseYear() < preferences.getMinYear() || 
                        game.getReleaseYear() > preferences.getMaxYear()) {
                        include = false;
                    }
                    
                    // Фильтр по возрастному рейтингу
                    if (!preferences.isAdultContentEnabled() && isAdultRated(game.getEsrbRating())) {
                        include = false;
                    }
                } 
                else if (content instanceof BookEntity) {
                    BookEntity book = (BookEntity) content;
                    
                    // Фильтр по жанрам
                    if (!preferredGenres.isEmpty() && !containsAny(book.getGenres(), preferredGenres)) {
                        include = false;
                    }
                    
                    // Фильтр по году выпуска
                    if (book.getPublishYear() < preferences.getMinYear() || 
                        book.getPublishYear() > preferences.getMaxYear()) {
                        include = false;
                    }
                } 
                else if (content instanceof AnimeEntity) {
                    AnimeEntity anime = (AnimeEntity) content;
                    
                    // Фильтр по жанрам
                    if (!preferredGenres.isEmpty() && !containsAny(anime.getGenres(), preferredGenres)) {
                        include = false;
                    }
                    
                    // Фильтр по году выпуска
                    if (anime.getReleaseYear() < preferences.getMinYear() || 
                        anime.getReleaseYear() > preferences.getMaxYear()) {
                        include = false;
                    }
                }
                
                // Если элемент прошёл все фильтры, добавляем его в отфильтрованный список
                if (include) {
                    filteredList.add(content);
                }
            }
            
            return filteredList;
            
        } catch (Exception e) {
            Log.e(TAG, "Error filtering content: " + e.getMessage());
            e.printStackTrace();
            return contentList; // В случае ошибки возвращаем исходный список
        }
    }

    /**
     * Проверяет, содержит ли строка с разделителями хотя бы один элемент из списка
     * @param commaString строка с разделителями (запятыми)
     * @param values список значений для проверки
     * @return true, если содержит хотя бы одно значение
     */
    private static boolean containsAny(String commaString, List<String> values) {
        if (commaString == null || commaString.isEmpty() || values == null || values.isEmpty()) {
            return false;
        }
        
        String[] items = commaString.split(",");
        for (String item : items) {
            String trimmed = item.trim();
            if (values.contains(trimmed)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Проверяет, является ли рейтинг взрослым (18+)
     * @param rating рейтинг (например, ESRB)
     * @return true, если контент для взрослых
     */
    private static boolean isAdultRated(String rating) {
        if (rating == null) {
            return false;
        }
        
        // Проверяем рейтинги различных систем
        return rating.equals("M") || rating.equals("AO") || // ESRB
               rating.equals("18+") || rating.equals("NC-17") || // MPAA/Возрастные
               rating.equals("R18+"); // Другие рейтинги
    }

    /**
     * Парсит JSON-строку в список строк
     * @param jsonString JSON-строка
     * @return список строк
     * @throws JSONException если ошибка парсинга
     */
    private static List<String> parseJsonArray(String jsonString) throws JSONException {
        List<String> result = new ArrayList<>();
        
        if (jsonString != null && !jsonString.isEmpty()) {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                result.add(jsonArray.getString(i));
            }
        }
        
        return result;
    }
}