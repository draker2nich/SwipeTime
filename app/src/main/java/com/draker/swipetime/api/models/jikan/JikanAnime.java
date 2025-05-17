package com.draker.swipetime.api.models.jikan;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Модель аниме из Jikan API
 */
public class JikanAnime {
    @SerializedName("mal_id")
    private int malId;

    @SerializedName("title")
    private String title;

    @SerializedName("title_english")
    private String titleEnglish;

    @SerializedName("title_japanese")
    private String titleJapanese;

    @SerializedName("synopsis")
    private String synopsis;

    @SerializedName("type")
    private String type;

    @SerializedName("source")
    private String source;

    @SerializedName("episodes")
    private Integer episodes;

    @SerializedName("status")
    private String status;

    @SerializedName("airing")
    private boolean airing;

    @SerializedName("aired")
    private JikanAired aired;

    @SerializedName("duration")
    private String duration;

    @SerializedName("rating")
    private String rating;

    @SerializedName("score")
    private float score;

    @SerializedName("scored_by")
    private int scoredBy;

    @SerializedName("rank")
    private int rank;

    @SerializedName("popularity")
    private int popularity;

    @SerializedName("members")
    private int members;

    @SerializedName("favorites")
    private int favorites;

    @SerializedName("images")
    private JikanImages images;

    @SerializedName("studios")
    private List<JikanStudio> studios;

    @SerializedName("genres")
    private List<JikanGenre> genres;

    public int getMalId() {
        return malId;
    }

    public void setMalId(int malId) {
        this.malId = malId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleEnglish() {
        return titleEnglish;
    }

    public void setTitleEnglish(String titleEnglish) {
        this.titleEnglish = titleEnglish;
    }

    public String getTitleJapanese() {
        return titleJapanese;
    }

    public void setTitleJapanese(String titleJapanese) {
        this.titleJapanese = titleJapanese;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getEpisodes() {
        return episodes;
    }

    public void setEpisodes(Integer episodes) {
        this.episodes = episodes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAiring() {
        return airing;
    }

    public void setAiring(boolean airing) {
        this.airing = airing;
    }

    public JikanAired getAired() {
        return aired;
    }

    public void setAired(JikanAired aired) {
        this.aired = aired;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public int getScoredBy() {
        return scoredBy;
    }

    public void setScoredBy(int scoredBy) {
        this.scoredBy = scoredBy;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public int getMembers() {
        return members;
    }

    public void setMembers(int members) {
        this.members = members;
    }

    public int getFavorites() {
        return favorites;
    }

    public void setFavorites(int favorites) {
        this.favorites = favorites;
    }

    public JikanImages getImages() {
        return images;
    }

    public void setImages(JikanImages images) {
        this.images = images;
    }

    public List<JikanStudio> getStudios() {
        return studios;
    }

    public void setStudios(List<JikanStudio> studios) {
        this.studios = studios;
    }

    public List<JikanGenre> getGenres() {
        return genres;
    }

    public void setGenres(List<JikanGenre> genres) {
        this.genres = genres;
    }

    /**
     * Получить студии в виде строки, разделенной запятыми
     * @return строка с названиями студий
     */
    public String getStudiosString() {
        if (studios == null || studios.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < studios.size(); i++) {
            result.append(studios.get(i).getName());
            if (i < studios.size() - 1) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /**
     * Получить жанры в виде строки, разделенной запятыми
     * @return строка с названиями жанров
     */
    public String getGenresString() {
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
     * Получить URL изображения
     * @return URL изображения или null
     */
    public String getImageUrl() {
        if (images != null && images.getJpg() != null) {
            return images.getJpg().getLargeImageUrl();
        }
        return null;
    }

    /**
     * Получить год выпуска
     * @return год выпуска или 0, если дата не указана
     */
    public int getReleaseYear() {
        if (aired != null && aired.getFrom() != null) {
            return aired.getFromYear();
        }
        return 0;
    }

    /**
     * Получить число эпизодов
     * @return число эпизодов или 0, если не указано
     */
    public int getEpisodesCount() {
        return episodes != null ? episodes : 0;
    }
}