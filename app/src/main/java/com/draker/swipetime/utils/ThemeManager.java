package com.draker.swipetime.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Менеджер тем для управления светлой/темной темой и настройками доступности
 */
public class ThemeManager {
    
    private static final String PREF_THEME_MODE = "theme_mode";
    private static final String PREF_HIGH_CONTRAST = "high_contrast_mode";
    private static final String PREF_LARGE_TEXT = "large_text_mode";
    private static final String PREF_REDUCE_MOTION = "reduce_motion_mode";
    private static final String PREF_HAPTIC_FEEDBACK = "haptic_feedback_enabled";
    
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_AUTO = 2;
    
    private final SharedPreferences preferences;
    private final Context context;
    
    public ThemeManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = this.context.getSharedPreferences("swipetime_prefs", Context.MODE_PRIVATE);
    }
    
    /**
     * Применяет сохраненную тему
     */
    public void applyTheme() {
        int themeMode = getThemeMode();
        switch (themeMode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_AUTO:
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                }
                break;
        }
    }
    
    /**
     * Устанавливает режим темы
     */
    public void setThemeMode(int themeMode) {
        preferences.edit().putInt(PREF_THEME_MODE, themeMode).apply();
        applyTheme();
    }
    
    /**
     * Получает текущий режим темы
     */
    public int getThemeMode() {
        return preferences.getInt(PREF_THEME_MODE, THEME_AUTO);
    }
    
    /**
     * Получает название текущей темы
     */
    public String getThemeName(Context context) {
        int themeMode = getThemeMode();
        switch (themeMode) {
            case THEME_LIGHT:
                return context.getString(com.draker.swipetime.R.string.theme_light);
            case THEME_DARK:
                return context.getString(com.draker.swipetime.R.string.theme_dark);
            case THEME_AUTO:
            default:
                return context.getString(com.draker.swipetime.R.string.theme_auto);
        }
    }
    
    /**
     * Проверяет, включен ли режим высокого контраста
     */
    public boolean isHighContrastEnabled() {
        return preferences.getBoolean(PREF_HIGH_CONTRAST, false);
    }
    
    /**
     * Устанавливает режим высокого контраста
     */
    public void setHighContrastEnabled(boolean enabled) {
        preferences.edit().putBoolean(PREF_HIGH_CONTRAST, enabled).apply();
    }
    
    /**
     * Проверяет, включен ли режим крупного текста
     */
    public boolean isLargeTextEnabled() {
        return preferences.getBoolean(PREF_LARGE_TEXT, false);
    }
    
    /**
     * Устанавливает режим крупного текста
     */
    public void setLargeTextEnabled(boolean enabled) {
        preferences.edit().putBoolean(PREF_LARGE_TEXT, enabled).apply();
    }
    
    /**
     * Проверяет, включен ли режим упрощенных анимаций
     */
    public boolean isReduceMotionEnabled() {
        return preferences.getBoolean(PREF_REDUCE_MOTION, false);
    }
    
    /**
     * Устанавливает режим упрощенных анимаций
     */
    public void setReduceMotionEnabled(boolean enabled) {
        preferences.edit().putBoolean(PREF_REDUCE_MOTION, enabled).apply();
    }
    
    /**
     * Проверяет, включена ли вибрация
     */
    public boolean isHapticFeedbackEnabled() {
        return preferences.getBoolean(PREF_HAPTIC_FEEDBACK, true);
    }
    
    /**
     * Устанавливает режим вибрации
     */
    public void setHapticFeedbackEnabled(boolean enabled) {
        preferences.edit().putBoolean(PREF_HAPTIC_FEEDBACK, enabled).apply();
    }
    
    /**
     * Проверяет, используется ли темная тема в данный момент
     */
    public boolean isDarkTheme() {
        int currentNightMode = context.getResources().getConfiguration().uiMode 
                & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }
    
    /**
     * Переключает между светлой и темной темой
     */
    public void toggleTheme() {
        int currentMode = getThemeMode();
        int newMode = (currentMode == THEME_LIGHT) ? THEME_DARK : THEME_LIGHT;
        setThemeMode(newMode);
    }
    
    /**
     * Проверяет системные настройки доступности
     */
    public boolean isSystemAccessibilityEnabled() {
        // Проверка системных настроек доступности
        android.provider.Settings.System system = new android.provider.Settings.System();
        return android.provider.Settings.System.getInt(
            context.getContentResolver(),
            "accessibility_enabled", 0) == 1;
    }
    
    /**
     * Возвращает рекомендуемую длительность анимации на основе настроек
     */
    public long getAnimationDuration(long defaultDuration) {
        if (isReduceMotionEnabled()) {
            return 0; // Отключаем анимации
        }
        return defaultDuration;
    }
    
    /**
     * Получает короткую длительность анимации
     */
    public long getShortAnimTime() {
        return getAnimationDuration(context.getResources().getInteger(
            com.draker.swipetime.R.integer.short_anim_time));
    }
    
    /**
     * Получает среднюю длительность анимации
     */
    public long getMediumAnimTime() {
        return getAnimationDuration(context.getResources().getInteger(
            com.draker.swipetime.R.integer.medium_anim_time));
    }
    
    /**
     * Получает длинную длительность анимации
     */
    public long getLongAnimTime() {
        return getAnimationDuration(context.getResources().getInteger(
            com.draker.swipetime.R.integer.long_anim_time));
    }
    
    /**
     * Возвращает множитель размера текста
     */
    public float getTextSizeMultiplier() {
        if (isLargeTextEnabled()) {
            return 1.3f; // Увеличиваем текст на 30%
        }
        return 1.0f;
    }
}
