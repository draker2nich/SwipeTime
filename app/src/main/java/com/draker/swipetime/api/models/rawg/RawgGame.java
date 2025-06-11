package com.draker.swipetime.api.models.rawg;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Модель игры из RAWG API
 * Все вспомогательные классы встроены как внутренние
 */
public class RawgGame {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("description_raw")
    private String descriptionRaw;

    @SerializedName("background_image")
    private String backgroundImage;

    @SerializedName("released")
    private String released;

    @SerializedName("rating")
    private float rating;

    @SerializedName("esrb_rating")
    private SimpleItem esrbRating;

    @SerializedName("platforms")
    private List<PlatformWrapper> platforms;

    @SerializedName("genres")
    private List<SimpleItem> genres;

    @SerializedName("developers")
    private List<SimpleItem> developers;

    @SerializedName("publishers")
    private List<SimpleItem> publishers;

    // Конструктор
    public RawgGame() {}

    // Основные геттеры
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescriptionRaw() { return descriptionRaw; }
    public String getBackgroundImage() { return backgroundImage; }
    public String getReleased() { return released; }
    public float getRating() { return rating; }

    // Utility методы для получения строк
    public String getDeveloperNames() {
        return joinSimpleItems(developers);
    }

    public String getPublisherNames() {
        return joinSimpleItems(publishers);
    }

    public String getPlatformNames() {
        if (platforms == null || platforms.isEmpty()) return "";

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < platforms.size(); i++) {
            String name = platforms.get(i).getPlatformName();
            if (name != null && !name.isEmpty()) {
                result.append(name);
                if (i < platforms.size() - 1) result.append(", ");
            }
        }
        return result.toString();
    }

    public String getGenreNames() {
        return joinSimpleItems(genres);
    }

    public String getEsrbRatingName() {
        return esrbRating != null ? esrbRating.name : "Not Rated";
    }

    public int getReleaseYear() {
        if (released == null || released.length() < 4) return 0;
        try {
            return Integer.parseInt(released.substring(0, 4));
        } catch (Exception e) {
            return 0;
        }
    }

    // Приватный utility метод
    private String joinSimpleItems(List<SimpleItem> items) {
        if (items == null || items.isEmpty()) return "";

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).name != null) {
                result.append(items.get(i).name);
                if (i < items.size() - 1) result.append(", ");
            }
        }
        return result.toString();
    }

    /**
     * Универсальный класс для простых элементов (жанр, разработчик, издатель, рейтинг)
     */
    public static class SimpleItem {
        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;
    }

    /**
     * Обертка для платформы
     */
    public static class PlatformWrapper {
        @SerializedName("platform")
        private SimpleItem platform;

        public String getPlatformName() {
            return platform != null ? platform.name : "";
        }
    }
}

