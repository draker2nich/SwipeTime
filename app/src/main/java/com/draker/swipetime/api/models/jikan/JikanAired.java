package com.draker.swipetime.api.models.jikan;

import com.google.gson.annotations.SerializedName;

/**
 * Модель даты выхода аниме из Jikan API
 */
public class JikanAired {
    @SerializedName("from")
    private String from;

    @SerializedName("to")
    private String to;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Получить год начала выхода
     * @return год или 0, если дата не указана
     */
    public int getFromYear() {
        if (from == null || from.isEmpty()) {
            return 0;
        }
        
        try {
            // Формат даты: 2022-01-01T00:00:00+00:00
            if (from.length() >= 4) {
                return Integer.parseInt(from.substring(0, 4));
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Получить год окончания выхода
     * @return год или 0, если дата не указана или сериал еще выходит
     */
    public int getToYear() {
        if (to == null || to.isEmpty()) {
            return 0;
        }
        
        try {
            if (to.length() >= 4) {
                return Integer.parseInt(to.substring(0, 4));
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
}