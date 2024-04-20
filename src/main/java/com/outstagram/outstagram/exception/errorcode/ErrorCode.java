package com.outstagram.outstagram.exception.errorcode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    // 인증 관련 에러 코드
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "미인증 유저의 요청입니다."),

    // user 관련 에러 코드
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저는 존재하지 않습니다."),
    DUPLICATED(HttpStatus.CONFLICT, "중복됩니다."),

    // 암호화 관련 에러 코드,
    ALGORITHM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 암호화 알고리즘이 존재하지 않습니다."),


    // DB 관련 에러 코드
    INSERT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB insert 에러!!"),

    // 파일 입출력 관련 에러 코드
    FILE_IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 입출력 과정에서 에러가 발생했습니다."),

    // post 관련 에러 코드
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 게시물은 존재하지 않습니다."),

    ;


    private final HttpStatus httpStatusCode;
    private final String description;

}
