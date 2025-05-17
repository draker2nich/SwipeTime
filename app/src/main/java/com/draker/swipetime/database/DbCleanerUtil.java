package com.draker.swipetime.database;

import android.content.Context;
import android.util.Log;

import androidx.sqlite.db.SupportSQLiteDatabase;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Утилитный класс для очистки базы данных
 */
public class DbCleanerUtil {
    
    private static final String TAG = "DbCleanerUtil";
    private static final Executor executor = Executors.newSingleThreadExecutor();
    
    /**
     * Полностью удаляет базу данных приложения
     * Используйте этот метод только в крайнем случае, когда миграция не работает корректно
     * @param context контекст приложения
     * @return true, если база была успешно удалена
     */
    public static boolean deleteDatabase(Context context) {
        try {
            // Сначала закрываем и уничтожаем экземпляр базы данных
            AppDatabase.destroyInstance();
            
            // Получаем путь к базе данных и удаляем файл
            File dbFile = context.getDatabasePath("swipetime-db");
            if (dbFile.exists()) {
                boolean deleted = dbFile.delete();
                Log.i(TAG, "База данных удалена: " + deleted);
                
                // Также удаляем файлы журнала и резервные копии
                new File(dbFile.getPath() + "-shm").delete();
                new File(dbFile.getPath() + "-wal").delete();
                new File(dbFile.getPath() + "-journal").delete();
                
                return deleted;
            } else {
                Log.i(TAG, "База данных не существует");
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при удалении базы данных: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Удаляет тестовые данные из всех категорий, но сохраняет пользовательские данные
     * @param context контекст приложения
     */
    public static void clearTestData(Context context) {
        Log.d(TAG, "Начало очистки тестовых данных из всех категорий");
        
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                SupportSQLiteDatabase sqliteDb = db.getOpenHelper().getWritableDatabase();
                
                // Отключаем проверку внешних ключей для безопасного удаления
                sqliteDb.execSQL("PRAGMA foreign_keys = OFF");
                
                // Удаляем все фильмы, не отмеченные пользователем как понравившиеся
                sqliteDb.execSQL("DELETE FROM movies WHERE liked = 0");
                Log.d(TAG, "Удалены тестовые данные фильмов");
                
                // Удаляем все сериалы, не отмеченные пользователем
                sqliteDb.execSQL("DELETE FROM tv_shows WHERE liked = 0");
                Log.d(TAG, "Удалены тестовые данные сериалов");
                
                // Удаляем все игры, не отмеченные пользователем
                sqliteDb.execSQL("DELETE FROM games WHERE liked = 0");
                Log.d(TAG, "Удалены тестовые данные игр");
                
                // Удаляем все книги, не отмеченные пользователем
                sqliteDb.execSQL("DELETE FROM books WHERE liked = 0");
                Log.d(TAG, "Удалены тестовые данные книг");
                
                // Удаляем все аниме, не отмеченные пользователем
                sqliteDb.execSQL("DELETE FROM anime WHERE liked = 0");
                Log.d(TAG, "Удалены тестовые данные аниме");
                
                // Очищаем контент, не связанный с понравившимися элементами
                sqliteDb.execSQL("DELETE FROM content WHERE type NOT IN ('music') AND liked = 0");
                Log.d(TAG, "Удален тестовый общий контент");
                
                // Включаем обратно проверку внешних ключей
                sqliteDb.execSQL("PRAGMA foreign_keys = ON");
                
                Log.d(TAG, "Очистка тестовых данных успешно завершена");
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при очистке тестовых данных: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
