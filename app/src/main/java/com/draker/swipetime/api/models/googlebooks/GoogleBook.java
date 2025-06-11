package com.draker.swipetime.api.models.googlebooks;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Упрощенная модель книги из Google Books API
 * Объединяет всю необходимую информацию в одном классе
 */
public class GoogleBook {
    @SerializedName("id")
    private String id;

    // Основная информация о книге (из VolumeInfo)
    @SerializedName("volumeInfo")
    private VolumeInfo volumeInfo;

    // Информация о продаже (упрощенная)
    @SerializedName("saleInfo")
    private SaleInfo saleInfo;

    // Вложенный класс для volumeInfo
    public static class VolumeInfo {
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
        private ImageLinks imageLinks;

        @SerializedName("language")
        private String language;

        @SerializedName("industryIdentifiers")
        private List<IndustryIdentifier> industryIdentifiers;

        // Геттеры и сеттеры
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getSubtitle() { return subtitle; }
        public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

        public List<String> getAuthors() { return authors; }
        public void setAuthors(List<String> authors) { this.authors = authors; }

        public String getPublisher() { return publisher; }
        public void setPublisher(String publisher) { this.publisher = publisher; }

        public String getPublishedDate() { return publishedDate; }
        public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public int getPageCount() { return pageCount; }
        public void setPageCount(int pageCount) { this.pageCount = pageCount; }

        public List<String> getCategories() { return categories; }
        public void setCategories(List<String> categories) { this.categories = categories; }

        public float getAverageRating() { return averageRating; }
        public void setAverageRating(float averageRating) { this.averageRating = averageRating; }

        public int getRatingsCount() { return ratingsCount; }
        public void setRatingsCount(int ratingsCount) { this.ratingsCount = ratingsCount; }

        public ImageLinks getImageLinks() { return imageLinks; }
        public void setImageLinks(ImageLinks imageLinks) { this.imageLinks = imageLinks; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public List<IndustryIdentifier> getIndustryIdentifiers() { return industryIdentifiers; }
        public void setIndustryIdentifiers(List<IndustryIdentifier> industryIdentifiers) {
            this.industryIdentifiers = industryIdentifiers;
        }

        /**
         * Получить авторов в виде строки, разделенной запятыми
         */
        public String getAuthorsString() {
            if (authors == null || authors.isEmpty()) {
                return "Неизвестный автор";
            }
            return String.join(", ", authors);
        }

        /**
         * Получить категории в виде строки, разделенной запятыми
         */
        public String getCategoriesString() {
            if (categories == null || categories.isEmpty()) {
                return "Без категории";
            }
            return String.join(", ", categories);
        }

        /**
         * Получить год публикации
         */
        public int getPublishYear() {
            if (publishedDate == null || publishedDate.isEmpty()) {
                return 0;
            }
            try {
                if (publishedDate.length() >= 4) {
                    return Integer.parseInt(publishedDate.substring(0, 4));
                }
            } catch (NumberFormatException e) {
                return 0;
            }
            return 0;
        }

        /**
         * Получить ISBN
         */
        public String getIsbn() {
            if (industryIdentifiers == null || industryIdentifiers.isEmpty()) {
                return "";
            }

            for (IndustryIdentifier identifier : industryIdentifiers) {
                if ("ISBN_13".equals(identifier.getType())) {
                    return identifier.getIdentifier();
                } else if ("ISBN_10".equals(identifier.getType())) {
                    return identifier.getIdentifier();
                }
            }
            return "";
        }

        /**
         * Получить URL изображения обложки
         */
        public String getImageUrl() {
            if (imageLinks == null) {
                return null;
            }
            return imageLinks.getBestAvailableImage();
        }
    }

    // Упрощенный класс для изображений
    public static class ImageLinks {
        @SerializedName("smallThumbnail")
        private String smallThumbnail;

        @SerializedName("thumbnail")
        private String thumbnail;

        @SerializedName("small")
        private String small;

        @SerializedName("medium")
        private String medium;

        @SerializedName("large")
        private String large;

        @SerializedName("extraLarge")
        private String extraLarge;

        /**
         * Получить лучшее доступное изображение
         */
        public String getBestAvailableImage() {
            String imageUrl = null;

            // Приоритет: extraLarge > large > medium > small > thumbnail > smallThumbnail
            if (extraLarge != null && !extraLarge.isEmpty()) {
                imageUrl = extraLarge;
            } else if (large != null && !large.isEmpty()) {
                imageUrl = large;
            } else if (medium != null && !medium.isEmpty()) {
                imageUrl = medium;
            } else if (small != null && !small.isEmpty()) {
                imageUrl = small;
            } else if (thumbnail != null && !thumbnail.isEmpty()) {
                imageUrl = thumbnail;
            } else if (smallThumbnail != null && !smallThumbnail.isEmpty()) {
                imageUrl = smallThumbnail;
            }

            // Заменяем http на https для безопасности
            if (imageUrl != null && imageUrl.startsWith("http://")) {
                imageUrl = "https://" + imageUrl.substring(7);
            }

            // Убираем ограничения размера
            if (imageUrl != null && imageUrl.contains("&zoom=1")) {
                imageUrl = imageUrl.replace("&zoom=1", "");
            }

            return imageUrl;
        }

        // Геттеры и сеттеры для совместимости
        public String getSmallThumbnail() { return smallThumbnail; }
        public String getThumbnail() { return thumbnail; }
        public String getSmall() { return small; }
        public String getMedium() { return medium; }
        public String getLarge() { return large; }
        public String getExtraLarge() { return extraLarge; }
    }

    // Упрощенный класс для идентификаторов
    public static class IndustryIdentifier {
        @SerializedName("type")
        private String type;

        @SerializedName("identifier")
        private String identifier;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
    }

    // Упрощенный класс для информации о продаже
    public static class SaleInfo {
        @SerializedName("country")
        private String country;

        @SerializedName("saleability")
        private String saleability;

        @SerializedName("isEbook")
        private boolean isEbook;

        public String getCountry() { return country; }
        public String getSaleability() { return saleability; }
        public boolean isEbook() { return isEbook; }
    }

    // Основные геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public VolumeInfo getVolumeInfo() { return volumeInfo; }
    public void setVolumeInfo(VolumeInfo volumeInfo) { this.volumeInfo = volumeInfo; }

    public SaleInfo getSaleInfo() { return saleInfo; }
    public void setSaleInfo(SaleInfo saleInfo) { this.saleInfo = saleInfo; }

    // Удобные методы для быстрого доступа к данным
    public String getTitle() {
        return volumeInfo != null ? volumeInfo.getTitle() : null;
    }

    public String getDescription() {
        return volumeInfo != null ? volumeInfo.getDescription() : null;
    }

    public String getImageUrl() {
        return volumeInfo != null ? volumeInfo.getImageUrl() : null;
    }

    public String getAuthorsString() {
        return volumeInfo != null ? volumeInfo.getAuthorsString() : "Неизвестный автор";
    }

    public String getCategoriesString() {
        return volumeInfo != null ? volumeInfo.getCategoriesString() : "Без категории";
    }

    public int getPublishYear() {
        return volumeInfo != null ? volumeInfo.getPublishYear() : 0;
    }

    public float getAverageRating() {
        return volumeInfo != null ? volumeInfo.getAverageRating() : 0f;
    }

    public int getPageCount() {
        return volumeInfo != null ? volumeInfo.getPageCount() : 0;
    }

    public String getPublisher() {
        return volumeInfo != null ? volumeInfo.getPublisher() : null;
    }

    public String getIsbn() {
        return volumeInfo != null ? volumeInfo.getIsbn() : "";
    }
}