package com.tradebeyond.backend.exception;

import com.tradebeyond.backend.enums.StatusCode;
import com.tradebeyond.backend.resp.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<String>> handleBusiness(BusinessException businessException) {
        StatusCode status = businessException.getStatusCode();

        return ResponseEntity.status(status.getHttpStatus())
                .body(Result.fail(status.getCode(), status.getMessage()));
    }

}
