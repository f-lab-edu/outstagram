package com.outstagram.outstagram.error;

public class DuplicateEmailException extends RuntimeException{
    public DuplicateEmailException(String msg) {
        super(msg);
    }
}
