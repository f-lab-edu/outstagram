package com.outstagram.outstagram.dto.validator;

import com.outstagram.outstagram.dto.UserDTO;

public class UserDTOValidator {

    public static Boolean validateSignupData(UserDTO user) {
        return user.getEmail() != null && user.getPassword() != null && user.getNickname() != null;
    }

}
