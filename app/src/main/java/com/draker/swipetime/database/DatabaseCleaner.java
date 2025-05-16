package com.draker.swipetime.database;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * Утилита для очистки базы данных в случае проблем с миграцией
 */
public class DatabaseCleaner {
    private static final String TAG = "DatabaseCleaner";

    /**
     * Удаляет файл базы данных
     * 
     * @param context контекст приложения
     * @param dbName имя базы данных (swipetime-db)
     * @return true если база данных была удалена, false в противном случае
     */
    public static boolean deleteDatabase(Context context, String dbName) {
        try {
            for (String path : context.databaseList()) {
                Log.d(TAG, "Found database: " + path);
                if (path.contains(dbName) || path.equalsIgnoreCase(dbName)) {
                    File dbFile = context.getDatabasePath(path);
                    boolean deleted = dbFile.delete();
                    
                    Log.d(TAG, "Database " + path + " deleted: " + deleted);
                    
                    // Удаляем также файлы журналов и shm
                    File journalFile = new File(dbFile.getPath() + "-journal");
                    if (journalFile.exists()) {
                        journalFile.delete();
                    }
                    
                    File shmFile = new File(dbFile.getPath() + "-shm");
                    if (shmFile.exists()) {
                        shmFile.delete();
                    }
                    
                    File walFile = new File(dbFile.getPath() + "-wal");
                    if (walFile.exists()) {
                        walFile.delete();
                    }
                    
                    return deleted;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting database: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}