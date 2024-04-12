package com.outstagram.outstagram.exception;

import com.outstagram.outstagram.exception.errorcode.ErrorCodeIfs;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
public class ApiException extends RuntimeException{

    private final ErrorCodeIfs errorCodeIfs;
    private final String description;

    public ApiException(ErrorCodeIfs errorCodeIfs) {
        this.errorCodeIfs = errorCodeIfs;
        this.description = errorCodeIfs.getDescription();
    }

    public ApiException(ErrorCodeIfs errorCodeIfs, String message) {
        this.errorCodeIfs = errorCodeIfs;
        this.description = message;
    }
}
