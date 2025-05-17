package com.draker.swipetime.api.models.tmdb;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Модель сериала из TMDB API
 */
public class TMDbTVShow {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("overview")
    private String overview;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("backdrop_path")
    private String backdropPath;

    @SerializedName("first_air_date")
    private String firstAirDate;

    @SerializedName("last_air_date")
    private String lastAirDate;

    @SerializedName("genre_ids")
    private List<Integer> genreIds;

    @SerializedName("genres")
    private List<TMDbGenre> genres;

    @SerializedName("vote_average")
    private float voteAverage;

    @SerializedName("vote_count")
    private int voteCount;

    @SerializedName("popularity")
    private float popularity;

    @SerializedName("number_of_seasons")
    private int numberOfSeasons;

    @SerializedName("number_of_episodes")
    private int numberOfEpisodes;

    @SerializedName("episode_run_time")
    private List<Integer> episodeRunTime;

    @SerializedName("status")
    private String status;

    @SerializedName("created_by")
    private List<TMDbCreator> createdBy;

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

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getFirstAirDate() {
        return firstAirDate;
    }

    public void setFirstAirDate(String firstAirDate) {
        this.firstAirDate = firstAirDate;
    }

    public String getLastAirDate() {
        return lastAirDate;
    }

    public void setLastAirDate(String lastAirDate) {
        this.lastAirDate = lastAirDate;
    }

    public List<Integer> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(List<Integer> genreIds) {
        this.genreIds = genreIds;
    }

    public List<TMDbGenre> getGenres() {
        return genres;
    }

    public void setGenres(List<TMDbGenre> genres) {
        this.genres = genres;
    }

    public float getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(float voteAverage) {
        this.voteAverage = voteAverage;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public float getPopularity() {
        return popularity;
    }

    public void setPopularity(float popularity) {
        this.popularity = popularity;
    }

    public int getNumberOfSeasons() {
        return numberOfSeasons;
    }

    public void setNumberOfSeasons(int numberOfSeasons) {
        this.numberOfSeasons = numberOfSeasons;
    }

    public int getNumberOfEpisodes() {
        return numberOfEpisodes;
    }

    public void setNumberOfEpisodes(int numberOfEpisodes) {
        this.numberOfEpisodes = numberOfEpisodes;
    }

    public List<Integer> getEpisodeRunTime() {
        return episodeRunTime;
    }

    public void setEpisodeRunTime(List<Integer> episodeRunTime) {
        this.episodeRunTime = episodeRunTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<TMDbCreator> getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(List<TMDbCreator> createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Получить среднюю длительность эпизода
     * @return средняя длительность эпизода в минутах
     */
    public int getAverageEpisodeRunTime() {
        if (episodeRunTime == null || episodeRunTime.isEmpty()) {
            return 40; // Стандартное значение
        }
        int sum = 0;
        for (Integer runTime : episodeRunTime) {
            sum += runTime;
        }
        return sum / episodeRunTime.size();
    }

    /**
     * Получить имя создателя
     * @return имя создателя сериала или пустая строка
     */
    public String getCreatorName() {
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
}
