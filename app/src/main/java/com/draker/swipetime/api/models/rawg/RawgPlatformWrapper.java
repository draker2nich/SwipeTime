package com.draker.swipetime.api.models.rawg;

import com.google.gson.annotations.SerializedName;

/**
 * Обертка для платформы из RAWG API
 */
public class RawgPlatformWrapper {
    @SerializedName("platform")
    private RawgPlatform platform;

    public RawgPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(RawgPlatform platform) {
        this.platform = platform;
    }
}