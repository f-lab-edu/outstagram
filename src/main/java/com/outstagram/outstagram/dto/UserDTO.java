package com.outstagram.outstagram.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
public class UserDTO {

    private Long id;
    private String nickname;
    private String email;
    private String password;
    private String imgUrl;
    private Boolean isDeleted;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;


    public static Boolean checkSignupData(UserDTO user) {
        return user.getEmail() != null && user.getPassword() != null && user.getNickname() != null;
    }
}
