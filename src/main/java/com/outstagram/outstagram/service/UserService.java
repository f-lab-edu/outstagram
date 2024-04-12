package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.UserDTO;

public interface UserService {

    void insertUser(UserDTO userInfo);
    Boolean isDuplicatedEmail(String email);
    Boolean isDuplicatedNickname(String nickname);

    UserDTO login(String email, String password);

}
