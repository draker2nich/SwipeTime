package com.draker.swipetime.api.models.rawg;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Модель игры из RAWG API
 */
public class RawgGame {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("description_raw")
    private String descriptionRaw;

    @SerializedName("background_image")
    private String backgroundImage;

    @SerializedName("released")
    private String released;

    @SerializedName("rating")
    private float rating;

    @SerializedName("ratings_count")
    private int ratingsCount;

    @SerializedName("esrb_rating")
    private RawgESRBRating esrbRating;

    @SerializedName("platforms")
    private List<RawgPlatformWrapper> platforms;

    @SerializedName("genres")
    private List<RawgGenre> genres;

    @SerializedName("developers")
    private List<RawgDeveloper> developers;

    @SerializedName("publishers")
    private List<RawgPublisher> publishers;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionRaw() {
        return descriptionRaw;
    }

    public void setDescriptionRaw(String descriptionRaw) {
        this.descriptionRaw = descriptionRaw;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public String getReleased() {
        return released;
    }

    public void setReleased(String released) {
        this.released = released;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getRatingsCount() {
        return ratingsCount;
    }

    public void setRatingsCount(int ratingsCount) {
        this.ratingsCount = ratingsCount;
    }

    public RawgESRBRating getEsrbRating() {
        return esrbRating;
    }

    public void setEsrbRating(RawgESRBRating esrbRating) {
        this.esrbRating = esrbRating;
    }

    public List<RawgPlatformWrapper> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<RawgPlatformWrapper> platforms) {
        this.platforms = platforms;
    }

    public List<RawgGenre> getGenres() {
        return genres;
    }

    public void setGenres(List<RawgGenre> genres) {
        this.genres = genres;
    }

    public List<RawgDeveloper> getDevelopers() {
        return developers;
    }

    public void setDevelopers(List<RawgDeveloper> developers) {
        this.developers = developers;
    }

    public List<RawgPublisher> getPublishers() {
        return publishers;
    }

    public void setPublishers(List<RawgPublisher> publishers) {
        this.publishers = publishers;
    }

    /**
     * Получить имена разработчиков в виде строки, разделенной запятыми
     * @return строка с именами разработчиков
     */
    public String getDeveloperNames() {
        if (developers == null || developers.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < developers.size(); i++) {
            result.append(developers.get(i).getName());
            if (i < developers.size() - 1) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /**
     * Получить имена издателей в виде строки, разделенной запятыми
     * @return строка с именами издателей
     */
    public String getPublisherNames() {
        if (publishers == null || publishers.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < publishers.size(); i++) {
            result.append(publishers.get(i).getName());
            if (i < publishers.size() - 1) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /**
     * Получить платформы в виде строки, разделенной запятыми
     * @return строка с названиями платформ
     */
    public String getPlatformNames() {
        if (platforms == null || platforms.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < platforms.size(); i++) {
            result.append(platforms.get(i).getPlatform().getName());
            if (i < platforms.size() - 1) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /**
     * Получить жанры в виде строки, разделенной запятыми
     * @return строка с названиями жанров
     */
    public String getGenreNames() {
        if (genres == null || genres.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < genres.size(); i++) {
            result.append(genres.get(i).getName());
            if (i < genres.size() - 1) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /**
     * Получить возрастной рейтинг
     * @return строка с возрастным рейтингом или "Not Rated"
     */
    public String getEsrbRatingName() {
        if (esrbRating == null) {
            return "Not Rated";
        }
        return esrbRating.getName();
    }

    /**
     * Получить год выпуска из даты
     * @return год выпуска или 0, если дата не указана
     */
    public int getReleaseYear() {
        if (released == null || released.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(released.substring(0, 4));
        } catch (Exception e) {
            return 0;
        }
    }
}