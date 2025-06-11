package com.draker.swipetime.api.models.rawg;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Ультра-компактная модель ответа RAWG API
 */
public class RawgGameResponse {
    @SerializedName("count")
    public int count;

    @SerializedName("next")
    public String next;

    @SerializedName("results")
    public List<RawgGame> results;

    public boolean hasNextPage() {
        return next != null && !next.isEmpty();
    }

    public boolean isEmpty() {
        return results == null || results.isEmpty();
    }
}
