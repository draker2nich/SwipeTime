package com.draker.swipetime.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

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
import com.draker.swipetime.database.dao.UserPreferencesDao;
import com.draker.swipetime.database.dao.UserStatsDao;
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
import com.draker.swipetime.database.entities.UserPreferencesEntity;
import com.draker.swipetime.database.entities.UserStatsEntity;

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
        ReviewEntity.class,
        UserStatsEntity.class,
        UserPreferencesEntity.class
    },
    version = 5,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "swipetime-db";
    private static AppDatabase instance;
    
    // Миграция с версии 1 на версию 2
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 1. Обработка таблицы content
            database.execSQL("CREATE TABLE IF NOT EXISTS `content_new` (" +
                    "`id` TEXT NOT NULL, " +
                    "`title` TEXT, " +
                    "`description` TEXT, " +
                    "`image_url` TEXT, " +
                    "`category` TEXT, " +
                    "`content_type` TEXT, " +
                    "`liked` INTEGER NOT NULL, " +
                    "`watched` INTEGER NOT NULL, " +
                    "`rating` REAL NOT NULL, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "`updated_at` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`id`))");
            
            database.execSQL("INSERT INTO content_new SELECT id, title, description, image_url, " +
                    "category, content_type, liked, viewed AS watched, rating, created_at, updated_at FROM content");
            
            database.execSQL("DROP TABLE content");
            database.execSQL("ALTER TABLE content_new RENAME TO content");
            
            // 2. Обработка таблицы movies
            database.execSQL("CREATE TABLE IF NOT EXISTS `movies_new` (" +
                    "`id` TEXT NOT NULL, " +
                    "`title` TEXT, " +
                    "`description` TEXT, " +
                    "`image_url` TEXT, " +
                    "`category` TEXT, " +
                    "`content_type` TEXT, " +
                    "`liked` INTEGER NOT NULL, " +
                    "`watched` INTEGER NOT NULL, " +
                    "`rating` REAL NOT NULL, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "`updated_at` INTEGER NOT NULL, " +
                    "`director` TEXT, " +
                    "`release_year` INTEGER NOT NULL, " +
                    "`duration` INTEGER NOT NULL, " +
                    "`genres` TEXT, " +
                    "PRIMARY KEY(`id`))");
            
            database.execSQL("INSERT INTO movies_new SELECT id, title, description, image_url, " +
                    "category, content_type, liked, viewed AS watched, rating, created_at, updated_at, " +
                    "director, release_year, duration, genres FROM movies");
            
            database.execSQL("DROP TABLE movies");
            database.execSQL("ALTER TABLE movies_new RENAME TO movies");
            
            // 3. Обработка таблицы tv_shows
            database.execSQL("CREATE TABLE IF NOT EXISTS `tv_shows_new` (" +
                    "`id` TEXT NOT NULL, " +
                    "`title` TEXT, " +
                    "`description` TEXT, " +
                    "`image_url` TEXT, " +
                    "`category` TEXT, " +
                    "`content_type` TEXT, " +
                    "`liked` INTEGER NOT NULL, " +
                    "`watched` INTEGER NOT NULL, " +
                    "`rating` REAL NOT NULL, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "`updated_at` INTEGER NOT NULL, " +
                    "`creator` TEXT, " +
                    "`start_year` INTEGER NOT NULL, " +
                    "`end_year` INTEGER NOT NULL, " +
                    "`seasons` INTEGER NOT NULL, " +
                    "`episodes` INTEGER NOT NULL, " +
                    "`genres` TEXT, " +
                    "`status` TEXT, " +
                    "PRIMARY KEY(`id`))");
            
            database.execSQL("INSERT INTO tv_shows_new SELECT id, title, description, image_url, " +
                    "category, content_type, liked, viewed AS watched, rating, created_at, updated_at, " +
                    "creator, start_year, end_year, seasons, episodes, genres, status FROM tv_shows");
            
            database.execSQL("DROP TABLE tv_shows");
            database.execSQL("ALTER TABLE tv_shows_new RENAME TO tv_shows");
            
            // 4. Обработка таблицы anime
            database.execSQL("CREATE TABLE IF NOT EXISTS `anime_new` (" +
                    "`id` TEXT NOT NULL, " +
                    "`title` TEXT, " +
                    "`description` TEXT, " +
                    "`image_url` TEXT, " +
                    "`category` TEXT, " +
                    "`content_type` TEXT, " +
                    "`liked` INTEGER NOT NULL, " +
                    "`watched` INTEGER NOT NULL, " +
                    "`rating` REAL NOT NULL, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "`updated_at` INTEGER NOT NULL, " +
                    "`studio` TEXT, " +
                    "`release_year` INTEGER NOT NULL, " +
                    "`episodes` INTEGER NOT NULL, " +
                    "`genres` TEXT, " +
                    "`status` TEXT, " +
                    "`type` TEXT, " +
                    "PRIMARY KEY(`id`))");
            
            database.execSQL("INSERT INTO anime_new SELECT id, title, description, image_url, " +
                    "category, content_type, liked, viewed AS watched, rating, created_at, updated_at, " +
                    "studio, release_year, episodes, genres, status, type FROM anime");
            
            database.execSQL("DROP TABLE anime");
            database.execSQL("ALTER TABLE anime_new RENAME TO anime");
            
            // 5. Обработка таблицы games - добавление поля is_completed
            database.execSQL("CREATE TABLE IF NOT EXISTS `games_new` (" +
                    "`id` TEXT NOT NULL, " +
                    "`title` TEXT, " +
                    "`description` TEXT, " +
                    "`image_url` TEXT, " +
                    "`category` TEXT, " +
                    "`content_type` TEXT, " +
                    "`liked` INTEGER NOT NULL, " +
                    "`is_completed` INTEGER NOT NULL, " +
                    "`rating` REAL NOT NULL, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "`updated_at` INTEGER NOT NULL, " +
                    "`developer` TEXT, " +
                    "`publisher` TEXT, " +
                    "`release_year` INTEGER NOT NULL, " +
                    "`platforms` TEXT, " +
                    "`genres` TEXT, " +
                    "`esrb_rating` TEXT, " +
                    "PRIMARY KEY(`id`))");
            
            database.execSQL("INSERT INTO games_new SELECT id, title, description, image_url, " +
                    "category, content_type, liked, viewed AS is_completed, rating, created_at, updated_at, " +
                    "developer, publisher, release_year, platforms, genres, esrb_rating FROM games");
            
            database.execSQL("DROP TABLE games");
            database.execSQL("ALTER TABLE games_new RENAME TO games");
            
            // 6. Обработка таблицы books - добавление поля is_read
            database.execSQL("CREATE TABLE IF NOT EXISTS `books_new` (" +
                    "`id` TEXT NOT NULL, " +
                    "`title` TEXT, " +
                    "`description` TEXT, " +
                    "`image_url` TEXT, " +
                    "`category` TEXT, " +
                    "`content_type` TEXT, " +
                    "`liked` INTEGER NOT NULL, " +
                    "`is_read` INTEGER NOT NULL, " +
                    "`rating` REAL NOT NULL, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "`updated_at` INTEGER NOT NULL, " +
                    "`author` TEXT, " +
                    "`publisher` TEXT, " +
                    "`publish_year` INTEGER NOT NULL, " +
                    "`page_count` INTEGER NOT NULL, " +
                    "`genres` TEXT, " +
                    "`isbn` TEXT, " +
                    "PRIMARY KEY(`id`))");
            
            database.execSQL("INSERT INTO books_new SELECT id, title, description, image_url, " +
                    "category, content_type, liked, viewed AS is_read, rating, created_at, updated_at, " +
                    "author, publisher, publish_year, page_count, genres, isbn FROM books");
            
            database.execSQL("DROP TABLE books");
            database.execSQL("ALTER TABLE books_new RENAME TO books");
        }
    };
    
    // Миграция с версии 3 на версию 4 - добавление таблицы user_stats
    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Создаем таблицу статистики пользователя
            database.execSQL("CREATE TABLE IF NOT EXISTS `user_stats` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`user_id` TEXT NOT NULL, " +
                    "`swipes_count` INTEGER NOT NULL DEFAULT 0, " +
                    "`right_swipes_count` INTEGER NOT NULL DEFAULT 0, " +
                    "`left_swipes_count` INTEGER NOT NULL DEFAULT 0, " +
                    "`ratings_count` INTEGER NOT NULL DEFAULT 0, " +
                    "`reviews_count` INTEGER NOT NULL DEFAULT 0, " +
                    "`consumed_count` INTEGER NOT NULL DEFAULT 0, " +
                    "`total_actions` INTEGER NOT NULL DEFAULT 0, " +
                    "`streak_days` INTEGER NOT NULL DEFAULT 0, " +
                    "`last_activity_date` INTEGER NOT NULL DEFAULT 0, " +
                    "`achievements_count` INTEGER NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY(`user_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            
            // Создаем индекс для user_id
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_user_stats_user_id` ON `user_stats` (`user_id`)");
            
            // Заполняем таблицу статистики для существующих пользователей
            database.execSQL("INSERT INTO `user_stats` (`user_id`, `last_activity_date`) " +
                    "SELECT `id`, `last_login` FROM `users`");
        }
    };
    
    // Миграция с версии 4 на версию 5 - добавление таблицы user_preferences
    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Проверяем существование таблицы
            database.execSQL("DROP TABLE IF EXISTS `user_preferences`");
            
            // Создаем таблицу пользовательских настроек с правильной схемой
            database.execSQL("CREATE TABLE IF NOT EXISTS `user_preferences` (" +
                    "`user_id` TEXT NOT NULL, " +
                    "`preferred_genres` TEXT, " +
                    "`min_duration` INTEGER NOT NULL DEFAULT 0, " +
                    "`max_duration` INTEGER NOT NULL DEFAULT 2147483647, " +
                    "`preferred_countries` TEXT, " +
                    "`preferred_languages` TEXT, " +
                    "`min_year` INTEGER NOT NULL DEFAULT 1900, " +
                    "`max_year` INTEGER NOT NULL DEFAULT 2100, " +
                    "`interests_tags` TEXT, " +
                    "`adult_content_enabled` INTEGER NOT NULL DEFAULT 0, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "`updated_at` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`user_id`), " +
                    "FOREIGN KEY(`user_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            
            // Создаем индекс для внешнего ключа
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_user_preferences_user_id` ON `user_preferences` (`user_id`)");
        }
    };

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
    public abstract UserStatsDao userStatsDao();
    public abstract UserPreferencesDao userPreferencesDao();

    // Singleton паттерн для доступа к базе данных
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                DATABASE_NAME
            )
            .fallbackToDestructiveMigration() // При изменении схемы БД удаляем старую и создаем новую
            .addMigrations(MIGRATION_1_2, MIGRATION_3_4, MIGRATION_4_5) // Добавляем миграции
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
