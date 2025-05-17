package com.draker.swipetime.database;

import android.content.Context;
import android.util.Log;

import androidx.sqlite.db.SupportSQLiteDatabase;

import com.draker.swipetime.database.entities.AchievementEntity;
import com.draker.swipetime.database.entities.AnimeEntity;
import com.draker.swipetime.database.entities.BookEntity;
import com.draker.swipetime.database.entities.ContentEntity;
import com.draker.swipetime.database.entities.GameEntity;
import com.draker.swipetime.database.entities.MovieEntity;
import com.draker.swipetime.database.entities.TVShowEntity;
import com.draker.swipetime.database.entities.UserAchievementCrossRef;
import com.draker.swipetime.database.entities.UserEntity;
import com.draker.swipetime.database.entities.UserStatsEntity;
import com.draker.swipetime.utils.GamificationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Класс для генерации тестовых данных
 */
public class DataGenerator {
    private static final String TAG = "DataGenerator";
    private static final Executor executor = Executors.newSingleThreadExecutor();

    /**
     * Заполнить базу данных тестовыми данными
     * @param context контекст приложения
     */
    public static void populateDatabase(Context context) {
        Log.d(TAG, "Начало заполнения базы данных тестовыми данными");

        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);

                // Проверяем, есть ли данные в базе
                int movieCount = db.movieDao().getCount();
                int tvShowCount = db.tvShowDao().getCount();
                int gameCount = db.gameDao().getCount();
                int bookCount = db.bookDao().getCount();
                int animeCount = db.animeDao().getCount();
                int contentCount = db.contentDao().getCount();
                int userCount = db.userDao().getCount();

                Log.d(TAG, "Проверка данных в базе: фильмы=" + movieCount +
                        ", сериалы=" + tvShowCount +
                        ", игры=" + gameCount +
                        ", книги=" + bookCount +
                        ", аниме=" + animeCount +
                        ", контент=" + contentCount +
                        ", пользователи=" + userCount);

                // Если нет пользователей, создаем их
                if (userCount == 0) {
                    Log.d(TAG, "Создаем пользователей");
                    
                    // Создаем пользователя
                    UserEntity defaultUser = getDefaultUser();
                    db.userDao().insert(defaultUser);
                    
                    // Создаем статистику пользователя
                    UserStatsEntity stats = new UserStatsEntity(defaultUser.getId());
                    stats.setSwipesCount(0);
                    stats.setRightSwipesCount(0);
                    stats.setLeftSwipesCount(0);
                    stats.setRatingsCount(0);
                    stats.setReviewsCount(0);
                    stats.setConsumedCount(0);
                    stats.setStreakDays(0);
                    stats.setLastActivityDate(System.currentTimeMillis());
                    db.userStatsDao().insert(stats);

                    // Инициализация базового набора достижений
                    GamificationManager gamificationManager = GamificationManager.getInstance(context);
                    
                    Log.d(TAG, "Пользователи созданы");
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при заполнении базы данных: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Очистить базу данных
     * @param db экземпляр базы данных
     */
    private static void clearDatabase(AppDatabase db) {
        try {
            // Очистка всех таблиц с помощью SQL
            SupportSQLiteDatabase sqliteDb = db.getOpenHelper().getWritableDatabase();

            // Отключаем проверку внешних ключей
            sqliteDb.execSQL("PRAGMA foreign_keys = OFF");

            // Удаляем данные из всех таблиц
            sqliteDb.execSQL("DELETE FROM reviews");
            sqliteDb.execSQL("DELETE FROM user_achievements");
            sqliteDb.execSQL("DELETE FROM achievements");
            sqliteDb.execSQL("DELETE FROM user_stats");
            sqliteDb.execSQL("DELETE FROM movies");
            sqliteDb.execSQL("DELETE FROM tv_shows");
            sqliteDb.execSQL("DELETE FROM games");
            sqliteDb.execSQL("DELETE FROM books");
            sqliteDb.execSQL("DELETE FROM anime");
            sqliteDb.execSQL("DELETE FROM users");
            sqliteDb.execSQL("DELETE FROM content");

            // Включаем проверку внешних ключей
            sqliteDb.execSQL("PRAGMA foreign_keys = ON");

            Log.d(TAG, "База данных очищена успешно");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при очистке базы данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Создать тестовый список музыки
     * @return список музыки как ContentEntity
     */
    private static List<ContentEntity> getMusic() {
        List<ContentEntity> musicList = new ArrayList<>();

        // Добавляем музыку через общую сущность ContentEntity
        ContentEntity music1 = new ContentEntity(
                "music-1",
                "Bohemian Rhapsody",
                "Одна из самых известных рок-композиций всех времен, выпущенная группой Queen в 1975 году.",
                "https://m.media-amazon.com/images/I/61w14nRnVOL._AC_UL400_.jpg",
                "Музыка",
                "music"
        );
        music1.setRating(9.8f);
        musicList.add(music1);

        ContentEntity music2 = new ContentEntity(
                "music-2",
                "Billie Jean",
                "Один из самых узнаваемых хитов Майкла Джексона, выпущенный в 1983 году.",
                "https://m.media-amazon.com/images/I/81xfGi5QBCL._AC_UL400_.jpg",
                "Музыка",
                "music"
        );
        music2.setRating(9.5f);
        musicList.add(music2);

        ContentEntity music3 = new ContentEntity(
                "music-3",
                "Imagine",
                "Культовая песня Джона Леннона, выпущенная в 1971 году, ставшая гимном мира и единства.",
                "https://m.media-amazon.com/images/I/61u15Mv-80L._AC_UL400_.jpg",
                "Музыка",
                "music"
        );
        music3.setRating(9.4f);
        musicList.add(music3);

        ContentEntity music4 = new ContentEntity(
                "music-4",
                "Smells Like Teen Spirit",
                "Прорывной хит группы Nirvana, выпущенный в 1991 году, ставший гимном поколения.",
                "https://m.media-amazon.com/images/I/61Xj+2y6AQL._AC_UL400_.jpg",
                "Музыка",
                "music"
        );
        music4.setRating(9.6f);
        musicList.add(music4);

        ContentEntity music5 = new ContentEntity(
                "music-5",
                "Like a Rolling Stone",
                "Революционная песня Боба Дилана, выпущенная в 1965 году, изменившая лицо рок-музыки.",
                "https://m.media-amazon.com/images/I/91AHUgFBIJL._AC_UL400_.jpg",
                "Музыка",
                "music"
        );
        music5.setRating(9.2f);
        musicList.add(music5);

        return musicList;
    }

    /**
     * Создать тестовый список фильмов
     * @return список фильмов
     */
    private static List<MovieEntity> getMovies() {
        List<MovieEntity> movies = new ArrayList<>();

        movies.add(new MovieEntity(
                "movie-1",
                "Начало",
                "Вор, который крадет корпоративные секреты через использование технологии разделения сна, получает задание внедрить идею в сознание директора компании.",
                "https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg",
                "Кристофер Нолан",
                2010,
                148,
                "Фантастика, Боевик, Триллер, Детектив"
        ));

        movies.add(new MovieEntity(
                "movie-2",
                "Интерстеллар",
                "Группа исследователей путешествует через червоточину в поисках новой планеты для человечества.",
                "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                "Кристофер Нолан",
                2014,
                169,
                "Фантастика, Драма, Приключения"
        ));

        movies.add(new MovieEntity(
                "movie-3",
                "Матрица",
                "Хакер узнает от таинственных повстанцев о фальшивой природе его реальности и о его роли в войне против её создателей.",
                "https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg",
                "Вачовски",
                1999,
                136,
                "Фантастика, Боевик"
        ));

        movies.add(new MovieEntity(
                "movie-4",
                "Бойцовский клуб",
                "Страдающий от бессонницы офисный работник и харизматичный торговец мылом основывают подпольный бойцовский клуб.",
                "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
                "Дэвид Финчер",
                1999,
                139,
                "Драма, Триллер"
        ));

        movies.add(new MovieEntity(
                "movie-5",
                "Форрест Гамп",
                "История жизни простого человека с IQ 75, который невольно стал участником многих исторических событий.",
                "https://image.tmdb.org/t/p/w500/clolk7rB5lAjs41SD0Vt6IXYLMm.jpg",
                "Роберт Земекис",
                1994,
                142,
                "Драма, Комедия, Мелодрама"
        ));

        return movies;
    }

    /**
     * Создать тестовый список сериалов
     * @return список сериалов
     */
    private static List<TVShowEntity> getTVShows() {
        List<TVShowEntity> tvShows = new ArrayList<>();

        tvShows.add(new TVShowEntity(
                "tvshow-1",
                "Игра престолов",
                "Несколько знатных семей ведут борьбу за Железный трон, в то время как древняя угроза возвращается после тысячелетнего сна.",
                "https://image.tmdb.org/t/p/w500/u3bZgnGQ9T01sWNhyveQz0wH0Hl.jpg",
                "Дэвид Бениофф, Д.Б. Уайсс",
                2011,
                2019,
                8,
                73,
                "Фэнтези, Драма, Боевик, Приключения",
                "finished"
        ));

        tvShows.add(new TVShowEntity(
                "tvshow-2",
                "Во все тяжкие",
                "Учитель химии, у которого диагностировали неоперабельный рак легких, начинает производить метамфетамин, чтобы обеспечить будущее своей семьи.",
                "https://image.tmdb.org/t/p/w500/ggFHVNu6YYI5L9pCfOacjizRGt.jpg",
                "Винс Гиллиган",
                2008,
                2013,
                5,
                62,
                "Драма, Криминал, Триллер",
                "finished"
        ));

        tvShows.add(new TVShowEntity(
                "tvshow-3",
                "Чернобыль",
                "В апреле 1986 года взрыв на Чернобыльской АЭС стал одной из самых страшных ядерных катастроф в истории человечества.",
                "https://image.tmdb.org/t/p/w500/hlLXt2tOPT6RRnjiUmoxyG1LTFi.jpg",
                "Крэйг Мейзин",
                2019,
                2019,
                1,
                5,
                "Драма, История, Триллер",
                "finished"
        ));

        tvShows.add(new TVShowEntity(
                "tvshow-4",
                "Шерлок",
                "Современная адаптация произведений сэра Артура Конан Дойла о детективе Шерлоке Холмсе и его напарнике докторе Ватсоне.",
                "https://image.tmdb.org/t/p/w500/7WTsnHkbA0FaG6R9dikbd7B9T3m.jpg",
                "Марк Гэтисс, Стивен Моффат",
                2010,
                2017,
                4,
                13,
                "Детектив, Драма, Криминал",
                "finished"
        ));

        tvShows.add(new TVShowEntity(
                "tvshow-5",
                "Мандалорец",
                "Одинокий охотник за головами путешествует по дальним уголкам галактики, вдали от власти Новой Республики.",
                "https://image.tmdb.org/t/p/w500/sWgBv7LV2PRoQgkxwlibdGXKz1S.jpg",
                "Джон Фавро",
                2019,
                2023,
                3,
                24,
                "Фантастика, Боевик, Приключения",
                "ongoing"
        ));

        return tvShows;
    }

    /**
     * Создать тестовый список игр
     * @return список игр
     */
    private static List<GameEntity> getGames() {
        List<GameEntity> games = new ArrayList<>();

        games.add(new GameEntity(
                "game-1",
                "The Witcher 3: Wild Hunt",
                "Вы — Геральт из Ривии, наемный охотник на чудовищ. Вы направляетесь на поиски ребенка из древнего пророчества, живого оружия, способного изменить мир.",
                "https://cdn.cloudflare.steamstatic.com/steam/apps/292030/header.jpg",
                "CD Projekt RED",
                "CD Projekt RED",
                2015,
                "PC, PlayStation 4, Xbox One, Nintendo Switch, PlayStation 5, Xbox Series X/S",
                "RPG, Открытый мир, Фэнтези",
                "M (Mature)"
        ));

        games.add(new GameEntity(
                "game-2",
                "Red Dead Redemption 2",
                "Америка, 1899 год. Конец эпохи Дикого Запада. После неудачного ограбления банка Артур Морган и банда Ван дер Линде вынуждены скрываться.",
                "https://cdn.cloudflare.steamstatic.com/steam/apps/1174180/header.jpg",
                "Rockstar Games",
                "Rockstar Games",
                2018,
                "PlayStation 4, Xbox One, PC, Google Stadia",
                "Приключения, Боевик, Открытый мир",
                "M (Mature)"
        ));

        games.add(new GameEntity(
                "game-3",
                "The Legend of Zelda: Breath of the Wild",
                "Шаг в мир приключений и исследуйте огромное открытое королевство Хайрул как хотите в этой трехмерной игре.",
                "https://assets.nintendo.com/image/upload/c_pad,f_auto,q_auto,w_960/ncom/en_US/games/switch/t/the-legend-of-zelda-breath-of-the-wild-switch/hero",
                "Nintendo",
                "Nintendo",
                2017,
                "Nintendo Switch, Wii U",
                "Приключения, Ролевая игра, Открытый мир",
                "E10+ (Everyone 10+)"
        ));

        games.add(new GameEntity(
                "game-4",
                "God of War (2018)",
                "С окончанием эры богов, Кратос, бывший спартанский воин, пытается оставить свое темное прошлое позади, живя как обычный человек в царстве скандинавских богов и монстров.",
                "https://cdn.cloudflare.steamstatic.com/steam/apps/1593500/header.jpg",
                "Santa Monica Studio",
                "Sony Interactive Entertainment",
                2018,
                "PlayStation 4, PC",
                "Боевик, Приключения, RPG",
                "M (Mature)"
        ));

        games.add(new GameEntity(
                "game-5",
                "Minecraft",
                "Minecraft — это игра о размещении блоков и приключениях. Исследуйте случайно сгенерированные миры и стройте от простых домов до величественных замков.",
                "https://cdn.akamai.steamstatic.com/steam/apps/1440440/header.jpg",
                "Mojang Studios",
                "Mojang Studios",
                2011,
                "PC, PlayStation, Xbox, Nintendo Switch, iOS, Android",
                "Песочница, Выживание, Приключения",
                "E10+ (Everyone 10+)"
        ));

        return games;
    }

    /**
     * Создать тестовый список книг
     * @return список книг
     */
    private static List<BookEntity> getBooks() {
        List<BookEntity> books = new ArrayList<>();

        books.add(new BookEntity(
                "book-1",
                "1984",
                "Антиутопический роман о тоталитарном обществе и попытках одного человека восстать против системы.",
                "https://m.media-amazon.com/images/I/71kxa1-0mfL._AC_UF1000,1000_QL80_.jpg",
                "Джордж Оруэлл",
                "Secker & Warburg",
                1949,
                328,
                "Антиутопия, Фантастика, Политика",
                "978-5-699-92122-9"
        ));

        books.add(new BookEntity(
                "book-2",
                "Гарри Поттер и философский камень",
                "Первая книга из серии о юном волшебнике Гарри Поттере, который узнает о своем необычном происхождении и поступает в школу магии Хогвартс.",
                "https://m.media-amazon.com/images/I/81iqZ2HHD-L._AC_UF1000,1000_QL80_.jpg",
                "Дж. К. Роулинг",
                "Bloomsbury",
                1997,
                223,
                "Фэнтези, Приключения, Магический реализм",
                "978-5-353-00308-3"
        ));

        books.add(new BookEntity(
                "book-3",
                "Властелин колец",
                "Эпическая фэнтези-сага о хоббите Фродо, который должен уничтожить Кольцо Всевластия, чтобы спасти Средиземье от Тёмного Властелина Саурона.",
                "https://m.media-amazon.com/images/I/71jLBXtWJWL._AC_UF1000,1000_QL80_.jpg",
                "Дж. Р. Р. Толкин",
                "Allen & Unwin",
                1954,
                1178,
                "Фэнтези, Приключения, Эпос",
                "978-5-17-113632-0"
        ));

        books.add(new BookEntity(
                "book-4",
                "Преступление и наказание",
                "Психологический роман о нравственных дилеммах бедного студента Раскольникова, который решается на убийство.",
                "https://m.media-amazon.com/images/I/81sQQDaHYdL._AC_UF1000,1000_QL80_.jpg",
                "Фёдор Достоевский",
                "The Russian Messenger",
                1866,
                551,
                "Классика, Психологический роман, Драма",
                "978-5-389-09973-3"
        ));

        books.add(new BookEntity(
                "book-5",
                "Алхимик",
                "Философская сказка о пастухе, который отправляется на поиски своего сокровища и находит истинное значение жизни.",
                "https://m.media-amazon.com/images/I/71z4Y3p3gKL._AC_UF1000,1000_QL80_.jpg",
                "Пауло Коэльо",
                "HarperCollins",
                1988,
                197,
                "Философия, Приключения, Духовность",
                "978-5-17-087908-8"
        ));

        return books;
    }

    /**
     * Создать тестовый список аниме
     * @return список аниме
     */
    private static List<AnimeEntity> getAnimes() {
        List<AnimeEntity> animes = new ArrayList<>();

        animes.add(new AnimeEntity(
                "anime-1",
                "Атака титанов",
                "В мире, где человечество живет внутри городов, окруженных огромными стенами из-за страха перед гигантскими гуманоидами, называемыми Титанами, мальчик по имени Эрен клянется истребить их после того, как они разрушили его город и убили его мать.",
                "https://image.tmdb.org/t/p/w500/aiy35Evcofzl7hASZZvsFgltHTX.jpg",
                "Wit Studio, MAPPA",
                2013,
                86,
                "Экшен, Драма, Фэнтези, Сёнен",
                "finished",
                "TV"
        ));

        animes.add(new AnimeEntity(
                "anime-2",
                "Ванпанчмен",
                "Сатира на супергеройские истории, рассказывающая о Сайтаме, супергерое, который может победить любого противника одним ударом, но скучает от отсутствия достойного вызова.",
                "https://image.tmdb.org/t/p/w500/iE3s0lG5QVdEHOEZnoAxjmMtvne.jpg",
                "Madhouse, J.C. Staff",
                2015,
                24,
                "Комедия, Экшен, Сёнен",
                "ongoing",
                "TV"
        ));

        animes.add(new AnimeEntity(
                "anime-3",
                "Стальной алхимик: Братство",
                "Братья Эдвард и Альфонс Элрик ищут философский камень, чтобы восстановить свои тела после неудачной попытки воскресить мать с помощью алхимии.",
                "https://image.tmdb.org/t/p/w500/9Cl6JnRxHBzHRCbvM4TfbOgfp5Z.jpg",
                "Bones",
                2009,
                64,
                "Экшен, Приключения, Драма, Фэнтези, Сёнен",
                "finished",
                "TV"
        ));

        animes.add(new AnimeEntity(
                "anime-4",
                "Тетрадь смерти",
                "Студент находит сверхъестественную тетрадь, которая дает ему способность убивать любого, чье имя он в ней запишет, и начинает создавать утопию, свободную от преступников, но его преследует гениальный детектив.",
                "https://image.tmdb.org/t/p/w500/g5ZHb2CxcjAmkfbCsP0wqlTFtcL.jpg",
                "Madhouse",
                2006,
                37,
                "Мистика, Психологическое, Триллер, Сёнен",
                "finished",
                "TV"
        ));

        animes.add(new AnimeEntity(
                "anime-5",
                "Твое имя",
                "Двое незнакомцев обнаруживают, что они связаны странным образом. Они строят связь и открывают судьбу, которая соединяет их.",
                "https://image.tmdb.org/t/p/w500/q719jXXEzOoYaps6babgKnONONX.jpg",
                "CoMix Wave Films",
                2016,
                1,
                "Драма, Романтика, Сверхъестественное",
                "finished",
                "Movie"
        ));

        return animes;
    }

    /**
     * Создать пользователя по умолчанию
     * @return пользователь по умолчанию
     */
    private static UserEntity getDefaultUser() {
        return new UserEntity(
                "user_1",
                "Пользователь",
                "user@swipetime.com",
                "https://i.pravatar.cc/150?img=1"
        );
    }
}