package com.outstagram.outstagram.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserLoginRes {
    public enum LoginStatus {
        SUCCESS, FAIL, DELETED
    }


    private LoginStatus result;

}
