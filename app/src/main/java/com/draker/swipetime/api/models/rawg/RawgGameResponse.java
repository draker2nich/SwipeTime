package com.draker.swipetime.api.models.rawg;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Модель ответа на запрос списка игр из RAWG API
 */
public class RawgGameResponse {
    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<RawgGame> results;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public List<RawgGame> getResults() {
        return results;
    }

    public void setResults(List<RawgGame> results) {
        this.results = results;
    }
}