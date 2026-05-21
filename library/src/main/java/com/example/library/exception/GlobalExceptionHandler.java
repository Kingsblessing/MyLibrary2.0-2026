package com.example.library.exception;

import com.example.library.common.ApiResponse;
import com.example.library.common.ErrorCodes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> handleBizException(BizException e) {
        return ResponseEntity.ok(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.ok(ApiResponse.error(ErrorCodes.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.ok(ApiResponse.error(ErrorCodes.SERVER_ERROR, "服务器内部错误"));
    }
}
