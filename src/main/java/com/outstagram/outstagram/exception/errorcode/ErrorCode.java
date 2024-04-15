package com.outstagram.outstagram.exception.errorcode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    // user 관련 에러 코드
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저는 존재하지 않습니다."),
    DUPLICATED(HttpStatus.CONFLICT, "중복됩니다."),

    // 암호화 관련 에러 코드,
    ALGORITHM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 암호화 알고리즘이 존재하지 않습니다."),


    // DB 관련 에러 코드
    INSERT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB insert 에러!!")
    ;


    private final HttpStatus httpStatusCode;
    private final String description;

    }
