package com.outstagram.outstagram.exception.errorcode;

import org.springframework.http.HttpStatus;

public interface ErrorCodeIfs {

    HttpStatus getHttpStatusCode();

    String getDescription();
}
