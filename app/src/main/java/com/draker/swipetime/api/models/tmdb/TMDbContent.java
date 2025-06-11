package com.draker.swipetime.api.models.tmdb;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Объединенная модель для фильмов и сериалов TMDb
 * Заменяет TMDbMovie.java и TMDbTVShow.java
 */
public class TMDbContent {
    public enum ContentType {
        MOVIE, TV_SHOW
    }

    @SerializedName("id")
    private int id;

    // Название - title для фильмов, name для сериалов
    @SerializedName("title")
    private String title;

    @SerializedName("name")
    private String name;

    @SerializedName("overview")
    private String overview;

    @SerializedName("poster_path")
    private String posterPath;

    // Дата выхода - release_date для фильмов, first_air_date для сериалов
    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("first_air_date")
    private String firstAirDate;

    @SerializedName("genre_ids")
    private List<Integer> genreIds;

    @SerializedName("genres")
    private List<TMDbGenre> genres;

    @SerializedName("vote_average")
    private float voteAverage;

    // Специфичные поля для фильмов
    @SerializedName("runtime")
    private Integer runtime;

    // Специфичные поля для сериалов
    @SerializedName("number_of_seasons")
    private Integer numberOfSeasons;

    @SerializedName("number_of_episodes")
    private Integer numberOfEpisodes;

    @SerializedName("episode_run_time")
    private List<Integer> episodeRunTime;

    @SerializedName("created_by")
    private List<TMDbCreator> createdBy;

    // Поле для режиссера (заполняется из TMDbCredits)
    private String director;

    // Тип контента (устанавливается программно)
    private ContentType contentType;

    // Основные геттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    /**
     * Получить название контента (универсально для фильмов и сериалов)
     */
    public String getTitle() {
        if (contentType == ContentType.TV_SHOW) {
            return name != null ? name : title;
        }
        return title != null ? title : name;
    }

    public void setTitle(String title) { this.title = title; }
    public void setName(String name) { this.name = name; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    /**
     * Получить дату выхода (универсально для фильмов и сериалов)
     */
    public String getReleaseDate() {
        if (contentType == ContentType.TV_SHOW) {
            return firstAirDate != null ? firstAirDate : releaseDate;
        }
        return releaseDate != null ? releaseDate : firstAirDate;
    }

    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public void setFirstAirDate(String firstAirDate) { this.firstAirDate = firstAirDate; }

    public List<Integer> getGenreIds() { return genreIds; }
    public void setGenreIds(List<Integer> genreIds) { this.genreIds = genreIds; }

    public List<TMDbGenre> getGenres() { return genres; }
    public void setGenres(List<TMDbGenre> genres) { this.genres = genres; }

    public float getVoteAverage() { return voteAverage; }
    public void setVoteAverage(float voteAverage) { this.voteAverage = voteAverage; }

    // Геттеры для фильмов
    public Integer getRuntime() { return runtime; }
    public void setRuntime(Integer runtime) { this.runtime = runtime; }

    // Геттеры для сериалов
    public Integer getNumberOfSeasons() { return numberOfSeasons; }
    public void setNumberOfSeasons(Integer numberOfSeasons) { this.numberOfSeasons = numberOfSeasons; }

    public Integer getNumberOfEpisodes() { return numberOfEpisodes; }
    public void setNumberOfEpisodes(Integer numberOfEpisodes) { this.numberOfEpisodes = numberOfEpisodes; }

    public List<Integer> getEpisodeRunTime() { return episodeRunTime; }
    public void setEpisodeRunTime(List<Integer> episodeRunTime) { this.episodeRunTime = episodeRunTime; }

    public List<TMDbCreator> getCreatedBy() { return createdBy; }
    public void setCreatedBy(List<TMDbCreator> createdBy) { this.createdBy = createdBy; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public ContentType getContentType() { return contentType; }
    public void setContentType(ContentType contentType) { this.contentType = contentType; }

    // Утилитарные методы

    /**
     * Получить год выхода как число
     */
    public int getYear() {
        String date = getReleaseDate();
        if (date != null && date.length() >= 4) {
            try {
                return Integer.parseInt(date.substring(0, 4));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Получить строку с жанрами
     */
    public String getGenresString() {
        if (genres == null || genres.isEmpty()) {
            return "";
        }
        StringBuilder genresBuilder = new StringBuilder();
        for (int i = 0; i < genres.size(); i++) {
            genresBuilder.append(genres.get(i).getName());
            if (i < genres.size() - 1) {
                genresBuilder.append(", ");
            }
        }
        return genresBuilder.toString();
    }

    /**
     * Получить среднюю длительность эпизода для сериалов
     */
    public int getAverageEpisodeRunTime() {
        if (episodeRunTime == null || episodeRunTime.isEmpty()) {
            return 40;
        }
        int sum = 0;
        for (Integer runTime : episodeRunTime) {
            if (runTime != null) {
                sum += runTime;
            }
        }
        return episodeRunTime.size() > 0 ? sum / episodeRunTime.size() : 40;
    }

    /**
     * Получить имена создателей сериала
     */
    public String getCreatorNames() {
        if (createdBy == null || createdBy.isEmpty()) {
            return "";
        }
        StringBuilder creators = new StringBuilder();
        for (int i = 0; i < createdBy.size(); i++) {
            creators.append(createdBy.get(i).getName());
            if (i < createdBy.size() - 1) {
                creators.append(", ");
            }
        }
        return creators.toString();
    }

    /**
     * Проверка валидности данных
     */
    public boolean isValid() {
        return id > 0 && getTitle() != null && !getTitle().trim().isEmpty();
    }

    // =========================
    // ВСТРОЕННЫЕ КЛАССЫ
    // =========================

    /**
     * Встроенная модель жанра
     * Заменяет TMDbGenre.java
     */
    public static class TMDbGenre {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        public TMDbGenre() {}
        public TMDbGenre(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @Override
        public String toString() { return name != null ? name : ""; }
        public boolean isValid() { return id > 0 && name != null && !name.trim().isEmpty(); }
    }

    /**
     * Встроенная модель создателя сериала
     * Заменяет TMDbCreator.java
     */
    public static class TMDbCreator {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        public TMDbCreator() {}
        public TMDbCreator(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @Override
        public String toString() { return name != null ? name : ""; }
        public boolean isValid() { return id > 0 && name != null && !name.trim().isEmpty(); }
    }
}