package com.draker.swipetime.api.models.tmdb;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Упрощенная модель для получения информации о создателях контента
 * Заменяет старые TMDbCredits.java и TMDbCrew.java
 * Оптимизирована только для получения режиссера
 */
public class TMDbCredits {
    @SerializedName("id")
    private int id;

    @SerializedName("crew")
    private List<TMDbCrew> crew;

    public TMDbCredits() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public List<TMDbCrew> getCrew() { return crew; }
    public void setCrew(List<TMDbCrew> crew) { this.crew = crew; }

    /**
     * Получить имя режиссера
     * @return имя режиссера или пустая строка если не найден
     */
    public String getDirector() {
        if (crew == null || crew.isEmpty()) {
            return "";
        }

        for (TMDbCrew member : crew) {
            if (member != null && "Director".equals(member.getJob())) {
                return member.getName() != null ? member.getName() : "";
            }
        }

        return "";
    }

    /**
     * Получить всех режиссеров (если их несколько)
     * @return строка с именами режиссеров через запятую
     */
    public String getAllDirectors() {
        if (crew == null || crew.isEmpty()) {
            return "";
        }

        StringBuilder directors = new StringBuilder();
        for (TMDbCrew member : crew) {
            if (member != null && "Director".equals(member.getJob()) && member.getName() != null) {
                if (directors.length() > 0) {
                    directors.append(", ");
                }
                directors.append(member.getName());
            }
        }

        return directors.toString();
    }

    /**
     * Проверка наличия режиссера
     */
    public boolean hasDirector() {
        return !getDirector().isEmpty();
    }

    // =========================
    // ВСТРОЕННЫЙ КЛАСС TMDbCrew
    // =========================

    /**
     * Встроенная упрощенная модель члена съемочной группы
     * Заменяет TMDbCrew.java
     * Содержит только необходимые поля для получения режиссера
     */
    public static class TMDbCrew {
        @SerializedName("name")
        private String name;

        @SerializedName("job")
        private String job;

        public TMDbCrew() {}
        public TMDbCrew(String name, String job) {
            this.name = name;
            this.job = job;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getJob() { return job; }
        public void setJob(String job) { this.job = job; }

        /**
         * Проверка является ли член группы режиссером
         */
        public boolean isDirector() {
            return "Director".equals(job);
        }

        /**
         * Проверка валидности данных
         */
        public boolean isValid() {
            return name != null && !name.trim().isEmpty() &&
                    job != null && !job.trim().isEmpty();
        }

        @Override
        public String toString() {
            return name + " (" + job + ")";
        }
    }
}