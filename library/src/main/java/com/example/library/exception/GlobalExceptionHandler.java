package com.example.library.exception;

import com.example.library.common.ApiResponse;
import com.example.library.common.ErrorCodes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> handleBizException(BizException e) {
        int httpStatus = mapToHttp(e.getCode());
        return ResponseEntity.status(httpStatus)
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCodes.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.SERVER_ERROR, "服务器内部错误"));
    }

    private int mapToHttp(int code) {
        return switch (code) {
            case ErrorCodes.BAD_REQUEST, ErrorCodes.USERNAME_EXISTS,
                 ErrorCodes.OLD_PASSWORD_WRONG, ErrorCodes.ISBN_EXISTS,
                 ErrorCodes.STOCK_NOT_ENOUGH, ErrorCodes.PURCHASE_STATUS_INVALID,
                 ErrorCodes.PURCHASE_MUST_BE_PAID -> 400;
            case ErrorCodes.UNAUTHORIZED, ErrorCodes.LOGIN_FAILED -> 401;
            case ErrorCodes.FORBIDDEN -> 403;
            case ErrorCodes.NOT_FOUND, ErrorCodes.BOOK_NOT_FOUND,
                 ErrorCodes.PURCHASE_NOT_FOUND -> 404;
            default -> 500;
        };
    }
}
