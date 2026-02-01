package com.tradebeyond.backend.enums;

import lombok.Getter;

@Getter
public enum EventTypeEnum {

    ORDER_CREATED(0, "訂單建立"),

    SENDING(1, "準備發送"),

    NOTIFY_USER(5, "發送通知"),

    FAILED(9, "處理失敗");

    private final Integer code;
    private final String value;

    EventTypeEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public static EventTypeEnum getByValue(String value){
        for(EventTypeEnum e : EventTypeEnum.values()){
            if(e.value.equals(value)){
                return e;
            }
        }
        return null;
    }
}
