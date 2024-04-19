package com.outstagram.outstagram.exception;

import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import lombok.Getter;


@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String description;

    public ApiException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.description = errorCode.getDescription();
    }

    public ApiException(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.description = message;
    }
}
