package com.tradebeyond.backend.enums;

import lombok.Getter;

@Getter
public enum PayloadEnum {

    ORDER_CREATED(0, "您已成功下訂單!");

    private final Integer code;
    private final String desc;

    PayloadEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDesc(Integer code) {
        for(PayloadEnum p : PayloadEnum.values()) {
            if(p.getCode().equals(code)) {
                return p.getDesc();
            }
        }
        return null;
    }
}
