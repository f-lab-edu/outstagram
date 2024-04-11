package com.outstagram.outstagram.error;

public class DuplicateNicknameException extends RuntimeException{
    public DuplicateNicknameException(String msg) {
        super(msg);
    }
}
