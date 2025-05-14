package com.draker.swipetime.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.draker.swipetime.database.entities.BookEntity;

import java.util.List;

/**
 * DAO для операций с книгами
 */
@Dao
public interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BookEntity book);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BookEntity> books);

    @Update
    void update(BookEntity book);

    @Delete
    void delete(BookEntity book);

    @Query("DELETE FROM books WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM books WHERE id = :id")
    BookEntity getById(String id);

    @Query("SELECT * FROM books WHERE id = :id")
    LiveData<BookEntity> observeById(String id);

    @Query("SELECT * FROM books")
    List<BookEntity> getAll();

    @Query("SELECT * FROM books")
    LiveData<List<BookEntity>> observeAll();

    @Query("SELECT * FROM books WHERE liked = 1")
    List<BookEntity> getLiked();

    @Query("SELECT * FROM books WHERE liked = 1")
    LiveData<List<BookEntity>> observeLiked();

    @Query("SELECT * FROM books WHERE genres LIKE '%' || :genre || '%'")
    List<BookEntity> getByGenre(String genre);

    @Query("SELECT * FROM books WHERE genres LIKE '%' || :genre || '%'")
    LiveData<List<BookEntity>> observeByGenre(String genre);

    @Query("SELECT * FROM books WHERE author LIKE '%' || :author || '%'")
    List<BookEntity> getByAuthor(String author);

    @Query("SELECT * FROM books WHERE author LIKE '%' || :author || '%'")
    LiveData<List<BookEntity>> observeByAuthor(String author);

    @Query("SELECT * FROM books WHERE publisher LIKE '%' || :publisher || '%'")
    List<BookEntity> getByPublisher(String publisher);

    @Query("SELECT * FROM books WHERE publish_year = :year")
    List<BookEntity> getByPublishYear(int year);

    @Query("SELECT * FROM books WHERE isbn = :isbn")
    BookEntity getByIsbn(String isbn);

    @Query("SELECT * FROM books WHERE page_count <= :pageCount")
    List<BookEntity> getByMaxPageCount(int pageCount);

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<BookEntity> search(String query);

    @Query("DELETE FROM books")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM books")
    int getCount();

    @Query("SELECT COUNT(*) FROM books WHERE liked = 1")
    int getLikedCount();

    @Query("SELECT COUNT(*) FROM books WHERE genres LIKE '%' || :genre || '%'")
    int getCountByGenre(String genre);
}
