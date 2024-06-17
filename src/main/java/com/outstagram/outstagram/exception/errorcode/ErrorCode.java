package com.outstagram.outstagram.exception.errorcode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    // 인증 관련 에러 코드
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "미인증 유저의 요청입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."),

    // user 관련 에러 코드
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저는 존재하지 않습니다."),
    DUPLICATED(HttpStatus.CONFLICT, "중복됩니다."),

    // 암호화 관련 에러 코드,
    ALGORITHM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 암호화 알고리즘이 존재하지 않습니다."),


    // DB 관련 에러 코드
    INSERT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB insert 에러!!"),
    DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이미 삭제했거나, 삭제할 데이터가 없습니다!"),
    UPDATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB update 에러!!"),

    // 파일 입출력 관련 에러 코드
    FILE_IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 입출력 과정에서 에러가 발생했습니다."),

    // post 관련 에러 코드,
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 게시물은 존재하지 않습니다."),

    // lock 관련 에러 코드
    RETRY_EXCEEDED(HttpStatus.INTERNAL_SERVER_ERROR, "업데이트 재시도 횟수를 초과했습니다."),

    // comment 관련 에러 코드
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 댓글은 존재하지 않습니다."),

    // snowflake 관련 에러 코드
    NODEID_INVALID_RANGE(HttpStatus.BAD_REQUEST, "NodeId 범위를 벗어났습니다."),
    TIMESTAMP_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "시스템의 timestamp 기능에 문제가 발생했습니다."),

    // S3 관련 에러 코드,
    EMPTY_FILE_EXCEPTION(HttpStatus.BAD_REQUEST, "빈 파일입니다."),
    NO_FILE_EXTENTION(HttpStatus.BAD_REQUEST, "확장자가 없습니다."),
    INVALID_FILE_EXTENTION(HttpStatus.BAD_REQUEST, "부적절한 이미지 확장자입니다."),
    IO_EXCEPTION_ON_IMAGE_UPLOAD(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 중 에러가 발생했습니다."),
    PUT_OBJECT_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "S3에 이미지 업로드 중 에러가 발생했습니다."),
    IO_EXCEPTION_ON_IMAGE_DELETE(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제 중 에러가 발생했습니다."),
    SAVE_IMAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB에 이미지 정보 저장에 실패했습니다."),

    // follow 관련 에러 코드
    DUPLICATED_FOLLOW(HttpStatus.CONFLICT, "이미 팔로우한 사용자 입니다."),
    SELF_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신을 팔로우할 수 없습니다."),


    // like 관련 에러 코드
    DUPLICATED_LIKE(HttpStatus.CONFLICT, "이미 좋아요한 게시물입니다."),
    NOT_FOUND_LIKE(HttpStatus.INTERNAL_SERVER_ERROR, "좋아요 기록이 없습니다"),

    // bookmark 관련 에러 코드
    DUPLICATED_BOOKMARK(HttpStatus.CONFLICT, "이미 북마크한 게시물입니다."),
    NOT_FOUND_BOOKMARK(HttpStatus.INTERNAL_SERVER_ERROR, "북마크 기록이 없습니다"),

    NULL_POINT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "null point error 입니다."),
    JSON_CONVERTING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "json <-> String 변환 과정 중 에러가 발생했습니다."),

    ;

    private final HttpStatus httpStatusCode;
    private final String description;

}
