package com.draker.swipetime.database;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * Утилитный класс для очистки базы данных
 */
public class DbCleanerUtil {
    
    private static final String TAG = "DbCleanerUtil";
    
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
}
