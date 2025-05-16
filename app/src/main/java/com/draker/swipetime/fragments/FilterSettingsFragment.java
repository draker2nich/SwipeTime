package com.draker.swipetime.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.draker.swipetime.R;
import com.draker.swipetime.viewmodels.FilterViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

/**
 * Фрагмент для настройки фильтров
 */
public class FilterSettingsFragment extends Fragment {

    private static final String TAG = "FilterSettingsFragment";

    private FilterViewModel filterViewModel;
    
    // UI компоненты
    private ChipGroup genresChipGroup;
    private ChipGroup countriesChipGroup;
    private ChipGroup languagesChipGroup;
    private ChipGroup tagsChipGroup;
    private EditText minYearEdit;
    private EditText maxYearEdit;
    private EditText minDurationEdit;
    private EditText maxDurationEdit;
    private SwitchMaterial adultContentSwitch;
    private Button btnReset;
    private Button btnApply;
    private ImageButton btnClose;
    
    // Интерфейс для обратного вызова
    public interface OnFilterSettingsClosedListener {
        void onFilterSettingsClosed(boolean filtersApplied);
    }
    
    private OnFilterSettingsClosedListener listener;
    
    // Флаг для отслеживания применения фильтров
    private boolean filtersApplied = false;

    public static FilterSettingsFragment newInstance() {
        return new FilterSettingsFragment();
    }
    
    public void setOnFilterSettingsClosedListener(OnFilterSettingsClosedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Инициализация ViewModel
        filterViewModel = new ViewModelProvider(requireActivity()).get(FilterViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Инициализация UI компонентов
        genresChipGroup = view.findViewById(R.id.genres_chip_group);
        countriesChipGroup = view.findViewById(R.id.countries_chip_group);
        languagesChipGroup = view.findViewById(R.id.languages_chip_group);
        tagsChipGroup = view.findViewById(R.id.tags_chip_group);
        minYearEdit = view.findViewById(R.id.min_year_edit);
        maxYearEdit = view.findViewById(R.id.max_year_edit);
        minDurationEdit = view.findViewById(R.id.min_duration_edit);
        maxDurationEdit = view.findViewById(R.id.max_duration_edit);
        adultContentSwitch = view.findViewById(R.id.adult_content_switch);
        btnReset = view.findViewById(R.id.btn_reset);
        btnApply = view.findViewById(R.id.btn_apply);
        btnClose = view.findViewById(R.id.btn_close);
        
        // Настройка кнопок
        btnReset.setOnClickListener(v -> resetFilters());
        btnApply.setOnClickListener(v -> applyFilters());
        btnClose.setOnClickListener(v -> closeSettings());
        
        // Заполнение чипов для жанров
        setupGenreChips();
        
        // Заполнение чипов для стран
        setupCountryChips();
        
        // Заполнение чипов для языков
        setupLanguageChips();
        
        // Заполнение чипов для тегов интересов
        setupTagChips();
        
        // Загружаем текущие настройки из ViewModel
        loadCurrentSettings();
        
        // Наблюдаем за изменениями в настройках
        observeSettings();
    }

    /**
     * Настраивает чипы для жанров
     */
    private void setupGenreChips() {
        genresChipGroup.removeAllViews();
        
        for (String genre : FilterViewModel.AVAILABLE_GENRES) {
            Chip chip = createFilterChip(genre);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filterViewModel.addGenre(genre);
                } else {
                    filterViewModel.removeGenre(genre);
                }
            });
            genresChipGroup.addView(chip);
        }
    }

    /**
     * Настраивает чипы для стран
     */
    private void setupCountryChips() {
        countriesChipGroup.removeAllViews();
        
        for (String country : FilterViewModel.AVAILABLE_COUNTRIES) {
            Chip chip = createFilterChip(country);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filterViewModel.addCountry(country);
                } else {
                    filterViewModel.removeCountry(country);
                }
            });
            countriesChipGroup.addView(chip);
        }
    }

    /**
     * Настраивает чипы для языков
     */
    private void setupLanguageChips() {
        languagesChipGroup.removeAllViews();
        
        for (String language : FilterViewModel.AVAILABLE_LANGUAGES) {
            Chip chip = createFilterChip(language);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filterViewModel.addLanguage(language);
                } else {
                    filterViewModel.removeLanguage(language);
                }
            });
            languagesChipGroup.addView(chip);
        }
    }

    /**
     * Настраивает чипы для тегов интересов
     */
    private void setupTagChips() {
        tagsChipGroup.removeAllViews();
        
        for (String tag : FilterViewModel.AVAILABLE_TAGS) {
            Chip chip = createFilterChip(tag);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filterViewModel.addTag(tag);
                } else {
                    filterViewModel.removeTag(tag);
                }
            });
            tagsChipGroup.addView(chip);
        }
    }

    /**
     * Создает чип для фильтра
     * @param text текст для чипа
     * @return чип
     */
    private Chip createFilterChip(String text) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setChipBackgroundColorResource(R.color.chip_background_color_selector);
        chip.setTextColor(getResources().getColorStateList(R.color.chip_text_color_selector, null));
        chip.setChipStrokeWidth(1);
        chip.setChipStrokeColorResource(R.color.accent_purple_light);
        return chip;
    }

    /**
     * Загружает текущие настройки из ViewModel
     */
    private void loadCurrentSettings() {
        // Загружаем выбранные жанры
        filterViewModel.getSelectedGenres().observe(getViewLifecycleOwner(), genres -> {
            for (int i = 0; i < genresChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) genresChipGroup.getChildAt(i);
                chip.setChecked(genres.contains(chip.getText().toString()));
            }
        });
        
        // Загружаем выбранные страны
        filterViewModel.getSelectedCountries().observe(getViewLifecycleOwner(), countries -> {
            for (int i = 0; i < countriesChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) countriesChipGroup.getChildAt(i);
                chip.setChecked(countries.contains(chip.getText().toString()));
            }
        });
        
        // Загружаем выбранные языки
        filterViewModel.getSelectedLanguages().observe(getViewLifecycleOwner(), languages -> {
            for (int i = 0; i < languagesChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) languagesChipGroup.getChildAt(i);
                chip.setChecked(languages.contains(chip.getText().toString()));
            }
        });
        
        // Загружаем выбранные теги
        filterViewModel.getSelectedTags().observe(getViewLifecycleOwner(), tags -> {
            for (int i = 0; i < tagsChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) tagsChipGroup.getChildAt(i);
                chip.setChecked(tags.contains(chip.getText().toString()));
            }
        });
        
        // Загружаем диапазон лет
        filterViewModel.getMinYear().observe(getViewLifecycleOwner(), minYear -> {
            minYearEdit.setText(String.valueOf(minYear));
        });
        
        filterViewModel.getMaxYear().observe(getViewLifecycleOwner(), maxYear -> {
            maxYearEdit.setText(String.valueOf(maxYear));
        });
        
        // Загружаем диапазон длительности
        filterViewModel.getMinDuration().observe(getViewLifecycleOwner(), minDuration -> {
            minDurationEdit.setText(String.valueOf(minDuration));
        });
        
        filterViewModel.getMaxDuration().observe(getViewLifecycleOwner(), maxDuration -> {
            maxDurationEdit.setText(String.valueOf(maxDuration));
        });
        
        // Загружаем настройку контента 18+
        filterViewModel.getAdultContentEnabled().observe(getViewLifecycleOwner(), enabled -> {
            adultContentSwitch.setChecked(enabled);
        });
    }

    /**
     * Наблюдает за изменениями в настройках
     */
    private void observeSettings() {
        // Здесь можно добавить дополнительную логику для отслеживания изменений
    }

    /**
     * Сбрасывает все фильтры
     */
    private void resetFilters() {
        // Сбрасываем все фильтры в ViewModel
        filterViewModel.resetPreferences();
        
        // Отображаем сообщение
        Toast.makeText(requireContext(), "Фильтры сброшены", Toast.LENGTH_SHORT).show();
    }

    /**
     * Применяет фильтры
     */
    private void applyFilters() {
        // Считываем значения из полей ввода
        try {
            // Диапазон лет
            int minYear = Integer.parseInt(minYearEdit.getText().toString());
            int maxYear = Integer.parseInt(maxYearEdit.getText().toString());
            
            // Проверка корректности диапазона
            if (minYear > maxYear) {
                Toast.makeText(requireContext(), "Некорректный диапазон лет", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Диапазон длительности
            int minDuration = Integer.parseInt(minDurationEdit.getText().toString());
            int maxDuration = Integer.parseInt(maxDurationEdit.getText().toString());
            
            // Проверка корректности диапазона
            if (minDuration > maxDuration) {
                Toast.makeText(requireContext(), "Некорректный диапазон длительности", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Устанавливаем значения в ViewModel
            filterViewModel.setMinYear(minYear);
            filterViewModel.setMaxYear(maxYear);
            filterViewModel.setMinDuration(minDuration);
            filterViewModel.setMaxDuration(maxDuration);
            
            // Устанавливаем настройку контента 18+
            filterViewModel.setAdultContentEnabled(adultContentSwitch.isChecked());
            
            // Сохраняем все настройки
            filterViewModel.saveUserPreferences();
            
            // Устанавливаем флаг применения фильтров
            filtersApplied = true;
            
            // Отображаем сообщение
            Toast.makeText(requireContext(), "Фильтры применены", Toast.LENGTH_SHORT).show();
            
            // Закрываем экран настроек
            closeSettings();
            
        } catch (NumberFormatException e) {
            // Обработка ошибки при парсинге чисел
            Toast.makeText(requireContext(), "Введите корректные числовые значения", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Закрывает экран настроек
     */
    private void closeSettings() {
        // Вызываем обратный вызов, если он установлен
        if (listener != null) {
            listener.onFilterSettingsClosed(filtersApplied);
        }
        
        // Закрываем фрагмент
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}