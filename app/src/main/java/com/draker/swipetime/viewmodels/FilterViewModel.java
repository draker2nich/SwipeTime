package com.draker.swipetime.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.draker.swipetime.database.entities.UserPreferencesEntity;
import com.draker.swipetime.repository.UserPreferencesRepository;
import com.draker.swipetime.utils.GamificationIntegrator;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel для управления настройками пользователя и фильтрации контента
 */
public class FilterViewModel extends AndroidViewModel {

    private static final String TAG = "FilterViewModel";
    private UserPreferencesRepository preferencesRepository;
    private MutableLiveData<List<String>> selectedGenres = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<String>> selectedCountries = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<String>> selectedLanguages = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<String>> selectedTags = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Integer> minDuration = new MutableLiveData<>(0);
    private MutableLiveData<Integer> maxDuration = new MutableLiveData<>(300);
    private MutableLiveData<Integer> minYear = new MutableLiveData<>(1900);
    private MutableLiveData<Integer> maxYear = new MutableLiveData<>(2025);
    private MutableLiveData<Boolean> adultContentEnabled = new MutableLiveData<>(false);
    
    // Константы для представления в UI
    public static final String[] AVAILABLE_GENRES = {
        "Боевик", "Комедия", "Драма", "Фантастика", "Ужасы", "Триллер", 
        "Детектив", "Мелодрама", "Приключения", "Документальный", "Мультфильм", 
        "Вестерн", "Мюзикл", "Спорт", "Биография", "История", "Военный", "Фэнтези"
    };
    
    public static final String[] AVAILABLE_COUNTRIES = {
        "США", "Россия", "Великобритания", "Франция", "Германия", "Италия", 
        "Испания", "Япония", "Китай", "Южная Корея", "Индия", "Канада", 
        "Австралия", "Бразилия", "Швеция", "Норвегия", "Финляндия", "Дания"
    };
    
    public static final String[] AVAILABLE_LANGUAGES = {
        "Русский", "Английский", "Французский", "Немецкий", "Испанский", 
        "Итальянский", "Японский", "Китайский", "Корейский", "Хинди"
    };

    public static final String[] AVAILABLE_TAGS = {
        "Психология", "Фэнтези", "Научная фантастика", "Классика", 
        "Бестселлер", "Криминал", "Супергерои", "Экшн", "Стратегия", 
        "Ролевая игра", "Шутер", "Романтика", "Постапокалипсис", 
        "Космос", "Средневековье", "Артхаус", "Независимое кино", 
        "Расследование", "Зомби", "Вампиры", "Магия", "Семейное", 
        "Анимация", "Документальное", "Биография", "История", "Музыка"
    };

    // ID текущего пользователя
    private String currentUserId;

    public FilterViewModel(@NonNull Application application) {
        super(application);
        preferencesRepository = new UserPreferencesRepository(application);
        
        // Получаем ID текущего пользователя
        currentUserId = GamificationIntegrator.getCurrentUserId(application);
        Log.d(TAG, "Используется ID пользователя для фильтров: " + currentUserId);
        
        // Загрузка сохраненных настроек пользователя
        loadUserPreferences();
    }

    /**
     * Загрузка настроек пользователя из репозитория
     */
    private void loadUserPreferences() {
        UserPreferencesEntity preferences = preferencesRepository.getByUserId(currentUserId);
        
        // Загружаем жанры
        List<String> genres = preferencesRepository.getGenres(currentUserId);
        if (!genres.isEmpty()) {
            selectedGenres.setValue(genres);
        }
        
        // Загружаем страны
        List<String> countries = preferencesRepository.getCountries(currentUserId);
        if (!countries.isEmpty()) {
            selectedCountries.setValue(countries);
        }
        
        // Загружаем языки
        List<String> languages = preferencesRepository.getLanguages(currentUserId);
        if (!languages.isEmpty()) {
            selectedLanguages.setValue(languages);
        }
        
        // Загружаем теги интересов
        List<String> tags = preferencesRepository.getInterestsTags(currentUserId);
        if (!tags.isEmpty()) {
            selectedTags.setValue(tags);
        }
        
        // Загружаем диапазон длительности
        if (preferences != null) {
            minDuration.setValue(preferences.getMinDuration());
            maxDuration.setValue(preferences.getMaxDuration());
            
            // Загружаем диапазон годов
            minYear.setValue(preferences.getMinYear());
            maxYear.setValue(preferences.getMaxYear());
            
            // Загружаем настройку контента 18+
            adultContentEnabled.setValue(preferences.isAdultContentEnabled());
        }
    }

    /**
     * Обновление ID текущего пользователя и перезагрузка настроек
     * @param userId ID пользователя
     */
    public void updateCurrentUserId(String userId) {
        if (userId != null && !userId.isEmpty()) {
            this.currentUserId = userId;
            Log.d(TAG, "ID пользователя для фильтров обновлен: " + userId);
            loadUserPreferences();
        }
    }

    /**
     * Получить текущий ID пользователя
     * @return ID пользователя
     */
    public String getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Сохранение настроек пользователя в репозиторий
     */
    public void saveUserPreferences() {
        // Проверяем, что ID пользователя актуальный
        String userId = GamificationIntegrator.getCurrentUserId(getApplication());
        if (!userId.equals(currentUserId)) {
            currentUserId = userId;
            Log.d(TAG, "ID пользователя обновлен перед сохранением настроек: " + userId);
        }
        
        // Сохраняем жанры
        preferencesRepository.updateGenres(currentUserId, selectedGenres.getValue());
        
        // Сохраняем страны
        preferencesRepository.updateCountries(currentUserId, selectedCountries.getValue());
        
        // Сохраняем языки
        preferencesRepository.updateLanguages(currentUserId, selectedLanguages.getValue());
        
        // Сохраняем теги интересов
        preferencesRepository.updateInterestsTags(currentUserId, selectedTags.getValue());
        
        // Сохраняем диапазон длительности
        preferencesRepository.updateDurationRange(
            currentUserId, 
            minDuration.getValue() != null ? minDuration.getValue() : 0,
            maxDuration.getValue() != null ? maxDuration.getValue() : 300
        );
        
        // Сохраняем диапазон годов
        preferencesRepository.updateYearRange(
            currentUserId,
            minYear.getValue() != null ? minYear.getValue() : 1900,
            maxYear.getValue() != null ? maxYear.getValue() : 2025
        );
        
        // Сохраняем настройку контента 18+
        preferencesRepository.updateAdultContentEnabled(
            currentUserId,
            adultContentEnabled.getValue() != null && adultContentEnabled.getValue()
        );
        
        Log.d(TAG, "Настройки успешно сохранены для пользователя: " + currentUserId);
    }

    /**
     * Сбросить все настройки к значениям по умолчанию
     */
    public void resetPreferences() {
        selectedGenres.setValue(new ArrayList<>());
        selectedCountries.setValue(new ArrayList<>());
        selectedLanguages.setValue(new ArrayList<>());
        selectedTags.setValue(new ArrayList<>());
        minDuration.setValue(0);
        maxDuration.setValue(300);
        minYear.setValue(1900);
        maxYear.setValue(2025);
        adultContentEnabled.setValue(false);
        
        // Сохраняем сброшенные настройки
        saveUserPreferences();
    }

    // Геттеры и сеттеры для LiveData
    
    public LiveData<List<String>> getSelectedGenres() {
        return selectedGenres;
    }
    
    public void addGenre(String genre) {
        List<String> current = selectedGenres.getValue();
        if (current != null && !current.contains(genre)) {
            current.add(genre);
            selectedGenres.setValue(current);
        }
    }
    
    public void removeGenre(String genre) {
        List<String> current = selectedGenres.getValue();
        if (current != null) {
            current.remove(genre);
            selectedGenres.setValue(current);
        }
    }
    
    public LiveData<List<String>> getSelectedCountries() {
        return selectedCountries;
    }
    
    public void addCountry(String country) {
        List<String> current = selectedCountries.getValue();
        if (current != null && !current.contains(country)) {
            current.add(country);
            selectedCountries.setValue(current);
        }
    }
    
    public void removeCountry(String country) {
        List<String> current = selectedCountries.getValue();
        if (current != null) {
            current.remove(country);
            selectedCountries.setValue(current);
        }
    }
    
    public LiveData<List<String>> getSelectedLanguages() {
        return selectedLanguages;
    }
    
    public void addLanguage(String language) {
        List<String> current = selectedLanguages.getValue();
        if (current != null && !current.contains(language)) {
            current.add(language);
            selectedLanguages.setValue(current);
        }
    }
    
    public void removeLanguage(String language) {
        List<String> current = selectedLanguages.getValue();
        if (current != null) {
            current.remove(language);
            selectedLanguages.setValue(current);
        }
    }
    
    public LiveData<List<String>> getSelectedTags() {
        return selectedTags;
    }
    
    public void addTag(String tag) {
        List<String> current = selectedTags.getValue();
        if (current != null && !current.contains(tag)) {
            current.add(tag);
            selectedTags.setValue(current);
        }
    }
    
    public void removeTag(String tag) {
        List<String> current = selectedTags.getValue();
        if (current != null) {
            current.remove(tag);
            selectedTags.setValue(current);
        }
    }
    
    public LiveData<Integer> getMinDuration() {
        return minDuration;
    }
    
    public void setMinDuration(int min) {
        this.minDuration.setValue(min);
    }
    
    public LiveData<Integer> getMaxDuration() {
        return maxDuration;
    }
    
    public void setMaxDuration(int max) {
        this.maxDuration.setValue(max);
    }
    
    public LiveData<Integer> getMinYear() {
        return minYear;
    }
    
    public void setMinYear(int min) {
        this.minYear.setValue(min);
    }
    
    public LiveData<Integer> getMaxYear() {
        return maxYear;
    }
    
    public void setMaxYear(int max) {
        this.maxYear.setValue(max);
    }
    
    public LiveData<Boolean> getAdultContentEnabled() {
        return adultContentEnabled;
    }
    
    public void setAdultContentEnabled(boolean enabled) {
        this.adultContentEnabled.setValue(enabled);
    }
}