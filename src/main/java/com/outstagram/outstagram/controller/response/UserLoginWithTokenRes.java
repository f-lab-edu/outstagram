package com.outstagram.outstagram.controller.response;

import com.outstagram.outstagram.controller.response.UserLoginRes.LoginStatus;
import com.outstagram.outstagram.util.token.TokenResDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserLoginWithTokenRes {

    private LoginStatus result;
    private TokenResDTO token;

}
