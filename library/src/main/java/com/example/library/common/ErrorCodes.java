package com.example.library.common;

public final class ErrorCodes {
    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int SERVER_ERROR = 500;

    public static final int LOGIN_FAILED = 1001;
    public static final int USERNAME_EXISTS = 1002;
    public static final int OLD_PASSWORD_WRONG = 1003;

    public static final int BOOK_NOT_FOUND = 2001;
    public static final int ISBN_EXISTS = 2002;
    public static final int STOCK_NOT_ENOUGH = 2003;

    public static final int PURCHASE_NOT_FOUND = 3001;
    public static final int PURCHASE_STATUS_INVALID = 3002;
    public static final int PURCHASE_MUST_BE_PAID = 3003;

    private ErrorCodes() {}
}
