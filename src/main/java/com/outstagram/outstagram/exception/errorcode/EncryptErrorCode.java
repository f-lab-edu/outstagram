package com.outstagram.outstagram.exception.errorcode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum EncryptErrorCode implements ErrorCodeIfs {

    ALGORITHM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 암호화 알고리즘이 존재하지 않습니다."),

    ;

    private final HttpStatus httpStatusCode;
    private final String description;
}
