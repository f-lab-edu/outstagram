package com.outstagram.outstagram.exception.errorcode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    // 인증 관련 에러 코드
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "미인증 유저의 요청입니다."),

    // 기본 에러 코드
    NULL_POINT(HttpStatus.INTERNAL_SERVER_ERROR, "Null Point Error!!"),

    // user 관련 에러 코드
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저는 존재하지 않습니다."),
    DUPLICATED(HttpStatus.CONFLICT, "중복됩니다."),

    // 암호화 관련 에러 코드,
    ALGORITHM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 암호화 알고리즘이 존재하지 않습니다."),


    // DB 관련 에러 코드
    INSERT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB insert 에러!!"),


    // 토큰 관련 에러 코드
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰입니다!"),
    EXPIRED_TOKEN(HttpStatus.BAD_REQUEST, "만료된 토큰입니다!"),
    TOKEN_EXCEPTION(HttpStatus.BAD_REQUEST, "알 수 없는 토큰 에러입니다!"),

    ;

    private final HttpStatus httpStatusCode;
    private final String description;

    }
