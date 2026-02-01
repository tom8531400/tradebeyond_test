package com.tradebeyond.backend.resp;

import com.tradebeyond.backend.enums.StatusCode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result<T> {

    private int code;

    private T data;

    private String msg;

    public static <T> Result<T> success(T data) {
        return new Result<>(StatusCode.SUCCESS.getCode(), data, StatusCode.SUCCESS.getMessage());
    }

    public static <T> Result<T> fail(int code, String errorMsg) {
        return new Result<>(code, null, errorMsg);
    }

}
