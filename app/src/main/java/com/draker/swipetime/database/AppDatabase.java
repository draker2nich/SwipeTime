package com.draker.swipetime.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.draker.swipetime.database.dao.AchievementDao;
import com.draker.swipetime.database.dao.AnimeDao;
import com.draker.swipetime.database.dao.BookDao;
import com.draker.swipetime.database.dao.ContentDao;
import com.draker.swipetime.database.dao.GameDao;
import com.draker.swipetime.database.dao.MovieDao;
import com.draker.swipetime.database.dao.ReviewDao;
import com.draker.swipetime.database.dao.TVShowDao;
import com.draker.swipetime.database.dao.UserAchievementDao;
import com.draker.swipetime.database.dao.UserDao;
import com.draker.swipetime.database.entities.AchievementEntity;
import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.ReviewEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.database.entities.UserAchievementCrossRef;
import com.draker.swipetime.database.entities.UserEntity;

/**
 * Главный класс базы данных приложения
 */
@Database(
    entities = {
        ContentEntity.class,
        MovieEntity.class,
        TVShowEntity.class,
        GameEntity.class,
        BookEntity.class,
        AnimeEntity.class,
        UserEntity.class,
        AchievementEntity.class,
        UserAchievementCrossRef.class,
        ReviewEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "swipetime-db";
    private static AppDatabase instance;

    // DAOs
    public abstract ContentDao contentDao();
    public abstract MovieDao movieDao();
    public abstract TVShowDao tvShowDao();
    public abstract GameDao gameDao();
    public abstract BookDao bookDao();
    public abstract AnimeDao animeDao();
    public abstract UserDao userDao();
    public abstract AchievementDao achievementDao();
    public abstract UserAchievementDao userAchievementDao();
    public abstract ReviewDao reviewDao();

    // Singleton паттерн для доступа к базе данных
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                DATABASE_NAME
            )
            .fallbackToDestructiveMigration() // При изменении схемы БД удаляем старую и создаем новую
            .allowMainThreadQueries() // ВНИМАНИЕ: Временное решение для прототипа. В production-версии нужно использовать асинхронные запросы или LiveData
            .build();
        }
        return instance;
    }

    /**
     * Очистить синглтон для тестов
     */
    public static void destroyInstance() {
        instance = null;
    }
}
