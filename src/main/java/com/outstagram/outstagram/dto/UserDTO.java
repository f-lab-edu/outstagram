package com.outstagram.outstagram.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
public class UserDTO {

    @Id
    private Long id;

    @NotBlank(message = "닉네임은 필수 입력입니다.")
    @Size(min = 4, message = "닉네임은 최소 4글자 이상이어야 합니다.")
    private String nickname;

    @NotBlank(message = "이메일은 필수 입력입니다.")
    @Email(message = "이메일 형식이어야 합니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8글자 이상이어야 합니다.")
    private String password;

    private String imgUrl;
    private Boolean isDeleted;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;


    public static Boolean checkSignupData(UserDTO user) {
        return user.getEmail() != null && user.getPassword() != null && user.getNickname() != null;
    }
}
