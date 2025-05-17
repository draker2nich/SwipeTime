package com.draker.swipetime.api.repositories;

import android.util.Log;

import com.draker.swipetime.api.ApiConstants;
import com.draker.swipetime.database.entities.BookEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Локальный репозиторий для работы с книгами
 * Заменяет Google Books API, который имеет проблемы с доступом
 */
public class LocalBooksRepository {
    private static final String TAG = "LocalBooksRepository";
    private static final Random random = new Random();

    /**
     * Получить список популярных книг
     * @param page номер страницы
     * @return Observable со списком BookEntity
     */
    public Observable<List<BookEntity>> getPopularBooks(int page) {
        return Observable.fromCallable(() -> generateBooksList(page, "popular", null))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Поиск книг
     * @param query поисковый запрос
     * @param page номер страницы
     * @return Observable со списком BookEntity
     */
    public Observable<List<BookEntity>> searchBooks(String query, int page) {
        return Observable.fromCallable(() -> generateBooksList(page, "search", query))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Получить книги по категории
     * @param category категория
     * @param page номер страницы
     * @return Observable со списком BookEntity
     */
    public Observable<List<BookEntity>> getBooksByCategory(String category, int page) {
        return Observable.fromCallable(() -> generateBooksList(page, category, null))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Получить книги по автору
     * @param author автор
     * @param page номер страницы
     * @return Observable со списком BookEntity
     */
    public Observable<List<BookEntity>> getBooksByAuthor(String author, int page) {
        return Observable.fromCallable(() -> generateBooksList(page, "author", author))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Сгенерировать тестовый список книг
     * @param page номер страницы
     * @param mode режим ("popular", "search", "author" или категория)
     * @param query поисковый запрос (для режимов "search" и "author")
     * @return список BookEntity
     */
    private List<BookEntity> generateBooksList(int page, String mode, String query) {
        List<BookEntity> books = new ArrayList<>();
        int startIndex = (page - 1) * 20;
        
        // Примерное количество книг для возврата
        int count = Math.min(20, 100 - startIndex);
        if (count <= 0) {
            return books;
        }

        for (int i = 0; i < count; i++) {
            books.add(generateBook(startIndex + i, mode, query));
        }

        return books;
    }

    /**
     * Сгенерировать тестовую книгу
     * @param index индекс книги
     * @param mode режим
     * @param query поисковый запрос
     * @return BookEntity
     */
    private BookEntity generateBook(int index, String mode, String query) {
        // Список возможных авторов
        String[] authors = {
            "Стивен Кинг", "Дж.К. Роулинг", "Дж.Р.Р. Толкин", "Фёдор Достоевский", 
            "Агата Кристи", "Джордж Р.Р. Мартин", "Лев Толстой", "Виктор Пелевин",
            "Нил Гейман", "Харуки Мураками", "Энди Вейр", "Дэн Браун",
            "Антон Чехов", "Александр Пушкин", "Рэй Брэдбери", "Эрнест Хемингуэй",
            "Джейн Остин", "Габриэль Гарсия Маркес", "Михаил Булгаков", "Борис Акунин"
        };
        
        // Список возможных издательств
        String[] publishers = {
            "Эксмо", "АСТ", "Азбука", "Росмэн", "Альпина Паблишер",
            "МИФ", "Питер", "ОЛМА Медиа Групп", "РИПОЛ классик", "Азбука-Аттикус"
        };
        
        // Список возможных жанров
        String[] genres = {
            "Фантастика", "Фэнтези", "Детектив", "Триллер", "Роман", 
            "Научная фантастика", "Приключения", "Детская литература", 
            "Историческая проза", "Ужасы", "Биография", "Научно-популярная литература",
            "Классическая литература", "Современная проза", "Психология", "Бизнес-литература"
        };
        
        // Список возможных URL изображений обложек
        String[] imageUrls = {
            "https://m.media-amazon.com/images/I/71kxa1-0mfL._AC_UF1000,1000_QL80_.jpg",
            "https://m.media-amazon.com/images/I/81iqZ2HHD-L._AC_UF1000,1000_QL80_.jpg",
            "https://m.media-amazon.com/images/I/71jLBXtWJWL._AC_UF1000,1000_QL80_.jpg",
            "https://m.media-amazon.com/images/I/81sQQDaHYdL._AC_UF1000,1000_QL80_.jpg",
            "https://m.media-amazon.com/images/I/71z4Y3p3gKL._AC_UF1000,1000_QL80_.jpg",
            "https://m.media-amazon.com/images/I/91dSMhdIzTL._AC_UF1000,1000_QL80_.jpg",
            "https://m.media-amazon.com/images/I/71XWbXzOFCL._AC_UF1000,1000_QL80_.jpg",
            "https://m.media-amazon.com/images/I/71Q1tPupKjL._AC_UF1000,1000_QL80_.jpg",
            "https://m.media-amazon.com/images/I/81l3rZK4lnL._AC_UF1000,1000_QL80_.jpg",
            "https://m.media-amazon.com/images/I/91AF8bMDNAL._AC_UF1000,1000_QL80_.jpg",
            "https://m.media-amazon.com/images/I/81m2bQEbLRL._AC_UF1000,1000_QL80_.jpg",
            "https://m.media-amazon.com/images/I/91uwocAMtSL._AC_UF1000,1000_QL80_.jpg"
        };
        
        // Даты публикации (годы в диапазоне 1900-2023)
        int publishYear = 1900 + random.nextInt(124);
        
        // Количество страниц (50-1000)
        int pageCount = 50 + random.nextInt(951);
        
        // Уникальный ID для книги
        String id = "book-" + UUID.randomUUID().toString().substring(0, 8);
        
        // Выбор автора, издательства, жанра и обложки
        String author = authors[random.nextInt(authors.length)];
        String publisher = publishers[random.nextInt(publishers.length)];
        
        // Формирование списка жанров (1-3 жанра)
        int genreCount = 1 + random.nextInt(3);
        StringBuilder genresBuilder = new StringBuilder();
        for (int i = 0; i < genreCount; i++) {
            String genre = genres[random.nextInt(genres.length)];
            if (i > 0) {
                genresBuilder.append(", ");
            }
            genresBuilder.append(genre);
        }
        
        // URL изображения обложки
        String imageUrl = imageUrls[random.nextInt(imageUrls.length)];
        
        // Формирование заголовка и описания в зависимости от режима
        String title;
        String description;
        
        if ("search".equals(mode) && query != null && !query.isEmpty()) {
            // Для режима поиска включаем запрос в заголовок и описание
            title = "Книга о " + query + " (том " + (index + 1) + ")";
            description = "Увлекательное произведение, рассказывающее о " + query + ". " +
                    "Автор " + author + " приглашает читателя в удивительный мир, " +
                    "полный приключений и открытий. Издательство " + publisher + ", " +
                    publishYear + " год. Жанр: " + genresBuilder + ".";
        } else if ("author".equals(mode) && query != null && !query.isEmpty()) {
            // Для режима автора используем запрос как имя автора
            author = query;
            title = "Произведение " + author + " №" + (index + 1);
            description = "Одно из лучших произведений автора " + author + ". " +
                    "Издано " + publisher + " в " + publishYear + " году. " +
                    "Жанр: " + genresBuilder + ". Количество страниц: " + pageCount + ".";
        } else if ("popular".equals(mode)) {
            // Для популярных книг
            String[] popularTitles = {
                "Гарри Поттер и философский камень", "1984", "Преступление и наказание",
                "Мастер и Маргарита", "Война и мир", "Властелин колец", "Алхимик",
                "Маленький принц", "Три товарища", "Крёстный отец", "Сто лет одиночества",
                "Портрет Дориана Грея", "Унесенные ветром", "Шантарам", "Гордость и предубеждение",
                "Над пропастью во ржи", "Идиот", "Граф Монте-Кристо", "Анна Каренина", "Великий Гэтсби",
                "451 градус по Фаренгейту", "Старик и море", "Убить пересмешника",
                "Двенадцать стульев", "Маленькие женщины", "Зеленая миля", "Вокруг света за 80 дней"
            };
            
            title = popularTitles[index % popularTitles.length];
            description = "Бестселлер автора " + author + ". " +
                    "Книга, покорившая сердца миллионов читателей по всему миру. " +
                    "Издательство " + publisher + ", " + publishYear + " год. " +
                    "Жанр: " + genresBuilder + ". Количество страниц: " + pageCount + ".";
        } else {
            // Для книг по категории
            title = "Книга в жанре " + mode + " #" + (index + 1);
            description = "Захватывающее произведение в жанре " + mode + ". " +
                    "Автор " + author + " раскрывает все тайны и секреты. " +
                    "Издательство " + publisher + ", " + publishYear + " год. " +
                    "Количество страниц: " + pageCount + ".";
        }
        
        // Генерация ISBN (10 или 13 цифр)
        StringBuilder isbnBuilder = new StringBuilder();
        int isbnLength = random.nextBoolean() ? 10 : 13;
        for (int i = 0; i < isbnLength; i++) {
            isbnBuilder.append(random.nextInt(10));
        }
        String isbn = isbnBuilder.toString();
        
        // Создание и возврат объекта BookEntity
        return new BookEntity(
                id,
                title,
                description,
                imageUrl,
                author,
                publisher,
                publishYear,
                pageCount,
                genresBuilder.toString(),
                isbn
        );
    }
}