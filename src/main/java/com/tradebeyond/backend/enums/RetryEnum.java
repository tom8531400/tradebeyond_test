package com.tradebeyond.backend.enums;

import lombok.Getter;

@Getter
public enum RetryEnum {

    NONE(0),
    ONCE(1),
    TWICE(2),
    THREE_TIMES(3),
    FOUR_TIMES(4),
    FIVE_TIMES(5);

    private final int count;

    RetryEnum(int count) {
        this.count = count;
    }
}
