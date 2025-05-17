package com.draker.swipetime.api.models.googlebooks;

import com.google.gson.annotations.SerializedName;

/**
 * Модель идентификатора книги из Google Books API
 */
public class GoogleBookIndustryIdentifier {
    @SerializedName("type")
    private String type;

    @SerializedName("identifier")
    private String identifier;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}