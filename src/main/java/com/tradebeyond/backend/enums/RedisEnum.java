package com.tradebeyond.backend.enums;

import lombok.Getter;

@Getter
public enum RedisEnum {

    CACHE("cache:backend:"),
    LOCK("lock:backend:"),

    CACHE_EVENT_ID_KEY(CACHE.key + "CACHE_EVENT_ID_KEY"),
    CACHE_LATEST_VERSION_KEY(CACHE.key + "CACHE_LATEST_VERSION_KEY"),
    CACHE_TAX_RAT_KEY(CACHE.key + "CACHE_TAX_RAT_KEY"),
    CACHE_USER_KEY(CACHE.key + "CACHE_USER_KEY");

    private final String key;

    RedisEnum(String key) {
        this.key = key;
    }
}
