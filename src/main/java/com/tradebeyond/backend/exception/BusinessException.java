package com.tradebeyond.backend.exception;


import com.tradebeyond.backend.enums.StatusCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final StatusCode statusCode;
    private Object data;

    public BusinessException(StatusCode statusCode) {
        super(statusCode.getMessage());
        this.statusCode = statusCode;
    }

    public BusinessException(StatusCode statusCode, Object data) {
        super(statusCode.getMessage());
        this.statusCode = statusCode;
        this.data = data;
    }

}
