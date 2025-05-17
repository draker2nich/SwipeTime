package com.draker.swipetime.api.models.tmdb;

import com.google.gson.annotations.SerializedName;

/**
 * Модель члена съемочной группы из TMDB API
 */
public class TMDbCrew {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("job")
    private String job;

    @SerializedName("department")
    private String department;

    @SerializedName("profile_path")
    private String profilePath;

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

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }
}