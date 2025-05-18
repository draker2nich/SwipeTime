package com.draker.swipetime.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Вспомогательный класс для миграции между версиями фрагментов
 */
public class FragmentMigrationHelper {
    private static final String TAG = "FragmentMigration";
    private static final String PREFS_NAME = "fragment_migration_prefs";
    private static final String KEY_USING_INFINITE_FRAGMENTS = "using_infinite_fragments";
    
    /**
     * Проверяет, следует ли использовать бесконечные фрагменты
     * @param context контекст приложения
     * @return true, если следует использовать бесконечные фрагменты
     */
    public static boolean shouldUseInfiniteFragments(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // По умолчанию используем бесконечные фрагменты
        return prefs.getBoolean(KEY_USING_INFINITE_FRAGMENTS, true);
    }
    
    /**
     * Устанавливает использование бесконечных фрагментов
     * @param context контекст приложения
     * @param useInfinite true, если следует использовать бесконечные фрагменты
     */
    public static void setUseInfiniteFragments(Context context, boolean useInfinite) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_USING_INFINITE_FRAGMENTS, useInfinite).apply();
        
        Log.d(TAG, "Установлен режим бесконечных фрагментов: " + useInfinite);
    }
}