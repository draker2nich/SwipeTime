package com.draker.swipetime.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.UserPreferencesDao;
import com.draker.swipetime.database.entities.UserPreferencesEntity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Репозиторий для работы с пользовательскими настройками и предпочтениями
 */
public class UserPreferencesRepository {

    private UserPreferencesDao userPreferencesDao;

    public UserPreferencesRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userPreferencesDao = db.userPreferencesDao();
    }

    /**
     * Получить предпочтения пользователя
     * @param userId ID пользователя
     * @return предпочтения пользователя или null, если не найдены
     */
    public UserPreferencesEntity getByUserId(String userId) {
        UserPreferencesEntity preferences = userPreferencesDao.getByUserId(userId);
        if (preferences == null) {
            // Если у пользователя еще нет настроек, создаем их с значениями по умолчанию
            preferences = new UserPreferencesEntity();
            preferences.setUserId(userId);
            userPreferencesDao.insert(preferences);
        }
        return preferences;
    }

    /**
     * Наблюдать за предпочтениями пользователя
     * @param userId ID пользователя
     * @return LiveData с предпочтениями пользователя
     */
    public LiveData<UserPreferencesEntity> observeByUserId(String userId) {
        return userPreferencesDao.observeByUserId(userId);
    }

    /**
     * Обновить предпочтения пользователя
     * @param preferences новые предпочтения
     */
    public void update(UserPreferencesEntity preferences) {
        userPreferencesDao.update(preferences);
    }

    /**
     * Обновить предпочитаемые жанры
     * @param userId ID пользователя
     * @param genres список жанров
     */
    public void updateGenres(String userId, List<String> genres) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (String genre : genres) {
                jsonArray.put(genre);
            }
            userPreferencesDao.updateGenres(userId, jsonArray.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Получить список предпочитаемых жанров
     * @param userId ID пользователя
     * @return список жанров
     */
    public List<String> getGenres(String userId) {
        UserPreferencesEntity preferences = getByUserId(userId);
        List<String> genres = new ArrayList<>();
        
        if (preferences != null && preferences.getPreferredGenres() != null) {
            try {
                JSONArray jsonArray = new JSONArray(preferences.getPreferredGenres());
                for (int i = 0; i < jsonArray.length(); i++) {
                    genres.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        return genres;
    }

    /**
     * Обновить предпочитаемые страны
     * @param userId ID пользователя
     * @param countries список стран
     */
    public void updateCountries(String userId, List<String> countries) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (String country : countries) {
                jsonArray.put(country);
            }
            userPreferencesDao.updateCountries(userId, jsonArray.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Получить список предпочитаемых стран
     * @param userId ID пользователя
     * @return список стран
     */
    public List<String> getCountries(String userId) {
        UserPreferencesEntity preferences = getByUserId(userId);
        List<String> countries = new ArrayList<>();
        
        if (preferences != null && preferences.getPreferredCountries() != null) {
            try {
                JSONArray jsonArray = new JSONArray(preferences.getPreferredCountries());
                for (int i = 0; i < jsonArray.length(); i++) {
                    countries.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        return countries;
    }

    /**
     * Обновить предпочитаемые языки
     * @param userId ID пользователя
     * @param languages список языков
     */
    public void updateLanguages(String userId, List<String> languages) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (String language : languages) {
                jsonArray.put(language);
            }
            userPreferencesDao.updateLanguages(userId, jsonArray.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Получить список предпочитаемых языков
     * @param userId ID пользователя
     * @return список языков
     */
    public List<String> getLanguages(String userId) {
        UserPreferencesEntity preferences = getByUserId(userId);
        List<String> languages = new ArrayList<>();
        
        if (preferences != null && preferences.getPreferredLanguages() != null) {
            try {
                JSONArray jsonArray = new JSONArray(preferences.getPreferredLanguages());
                for (int i = 0; i < jsonArray.length(); i++) {
                    languages.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        return languages;
    }

    /**
     * Обновить теги интересов
     * @param userId ID пользователя
     * @param tags список тегов
     */
    public void updateInterestsTags(String userId, List<String> tags) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (String tag : tags) {
                jsonArray.put(tag);
            }
            userPreferencesDao.updateInterestsTags(userId, jsonArray.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Получить список тегов интересов
     * @param userId ID пользователя
     * @return список тегов
     */
    public List<String> getInterestsTags(String userId) {
        UserPreferencesEntity preferences = getByUserId(userId);
        List<String> tags = new ArrayList<>();
        
        if (preferences != null && preferences.getInterestsTags() != null) {
            try {
                JSONArray jsonArray = new JSONArray(preferences.getInterestsTags());
                for (int i = 0; i < jsonArray.length(); i++) {
                    tags.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        return tags;
    }

    /**
     * Обновить диапазон длительности
     * @param userId ID пользователя
     * @param minDuration минимальная длительность
     * @param maxDuration максимальная длительность
     */
    public void updateDurationRange(String userId, int minDuration, int maxDuration) {
        userPreferencesDao.updateDurationRange(userId, minDuration, maxDuration);
    }

    /**
     * Обновить диапазон годов
     * @param userId ID пользователя
     * @param minYear минимальный год
     * @param maxYear максимальный год
     */
    public void updateYearRange(String userId, int minYear, int maxYear) {
        userPreferencesDao.updateYearRange(userId, minYear, maxYear);
    }

    /**
     * Обновить настройку показа контента 18+
     * @param userId ID пользователя
     * @param enabled разрешить контент 18+
     */
    public void updateAdultContentEnabled(String userId, boolean enabled) {
        userPreferencesDao.updateAdultContentEnabled(userId, enabled);
    }
}