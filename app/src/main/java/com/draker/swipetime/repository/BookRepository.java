package com.draker.swipetime.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.BookDao;
import com.draker.swipetime.database.entities.BookEntity;

import java.util.List;

/**
 * Репозиторий для работы с книгами
 */
public class BookRepository {

    private BookDao bookDao;

    public BookRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        bookDao = db.bookDao();
    }

    /**
     * Добавить новую книгу
     * @param book книга
     */
    public void insert(BookEntity book) {
        bookDao.insert(book);
    }

    /**
     * Добавить несколько книг
     * @param books список книг
     */
    public void insertAll(List<BookEntity> books) {
        bookDao.insertAll(books);
    }

    /**
     * Обновить книгу
     * @param book книга
     */
    public void update(BookEntity book) {
        bookDao.update(book);
    }

    /**
     * Удалить книгу
     * @param book книга
     */
    public void delete(BookEntity book) {
        bookDao.delete(book);
    }
    
    /**
     * Удалить книгу по ID
     * @param id ID книги
     */
    public void deleteById(String id) {
        bookDao.deleteById(id);
    }
    
    /**
     * Удалить все книги
     */
    public void deleteAll() {
        bookDao.deleteAll();
    }

    /**
     * Получить книгу по ID
     * @param id ID книги
     * @return книга или null, если не найдена
     */
    public BookEntity getById(String id) {
        return bookDao.getById(id);
    }

    /**
     * Получить все книги
     * @return список всех книг
     */
    public List<BookEntity> getAll() {
        return bookDao.getAll();
    }

    /**
     * Получить список понравившихся книг
     * @return список книг с отметкой liked=true
     */
    public List<BookEntity> getLiked() {
        return bookDao.getLiked();
    }

    /**
     * Обновить статус "понравилось" для книги
     * @param id ID книги
     * @param liked новый статус
     */
    public void updateLikedStatus(String id, boolean liked) {
        bookDao.updateLikedStatus(id, liked);
    }

    /**
     * Обновить статус "прочитано" для книги
     * @param id ID книги
     * @param read новый статус
     */
    public void updateReadStatus(String id, boolean read) {
        bookDao.updateReadStatus(id, read);
    }

    /**
     * Получить список прочитанных книг
     * @return список книг с отметкой read=true
     */
    public List<BookEntity> getRead() {
        return bookDao.getRead();
    }
    
    /**
     * Получить книги по жанру
     * @param genre жанр
     * @return список книг указанного жанра
     */
    public List<BookEntity> getByGenre(String genre) {
        return bookDao.getByGenre(genre);
    }
    
    /**
     * Получить книги по автору
     * @param author автор
     * @return список книг указанного автора
     */
    public List<BookEntity> getByAuthor(String author) {
        return bookDao.getByAuthor(author);
    }
    
    /**
     * Получить книги по издателю
     * @param publisher издатель
     * @return список книг указанного издателя
     */
    public List<BookEntity> getByPublisher(String publisher) {
        return bookDao.getByPublisher(publisher);
    }
    
    /**
     * Получить книги по году издания
     * @param year год издания
     * @return список книг указанного года
     */
    public List<BookEntity> getByPublishYear(int year) {
        return bookDao.getByPublishYear(year);
    }
    
    /**
     * Получить книгу по ISBN
     * @param isbn ISBN
     * @return книга или null, если не найдена
     */
    public BookEntity getByIsbn(String isbn) {
        return bookDao.getByIsbn(isbn);
    }
    
    /**
     * Получить книги с ограничением по количеству страниц
     * @param pageCount максимальное количество страниц
     * @return список книг с количеством страниц не более указанного
     */
    public List<BookEntity> getByMaxPageCount(int pageCount) {
        return bookDao.getByMaxPageCount(pageCount);
    }

    /**
     * Наблюдать за всеми книгами (LiveData)
     * @return LiveData со списком всех книг
     */
    public LiveData<List<BookEntity>> observeAll() {
        return bookDao.observeAll();
    }
    
    /**
     * Наблюдать за понравившимися книгами (LiveData)
     * @return LiveData со списком понравившихся книг
     */
    public LiveData<List<BookEntity>> observeLiked() {
        return bookDao.observeLiked();
    }

    /**
     * Наблюдать за прочитанными книгами (LiveData)
     * @return LiveData со списком прочитанных книг
     */
    public LiveData<List<BookEntity>> observeRead() {
        return bookDao.observeRead();
    }
    
    /**
     * Поиск книг по запросу
     * @param query поисковый запрос
     * @return список книг, соответствующих запросу
     */
    public List<BookEntity> search(String query) {
        return bookDao.search(query);
    }
    
    /**
     * Получить количество всех книг
     * @return количество книг
     */
    public int getCount() {
        return bookDao.getCount();
    }
    
    /**
     * Получить количество понравившихся книг
     * @return количество понравившихся книг
     */
    public int getLikedCount() {
        return bookDao.getLikedCount();
    }
    
    /**
     * Получить количество прочитанных книг
     * @return количество прочитанных книг
     */
    public int getReadCount() {
        return bookDao.getReadCount();
    }
    
    /**
     * Получить количество книг по жанру
     * @param genre жанр
     * @return количество книг указанного жанра
     */
    public int getCountByGenre(String genre) {
        return bookDao.getCountByGenre(genre);
    }
    
    /**
     * Наблюдать за книгой по ID (LiveData)
     * @param id ID книги
     * @return LiveData с книгой
     */
    public LiveData<BookEntity> observeById(String id) {
        return bookDao.observeById(id);
    }
    
    /**
     * Наблюдать за книгами по жанру (LiveData)
     * @param genre жанр
     * @return LiveData со списком книг указанного жанра
     */
    public LiveData<List<BookEntity>> observeByGenre(String genre) {
        return bookDao.observeByGenre(genre);
    }
    
    /**
     * Наблюдать за книгами по автору (LiveData)
     * @param author автор
     * @return LiveData со списком книг указанного автора
     */
    public LiveData<List<BookEntity>> observeByAuthor(String author) {
        return bookDao.observeByAuthor(author);
    }
}