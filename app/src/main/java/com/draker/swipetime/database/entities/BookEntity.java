package com.draker.swipetime.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

/**
 * Сущность для книг
 */
@Entity(tableName = "books")
public class BookEntity extends ContentEntity {

    @ColumnInfo(name = "author")
    private String author;

    @ColumnInfo(name = "publisher")
    private String publisher;

    @ColumnInfo(name = "publish_year")
    private int publishYear;

    @ColumnInfo(name = "page_count")
    private int pageCount;

    @ColumnInfo(name = "genres")
    private String genres;

    @ColumnInfo(name = "isbn")
    private String isbn;
    
    @ColumnInfo(name = "is_read")
    private boolean read;

    public BookEntity() {
        super();
        setContentType("book");
    }

    @Ignore
    public BookEntity(String id, String title, String description, String imageUrl,
                      String author, String publisher, int publishYear, 
                      int pageCount, String genres, String isbn) {
        super(id, title, description, imageUrl, "Книги", "book");
        this.author = author;
        this.publisher = publisher;
        this.publishYear = publishYear;
        this.pageCount = pageCount;
        this.genres = genres;
        this.isbn = isbn;
        this.read = false;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
        setUpdatedAt(System.currentTimeMillis());
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
        setUpdatedAt(System.currentTimeMillis());
    }

    public int getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(int publishYear) {
        this.publishYear = publishYear;
        setUpdatedAt(System.currentTimeMillis());
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
        setUpdatedAt(System.currentTimeMillis());
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
        setUpdatedAt(System.currentTimeMillis());
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
        setUpdatedAt(System.currentTimeMillis());
    }
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
        setUpdatedAt(System.currentTimeMillis());
    }
}