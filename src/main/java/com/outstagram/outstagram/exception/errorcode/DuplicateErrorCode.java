package com.outstagram.outstagram.exception.errorcode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum DuplicateErrorCode implements ErrorCodeIfs {

    DUPLICATED(HttpStatus.CONFLICT, "중복됩니다."),

    ;

    private final HttpStatus httpStatusCode;
    private final String description;
}
