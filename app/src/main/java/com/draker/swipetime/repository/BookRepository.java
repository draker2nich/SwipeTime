package com.draker.swipetime.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.draker.swipetime.database.AppDatabase;
import com.draker.swipetime.database.dao.BookDao;
import com.draker.swipetime.database.entities.BookEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Репозиторий для работы с книгами
 */
public class BookRepository {

    private final BookDao bookDao;
    private final Executor executor;

    public BookRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        bookDao = db.bookDao();
        executor = Executors.newSingleThreadExecutor();
    }

    // Методы для работы с книгами в фоновом потоке

    public void insert(BookEntity book) {
        executor.execute(() -> bookDao.insert(book));
    }

    public void insertAll(List<BookEntity> books) {
        executor.execute(() -> bookDao.insertAll(books));
    }

    public void update(BookEntity book) {
        executor.execute(() -> bookDao.update(book));
    }

    public void delete(BookEntity book) {
        executor.execute(() -> bookDao.delete(book));
    }

    public void deleteById(String id) {
        executor.execute(() -> bookDao.deleteById(id));
    }

    public void deleteAll() {
        executor.execute(bookDao::deleteAll);
    }

    // Синхронные методы для получения данных

    public BookEntity getById(String id) {
        return bookDao.getById(id);
    }

    public List<BookEntity> getAll() {
        return bookDao.getAll();
    }

    public List<BookEntity> getLiked() {
        return bookDao.getLiked();
    }

    public List<BookEntity> getByGenre(String genre) {
        return bookDao.getByGenre(genre);
    }

    public List<BookEntity> getByAuthor(String author) {
        return bookDao.getByAuthor(author);
    }

    public List<BookEntity> getByPublisher(String publisher) {
        return bookDao.getByPublisher(publisher);
    }

    public List<BookEntity> getByPublishYear(int year) {
        return bookDao.getByPublishYear(year);
    }

    public BookEntity getByIsbn(String isbn) {
        return bookDao.getByIsbn(isbn);
    }

    public List<BookEntity> getByMaxPageCount(int pageCount) {
        return bookDao.getByMaxPageCount(pageCount);
    }

    public List<BookEntity> search(String query) {
        return bookDao.search(query);
    }

    public int getCount() {
        return bookDao.getCount();
    }

    public int getLikedCount() {
        return bookDao.getLikedCount();
    }

    public int getCountByGenre(String genre) {
        return bookDao.getCountByGenre(genre);
    }

    // LiveData методы для наблюдения за данными

    public LiveData<BookEntity> observeById(String id) {
        return bookDao.observeById(id);
    }

    public LiveData<List<BookEntity>> observeAll() {
        return bookDao.observeAll();
    }

    public LiveData<List<BookEntity>> observeLiked() {
        return bookDao.observeLiked();
    }

    public LiveData<List<BookEntity>> observeByGenre(String genre) {
        return bookDao.observeByGenre(genre);
    }

    public LiveData<List<BookEntity>> observeByAuthor(String author) {
        return bookDao.observeByAuthor(author);
    }
}
