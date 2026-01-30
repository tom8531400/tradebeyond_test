package com.tradebeyond.backend.exception;


import com.tradebeyond.backend.enums.StatusCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code) {
        super(StatusCode.FAIL.getMessage());
        this.code = code;
    }

}
