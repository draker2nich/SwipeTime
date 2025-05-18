package com.draker.swipetime.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Вспомогательный класс для управления миграцией между различными типами фрагментов
 */
public class FragmentMigrationHelper {
    private static final String TAG = "FragmentMigration";
    private static final String PREFS_NAME = "fragment_migration_prefs";
    private static final String KEY_USE_INFINITE_FRAGMENTS = "use_infinite_fragments";
    
    /**
     * Устанавливает флаг использования бесконечных фрагментов
     * @param context контекст приложения
     * @param useInfiniteFragments true, если нужно использовать бесконечные фрагменты
     */
    public static void setUseInfiniteFragments(Context context, boolean useInfiniteFragments) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_USE_INFINITE_FRAGMENTS, useInfiniteFragments).apply();
            Log.d(TAG, "Установлен режим бесконечных фрагментов: " + useInfiniteFragments);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при установке режима фрагментов: " + e.getMessage());
        }
    }
    
    /**
     * Проверяет, следует ли использовать бесконечные фрагменты
     * @param context контекст приложения
     * @return true, если нужно использовать бесконечные фрагменты
     */
    public static boolean shouldUseInfiniteFragments(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            // По умолчанию используем бесконечные фрагменты
            return prefs.getBoolean(KEY_USE_INFINITE_FRAGMENTS, true);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении режима фрагментов: " + e.getMessage());
            return true; // По умолчанию используем бесконечные фрагменты
        }
    }
    
    /**
     * Переключает режим фрагментов
     * @param context контекст приложения
     * @return новый режим после переключения
     */
    public static boolean toggleFragmentMode(Context context) {
        boolean currentMode = shouldUseInfiniteFragments(context);
        boolean newMode = !currentMode;
        setUseInfiniteFragments(context, newMode);
        return newMode;
    }
}
