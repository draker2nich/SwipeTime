package com.draker.swipetime.api.models.tmdb;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Модель актерского состава и съемочной группы из TMDB API
 */
public class TMDbCredits {
    @SerializedName("id")
    private int id;

    @SerializedName("cast")
    private List<TMDbCast> cast;

    @SerializedName("crew")
    private List<TMDbCrew> crew;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<TMDbCast> getCast() {
        return cast;
    }

    public void setCast(List<TMDbCast> cast) {
        this.cast = cast;
    }

    public List<TMDbCrew> getCrew() {
        return crew;
    }
    
    public void setCrew(List<TMDbCrew> crew) {
        this.crew = crew;
    }
    
    /**
     * Получить режиссера фильма
     * @return имя режиссера или пустая строка
     */
    public String getDirector() {
        if (crew == null || crew.isEmpty()) {
            return "";
        }
        
        for (TMDbCrew member : crew) {
            if ("Director".equals(member.getJob())) {
                return member.getName();
            }
        }
        
        return "";
    }
}