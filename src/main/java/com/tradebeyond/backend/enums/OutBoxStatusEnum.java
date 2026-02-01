package com.tradebeyond.backend.enums;

import lombok.Getter;

@Getter
public enum OutBoxStatusEnum {

    NEW(0, "待發送"),
    IN_FLIGHT(1, "已鎖定發送"),
    PUBLISHED(2, "已到Broker"),
    FAILED(3, "發送失敗"),
    UNROUTABLE(4, "路由失敗"),
    DEAD(5, "超過重試次數"),
    DEAD_IN_FLIGHT (6, "死信已鎖定發送"),
    DEAD_PUBLISHED(7, "死信已到Broker");

    private final int code;

    private final String desc;

    OutBoxStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
