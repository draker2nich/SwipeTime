package com.draker.swipetime.api.models.googlebooks;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Модель информации о книге из Google Books API
 */
public class GoogleBookVolumeInfo {
    @SerializedName("title")
    private String title;

    @SerializedName("subtitle")
    private String subtitle;

    @SerializedName("authors")
    private List<String> authors;

    @SerializedName("publisher")
    private String publisher;

    @SerializedName("publishedDate")
    private String publishedDate;

    @SerializedName("description")
    private String description;

    @SerializedName("pageCount")
    private int pageCount;

    @SerializedName("categories")
    private List<String> categories;

    @SerializedName("averageRating")
    private float averageRating;

    @SerializedName("ratingsCount")
    private int ratingsCount;

    @SerializedName("imageLinks")
    private GoogleBookImageLinks imageLinks;

    @SerializedName("language")
    private String language;

    @SerializedName("industryIdentifiers")
    private List<GoogleBookIndustryIdentifier> industryIdentifiers;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public int getRatingsCount() {
        return ratingsCount;
    }

    public void setRatingsCount(int ratingsCount) {
        this.ratingsCount = ratingsCount;
    }

    public GoogleBookImageLinks getImageLinks() {
        return imageLinks;
    }

    public void setImageLinks(GoogleBookImageLinks imageLinks) {
        this.imageLinks = imageLinks;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<GoogleBookIndustryIdentifier> getIndustryIdentifiers() {
        return industryIdentifiers;
    }

    public void setIndustryIdentifiers(List<GoogleBookIndustryIdentifier> industryIdentifiers) {
        this.industryIdentifiers = industryIdentifiers;
    }

    /**
     * Получить авторов в виде строки, разделенной запятыми
     * @return строка с именами авторов
     */
    public String getAuthorsString() {
        if (authors == null || authors.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < authors.size(); i++) {
            result.append(authors.get(i));
            if (i < authors.size() - 1) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /**
     * Получить категории в виде строки, разделенной запятыми
     * @return строка с категориями
     */
    public String getCategoriesString() {
        if (categories == null || categories.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < categories.size(); i++) {
            result.append(categories.get(i));
            if (i < categories.size() - 1) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /**
     * Получить ISBN
     * @return ISBN или пустая строка
     */
    public String getIsbn() {
        if (industryIdentifiers == null || industryIdentifiers.isEmpty()) {
            return "";
        }

        for (GoogleBookIndustryIdentifier identifier : industryIdentifiers) {
            if ("ISBN_13".equals(identifier.getType())) {
                return identifier.getIdentifier();
            } else if ("ISBN_10".equals(identifier.getType())) {
                return identifier.getIdentifier();
            }
        }

        return "";
    }
    
    /**
     * Получить год публикации
     * @return год публикации или 0, если дата не указана
     */
    public int getPublishYear() {
        if (publishedDate == null || publishedDate.isEmpty()) {
            return 0;
        }
        
        // Обработка разных форматов даты
        try {
            // Обработка полной даты: YYYY-MM-DD
            if (publishedDate.length() >= 4) {
                return Integer.parseInt(publishedDate.substring(0, 4));
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
}