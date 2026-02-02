package com.tradebeyond.backend.enums;

import com.tradebeyond.backend.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum StatusCode implements ErrorCode {

    SUCCESS(0, "success", HttpStatus.OK),

    FAIL(1, "fail", HttpStatus.INTERNAL_SERVER_ERROR),

    USER_NOT_FOUND(2, "user not found", HttpStatus.NOT_FOUND),

    PRODUCT_NOT_FOUND(3, "product not found", HttpStatus.NOT_FOUND),

    ORDER_NOT_FOUND(4, "order not found", HttpStatus.NOT_FOUND),

    ORDER_CREATE_FAILED(5, "order create failed", HttpStatus.INTERNAL_SERVER_ERROR),

    ORDER_UPDATE_FAILED(6, "order update failed", HttpStatus.INTERNAL_SERVER_ERROR),

    USER_DELETE_FAILED(7, "user delete failed", HttpStatus.INTERNAL_SERVER_ERROR),

    ORDER_OUTBOX_CREATE_FAILED(8, "order outbox create failed", HttpStatus.INTERNAL_SERVER_ERROR),

    LOCK_FAILED(9, "lock failed", HttpStatus.CONFLICT),

    PARAMS_INVALID(10, "params invalid", HttpStatus.BAD_REQUEST),

    ERROR(11, "error", HttpStatus.INTERNAL_SERVER_ERROR);


    private final int code;
    private final String message;
    private final HttpStatus httpStatus;


    StatusCode(int i, String success, HttpStatus httpStatus) {
        this.code = i;
        this.message = success;
        this.httpStatus = httpStatus;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
