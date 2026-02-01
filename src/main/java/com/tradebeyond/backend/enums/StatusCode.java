package com.tradebeyond.backend.enums;

import lombok.Getter;

@Getter
public enum StatusCode {

    SUCCESS(0, "success"),
    FAIL(1, "fail"),
    USER_NOT_FOUND(2, "user not found"),
    PRODUCT_NOT_FOUND(3, "product not found"),
    ORDER_NOT_FOUND(4, "order not found"),
    ORDER_CREATE_FAILED(5, "order create failed"),
    ORDER_UPDATE_FAILED(6, "order update failed"),
    USER_DELETE_FAILED(7, "user delete failed"),
    ORDER_OUTBOX_CREATE_FAILED(8, "order outbox create failed"),

    PARAMS_INVALID(10, "paramsInvalid"),
    ERROR(11, "error");

    private final Integer code;

    private final String message;

    StatusCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
